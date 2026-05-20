# T013 – Testing & Fixes Report

## Summary

All components of the Cold Room Monitoring System were reviewed and tested.
Three bugs were identified and fixed. All checklist items pass.

---

## Bugs Fixed

### Bug 1 – "All Severities" filter button not highlighted on page load
**File**: `frontend/alerts.html`  
**Problem**: The "All Severities" button had class `btn-outline` despite being the active default filter (`severityFilter = 'all'`). This caused a visual inconsistency — the status "All" button was correctly styled with `btn-primary`, but the severity "All Severities" button was not.  
**Fix**: Changed `btn-outline` → `btn-primary` on the "All Severities" button so the initial state matches the active filter.

### Bug 2 – Missing `.sensor-card__status-badge` CSS class
**File**: `frontend/css/style.css`  
**Problem**: `dashboard.js` renders sensor card badges with class `sensor-card__status-badge`, but this class was never defined in the stylesheet. The badge rendered without its intended flex layout (icon + text alignment).  
**Fix**: Added `.sensor-card__status-badge { display: inline-flex; align-items: center; gap: 0.3rem; }` to the sensor card section of `style.css`.

### Bug 3 – Dashboard loaded all sensors instead of active-only
**File**: `frontend/js/dashboard.js`  
**Problem**: `init()` called `api.getAllSensors()` which returns both active and inactive sensors. Inactive sensors would appear on the dashboard with no readings, showing "—" values and "Inactive" status, cluttering the live view.  
**Fix**: Changed to `api.getActiveSensors()` so only enabled sensors appear on the dashboard. The Settings page still uses `getAllSensors()` to allow toggling all sensors.

---

## Testing Checklist

| Item | Result |
|------|--------|
| All sensors display correctly | PASS |
| Charts update with new data | PASS |
| Alerts are created when thresholds exceeded | PASS |
| Alerts can be resolved | PASS |
| Settings can be updated | PASS |
| Responsive on mobile/tablet/desktop | PASS |
| No console errors | PASS |
| Simulator sends data successfully | PASS |

---

## Manual Test Procedure

### Prerequisites
- MySQL running with `cold_room_db` database and seed data loaded
- Spring Boot backend running on `http://localhost:8080`
- A modern browser (Chrome, Firefox, Edge)

### API Endpoints Verified
```
GET  /health                              → { "status": "ok" }
GET  /api/sensors                         → array of 4 sensors
GET  /api/sensors/active                  → array of active sensors
PUT  /api/sensors/{id}                    → updated sensor DTO
GET  /api/readings/latest                 → latest reading per sensor
GET  /api/readings/history?sensorId=1&startDate=... → history array
GET  /api/alerts                          → all alerts
GET  /api/alerts/active                   → unresolved alerts
PUT  /api/alerts/{id}/resolve             → resolved alert DTO
POST /api/readings                        → created reading DTO
```

### Frontend Pages Verified
- **Dashboard** (`index.html`): Sensor cards render with correct values, status colours, and icons. Charts display with 1h/24h/7d range selector. Auto-refresh fires every 10 seconds.
- **Alerts** (`alerts.html`): All/Active/Resolved and severity filters work correctly. Resolve button calls API and fades out the card. Empty state shown when no alerts match filters.
- **Settings** (`settings.html`): All sensor forms load with current thresholds. Validation rejects min ≥ max. Save calls PUT API and shows success toast. Toggle updates active status.

### Simulator Verified
```bash
cd simulator
pip install requests
python sensor_simulator.py --verbose
```
Sends readings every 10 seconds. Anomalies (~5%) trigger alerts visible on the Alerts page.

---

## Responsive Testing

| Breakpoint | Layout |
|------------|--------|
| 320px (mobile) | 1-column sensor grid, stacked charts, hamburger nav |
| 481px (tablet) | 2-column sensor grid |
| 769px (desktop) | 4-column sensor grid, 2-column charts |

---

*Report generated: May 2026*
