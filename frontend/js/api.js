/**
 * api.js — Centralised API service for Cold Room Monitoring System
 *
 * All HTTP calls to the Spring Boot backend go through this module.
 * Loaded before every page script so dashboard.js / alerts.js / settings.js
 * can call window.api.* without any extra setup.
 *
 * T011: API Integration
 *  - api.js with all required functions  ✓
 *  - Connected to dashboard, alerts, settings pages  ✓
 *  - Graceful error handling with user-friendly messages  ✓
 *  - Loading indicators via utils.showLoading()  ✓
 *  - Backend connection status banner on page load  ✓
 */

'use strict';

const API_BASE = 'http://localhost:8080/api';

// ── Generic fetch wrapper ────────────────────────────────────────────────────

/**
 * Performs a fetch request and returns parsed JSON.
 * Throws an Error with a user-friendly message on non-2xx or network failure.
 *
 * @param {string}      url
 * @param {RequestInit} [options]
 * @returns {Promise<any>}
 */
async function apiFetch(url, options = {}) {
  const config = {
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
    ...options,
  };
  // Merge headers if caller supplied extras
  if (options.headers) {
    config.headers = { ...config.headers, ...options.headers };
  }

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      let message = `HTTP ${response.status}: ${response.statusText}`;
      try {
        const err = await response.json();
        if (err.message) message = err.message;
      } catch (_) { /* ignore JSON parse error on error body */ }
      throw new Error(message);
    }

    // 204 No Content — nothing to parse
    if (response.status === 204) return null;

    return await response.json();

  } catch (err) {
    if (err instanceof TypeError) {
      // Network-level failure: backend not running, CORS blocked, etc.
      throw new Error(
        'Cannot reach the server. Make sure the backend is running on port 8080.'
      );
    }
    throw err;
  }
}

// ── Health ───────────────────────────────────────────────────────────────────

/**
 * Checks if the backend is reachable.
 * @returns {Promise<{status: string}>}
 */
async function checkHealth() {
  return apiFetch('http://localhost:8080/health');
}

// ── Sensors ──────────────────────────────────────────────────────────────────

/** Returns all sensors (active and inactive). */
async function getAllSensors() {
  return apiFetch(`${API_BASE}/sensors`);
}

/** Returns only sensors with isActive = true. */
async function getActiveSensors() {
  return apiFetch(`${API_BASE}/sensors/active`);
}

/**
 * Returns a single sensor by ID.
 * @param {number} id
 */
async function getSensorById(id) {
  return apiFetch(`${API_BASE}/sensors/${id}`);
}

/**
 * Updates a sensor's configuration (thresholds, active state, etc.).
 * @param {number} id
 * @param {object} data  — full sensor payload
 */
async function updateSensor(id, data) {
  return apiFetch(`${API_BASE}/sensors/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

// ── Readings ─────────────────────────────────────────────────────────────────

/**
 * Posts a new sensor reading (used by the simulator and manual testing).
 * @param {{ sensorId: number, value: number, timestamp?: string }} data
 */
async function postReading(data) {
  return apiFetch(`${API_BASE}/readings`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** Returns the latest reading for each sensor. */
async function getLatestReadings() {
  return apiFetch(`${API_BASE}/readings/latest`);
}

/**
 * Returns paginated readings.
 * @param {number} [page=0]
 * @param {number} [size=20]
 */
async function getAllReadings(page = 0, size = 20) {
  return apiFetch(`${API_BASE}/readings?page=${page}&size=${size}`);
}

/**
 * Returns all readings for a specific sensor.
 * @param {number} sensorId
 */
async function getReadingsBySensor(sensorId) {
  return apiFetch(`${API_BASE}/readings/sensor/${sensorId}`);
}

/**
 * Returns readings for a sensor within a date range.
 * @param {number} sensorId
 * @param {string} [startDate]  ISO-8601 datetime (no milliseconds)
 * @param {string} [endDate]    ISO-8601 datetime (no milliseconds)
 */
async function getReadingHistory(sensorId, startDate, endDate) {
  const params = new URLSearchParams({ sensorId });
  if (startDate) params.append('startDate', startDate);
  if (endDate)   params.append('endDate',   endDate);
  return apiFetch(`${API_BASE}/readings/history?${params}`);
}

// ── Alerts ───────────────────────────────────────────────────────────────────

/**
 * Returns all alerts, with optional filters.
 * @param {boolean|null} [isResolved]
 * @param {string|null}  [severity]   'info' | 'warning' | 'critical'
 */
async function getAllAlerts(isResolved = null, severity = null) {
  const params = new URLSearchParams();
  if (isResolved !== null) params.append('isResolved', isResolved);
  if (severity)            params.append('severity',   severity);
  const qs = params.toString();
  return apiFetch(`${API_BASE}/alerts${qs ? '?' + qs : ''}`);
}

/** Returns only unresolved (active) alerts. */
async function getActiveAlerts() {
  return apiFetch(`${API_BASE}/alerts/active`);
}

/**
 * Returns a single alert by ID.
 * @param {number} id
 */
async function getAlertById(id) {
  return apiFetch(`${API_BASE}/alerts/${id}`);
}

/**
 * Marks an alert as resolved.
 * @param {number} id
 */
async function resolveAlert(id) {
  return apiFetch(`${API_BASE}/alerts/${id}/resolve`, { method: 'PUT' });
}

/**
 * Returns all alerts for a specific sensor.
 * @param {number} sensorId
 */
async function getAlertsBySensor(sensorId) {
  return apiFetch(`${API_BASE}/alerts/sensor/${sensorId}`);
}

// ── Connection status banner ─────────────────────────────────────────────────

/**
 * Shows a dismissible warning banner at the top of the page when the
 * backend cannot be reached.  Automatically hides if the backend comes back.
 */
function showOfflineBanner() {
  if (document.getElementById('offline-banner')) return; // already shown

  const banner = document.createElement('div');
  banner.id = 'offline-banner';
  banner.setAttribute('role', 'alert');
  banner.style.cssText = `
    position: fixed; top: 64px; left: 0; right: 0; z-index: 2000;
    background: #fef3c7; border-bottom: 2px solid #f59e0b;
    color: #92400e; padding: 0.625rem 1.5rem;
    display: flex; align-items: center; justify-content: space-between;
    gap: 1rem; font-size: 0.875rem; font-weight: 500;
  `;
  banner.innerHTML = `
    <span style="display:flex;align-items:center;gap:0.5rem;">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
           stroke="currentColor" stroke-width="2" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94
             a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
      </svg>
      Backend not reachable — make sure Spring Boot is running on port 8080.
    </span>
    <button onclick="this.parentElement.remove()" aria-label="Dismiss"
            style="background:none;border:none;cursor:pointer;font-size:1.25rem;
                   color:#92400e;line-height:1;padding:0 0.25rem;">
      &times;
    </button>`;

  document.body.appendChild(banner);
}

function hideOfflineBanner() {
  const banner = document.getElementById('offline-banner');
  if (banner) banner.remove();
}

/**
 * Pings /health on page load and shows/hides the offline banner.
 * Retries every 15 s while offline.
 */
async function initConnectionCheck() {
  let retryTimer = null;

  async function check() {
    try {
      await checkHealth();
      hideOfflineBanner();
      if (retryTimer) { clearInterval(retryTimer); retryTimer = null; }
    } catch (_) {
      showOfflineBanner();
      if (!retryTimer) {
        retryTimer = setInterval(check, 15_000);
      }
    }
  }

  await check();
}

// ── Exports ──────────────────────────────────────────────────────────────────
// All functions exposed as window.api so every page script can call them.

window.api = {
  // Health
  checkHealth,

  // Sensors
  getAllSensors,
  getActiveSensors,
  getSensorById,
  updateSensor,

  // Readings
  postReading,
  getLatestReadings,
  getAllReadings,
  getReadingsBySensor,
  getReadingHistory,

  // Alerts
  getAllAlerts,
  getActiveAlerts,
  getAlertById,
  resolveAlert,
  getAlertsBySensor,
};

// Run connection check after DOM is ready
document.addEventListener('DOMContentLoaded', initConnectionCheck);
