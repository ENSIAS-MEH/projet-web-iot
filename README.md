# 🌡️ Cold Room Monitoring System

Real-time cold room monitoring system with anomaly detection, automatic alerts, and responsive web interface.

## Description

This project monitors cold room conditions (temperature, humidity, pressure, door status) via simulated IoT sensors. It includes a web dashboard, automatic alert system, and complete REST API.

## Tech Stack

- **Frontend**: HTML5 / CSS3 / JavaScript (Vanilla + Chart.js)
- **Backend**: Java 17+ with Spring Boot 3.x
- **Database**: MySQL 8.0+
- **API**: REST API (JSON)
- **Build Tool**: Maven
- **Simulator**: Python script

## Project Structure

```
cold-room-monitoring/
├── backend/           # Spring Boot project (REST API)
├── frontend/          # HTML/CSS/JS files
├── database/          # SQL scripts and schema
├── simulator/         # IoT sensor simulator
├── scripts/           # Utility scripts
└── docs/              # Documentation
```

## Quick Start

> Detailed instructions available in MANAGEMENT.md

1. Clone the repository
2. Set up MySQL (see `database/README.md`)
3. Configure Spring Boot (`backend/src/main/resources/application.properties`)
4. Run the backend: `mvn spring-boot:run`
5. Run the simulator: `python simulator/sensor_simulator.py`
6. Open `frontend/index.html` in a browser

## Team

4 developers working together on this IoT cold room monitoring system.

## Documentation

See **MANAGEMENT.md** for detailed task breakdown and project management.
