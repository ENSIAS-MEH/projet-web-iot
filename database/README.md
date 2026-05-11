# Database Setup - Cold Room Monitoring System

This folder contains all SQL scripts needed to set up the `cold_room_db` MySQL database.

## Files

| File | Description |
|------|-------------|
| `schema.sql` | Creates the database and all tables |
| `seed_data.sql` | Inserts 4 sample sensors and realistic readings |

## Prerequisites

- **MySQL 8.0+** installed and running locally
- A MySQL user with `CREATE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE` privileges

## Setup Instructions

### 1. Start MySQL

```bash
# Linux / macOS
sudo service mysql start

# Windows (run as Administrator)
net start MySQL80
```

### 2. Connect to MySQL

```bash
mysql -u root -p
```

### 3. Run the schema script

```bash
mysql -u root -p < database/schema.sql
```

Or from inside the MySQL shell:

```sql
SOURCE /path/to/database/schema.sql;
```

### 4. Load sample data

```bash
mysql -u root -p < database/seed_data.sql
```

Or from inside the MySQL shell:

```sql
SOURCE /path/to/database/seed_data.sql;
```

### 5. Verify the setup

```sql
USE cold_room_db;

SHOW TABLES;
-- Expected: alerts, sensor_readings, sensors

SELECT * FROM sensors;
-- Expected: 4 rows (Temperature, Humidity, Door, Pressure)

SELECT COUNT(*) FROM sensor_readings;
-- Expected: 24 rows of sample readings

SELECT * FROM alerts;
-- Expected: 1 sample critical alert
```

## Database Schema Overview

```
sensors
  └── id (PK)
  └── name, sensor_type, unit, location
  └── min_threshold, max_threshold
  └── is_active, created_at, updated_at

sensor_readings
  └── id (PK)
  └── sensor_id (FK → sensors.id)
  └── value, timestamp

alerts
  └── id (PK)
  └── sensor_id  (FK → sensors.id)
  └── reading_id (FK → sensor_readings.id)
  └── alert_type, severity, message
  └── is_resolved, created_at, resolved_at
```

## Sample Sensor Thresholds

| Sensor | Type | Unit | Min | Max |
|--------|------|------|-----|-----|
| Temperature Sensor | temperature | °C | -25 | -15 |
| Humidity Sensor | humidity | % | 40 | 60 |
| Door Sensor | door | boolean | 0 | 0 |
| Pressure Sensor | pressure | hPa | 1000 | 1020 |

## Spring Boot Configuration

Add the following to `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cold_room_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```

> Replace `yourpassword` with your actual MySQL root password.

## Troubleshooting

**MySQL won't connect:**
- Verify MySQL service is running: `mysqladmin -u root -p status`
- Check port 3306 is not blocked by a firewall

**Access denied:**
- Make sure the user has the required privileges:
  ```sql
  GRANT ALL PRIVILEGES ON cold_room_db.* TO 'root'@'localhost';
  FLUSH PRIVILEGES;
  ```

**Character encoding issues:**
- The schema uses `utf8mb4` — ensure your MySQL client also uses UTF-8:
  ```bash
  mysql --default-character-set=utf8mb4 -u root -p
  ```
