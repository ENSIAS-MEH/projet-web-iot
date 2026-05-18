# 📋 Cold Room Monitoring System - Project Management

## 👥 Team
- **Hamami**
- **Ilyas**
- **Houssam**
- **Aymane**

> Pick tasks based on your availability. Any developer can work on any task!

---

## 🎯 Project Goal
Build a **Cold Room Monitoring System** that works on **localhost** with real-time temperature tracking, alerts, and a beautiful responsive web interface.

---

## 🛠️ Tech Stack

### Backend
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x
- **API**: REST API (JSON)
- **Build Tool**: Maven

### Database
- **Database**: MySQL 8.0+
- **Access**: JDBC / JPA (Hibernate)

### Frontend
- **HTML5** - Structure
- **CSS3** - Styling (Flexbox/Grid for responsive design)
- **JavaScript (Vanilla)** - Interactivity
- **Chart.js** - Real-time graphs
- **Responsive Design** - Works on mobile, tablet, desktop

### Tools
- **Git/GitHub** - Version control
- **Postman** - API testing
- **VS Code / IntelliJ IDEA** - Code editors

---

## 📊 Task Board

| ID | Task | Assigned To | Branch | Status |
|----|------|-------------|--------|--------|
| T001 | Project Setup | - | setup | ✅ DONE |
| T002 | Database Schema | - | db-schema | ✅ DONE |
| T003 | Spring Boot Setup | - | spring-init | ✅ DONE |
| T004 | Sensor API | - | sensor-api | ✅ DONE |
| T005 | Readings API | - | readings-api | ✅ DONE |
| T006 | Alerts API | - | alerts-api | ✅ DONE |
| T007 | Frontend Structure | - | frontend-base | ⬜ TODO |
| T008 | Dashboard Page | - | dashboard | ⬜ TODO |
| T009 | Alerts Page | - | alerts-page | ⬜ TODO |
| T010 | Settings Page | - | settings-page | ⬜ TODO
 |
| T011 | API Integration | - | api-connect | ⬜ TODO |
| T012 | Simulator Script | - | simulator | ⬜ TODO |
| T013 | Testing & Fixes | - | testing | ⬜ TODO |

**Status Legend:**
- ⬜ TODO - Not started
- 🔄 IN PROGRESS - Currently working on it
- ✅ DONE - Completed and merged
- ❌ BLOCKED - Waiting for something

---

## 📝 Detailed Tasks

### Phase 1: Foundation

#### T001: Project Setup
**Assigned**: -  
**Branch**: `setup`  
**Status**: ✅ DONE

**What to do:**
- [x] Create GitHub repository
- [x] Create folder structure
- [x] Add .gitignore file
- [x] Create README.md
- [x] Push initial commit

**Folder Structure:**
```
cold-room-monitoring/
├── backend/          # Java Spring Boot project
├── frontend/         # HTML/CSS/JS files
├── database/         # SQL scripts
├── simulator/        # Python sensor simulator
└── docs/            # Documentation
```

---

#### T002: Database Schema
**Assigned**: -  
**Branch**: `db-schema`  
**Status**: ✅ DONE

**What to do:**
- [x] Install MySQL locally
- [x] Create database `cold_room_db`
- [x] Create SQL script for tables
- [x] Add sample data (4 sensors)
- [x] Test database connection

**Tables to create:**

**1. sensors**
```sql
- id (INT, PRIMARY KEY, AUTO_INCREMENT)
- name (VARCHAR) - e.g., "Temperature Sensor"
- sensor_type (VARCHAR) - temperature, humidity, door, pressure
- unit (VARCHAR) - °C, %, boolean, hPa
- location (VARCHAR) - e.g., "Main Chamber"
- min_threshold (DECIMAL) - minimum safe value
- max_threshold (DECIMAL) - maximum safe value
- is_active (BOOLEAN) - sensor enabled/disabled
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

**2. sensor_readings**
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- sensor_id (INT, FOREIGN KEY → sensors.id)
- value (DECIMAL) - the reading value
- timestamp (TIMESTAMP) - when reading was taken
```

**3. alerts**
```sql
- id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- sensor_id (INT, FOREIGN KEY → sensors.id)
- reading_id (BIGINT, FOREIGN KEY → sensor_readings.id)
- alert_type (VARCHAR) - threshold_exceeded, sensor_offline
- severity (VARCHAR) - info, warning, critical
- message (TEXT) - alert description
- is_resolved (BOOLEAN) - alert status
- created_at (TIMESTAMP)
- resolved_at (TIMESTAMP)
```

**Sample Sensors:**
1. Temperature: -25°C to -15°C
2. Humidity: 40% to 60%
3. Door: 0 (closed) or 1 (open)
4. Pressure: 1000 to 1020 hPa

**Files to create:**
- `database/schema.sql` - table creation
- `database/seed_data.sql` - sample sensors
- `database/README.md` - setup instructions

---

#### T003: Spring Boot Setup
**Assigned**: -  
**Branch**: `spring-init`  
**Status**: ✅ DONE

**What to do:**
- [x] Create Spring Boot project (Spring Initializr)
- [x] Add dependencies (Web, JPA, MySQL, Lombok)
- [x] Configure application.properties
- [x] Create entity classes (Sensor, SensorReading, Alert)
- [x] Test database connection
- [x] Create health check endpoint

**Dependencies needed:**
- Spring Web
- Spring Data JPA
- MySQL Driver
- Lombok
- Validation

**application.properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cold_room_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```

**Health Check:**
- Endpoint: `GET /health`
- Response: `{"status": "ok"}`

**Files to create:**
- `backend/src/main/java/.../entity/Sensor.java`
- `backend/src/main/java/.../entity/SensorReading.java`
- `backend/src/main/java/.../entity/Alert.java`
- `backend/src/main/resources/application.properties`

---

### Phase 2: Backend API

#### T004: Sensor API
**Assigned**: -  
**Branch**: `sensor-api`  
**Status**: ✅ DONE

**What to do:**
- [x] Create SensorRepository
- [x] Create SensorService
- [x] Create SensorController
- [x] Implement CRUD operations
- [x] Test with Postman

**Endpoints:**
- `GET /api/sensors` - Get all sensors
- `GET /api/sensors/{id}` - Get sensor by ID
- `POST /api/sensors` - Create new sensor
- `PUT /api/sensors/{id}` - Update sensor
- `DELETE /api/sensors/{id}` - Delete sensor
- `GET /api/sensors/active` - Get only active sensors

**Example Response:**
```json
{
  "id": 1,
  "name": "Temperature Sensor",
  "sensorType": "temperature",
  "unit": "°C",
  "location": "Main Chamber",
  "minThreshold": -25.0,
  "maxThreshold": -15.0,
  "isActive": true,
  "createdAt": "2026-05-11T10:00:00",
  "updatedAt": "2026-05-11T10:00:00"
}
```

---

#### T005: Readings API
**Assigned**: -  
**Branch**: `readings-api`  
**Status**: ⬜ TODO

**What to do:**
- [x] Create SensorReadingRepository
- [x] Create SensorReadingService
- [x] Create SensorReadingController
- [x] Implement anomaly detection logic
- [x] Test with Postman

**Endpoints:**
- `POST /api/readings` - Add new reading (from simulator)
- `GET /api/readings` - Get all readings (with pagination)
- `GET /api/readings/sensor/{sensorId}` - Get readings for specific sensor
- `GET /api/readings/latest` - Get latest reading for each sensor
- `GET /api/readings/history?sensorId=1&startDate=...&endDate=...` - Get history

**Anomaly Detection:**
When a reading is saved:
1. Check if value is outside min/max thresholds
2. If yes, automatically create an Alert
3. Set severity based on how far outside threshold

**Example Request:**
```json
POST /api/readings
{
  "sensorId": 1,
  "value": -18.5,
  "timestamp": "2026-05-11T10:30:00"
}
```

---

#### T006: Alerts API
**Assigned**: -  
**Branch**: `alerts-api`  
**Status**: ✅ DONE

**What to do:**
- [x] Create AlertRepository
- [x] Create AlertService
- [x] Create AlertController
- [x] Implement resolve alert functionality
- [x] Test with Postman

**Endpoints:**
- `GET /api/alerts` - Get all alerts
- `GET /api/alerts/active` - Get unresolved alerts only
- `GET /api/alerts/{id}` - Get alert by ID
- `PUT /api/alerts/{id}/resolve` - Mark alert as resolved
- `GET /api/alerts/sensor/{sensorId}` - Get alerts for specific sensor

**Example Response:**
```json
{
  "id": 1,
  "sensorId": 1,
  "sensorName": "Temperature Sensor",
  "readingId": 123,
  "alertType": "threshold_exceeded",
  "severity": "critical",
  "message": "Temperature exceeded maximum threshold: -10.5°C (max: -15.0°C)",
  "isResolved": false,
  "createdAt": "2026-05-11T10:35:00",
  "resolvedAt": null
}
```

---

### Phase 3: Frontend

#### T007: Frontend Structure
**Assigned**: -  
**Branch**: `frontend-base`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Create HTML files (index.html, alerts.html, settings.html)
- [ ] Create CSS files (global styles, variables)
- [ ] Create JS files (api.js, utils.js)
- [ ] Add navigation bar
- [ ] Make it responsive

**Files to create:**
```
frontend/
├── index.html          # Dashboard page
├── alerts.html         # Alerts management page
├── settings.html       # Settings page
├── css/
│   ├── style.css       # Global styles
│   └── responsive.css  # Media queries
├── js/
│   ├── api.js          # API calls
│   ├── dashboard.js    # Dashboard logic
│   ├── alerts.js       # Alerts page logic
│   └── settings.js     # Settings page logic
└── assets/
    ├── icons/          # SVG icons (temperature, humidity, door, etc.)
    └── images/         # Logos, backgrounds
```

**Recommended Icon Sources:**
- **Font Awesome** (free): https://fontawesome.com/icons
- **Heroicons** (free SVG): https://heroicons.com/
- **Feather Icons** (free SVG): https://feathericons.com/
- **Ionicons** (free): https://ionic.io/ionicons

**Design Requirements:**
- **Color Scheme**: Cool colors (blues, grays) to match "cold room" theme
- **Responsive**: Must work on:
  - Mobile (320px - 480px)
  - Tablet (481px - 768px)
  - Desktop (769px+)
- **Navigation**: Fixed top navbar with links to all pages
- **Font**: Clean, modern font (e.g., Inter, Roboto)

**⚠️ IMPORTANT FRONTEND RULES:**
- ❌ **NO EMOJIS** - Use SVG icons or Font Awesome instead
- ✅ **Use SVG icons** for all visual elements (sensors, alerts, buttons)
- ✅ **Adaptive UI/UX** - Interface must adapt to user's device and screen size
- ✅ **Professional look** - Clean, modern, business-ready design
- ✅ **Icon libraries**: Font Awesome, Heroicons, or custom SVG
- ✅ **Accessibility**: Proper contrast ratios, alt text for icons

**CSS Variables to define:**
```css
:root {
  --primary-color: #2563eb;
  --danger-color: #dc2626;
  --warning-color: #f59e0b;
  --success-color: #10b981;
  --bg-color: #f8fafc;
  --card-bg: #ffffff;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --border-radius: 8px;
  --shadow: 0 1px 3px rgba(0,0,0,0.1);
}
```

---

#### T008: Dashboard Page
**Assigned**: -  
**Branch**: `dashboard`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Create sensor cards layout (grid)
- [ ] Display current sensor values
- [ ] Add color indicators (green=ok, red=alert)
- [ ] Create charts section
- [ ] Integrate Chart.js for graphs
- [ ] Add time range selector (1h, 24h, 7d)
- [ ] Make it auto-refresh every 10 seconds

**Dashboard Layout:**

```
┌─────────────────────────────────────────┐
│    [ICON] Cold Room Monitor             │
│  [Dashboard] [Alerts] [Settings]        │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐  │
│  │[TEMP]│ │[DROP]│ │[DOOR]│ │[GAUGE│  │
│  │-18°C │ │ 52%  │ │Closed│ │1013  │  │
│  │ OK   │ │ OK   │ │ OK   │ │ OK   │  │
│  └──────┘ └──────┘ └──────┘ └──────┘  │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  Temperature History            │   │
│  │  [1h] [24h] [7d]               │   │
│  │                                 │   │
│  │  [Line Chart Here]              │   │
│  │                                 │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │  Humidity History               │   │
│  │  [Line Chart Here]              │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

**Icon Suggestions:**
- Temperature: Thermometer SVG icon
- Humidity: Water droplet SVG icon
- Door: Door/lock SVG icon
- Pressure: Gauge/meter SVG icon
- Use Font Awesome or download free SVG icons from Heroicons

**Sensor Card Features:**
- Large value display
- Unit label
- Status indicator (colored dot or background)
- Sensor name
- Last updated time
- Icon for sensor type

**Chart Features:**
- Line charts for temperature, humidity, pressure
- X-axis: Time
- Y-axis: Value
- Show threshold lines (min/max)
- Responsive (stack on mobile)
- Smooth animations

---

#### T009: Alerts Page
**Assigned**: -  
**Branch**: `alerts-page`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Create alerts table/list
- [ ] Add filter buttons (All, Active, Resolved)
- [ ] Add severity filter (Critical, Warning, Info)
- [ ] Add "Resolve" button for each alert
- [ ] Color-code by severity
- [ ] Make it responsive (cards on mobile)

**Alerts Page Layout:**

```
┌─────────────────────────────────────────┐
│  [ALERT ICON] Alerts Management         │
│  [Dashboard] [Alerts] [Settings]        │
├─────────────────────────────────────────┤
│                                         │
│  Filters:                               │
│  [All] [Active] [Resolved]              │
│  [Critical] [Warning] [Info]            │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ [!] CRITICAL                    │   │
│  │ Temperature exceeded threshold  │   │
│  │ Sensor: Temperature Sensor      │   │
│  │ Value: -10.5°C (max: -15°C)    │   │
│  │ Time: 2 minutes ago             │   │
│  │              [Resolve] [Details]│   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ [!] WARNING                     │   │
│  │ Humidity slightly high          │   │
│  │ Sensor: Humidity Sensor         │   │
│  │ Value: 62% (max: 60%)          │   │
│  │ Time: 15 minutes ago            │   │
│  │              [Resolve] [Details]│   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

**Icon Suggestions:**
- Critical: Red circle with exclamation mark SVG
- Warning: Yellow/orange triangle with exclamation SVG
- Info: Blue circle with "i" SVG
- Resolved: Green checkmark SVG

**Alert Card Features:**
- Severity icon and color
- Alert message
- Sensor name
- Current value vs threshold
- Timestamp (relative: "5 minutes ago")
- Resolve button (only for active alerts)
- Smooth fade-out when resolved

**Color Coding:**
- Critical: Red background/border (#dc2626) with red icon
- Warning: Yellow/orange background/border (#f59e0b) with orange icon
- Info: Blue background/border (#2563eb) with blue icon
- Resolved: Gray/muted (#6b7280) with checkmark icon

---

#### T010: Settings Page
**Assigned**: -  
**Branch**: `settings-page`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Create form for each sensor
- [ ] Load current threshold values
- [ ] Allow editing min/max thresholds
- [ ] Add save button
- [ ] Show success/error messages
- [ ] Add enable/disable toggle for sensors

**Settings Page Layout:**

```
┌─────────────────────────────────────────┐
│  [GEAR ICON] Settings                   │
│  [Dashboard] [Alerts] [Settings]        │
├─────────────────────────────────────────┤
│                                         │
│  Sensor Configuration                   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ [TEMP ICON] Temperature Sensor  │   │
│  │ Location: Main Chamber          │   │
│  │                                 │   │
│  │ Min Threshold: [-25] °C         │   │
│  │ Max Threshold: [-15] °C         │   │
│  │                                 │   │
│  │ Status: [Toggle] Active         │   │
│  │                                 │   │
│  │              [Save Changes]     │   │
│  └─────────────────────────────────┘   │
│                                         │
│  ┌─────────────────────────────────┐   │
│  │ [DROP ICON] Humidity Sensor     │   │
│  │ Location: Main Chamber          │   │
│  │                                 │   │
│  │ Min Threshold: [40] %           │   │
│  │ Max Threshold: [60] %           │   │
│  │                                 │   │
│  │ Status: [Toggle] Active         │   │
│  │                                 │   │
│  │              [Save Changes]     │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

**Icon Suggestions:**
- Settings: Gear/cog SVG icon
- Toggle switch: Custom CSS toggle or SVG switch
- Save button: Floppy disk or checkmark SVG icon

**Form Features:**
- Input fields for min/max thresholds
- Toggle switch for active/inactive
- Validation (min < max)
- Save button per sensor
- Success message: "Settings saved successfully!"
- Error message: "Failed to save. Please try again."

---

### Phase 4: Integration

#### T011: API Integration
**Assigned**: -  
**Branch**: `api-connect`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Create api.js with all API functions
- [ ] Connect dashboard to backend
- [ ] Connect alerts page to backend
- [ ] Connect settings page to backend
- [ ] Handle errors gracefully
- [ ] Add loading indicators

**api.js Functions:**
```javascript
// Sensors
async function getAllSensors()
async function getActiveSensors()
async function updateSensor(id, data)

// Readings
async function getLatestReadings()
async function getReadingHistory(sensorId, startDate, endDate)

// Alerts
async function getAllAlerts()
async function getActiveAlerts()
async function resolveAlert(id)
```

**Error Handling:**
- Show user-friendly error messages
- Log errors to console
- Retry failed requests (optional)

---

#### T012: Simulator Script
**Assigned**: -  
**Branch**: `simulator`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Create Python script to simulate sensors
- [ ] Generate realistic data
- [ ] Send data to backend API every 10 seconds
- [ ] Include occasional anomalies (5% of time)
- [ ] Add README with instructions

**Simulator Features:**
- Temperature: -18°C ± 2°C (normal), occasionally -10°C (anomaly)
- Humidity: 50% ± 5% (normal), occasionally 70% (anomaly)
- Pressure: 1013 hPa ± 5 hPa
- Door: 95% closed, 5% open

**Files to create:**
- `simulator/sensor_simulator.py`
- `simulator/requirements.txt` (requests library)
- `simulator/README.md`

---

#### T013: Testing & Fixes
**Assigned**: -  
**Branch**: `testing`  
**Status**: ⬜ TODO

**What to do:**
- [ ] Test all API endpoints
- [ ] Test frontend on different devices
- [ ] Test with simulator running
- [ ] Fix any bugs found
- [ ] Update documentation

**Testing Checklist:**
- ✅ All sensors display correctly
- ✅ Charts update with new data
- ✅ Alerts are created when thresholds exceeded
- ✅ Alerts can be resolved
- ✅ Settings can be updated
- ✅ Responsive on mobile/tablet/desktop
- ✅ No console errors
- ✅ Simulator sends data successfully

---

## 🔄 Workflow

### For Each Task:

1. **Pick a task** from the board (make sure dependencies are done)
2. **Assign yourself**: Update "Assigned To" with your name in MANAGEMENT.md
3. **Update status** to 🔄 IN PROGRESS
4. **Create branch**: `git checkout -b branch-name`
5. **Do the work** and test it
6. **Commit**: `git commit -m "short message"`
7. **Push**: `git push -u origin branch-name`
8. **Update status** to ✅ DONE
9. **Notify team** in group chat

### Example:
```markdown
| T002 | Database Schema | Hamami | db-schema | 🔄 IN PROGRESS |
```
When done:
```markdown
| T002 | Database Schema | Hamami | db-schema | ✅ DONE |
```

### Commit Message Examples:
- `add sensor entity`
- `create dashboard layout`
- `fix alert color bug`
- `update database schema`

### Branch Naming:
- Keep it short and descriptive
- Use lowercase with hyphens
- Examples: `db-schema`, `sensor-api`, `dashboard`

---

## 🆘 Need Help?

### Common Issues:

**MySQL won't connect:**
- Check MySQL is running
- Verify username/password in application.properties
- Make sure database exists

**CORS errors in frontend:**
- Add `@CrossOrigin` annotation to controllers
- Or configure CORS globally in Spring Boot

**Charts not showing:**
- Check Chart.js is loaded
- Verify data format matches Chart.js requirements
- Check browser console for errors

**API returns 404:**
- Verify endpoint URL is correct
- Check controller mapping
- Make sure Spring Boot is running

---

## 📚 Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Chart.js Docs**: https://www.chartjs.org/docs/
- **MySQL Tutorial**: https://www.mysqltutorial.org/
- **Responsive Design**: https://web.dev/responsive-web-design-basics/
- **Git Basics**: https://git-scm.com/book/en/v2/Getting-Started-Git-Basics

### Frontend Resources
- **Font Awesome Icons**: https://fontawesome.com/icons (free tier available)
- **Heroicons (SVG)**: https://heroicons.com/ (completely free)
- **Feather Icons**: https://feathericons.com/ (simple, clean SVG icons)
- **CSS Flexbox Guide**: https://css-tricks.com/snippets/css/a-guide-to-flexbox/
- **CSS Grid Guide**: https://css-tricks.com/snippets/css/complete-guide-grid/
- **Responsive Design Patterns**: https://responsivedesign.is/patterns/

---

## 🎨 Frontend Design Guidelines

### ⚠️ CRITICAL RULES - READ BEFORE CODING FRONTEND

**1. NO EMOJIS - Use Professional Icons**
```html
<!-- ❌ WRONG - Don't use emojis -->
<div>🌡️ Temperature</div>

<!-- ✅ CORRECT - Use SVG or Font Awesome -->
<div>
  <svg class="icon"><!-- thermometer SVG --></svg>
  Temperature
</div>

<!-- ✅ CORRECT - Font Awesome -->
<div>
  <i class="fa-solid fa-temperature-half"></i>
  Temperature
</div>
```

**2. Adaptive UI/UX Principles**
- **Mobile First**: Design for mobile, then scale up
- **Touch Targets**: Buttons minimum 44x44px for mobile
- **Readable Text**: Minimum 16px font size on mobile
- **Flexible Layouts**: Use Flexbox/Grid, avoid fixed widths
- **Breakpoints**: 
  - Mobile: 320px - 480px
  - Tablet: 481px - 768px
  - Desktop: 769px+

**3. Accessibility (A11y)**
```html
<!-- Always add alt text for icons -->
<img src="temp-icon.svg" alt="Temperature sensor icon">

<!-- Use semantic HTML -->
<button>Save</button>  <!-- Not <div onclick="..."> -->

<!-- Proper contrast ratios -->
/* Text on background must have 4.5:1 contrast ratio */
```

**4. Professional Look**
- Clean, minimal design
- Consistent spacing (use CSS variables)
- Smooth transitions (0.2s - 0.3s)
- Subtle shadows, not heavy effects
- Professional color palette (no bright neon colors)

**5. Icon Implementation Examples**

**Option A: Font Awesome (CDN)**
```html
<!-- Add to <head> -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<!-- Use in HTML -->
<i class="fa-solid fa-temperature-half"></i>
<i class="fa-solid fa-droplet"></i>
<i class="fa-solid fa-door-closed"></i>
<i class="fa-solid fa-gauge"></i>
<i class="fa-solid fa-triangle-exclamation"></i>
```

**Option B: Inline SVG (Best Performance)**
```html
<!-- Temperature Icon -->
<svg class="icon" viewBox="0 0 24 24" fill="currentColor">
  <path d="M12 2c1.1 0 2 .9 2 2v8.5c1.2.7 2 2 2 3.5 0 2.2-1.8 4-4 4s-4-1.8-4-4c0-1.5.8-2.8 2-3.5V4c0-1.1.9-2 2-2z"/>
</svg>

<style>
.icon {
  width: 24px;
  height: 24px;
  color: var(--primary-color);
}
</style>
```

**6. Responsive Card Example**
```css
.sensor-card {
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  background: var(--card-bg);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  transition: transform 0.2s;
}

.sensor-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

/* Mobile: Stack cards */
@media (max-width: 480px) {
  .sensor-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}

/* Tablet: 2 columns */
@media (min-width: 481px) and (max-width: 768px) {
  .sensor-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.5rem;
  }
}

/* Desktop: 4 columns */
@media (min-width: 769px) {
  .sensor-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 2rem;
  }
}
```

**7. Loading States & Feedback**
```html
<!-- Show loading spinner while fetching data -->
<div class="loading">
  <svg class="spinner" viewBox="0 0 50 50">
    <circle cx="25" cy="25" r="20"></circle>
  </svg>
  Loading sensors...
</div>

<!-- Success message -->
<div class="alert alert-success">
  <svg class="icon-check"><!-- checkmark SVG --></svg>
  Settings saved successfully!
</div>
```

**8. Button States**
```css
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

.btn:active {
  transform: translateY(0);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

---

## ✅ Definition of Done

A task is DONE when:
- ✅ Code is written and works
- ✅ Tested locally
- ✅ Committed to Git
- ✅ Pushed to GitHub
- ✅ Status updated in this file
- ✅ Team notified

---

**Last Updated**: May 11, 2026  
**Project Status**: 🟢 Active Development