/**
 * api.js — Centralised API service for Cold Room Monitoring System
 * All HTTP calls to the Spring Boot backend go through this module.
 */

const API_BASE = 'http://localhost:8080/api';

// ── Generic fetch wrapper ────────────────────────────────────────────────────

/**
 * Performs a fetch request and returns parsed JSON.
 * Throws an Error with a user-friendly message on failure.
 *
 * @param {string} url
 * @param {RequestInit} [options]
 * @returns {Promise<any>}
 */
async function apiFetch(url, options = {}) {
  const defaults = {
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
  };
  const config = { ...defaults, ...options };
  if (options.headers) {
    config.headers = { ...defaults.headers, ...options.headers };
  }

  try {
    const response = await fetch(url, config);

    if (!response.ok) {
      let message = `HTTP ${response.status}: ${response.statusText}`;
      try {
        const err = await response.json();
        if (err.message) message = err.message;
      } catch (_) { /* ignore parse error */ }
      throw new Error(message);
    }

    // 204 No Content
    if (response.status === 204) return null;

    return await response.json();
  } catch (err) {
    if (err instanceof TypeError) {
      // Network error (backend not running, CORS, etc.)
      throw new Error('Cannot reach the server. Make sure the backend is running on port 8080.');
    }
    throw err;
  }
}

// ── Sensors ──────────────────────────────────────────────────────────────────

/** Returns all sensors. */
async function getAllSensors() {
  return apiFetch(`${API_BASE}/sensors`);
}

/** Returns only active sensors. */
async function getActiveSensors() {
  return apiFetch(`${API_BASE}/sensors/active`);
}

/** Returns a single sensor by ID. */
async function getSensorById(id) {
  return apiFetch(`${API_BASE}/sensors/${id}`);
}

/**
 * Updates a sensor's configuration (thresholds, active state, etc.).
 * @param {number} id
 * @param {object} data
 */
async function updateSensor(id, data) {
  return apiFetch(`${API_BASE}/sensors/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

// ── Readings ─────────────────────────────────────────────────────────────────

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
 * Returns readings for a specific sensor.
 * @param {number} sensorId
 */
async function getReadingsBySensor(sensorId) {
  return apiFetch(`${API_BASE}/readings/sensor/${sensorId}`);
}

/**
 * Returns readings for a sensor within a date range.
 * @param {number}  sensorId
 * @param {string}  [startDate]  ISO-8601 datetime string
 * @param {string}  [endDate]    ISO-8601 datetime string
 */
async function getReadingHistory(sensorId, startDate, endDate) {
  const params = new URLSearchParams({ sensorId });
  if (startDate) params.append('startDate', startDate);
  if (endDate)   params.append('endDate',   endDate);
  return apiFetch(`${API_BASE}/readings/history?${params}`);
}

// ── Alerts ───────────────────────────────────────────────────────────────────

/** Returns all alerts, optionally filtered. */
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
 * Marks an alert as resolved.
 * @param {number} id
 */
async function resolveAlert(id) {
  return apiFetch(`${API_BASE}/alerts/${id}/resolve`, { method: 'PUT' });
}

/**
 * Returns alerts for a specific sensor.
 * @param {number} sensorId
 */
async function getAlertsBySensor(sensorId) {
  return apiFetch(`${API_BASE}/alerts/sensor/${sensorId}`);
}

// ── Health ───────────────────────────────────────────────────────────────────

/** Checks if the backend is reachable. */
async function checkHealth() {
  return apiFetch('http://localhost:8080/health');
}

// ── Exports (for use as ES module or global) ─────────────────────────────────
// When loaded via <script> tag these are available as window globals.
window.api = {
  getAllSensors,
  getActiveSensors,
  getSensorById,
  updateSensor,
  getLatestReadings,
  getAllReadings,
  getReadingsBySensor,
  getReadingHistory,
  getAllAlerts,
  getActiveAlerts,
  resolveAlert,
  getAlertsBySensor,
  checkHealth,
};
