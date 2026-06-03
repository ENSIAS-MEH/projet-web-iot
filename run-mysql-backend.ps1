param()

$ErrorActionPreference = "Stop"

function Write-Step($message) {
    Write-Host ""
    Write-Host "==> $message" -ForegroundColor Cyan
}

function Test-MySqlLogin($mysqlExe, $password) {
    cmd /c "`"$mysqlExe`" --protocol=TCP -h localhost -P 3306 -u root -p$password -e `"SELECT 1;`" >NUL 2>NUL" | Out-Null
    return ($LASTEXITCODE -eq 0)
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $projectRoot "backend"
$dbDir = Join-Path $projectRoot "database"
$applicationProperties = Join-Path $backendDir "src\main\resources\application.properties"
$dataInitializer = Join-Path $backendDir "src\main\java\com\coldroom\config\DataInitializer.java"
$schemaSource = Join-Path $dbDir "schema.sql"
$seedSource = Join-Path $dbDir "seed_data.sql"
$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
$mysqldExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
$dbName = "iot"
$dbPassword = "0000"
$serviceName = "MySQL84"
$mavenBin = "C:\Maven\apache-maven-3.9.16\bin"

if (-not (Test-Path $mysqlExe)) {
    throw "MySQL client not found at $mysqlExe"
}

if (-not (Test-Path $mysqldExe)) {
    throw "mysqld not found at $mysqldExe"
}

Write-Step "Updating backend config for MySQL database '$dbName'"
$applicationPropertiesContent = @"
# ============================================================
# Cold Room Monitoring System - Application Configuration
# ============================================================

# Server
server.port=8080

# DataSource
spring.datasource.url=jdbc:mysql://localhost:3306/${dbName}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=$dbPassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Jackson - serialize dates as ISO strings
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=UTC

# Logging
logging.level.com.coldroom=DEBUG
logging.level.org.hibernate.SQL=DEBUG
"@
[System.IO.File]::WriteAllText($applicationProperties, $applicationPropertiesContent, [System.Text.Encoding]::ASCII)

if (Test-Path $dataInitializer) {
    Write-Step "Disabling DataInitializer so SQL seed data is the single source of truth"
    $initializerContent = Get-Content $dataInitializer -Raw
    $initializerContent = $initializerContent -replace "(?m)^@Component\s*$", "// @Component"
    [System.IO.File]::WriteAllText($dataInitializer, $initializerContent, [System.Text.Encoding]::UTF8)
}

Write-Step "Preparing SQL files for database '$dbName'"
$schemaTemp = Join-Path $env:TEMP "iot-schema.sql"
$seedTemp = Join-Path $env:TEMP "iot-seed.sql"
$schemaContent = (Get-Content $schemaSource -Raw) -replace "cold_room_db", $dbName
$seedContent = (Get-Content $seedSource -Raw) -replace "cold_room_db", $dbName
[System.IO.File]::WriteAllText($schemaTemp, $schemaContent, [System.Text.Encoding]::UTF8)
[System.IO.File]::WriteAllText($seedTemp, $seedContent, [System.Text.Encoding]::UTF8)

Write-Step "Checking MySQL service"
$service = Get-Service -Name $serviceName -ErrorAction Stop
if ($service.Status -ne "Running") {
    Start-Service -Name $serviceName
    Start-Sleep -Seconds 5
}

Write-Step "Testing MySQL root login with password '$dbPassword'"
$loginWorks = Test-MySqlLogin $mysqlExe $dbPassword

if (-not $loginWorks) {
    Write-Step "Attempting automatic reset of MySQL root password to '$dbPassword'"
    $resetSql = Join-Path $env:TEMP "mysql-reset-root.sql"
    $resetContent = @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$dbPassword';
FLUSH PRIVILEGES;
"@
    [System.IO.File]::WriteAllText($resetSql, $resetContent, [System.Text.Encoding]::ASCII)

    try {
        Stop-Service -Name $serviceName -Force
        Start-Sleep -Seconds 5
    } catch {
        throw "Could not stop MySQL service '$serviceName'. Run PowerShell as Administrator and retry. $($_.Exception.Message)"
    }

    $mysqldProcess = Start-Process -FilePath $mysqldExe -ArgumentList "--console", "--skip-networking=0", "--init-file=$resetSql" -PassThru -WindowStyle Hidden
    Start-Sleep -Seconds 15

    if (-not $mysqldProcess.HasExited) {
        Stop-Process -Id $mysqldProcess.Id -Force
    }

    Start-Service -Name $serviceName
    Start-Sleep -Seconds 8

    $loginWorks = Test-MySqlLogin $mysqlExe $dbPassword
    if (-not $loginWorks) {
        throw "Automatic MySQL password reset failed. Root login with password '$dbPassword' still does not work."
    }
}

Write-Step "Creating database and loading schema"
cmd /c "`"$mysqlExe`" --protocol=TCP -h localhost -P 3306 -u root -p$dbPassword --default-character-set=utf8mb4 --execute=`"SOURCE $schemaTemp`""
if ($LASTEXITCODE -ne 0) {
    throw "Schema import failed."
}

Write-Step "Loading seed data"
cmd /c "`"$mysqlExe`" --protocol=TCP -h localhost -P 3306 -u root -p$dbPassword --default-character-set=utf8mb4 --execute=`"SOURCE $seedTemp`""
if ($LASTEXITCODE -ne 0) {
    throw "Seed import failed."
}

Write-Step "Ensuring port 8080 is free"
$pids = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
foreach ($pid in $pids) {
    if ($pid -and $pid -ne 0) {
        try {
            Stop-Process -Id $pid -Force -ErrorAction Stop
        } catch {
        }
    }
}

Write-Step "Starting Spring Boot backend"
$env:Path = "$mavenBin;$env:Path"
Set-Location $backendDir
mvn spring-boot:run
