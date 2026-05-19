/**
 * settings.js — Settings Page logic (T010)
 *
 * Responsibilities:
 *  - Load all sensors from the API
 *  - Render a config card per sensor (thresholds + active toggle)
 *  - Validate min < max before saving
 *  - PUT updated sensor to the API
 *  - Show success / error toast feedback
 */

'use strict';

// ── SVG icons used in sensor cards ──────────────────────────────────────────

const SENSOR_ICONS = {
  temperature: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
      <path stroke-linecap="round" stroke-linejoin="round"
        d="M14 14.76V3.5a2.5 2.5 0 00-5 0v11.26a4.5 4.5 0 105 0z"/>
    </svg>`,
  humidity: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
      <path stroke-linecap="round" stroke-linejoin="round"
        d="M12 2.69l5.66 5.66a8 8 0 11-11.31 0z"/>
    </svg>`,
  door: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
      <path stroke-linecap="round" stroke-linejoin="round"
        d="M3 21h18M9 21V5a2 2 0 012-2h4a2 2 0 012 2v16"/>
      <circle cx="14.5" cy="13" r="0.5" fill="currentColor"/>
    </svg>`,
  pressure: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
      <circle cx="12" cy="12" r="10"/>
      <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3"/>
    </svg>`,
  default: `
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
      <circle cx="12" cy="12" r="10"/>
      <line x1="12" y1="8" x2="12" y2="12"/>
      <line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>`,
};

// ── Helpers ──────────────────────────────────────────────────────────────────

function getIcon(type) {
  return SENSOR_ICONS[type] || SENSOR_ICONS.default;
}

function iconClass(type) {
  const map = {
    temperature: 'sensor-card__icon--temperature',
    humidity:    'sensor-card__icon--humidity',
    door:        'sensor-card__icon--door',
    pressure:    'sensor-card__icon--pressure',
  };
  return map[type] || 'sensor-card__icon--default';
}

// ── Card renderer ────────────────────────────────────────────────────────────

/**
 * Builds and returns a settings card DOM element for one sensor.
 * @param {object} sensor  — sensor DTO from the API
 * @returns {HTMLElement}
 */
function buildSensorCard(sensor) {
  const card = document.createElement('div');
  card.className = 'settings-card card';
  card.dataset.sensorId = sensor.id;

  card.innerHTML = `
    <!-- Card header: icon + name + location -->
    <div class="card-header">
      <h2>
        <span class="sensor-card__icon ${iconClass(sensor.sensorType)}" aria-hidden="true">
          ${getIcon(sensor.sensorType)}
        </span>
        <span>${utils.escapeHtml(sensor.name)}</span>
      </h2>
      <span class="text-muted text-sm">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             width="14" height="14" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round"
            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243
               a8 8 0 1111.314 0z"/>
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        ${utils.escapeHtml(sensor.location)}
      </span>
    </div>

    <!-- Card body: threshold inputs + toggle -->
    <div class="card-body">
      <form class="settings-form" novalidate
            aria-label="Settings for ${utils.escapeHtml(sensor.name)}">

        <div class="settings-thresholds">
          <!-- Min threshold -->
          <div class="form-group">
            <label class="form-label" for="min-${sensor.id}">
              Min Threshold
            </label>
            <div class="input-with-unit">
              <input
                type="number"
                id="min-${sensor.id}"
                name="minThreshold"
                class="form-control"
                value="${sensor.minThreshold}"
                step="0.1"
                aria-describedby="min-hint-${sensor.id}"
                required
              />
              <span class="input-unit" aria-hidden="true">${utils.escapeHtml(sensor.unit)}</span>
            </div>
            <span id="min-hint-${sensor.id}" class="form-hint">
              Readings below this value trigger an alert
            </span>
          </div>

          <!-- Max threshold -->
          <div class="form-group">
            <label class="form-label" for="max-${sensor.id}">
              Max Threshold
            </label>
            <div class="input-with-unit">
              <input
                type="number"
                id="max-${sensor.id}"
                name="maxThreshold"
                class="form-control"
                value="${sensor.maxThreshold}"
                step="0.1"
                aria-describedby="max-hint-${sensor.id}"
                required
              />
              <span class="input-unit" aria-hidden="true">${utils.escapeHtml(sensor.unit)}</span>
            </div>
            <span id="max-hint-${sensor.id}" class="form-hint">
              Readings above this value trigger an alert
            </span>
          </div>
        </div>

        <!-- Validation error message (hidden by default) -->
        <div class="settings-error" id="error-${sensor.id}" role="alert" aria-live="polite"
             style="display:none;">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
               aria-hidden="true">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span class="settings-error__text"></span>
        </div>

        <!-- Active toggle -->
        <div class="form-group">
          <label class="form-label" id="toggle-label-${sensor.id}">Sensor Status</label>
          <div class="toggle-wrapper" aria-labelledby="toggle-label-${sensor.id}">
            <label class="toggle">
              <input
                type="checkbox"
                id="active-${sensor.id}"
                name="isActive"
                ${sensor.isActive ? 'checked' : ''}
                aria-label="Enable ${utils.escapeHtml(sensor.name)}"
              />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-label" id="toggle-status-${sensor.id}">
              ${sensor.isActive ? 'Active' : 'Inactive'}
            </span>
          </div>
        </div>

        <!-- Save button -->
        <div class="settings-actions">
          <button type="submit" class="btn btn-primary" id="save-btn-${sensor.id}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M5 13l4 4L19 7"/>
            </svg>
            Save Changes
          </button>
        </div>

      </form>
    </div>
  `;

  // Wire up toggle label update
  const checkbox = card.querySelector(`#active-${sensor.id}`);
  const statusLabel = card.querySelector(`#toggle-status-${sensor.id}`);
  checkbox.addEventListener('change', () => {
    statusLabel.textContent = checkbox.checked ? 'Active' : 'Inactive';
  });

  // Wire up form submit
  const form = card.querySelector('.settings-form');
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    handleSave(sensor, card);
  });

  return card;
}

// ── Save handler ─────────────────────────────────────────────────────────────

/**
 * Reads form values, validates, and PUTs to the API.
 * @param {object}      sensor  — original sensor DTO
 * @param {HTMLElement} card    — the card DOM element
 */
async function handleSave(sensor, card) {
  const minInput  = card.querySelector(`#min-${sensor.id}`);
  const maxInput  = card.querySelector(`#max-${sensor.id}`);
  const checkbox  = card.querySelector(`#active-${sensor.id}`);
  const saveBtn   = card.querySelector(`#save-btn-${sensor.id}`);
  const errorBox  = card.querySelector(`#error-${sensor.id}`);
  const errorText = card.querySelector(`#error-${sensor.id} .settings-error__text`);

  const minVal = parseFloat(minInput.value);
  const maxVal = parseFloat(maxInput.value);

  // Hide previous error
  errorBox.style.display = 'none';
  minInput.classList.remove('input-error');
  maxInput.classList.remove('input-error');

  // Validate
  if (isNaN(minVal) || isNaN(maxVal)) {
    showFieldError(errorBox, errorText, minInput, maxInput, 'Both threshold values are required.');
    return;
  }

  if (minVal >= maxVal) {
    showFieldError(errorBox, errorText, minInput, maxInput,
      'Min threshold must be less than max threshold.');
    return;
  }

  // Disable button while saving
  saveBtn.disabled = true;
  saveBtn.innerHTML = `
    <svg class="spin-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor"
         stroke-width="2" aria-hidden="true">
      <path stroke-linecap="round" stroke-linejoin="round"
        d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581
           m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
    </svg>
    Saving...`;

  try {
    const payload = {
      name:         sensor.name,
      sensorType:   sensor.sensorType,
      unit:         sensor.unit,
      location:     sensor.location,
      minThreshold: minVal,
      maxThreshold: maxVal,
      isActive:     checkbox.checked,
    };

    await api.updateSensor(sensor.id, payload);

    // Update local sensor reference so re-saves use fresh values
    sensor.minThreshold = minVal;
    sensor.maxThreshold = maxVal;
    sensor.isActive     = checkbox.checked;

    utils.showToast('Settings saved successfully!', 'success');

  } catch (err) {
    console.error('Failed to save sensor settings:', err);
    utils.showToast(err.message || 'Failed to save. Please try again.', 'error');
  } finally {
    saveBtn.disabled = false;
    saveBtn.innerHTML = `
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
           aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
      </svg>
      Save Changes`;
  }
}

function showFieldError(errorBox, errorText, minInput, maxInput, message) {
  errorText.textContent = message;
  errorBox.style.display = 'flex';
  minInput.classList.add('input-error');
  maxInput.classList.add('input-error');
  minInput.focus();
}

// ── Page initialisation ──────────────────────────────────────────────────────

async function initSettingsPage() {
  const grid = document.getElementById('settings-grid');
  if (!grid) return;

  utils.showLoading(grid, 'Loading sensor configuration...');

  try {
    const sensors = await api.getAllSensors();

    if (!sensors || sensors.length === 0) {
      utils.showEmpty(grid, 'No sensors found. Add sensors via the API.');
      return;
    }

    // Clear loading state and render one card per sensor
    grid.innerHTML = '';
    sensors.forEach(sensor => {
      const card = buildSensorCard(sensor);
      grid.appendChild(card);
    });

  } catch (err) {
    console.error('Failed to load sensors:', err);
    grid.innerHTML = `
      <div class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
             aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round"
            d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94
               a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
        </svg>
        <p>${utils.escapeHtml(err.message || 'Failed to load sensor configuration.')}</p>
        <button class="btn btn-outline btn-sm" onclick="initSettingsPage()">
          Retry
        </button>
      </div>`;
  }
}

document.addEventListener('DOMContentLoaded', initSettingsPage);
