# Commands — Cold Room Monitoring System

> All commands assume your terminal opens at `c:\Users\my\Documents\test2`

---

## 1. Database Setup

Run once to create the schema and seed initial data.

```powershell
Get-Content projet-web-iot\database\schema.sql   | mysql -u root -p0000
Get-Content projet-web-iot\database\seed_data.sql | mysql -u root -p0000
```

---

## 2. Backend (Spring Boot)

Keep this terminal open.

```powershell
Set-Location projet-web-iot\backend
mvn spring-boot:run
```

- API: **http://localhost:8080/api**
- Health: **http://localhost:8080/health**

---

## 3. Sensor Simulator (Python)

Open a new terminal, keep it open.

```powershell
Set-Location projet-web-iot\simulator
py sensor_simulator.py --verbose
```

---

## 4. Frontend Dashboard

Open a new terminal, keep it open.

```powershell
Set-Location projet-web-iot\frontend
py -m http.server 8000
```

Then open **http://localhost:8000** in your browser.

---

## Full Startup Order

```
Terminal 1 → DB setup (one-time)
Terminal 2 → Set-Location projet-web-iot\backend   ; mvn spring-boot:run
Terminal 3 → Set-Location projet-web-iot\simulator ; py sensor_simulator.py --verbose
Terminal 4 → Set-Location projet-web-iot\frontend  ; py -m http.server 8000
```
