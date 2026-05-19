# Sensor Simulator — Cold Room Monitoring System

A Python script that simulates the 4 IoT sensors installed in the cold room and
sends readings to the Spring Boot backend every 10 seconds.

## Simulated Sensors

| # | Name | Type | Normal Range | Anomaly (5 % chance) |
|---|------|------|-------------|----------------------|
| 1 | Temperature Sensor | temperature | -18 °C ± 2 °C | -10 °C (exceeds max -15 °C) |
| 2 | Humidity Sensor | humidity | 50 % ± 5 % | 70 % (exceeds max 60 %) |
| 3 | Door Sensor | door | 0 (closed) 95 % | 1 (open) 5 % |
| 4 | Pressure Sensor | pressure | 1013 hPa ± 5 hPa | — |

## Prerequisites

- **Python 3.8+**
- **Spring Boot backend** running on `http://localhost:8080`
- **MySQL** with `cold_room_db` set up (run `database/schema.sql` and `database/seed_data.sql`)

## Installation

```bash
# From the simulator/ directory
pip install -r requirements.txt
```

Or with a virtual environment (recommended):

```bash
python -m venv .venv

# Windows
.venv\Scripts\activate

# macOS / Linux
source .venv/bin/activate

pip install -r requirements.txt
```

## Usage

### Basic (default: localhost:8080, every 10 seconds)

```bash
python sensor_simulator.py
```

### Custom backend URL

```bash
python sensor_simulator.py --url http://localhost:8080/api
```

### Custom interval (e.g. every 5 seconds)

```bash
python sensor_simulator.py --interval 5
```

### Verbose mode (shows HTTP status per request)

```bash
python sensor_simulator.py --verbose
```

### All options

```bash
python sensor_simulator.py --url http://localhost:8080/api --interval 10 --verbose
```

## Sample Output

```
10:30:00  ============================================================
10:30:00  Cold Room Sensor Simulator
10:30:00    Backend : http://localhost:8080/api
10:30:00    Interval: 10 seconds
10:30:00    Sensors : 4
10:30:00    Anomaly : 5% chance per reading
10:30:00  ============================================================
10:30:00  Checking backend connectivity...
10:30:00  Backend is up. Starting simulation.

10:30:00  ── Round 1 ──────────────────────────────────────────
10:30:00    Temperature Sensor      -17.83 °C        [OK]
10:30:00    Humidity Sensor          52.14 %         [OK]
10:30:00    Door Sensor               0.00 boolean   [OK]
10:30:00    Pressure Sensor        1011.47 hPa       [OK]
10:30:00    Sent 4/4 readings — sleeping 10 s

10:30:10  ── Round 2 ──────────────────────────────────────────
10:30:10    Temperature Sensor      -10.00 °C        [OK]   ← anomaly!
10:30:10    Humidity Sensor          48.76 %         [OK]
10:30:10    Door Sensor               0.00 boolean   [OK]
10:30:10    Pressure Sensor        1015.23 hPa       [OK]
10:30:10    Sent 4/4 readings — sleeping 10 s
```

When an anomaly reading is sent, the backend automatically creates an alert
visible on the **Alerts** page of the dashboard.

## How It Works

1. On startup the simulator pings `GET /health` and waits until the backend responds.
2. Every `interval` seconds it generates one reading per sensor:
   - **Temperature / Humidity / Pressure**: Gaussian noise around the normal mean.
   - **Door**: 0 (closed) with 95 % probability, 1 (open) with 5 %.
   - Any sensor with `has_anomaly = True` has a 5 % chance of sending an out-of-range value.
3. Each reading is POSTed to `POST /api/readings`.
4. The backend's anomaly-detection logic checks the value against the sensor's
   `min_threshold` / `max_threshold` and creates an `Alert` record automatically.
5. Stop the simulator at any time with **Ctrl + C**.

## Troubleshooting

**"Connection refused"**
- Make sure the Spring Boot backend is running: `mvn spring-boot:run` (from `backend/`)
- Verify the port: default is `8080`

**"requests not found"**
- Run `pip install -r requirements.txt`

**Readings appear but no alerts are created**
- Check that the sensor IDs in `SENSORS` list match the IDs in your database
- Verify thresholds in the Settings page match the expected ranges
