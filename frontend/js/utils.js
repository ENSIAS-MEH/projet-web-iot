/**
 * utils.js — Shared utility functions for Cold Room Monitoring System
 */

// ── Time formatting ──────────────────────────────────────────────────────────

/**
 * Returns a human-readable relative time string (e.g. "5 minutes ago").
 * @param {string|Date} dateInput
 * @returns {string}
 */
function timeAgo(dateInput) {
  const date = dateInput instanceof Date ? dateInput : new Date(dateInput);
  const now  = new Date();
  const diff = Math.floor((now - date) / 1000); // seconds

  if (diff < 5)   return 'just now';
  if (diff < 60)  return `${diff} seconds ago`;

  const mins = Math.floor(diff / 60);
  if (mins < 60)  return `${mins} minute${mins !== 1 ? 's' : ''} ago`;

  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} hour${hours !== 1 ? 's' : ''} ago`;

  const days = Math.floor(hours / 24);
  if (days < 7)   return `${days} day${days !== 1 ? 's' : ''} ago`;

  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

/**
 * Formats a date as a short locale string.
 * @param {string|Date} dateInput
 * @returns {string}
 */
function formatDate(dateInput) {
  const date = dateInput instanceof Date ? dateInput : new Date(dateInput);
  return date.toLocaleString(undefined, {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

/**
 * Returns an ISO-8601 datetime string offset by the given hours from now.
 * @param {number} hoursBack  negative = past
 * @returns {string}
 */
function isoHoursAgo(hoursBack) {
  const d = new Date();
  d.setHours(d.getHours() - hoursBack);
  return d.toISOString().slice(0, 19); // strip milliseconds
}

// ── DOM helpers ──────────────────────────────────────────────────────────────

/**
 * Shorthand for document.querySelector.
 * @param {string} selector
 * @param {Element} [root=document]
 * @returns {Element|null}
 */
function qs(selector, root = document) {
  return root.querySelector(selector);
}

/**
 * Shorthand for document.querySelectorAll (returns Array).
 * @param {string} selector
 * @param {Element} [root=document]
 * @returns {Element[]}
 */
function qsa(selector, root = document) {
  return Array.from(root.querySelectorAll(selector));
}

/**
 * Shows a toast notification.
 * @param {string} message
 * @param {'success'|'error'|'warning'|'info'} [type='info']
 * @param {number} [duration=4000]
 */
function showToast(message, type = 'info', duration = 4000) {
  let container = qs('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const icons = {
    success: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>`,
    error:   `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>`,
    warning: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/></svg>`,
    info:    `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>`,
  };

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.setAttribute('role', 'alert');
  toast.innerHTML = `${icons[type] || icons.info}<span class="toast-message">${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = '0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

/**
 * Escapes HTML special characters to prevent XSS.
 * @param {string} str
 * @returns {string}
 */
function escapeHtml(str) {
  const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
  return String(str).replace(/[&<>"']/g, m => map[m]);
}

/**
 * Sets an element's inner HTML to a loading spinner.
 * @param {Element} el
 * @param {string} [message='Loading...']
 */
function showLoading(el, message = 'Loading...') {
  el.innerHTML = `
    <div class="loading-state" role="status" aria-live="polite">
      <div class="spinner" aria-hidden="true"></div>
      <span>${escapeHtml(message)}</span>
    </div>`;
}

/**
 * Sets an element's inner HTML to an empty-state message.
 * @param {Element} el
 * @param {string} message
 */
function showEmpty(el, message) {
  el.innerHTML = `
    <div class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M9.75 9.75l4.5 4.5m0-4.5l-4.5 4.5M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
      </svg>
      <p>${escapeHtml(message)}</p>
    </div>`;
}

// ── Sensor helpers ───────────────────────────────────────────────────────────

/**
 * Returns the status of a sensor reading relative to its thresholds.
 * @param {number} value
 * @param {number} min
 * @param {number} max
 * @returns {'ok'|'warning'|'critical'}
 */
function getReadingStatus(value, min, max) {
  const range = max - min;
  if (value < min || value > max) {
    const deviation = value < min ? min - value : value - max;
    const pct = range > 0 ? deviation / range : 1;
    return pct > 0.20 ? 'critical' : 'warning';
  }
  return 'ok';
}

/**
 * Returns the CSS class for a severity level.
 * @param {string} severity  info | warning | critical
 * @returns {string}
 */
function severityClass(severity) {
  const map = { critical: 'badge-critical', warning: 'badge-warning', info: 'badge-info' };
  return map[severity] || 'badge-muted';
}

/**
 * Returns the CSS class for a reading status.
 * @param {'ok'|'warning'|'critical'} status
 * @returns {string}
 */
function statusClass(status) {
  const map = { ok: 'success', warning: 'warning', critical: 'critical' };
  return map[status] || 'muted';
}

// ── Navbar active link ───────────────────────────────────────────────────────

/**
 * Marks the nav link matching the current page as active.
 */
function setActiveNavLink() {
  const current = window.location.pathname.split('/').pop() || 'index.html';
  qsa('.nav-link').forEach(link => {
    const href = link.getAttribute('href') || '';
    if (href === current || (current === '' && href === 'index.html')) {
      link.classList.add('active');
      link.setAttribute('aria-current', 'page');
    }
  });
}

/**
 * Wires up the mobile hamburger toggle.
 */
function initNavToggle() {
  const toggle = qs('.navbar-toggle');
  const nav    = qs('.navbar-nav');
  if (!toggle || !nav) return;

  toggle.addEventListener('click', () => {
    const open = nav.classList.toggle('open');
    toggle.setAttribute('aria-expanded', open);
  });

  // Close on outside click
  document.addEventListener('click', e => {
    if (!toggle.contains(e.target) && !nav.contains(e.target)) {
      nav.classList.remove('open');
      toggle.setAttribute('aria-expanded', 'false');
    }
  });
}

// ── Init ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  setActiveNavLink();
  initNavToggle();
});

// ── Exports ──────────────────────────────────────────────────────────────────
window.utils = {
  timeAgo, formatDate, isoHoursAgo,
  qs, qsa,
  showToast, escapeHtml, showLoading, showEmpty,
  getReadingStatus, severityClass, statusClass,
  setActiveNavLink, initNavToggle,
};
