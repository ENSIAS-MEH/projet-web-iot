/**
 * dashboard.js — Dashboard page logic (T008)
 *
 * Features:
 *  - Sensor cards grid with live values, status indicators, icons
 *  - Chart.js line charts for temperature, humidity, pressure
 *  - Time range selector: 1h, 24h, 7d
 *  - Auto-refresh every 10 seconds
 */

// ── Constants ────────────────────────────────────────────────────────────────

const REFRESH_INTERVAL = 10_000; // 10 seconds

/** SVG icons keyed by sensor_type */
const SENSOR_ICONS = {
  temperature: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
      aria-hidden="true">
    <path stroke-linecap="round" stroke-linejoin="round"
      d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z"/>
  </svg>`,

  humidity: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
      aria-hidden="true">
    <path stroke-linecap="round" stroke-linejoin="round"
      d="M12 2.69l5.66 5.66a8 8 0 11-11.31 0z"/>
  </svg>`,

  door: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
      aria-hidden="true">
    <path stroke-linecap="round" stroke-linejoin="round"
      d="M3 21h18M5 21V5a2 2 0 012-2h10a2 2 0 012 2v16"/>
    <circle cx="15" cy="13" r="1" fill="currentColor"/>
  </svg>`,

  pressure: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
      aria-hidden="true">
    <circle cx="12" cy="12" r="10"/>
    <path stroke-linecap="round" stroke-linejoin="round"
      d="M12 8v4l3 3"/>
    <path stroke-linecap="round" d="M8.5 8.5l1.5 1.5"/>
  </svg>`,

  default: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
      aria-hidden="true">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="12"/>
    <line x1="12" y1="16" x2="12.01" y2="16"/>
  </svg>`,
};

/** Chart colours keyed by sensor_type */
const CHART_COLORS = {
  temperature: { line: '#2563eb', fill: 'rgba(37,99,235,0.08)' },
  humidity:    { line: '#0891b2', fill: 'rgba(8,145,178,0.08)' },
  pressure:    { line: '#7c3aed', fill: 'rgba(124,58,237,0.08)' },
  door:        { line: '#10b981', fill: 'rgba(16,185,129,0.08)' },
  default:     { line: '#64748b', fill: 'rgba(100,116,139,0.08)' },
};

// ── State ────────────────────────────────────────────────────────────────────

let sensors       = [];          // all sensors from API
let latestReadings = {};         // { sensorId: readingObject }
let charts        = {};          // { sensorId: Chart instance }
let activeRange   = '1h';        // current time range
let refreshTimer  = null;

// ── Helpers ──────────────────────────────────────────────────────────────────

function icon(type) {
  return SENSOR_ICONS[type] || SENSOR_ICONS.default;
}

function chartColor(type) {
  return CHART_COLORS[type] || CHART_COLORS.default;
}

/**
 * Returns a formatted display value for a sensor reading.
 * Door sensors show "Open" / "Closed" instead of 0/1.
 */
function formatValue(sensor, value) {
  if (value === null || value === undefined) return '—';
  if (sensor.sensorType === 'door') {
    return Number(value) === 1 ? 'Open' : 'Closed';
  }
  return Number(value).toFixed(1);
}

/**
 * Returns the status ('ok' | 'warning' | 'critical') for a reading.
 */
function readingStatus(sensor, value) {
  if (value === null || value === undefined) return 'inactive';
  return utils.getReadingStatus(
    Number(value),
    Number(sensor.minThreshold),
    Number(sensor.maxThreshold)
  );
}

/**
 * Returns the start ISO datetime for the selected range.
 */
function rangeStart(range) {
  const hours = range === '7d' ? 168 : range === '24h' ? 24 : 1;
  return utils.isoHoursAgo(hours);
}

// ── Sensor Cards ─────────────────────────────────────────────────────────────

/**
 * Builds the HTML for a single sensor card.
 */
function buildSensorCard(sensor, reading) {
  const value  = reading ? reading.value : null;
  const status = readingStatus(sensor, value);
  const display = formatValue(sensor, value);
  const unit   = sensor.sensorType === 'door' ? '' : (sensor.unit || '');
  const updated = reading ? utils.timeAgo(reading.timestamp) : 'No data';

  const statusLabel = status === 'ok' ? 'OK'
    : status === 'warning'  ? 'Warning'
    : status === 'critical' ? 'Alert'
    : 'Inactive';

  return `
    <article class="sensor-card sensor-card--${status}" role="listitem"
             aria-label="${utils.escapeHtml(sensor.name)}: ${utils.escapeHtml(display)} ${utils.escapeHtml(unit)}">
      <div class="sensor-card__header">
        <div class="sensor-card__icon sensor-card__icon--${sensor.sensorType || 'default'}">
          ${icon(sensor.sensorType)}
        </div>
        <span class="sensor-card__status-badge badge badge-${utils.statusClass(status) === 'success' ? 'success' : utils.statusClass(status) === 'warning' ? 'warning' : status === 'critical' ? 'critical' : 'muted'}">
          <span class="status-dot ${status === 'ok' ? 'ok' : status === 'critical' ? 'alert' : status === 'warning' ? 'alert' : 'inactive'}"></span>
          ${utils.escapeHtml(statusLabel)}
        </span>
      </div>

      <div class="sensor-card__body">
        <div class="sensor-value" aria-live="polite">
          ${utils.escapeHtml(display)}
          ${unit ? `<span class="sensor-unit">${utils.escapeHtml(unit)}</span>` : ''}
        </div>
        <div class="sensor-name">${utils.escapeHtml(sensor.name)}</div>
        <div class="sensor-location text-muted text-sm">${utils.escapeHtml(sensor.location || '')}</div>
      </div>

      <div class="sensor-card__footer text-xs text-muted">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        Updated ${utils.escapeHtml(updated)}
      </div>
    </article>`;
}

/**
 * Renders all sensor cards into #sensor-grid.
 */
function renderSensorCards() {
  const grid = document.getElementById('sensor-grid');
  if (!grid) return;

  if (!sensors.length) {
    utils.showEmpty(grid, 'No sensors found. Make sure the backend is running.');
    return;
  }

  grid.innerHTML = sensors.map(s => buildSensorCard(s, latestReadings[s.id])).join('');
}

// ── Charts ───────────────────────────────────────────────────────────────────

/**
 * Returns the chart-able sensor types (exclude door — binary value).
 */
function chartableSensors() {
  return sensors.filter(s => s.sensorType !== 'door');
}

/**
 * Builds the charts section HTML (containers only; Chart.js fills them).
 */
function buildChartsSection() {
  const section = document.getElementById('charts-section');
  if (!section) return;

  const chartable = chartableSensors();
  if (!chartable.length) {
    section.innerHTML = '';
    return;
  }

  // Time range selector
  const rangeBar = `
    <div class="chart-range-bar" role="group" aria-label="Time range selector">
      <span class="chart-range-label">History:</span>
      ${['1h', '24h', '7d'].map(r => `
        <button class="btn btn-sm btn-range ${r === activeRange ? 'btn-range--active' : 'btn-outline'}"
                data-range="${r}" aria-pressed="${r === activeRange}">
          ${r}
        </button>`).join('')}
    </div>`;

  const chartCards = chartable.map(s => {
    const colors = chartColor(s.sensorType);
    return `
      <div class="card chart-card">
        <div class="card-header">
          <h2>
            <span class="chart-icon">${icon(s.sensorType)}</span>
            ${utils.escapeHtml(s.name)} History
          </h2>
        </div>
        <div class="card-body">
          <div class="chart-wrapper">
            <canvas id="chart-${s.id}" aria-label="${utils.escapeHtml(s.name)} history chart"
                    role="img"></canvas>
          </div>
        </div>
      </div>`;
  }).join('');

  section.innerHTML = `
    ${rangeBar}
    <div class="charts-grid" style="margin-top:1rem;">
      ${chartCards}
    </div>`;

  // Wire range buttons
  section.querySelectorAll('.btn-range').forEach(btn => {
    btn.addEventListener('click', () => {
      activeRange = btn.dataset.range;
      // Update button states
      section.querySelectorAll('.btn-range').forEach(b => {
        const active = b.dataset.range === activeRange;
        b.classList.toggle('btn-range--active', active);
        b.classList.toggle('btn-outline', !active);
        b.setAttribute('aria-pressed', active);
      });
      loadChartData();
    });
  });
}

/**
 * Creates or updates a Chart.js line chart for a sensor.
 */
function renderChart(sensor, readings) {
  const canvas = document.getElementById(`chart-${sensor.id}`);
  if (!canvas) return;

  const colors = chartColor(sensor.sensorType);

  // Prepare data
  const labels = readings.map(r => {
    const d = new Date(r.timestamp);
    return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  });
  const values = readings.map(r => Number(r.value));

  const minLine = sensor.minThreshold !== null
    ? Array(readings.length).fill(Number(sensor.minThreshold)) : null;
  const maxLine = sensor.maxThreshold !== null
    ? Array(readings.length).fill(Number(sensor.maxThreshold)) : null;

  const datasets = [
    {
      label: sensor.name,
      data: values,
      borderColor: colors.line,
      backgroundColor: colors.fill,
      borderWidth: 2,
      pointRadius: readings.length > 60 ? 0 : 3,
      pointHoverRadius: 5,
      fill: true,
      tension: 0.35,
    },
  ];

  if (minLine) {
    datasets.push({
      label: `Min (${sensor.minThreshold} ${sensor.unit || ''})`,
      data: minLine,
      borderColor: 'rgba(220,38,38,0.5)',
      borderWidth: 1.5,
      borderDash: [6, 4],
      pointRadius: 0,
      fill: false,
      tension: 0,
    });
  }
  if (maxLine) {
    datasets.push({
      label: `Max (${sensor.maxThreshold} ${sensor.unit || ''})`,
      data: maxLine,
      borderColor: 'rgba(245,158,11,0.5)',
      borderWidth: 1.5,
      borderDash: [6, 4],
      pointRadius: 0,
      fill: false,
      tension: 0,
    });
  }

  if (charts[sensor.id]) {
    // Update existing chart
    charts[sensor.id].data.labels   = labels;
    charts[sensor.id].data.datasets = datasets;
    charts[sensor.id].update('none'); // no animation on refresh
  } else {
    // Create new chart
    charts[sensor.id] = new Chart(canvas, {
      type: 'line',
      data: { labels, datasets },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: {
            display: true,
            position: 'top',
            labels: {
              font: { family: "'Inter', system-ui, sans-serif", size: 12 },
              color: '#64748b',
              boxWidth: 12,
              padding: 16,
            },
          },
          tooltip: {
            backgroundColor: '#1e293b',
            titleColor: '#f8fafc',
            bodyColor: '#cbd5e1',
            padding: 10,
            cornerRadius: 6,
          },
        },
        scales: {
          x: {
            ticks: {
              color: '#94a3b8',
              font: { size: 11 },
              maxTicksLimit: 8,
              maxRotation: 0,
            },
            grid: { color: 'rgba(226,232,240,0.6)' },
          },
          y: {
            ticks: {
              color: '#94a3b8',
              font: { size: 11 },
              callback: v => `${v} ${sensor.unit || ''}`,
            },
            grid: { color: 'rgba(226,232,240,0.6)' },
          },
        },
      },
    });
  }
}

// ── Data loading ─────────────────────────────────────────────────────────────

/**
 * Loads the latest reading for each sensor and re-renders cards.
 */
async function loadLatestReadings() {
  try {
    const readings = await api.getLatestReadings();
    latestReadings = {};
    if (Array.isArray(readings)) {
      readings.forEach(r => { latestReadings[r.sensorId] = r; });
    }
    renderSensorCards();
  } catch (err) {
    console.error('Failed to load latest readings:', err);
    // Cards will show "No data" — don't crash the whole dashboard
  }
}

/**
 * Loads historical data for each chartable sensor and renders charts.
 */
async function loadChartData() {
  const start = rangeStart(activeRange);
  const end   = new Date().toISOString().slice(0, 19);

  for (const sensor of chartableSensors()) {
    try {
      const readings = await api.getReadingHistory(sensor.id, start, end);
      if (Array.isArray(readings)) {
        renderChart(sensor, readings);
      }
    } catch (err) {
      console.error(`Failed to load chart data for sensor ${sensor.id}:`, err);
    }
  }
}

/**
 * Full refresh: latest readings + chart data.
 */
async function refresh() {
  await loadLatestReadings();
  await loadChartData();
}

// ── Init ─────────────────────────────────────────────────────────────────────

async function init() {
  // Show loading state
  const grid = document.getElementById('sensor-grid');
  if (grid) utils.showLoading(grid, 'Loading sensors...');

  try {
    sensors = await api.getAllSensors();
  } catch (err) {
    console.error('Failed to load sensors:', err);
    if (grid) utils.showEmpty(grid, 'Could not connect to the backend. Is the server running on port 8080?');
    utils.showToast('Cannot reach the backend server.', 'error');
    return;
  }

  // Build charts section (containers + range bar)
  buildChartsSection();

  // Load data
  await refresh();

  // Auto-refresh every 10 s
  refreshTimer = setInterval(refresh, REFRESH_INTERVAL);
}

document.addEventListener('DOMContentLoaded', init);
