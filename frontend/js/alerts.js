/**
 * alerts.js — Alerts page logic (T009)
 *
 * Features:
 *  - Loads all alerts from the backend
 *  - Filter by status: All / Active / Resolved
 *  - Filter by severity: All / Critical / Warning / Info
 *  - Resolve button per active alert (calls PUT /api/alerts/{id}/resolve)
 *  - Color-coded cards by severity
 *  - Relative timestamps ("5 minutes ago")
 *  - Smooth fade-out on resolve
 *  - Responsive: cards on mobile, table-like on desktop
 */

'use strict';

// ── State ────────────────────────────────────────────────────────────────────

let allAlerts      = [];   // full list fetched from backend
let statusFilter   = 'all';
let severityFilter = 'all';

// ── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  initFilters();
  loadAlerts();
});

// ── Data loading ─────────────────────────────────────────────────────────────

async function loadAlerts() {
  const list = document.getElementById('alerts-list');
  utils.showLoading(list, 'Loading alerts...');

  try {
    allAlerts = await api.getAllAlerts();
    renderAlerts();
  } catch (err) {
    list.innerHTML = `
      <div class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round"
            d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
        </svg>
        <p>Failed to load alerts: ${utils.escapeHtml(err.message)}</p>
      </div>`;
    utils.showToast(err.message, 'error');
  }
}

// ── Rendering ─────────────────────────────────────────────────────────────────

function renderAlerts() {
  const list    = document.getElementById('alerts-list');
  const visible = applyFilters(allAlerts);

  if (visible.length === 0) {
    utils.showEmpty(list, getEmptyMessage());
    return;
  }

  list.innerHTML = visible.map(alert => buildAlertCard(alert)).join('');

  // Wire up resolve buttons
  list.querySelectorAll('.btn-resolve').forEach(btn => {
    btn.addEventListener('click', () => handleResolve(Number(btn.dataset.id)));
  });
}

/**
 * Builds the HTML for a single alert card.
 * @param {object} alert
 * @returns {string}
 */
function buildAlertCard(alert) {
  const severityInfo = getSeverityInfo(alert.severity);
  const statusBadge  = alert.isResolved
    ? `<span class="badge badge-success">
         <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"
              width="11" height="11" aria-hidden="true">
           <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
         </svg>
         Resolved
       </span>`
    : `<span class="badge ${severityInfo.badgeClass}">
         ${severityInfo.iconSvg}
         ${utils.escapeHtml(alert.severity)}
       </span>`;

  const resolveBtn = !alert.isResolved
    ? `<button class="btn btn-sm btn-outline btn-resolve" data-id="${alert.id}"
              aria-label="Resolve alert ${alert.id}">
         <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
              aria-hidden="true">
           <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
         </svg>
         Resolve
       </button>`
    : '';

  const resolvedInfo = alert.isResolved && alert.resolvedAt
    ? `<span class="text-xs text-muted">
         Resolved ${utils.timeAgo(alert.resolvedAt)}
       </span>`
    : '';

  return `
    <article class="alert-card alert-card--${utils.escapeHtml(alert.severity)}${alert.isResolved ? ' alert-card--resolved' : ''}"
             id="alert-${alert.id}"
             aria-label="Alert: ${utils.escapeHtml(alert.message)}">
      <div class="alert-card__header">
        <div class="alert-card__icon alert-card__icon--${utils.escapeHtml(alert.severity)}" aria-hidden="true">
          ${severityInfo.iconSvg}
        </div>
        <div class="alert-card__badges">
          ${statusBadge}
        </div>
      </div>

      <div class="alert-card__body">
        <p class="alert-card__message">${utils.escapeHtml(alert.message)}</p>
        <div class="alert-card__meta">
          <span class="text-sm text-muted">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 width="13" height="13" aria-hidden="true">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M9 3H5a2 2 0 00-2 2v4m6-6h10a2 2 0 012 2v4M9 3v18m0 0h10a2 2 0 002-2V9M9 21H5a2 2 0 01-2-2V9m0 0h18"/>
            </svg>
            ${utils.escapeHtml(alert.sensorName || 'Unknown sensor')}
          </span>
          <span class="text-sm text-muted">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                 width="13" height="13" aria-hidden="true">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            ${utils.timeAgo(alert.createdAt)}
          </span>
        </div>
      </div>

      <div class="alert-card__footer">
        ${resolveBtn}
        ${resolvedInfo}
      </div>
    </article>`;
}

// ── Resolve ───────────────────────────────────────────────────────────────────

async function handleResolve(alertId) {
  const card = document.getElementById(`alert-${alertId}`);
  const btn  = card ? card.querySelector('.btn-resolve') : null;

  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Resolving...';
  }

  try {
    const updated = await api.resolveAlert(alertId);

    // Update in local state
    const idx = allAlerts.findIndex(a => a.id === alertId);
    if (idx !== -1) allAlerts[idx] = updated;

    // Smooth fade-out then re-render
    if (card) {
      card.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
      card.style.opacity    = '0';
      card.style.transform  = 'translateX(20px)';
      setTimeout(() => renderAlerts(), 420);
    } else {
      renderAlerts();
    }

    utils.showToast('Alert resolved successfully', 'success');
  } catch (err) {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = `
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
             aria-hidden="true">
          <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
        </svg>
        Resolve`;
    }
    utils.showToast(`Failed to resolve: ${err.message}`, 'error');
  }
}

// ── Filters ───────────────────────────────────────────────────────────────────

function initFilters() {
  document.querySelectorAll('[data-filter]').forEach(btn => {
    btn.addEventListener('click', () => {
      const filterType = btn.dataset.filter;
      const value      = btn.dataset.value;

      if (filterType === 'status') {
        statusFilter = value;
        updateFilterButtons('#status-filters', value);
      } else if (filterType === 'severity') {
        severityFilter = value;
        updateFilterButtons('#severity-filters', value);
      }

      renderAlerts();
    });
  });
}

/**
 * Updates aria-pressed and btn classes for a filter group.
 * @param {string} groupSelector
 * @param {string} activeValue
 */
function updateFilterButtons(groupSelector, activeValue) {
  document.querySelectorAll(`${groupSelector} [data-filter]`).forEach(btn => {
    const isActive = btn.dataset.value === activeValue;
    btn.setAttribute('aria-pressed', isActive);
    btn.classList.toggle('btn-primary', isActive);
    btn.classList.toggle('btn-outline', !isActive);
  });
}

/**
 * Applies current status + severity filters to the full alert list.
 * @param {object[]} alerts
 * @returns {object[]}
 */
function applyFilters(alerts) {
  return alerts.filter(alert => {
    const matchStatus = statusFilter === 'all'
      || (statusFilter === 'active'   && !alert.isResolved)
      || (statusFilter === 'resolved' &&  alert.isResolved);

    const matchSeverity = severityFilter === 'all'
      || alert.severity === severityFilter;

    return matchStatus && matchSeverity;
  });
}

function getEmptyMessage() {
  if (statusFilter === 'active')   return 'No active alerts — all clear!';
  if (statusFilter === 'resolved') return 'No resolved alerts yet.';
  if (severityFilter !== 'all')    return `No ${severityFilter} alerts found.`;
  return 'No alerts found.';
}

// ── Severity helpers ──────────────────────────────────────────────────────────

/**
 * Returns icon SVG and badge CSS class for a severity level.
 * @param {string} severity
 * @returns {{ iconSvg: string, badgeClass: string }}
 */
function getSeverityInfo(severity) {
  switch (severity) {
    case 'critical':
      return {
        badgeClass: 'badge-critical',
        iconSvg: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       width="14" height="14" aria-hidden="true">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>`,
      };
    case 'warning':
      return {
        badgeClass: 'badge-warning',
        iconSvg: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       width="14" height="14" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round"
                      d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
                  </svg>`,
      };
    case 'info':
    default:
      return {
        badgeClass: 'badge-info',
        iconSvg: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                       width="14" height="14" aria-hidden="true">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>`,
      };
  }
}
