/**
 * Municipal Smart Public Beach Safety & Water Quality Monitoring System
 * Main Application Controller (ES Module)
 */

import { Store } from './store.js';
import { API, ApiError } from './api.js';
import {
  escapeHTML,
  showToast,
  showConfirmModal,
  openModal,
  closeModal,
  renderLoadingState,
  renderEmptyState,
  renderBadge,
  formatCurrency,
  formatDateTime,
  renderWaterQualityChart,
  renderFinancialChart
} from './components.js';

// Global Unauthorized Interceptor
window.handleUnauthorized = () => {
  Store.clearAuth();
  showAuthPage();
  showToast('Session expired or login required. Please sign in.', 'warning');
};

document.addEventListener('DOMContentLoaded', () => {
  initApp();
});

function initApp() {
  // Theme Setup
  Store.initTheme();
  updateThemeToggleUI();

  // Attach Top Header & Theme Event Listeners
  document.getElementById('btn-theme-toggle').addEventListener('click', toggleTheme);
  document.getElementById('btn-signout').addEventListener('click', handleSignOut);
  document.getElementById('mobile-nav-toggle').addEventListener('click', toggleMobileNav);

  // Attach Auth Toggle Listeners
  document.getElementById('auth-toggle-link').addEventListener('click', (e) => {
    e.preventDefault();
    toggleAuthForms();
  });

  document.getElementById('login-form').addEventListener('submit', handleLogin);
  document.getElementById('register-form').addEventListener('submit', handleRegister);

  // Navigation Links
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', (e) => {
      const view = e.currentTarget.getAttribute('data-view');
      if (view) switchView(view);
      closeMobileNav();
    });
  });

  // Modal Close Buttons
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const modalId = e.currentTarget.getAttribute('data-close-modal');
      closeModal(modalId);
    });
  });

  // Action Buttons
  document.getElementById('btn-add-zone')?.addEventListener('click', () => openZoneModal());
  document.getElementById('btn-add-sample')?.addEventListener('click', () => openSampleModal());
  document.getElementById('btn-add-contact')?.addEventListener('click', () => openContactModal());
  document.getElementById('btn-add-ledger')?.addEventListener('click', () => openLedgerModal());
  document.getElementById('btn-add-user')?.addEventListener('click', () => openUserModal());

  // Form Submissions
  document.getElementById('zone-form')?.addEventListener('submit', handleZoneSubmit);
  document.getElementById('sample-form')?.addEventListener('submit', handleSampleSubmit);
  document.getElementById('contact-form')?.addEventListener('submit', handleContactSubmit);
  document.getElementById('ledger-form')?.addEventListener('submit', handleLedgerSubmit);
  document.getElementById('user-form')?.addEventListener('submit', handleUserSubmit);

  // Live Sample Threshold Preview Listener
  const ecoliInput = document.getElementById('sample-ecoli');
  const enterococciInput = document.getElementById('sample-enterococci');
  const updateSamplePreview = () => {
    const ecoli = parseInt(ecoliInput.value) || 0;
    const enterococci = parseInt(enterococciInput.value) || 0;
    const previewBox = document.getElementById('sample-safety-preview');
    const previewStatus = document.getElementById('sample-preview-status');
    const previewDetail = document.getElementById('sample-preview-detail');

    if (ecoliInput.value !== '' || enterococciInput.value !== '') {
      previewBox.style.display = 'block';
      const isUnsafe = ecoli > 235 || enterococci > 104;
      if (isUnsafe) {
        previewBox.style.borderLeftColor = 'var(--unsafe-red)';
        previewStatus.innerHTML = '🔴 Expected Preview: UNSAFE (Closure Warning)';
        previewStatus.style.color = 'var(--unsafe-red)';
        previewDetail.textContent = 'Exceeds threshold limits (E. coli > 235 or Enterococci > 104). Server classification is authoritative.';
      } else {
        previewBox.style.borderLeftColor = 'var(--safe-green)';
        previewStatus.innerHTML = '🟢 Expected Preview: SAFE';
        previewStatus.style.color = 'var(--safe-green)';
        previewDetail.textContent = 'Within standard municipal safety thresholds. Server classification is authoritative.';
      }
    } else {
      previewBox.style.display = 'none';
    }
  };
  ecoliInput?.addEventListener('input', updateSamplePreview);
  enterococciInput?.addEventListener('input', updateSamplePreview);

  // Dashboard Zone Filter
  document.getElementById('dashboard-zone-filter')?.addEventListener('change', (e) => {
    loadDashboardData(e.target.value);
  });

  // Contacts & Ledger Tab Row
  document.getElementById('tab-btn-contacts')?.addEventListener('click', () => switchContactsLedgerTab('contacts'));
  document.getElementById('tab-btn-ledger')?.addEventListener('click', () => switchContactsLedgerTab('ledger'));

  // Reports Tab Row
  document.querySelectorAll('#reports-view .tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      document.querySelectorAll('#reports-view .tab-btn').forEach(b => b.classList.remove('active'));
      e.currentTarget.classList.add('active');
      const targetId = e.currentTarget.id;
      updateReportsFilterVisibility(targetId);
    });
  });

  document.getElementById('btn-run-report')?.addEventListener('click', runSelectedReport);

  // Check Auth and Initialize
  if (Store.isLoggedIn()) {
    showMainApp();
  } else {
    showAuthPage();
  }
}

/* ==========================================================================
   AUTH & THEME UI MANAGEMENT
   ========================================================================== */

function showAuthPage() {
  document.getElementById('auth-page').style.display = 'flex';
  document.getElementById('app-container').style.display = 'none';
}

function showMainApp() {
  document.getElementById('auth-page').style.display = 'none';
  document.getElementById('app-container').style.display = 'flex';

  // Render User Info in Sidebar
  const username = Store.getUsername();
  const role = Store.getRole();
  document.getElementById('user-display-name').textContent = username || 'User';
  document.getElementById('user-avatar-text').textContent = (username.charAt(0) || 'U').toUpperCase();

  const roleBadge = document.getElementById('user-display-role');
  roleBadge.textContent = role;
  roleBadge.className = `user-role-badge role-${role}`;

  // Apply Role Visibility
  document.querySelectorAll('.nav-item-admin').forEach(el => {
    el.style.display = Store.canManageUsers() ? 'flex' : 'none';
  });

  document.querySelectorAll('.role-edit-only').forEach(el => {
    el.style.display = Store.canEdit() ? 'inline-flex' : 'none';
  });

  // Pre-load Cache of Zones & Contacts for Form Dropdowns
  preloadReferenceData();

  // Load Active View
  switchView('dashboard');
}

async function preloadReferenceData() {
  try {
    const zones = await API.getZones();
    Store.setZonesCache(zones);
    populateZoneDropdowns(zones);
  } catch (e) {
    console.warn('Failed to pre-cache zones', e);
  }

  try {
    const contacts = await API.getContacts();
    Store.setContactsCache(contacts);
    populateContactDropdowns(contacts);
  } catch (e) {
    console.warn('Failed to pre-cache contacts', e);
  }
}

function populateZoneDropdowns(zones) {
  const selects = ['sample-zone-filter', 'dashboard-zone-filter', 'report-zone-select', 'sample-zone-id', 'ledger-zone-id'];
  selects.forEach(id => {
    const select = document.getElementById(id);
    if (!select) return;

    const currentVal = select.value;
    let html = id.includes('filter') || id.includes('select') ? '<option value="">All Zones</option>' : '<option value="">Select Beach Zone...</option>';
    (zones || []).forEach(z => {
      html += `<option value="${z.id}">${escapeHTML(z.name)} (${escapeHTML(z.analyticAccountCode)})</option>`;
    });
    select.innerHTML = html;
    select.value = currentVal;
  });
}

function populateContactDropdowns(contacts) {
  const selects = ['report-contact-select', 'ledger-contact-id'];
  selects.forEach(id => {
    const select = document.getElementById(id);
    if (!select) return;

    const currentVal = select.value;
    let html = id.includes('select') ? '<option value="">All Contacts</option>' : '<option value="">Select Contact...</option>';
    (contacts || []).forEach(c => {
      html += `<option value="${c.id}">${escapeHTML(c.name)} (${escapeHTML(c.type)})</option>`;
    });
    select.innerHTML = html;
    select.value = currentVal;
  });
}

function toggleAuthForms() {
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const prompt = document.getElementById('auth-toggle-prompt');
  const link = document.getElementById('auth-toggle-link');

  if (loginForm.style.display !== 'none') {
    loginForm.style.display = 'none';
    registerForm.style.display = 'block';
    prompt.textContent = 'Already have an account?';
    link.textContent = 'Sign In';
  } else {
    loginForm.style.display = 'block';
    registerForm.style.display = 'none';
    prompt.textContent = "Don't have an account?";
    link.textContent = 'Register here';
  }
}

async function handleLogin(e) {
  e.preventDefault();
  const submitBtn = document.getElementById('login-submit-btn');
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;

  submitBtn.disabled = true;
  submitBtn.textContent = 'Signing in...';

  try {
    const data = await API.login(username, password);
    Store.setAuth(data.token, data.username, data.role);
    showToast(`Welcome back, ${data.username}!`, 'success');
    showMainApp();
  } catch (err) {
    showToast(err.message || 'Login failed', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Sign In';
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const submitBtn = document.getElementById('register-submit-btn');
  const username = document.getElementById('register-username').value.trim();
  const password = document.getElementById('register-password').value;

  submitBtn.disabled = true;
  submitBtn.textContent = 'Creating account...';

  try {
    const data = await API.register(username, password);
    Store.setAuth(data.token, data.username, data.role);
    showToast('Registration successful! VIEWER account created.', 'success');
    showMainApp();
  } catch (err) {
    showToast(err.message || 'Registration failed', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Create Viewer Account';
  }
}

function handleSignOut() {
  Store.clearAuth();
  showToast('Signed out successfully.', 'info');
  showAuthPage();
}

function toggleTheme() {
  const current = Store.getTheme();
  const next = current === 'dark' ? 'light' : 'dark';
  Store.setTheme(next);
  updateThemeToggleUI();
}

function updateThemeToggleUI() {
  const current = Store.getTheme();
  const icon = document.getElementById('theme-toggle-icon');
  const text = document.getElementById('theme-toggle-text');
  if (current === 'dark') {
    if (icon) icon.textContent = '☀️';
    if (text) text.textContent = 'Light Mode';
  } else {
    if (icon) icon.textContent = '🌙';
    if (text) text.textContent = 'Dark Mode';
  }
}

function toggleMobileNav() {
  document.getElementById('sidebar').classList.toggle('mobile-open');
}

function closeMobileNav() {
  document.getElementById('sidebar').classList.remove('mobile-open');
}

/* ==========================================================================
   VIEW ROUTER & PAGE LOADERS
   ========================================================================== */

function switchView(viewName) {
  // Update Nav Active Item
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.getAttribute('data-view') === viewName);
  });

  // Update Page Title
  const titles = {
    dashboard: 'Dashboard Overview',
    zones: 'Beach Zones',
    samples: 'Water Quality Samples',
    'contacts-ledger': 'Contacts & Financial Ledger',
    reports: 'Analytics & Reports',
    users: 'User Management'
  };
  document.getElementById('page-title').textContent = titles[viewName] || 'Overview';

  // Toggle View Sections
  document.querySelectorAll('.view-section').forEach(sec => {
    sec.classList.remove('active');
  });

  const targetSec = document.getElementById(`${viewName}-view`);
  if (targetSec) {
    targetSec.classList.add('active');
  }

  Store.activeView = viewName;

  // Trigger Loader
  if (viewName === 'dashboard') loadDashboardData();
  else if (viewName === 'zones') loadZonesData();
  else if (viewName === 'samples') loadSamplesData();
  else if (viewName === 'contacts-ledger') loadContactsLedgerData();
  else if (viewName === 'reports') loadReportsView();
  else if (viewName === 'users') loadUsersData();
}

/* ==========================================================================
   1. DASHBOARD VIEW
   ========================================================================== */

async function loadDashboardData(zoneId = '') {
  const metricsContainer = document.getElementById('dashboard-metrics-container');
  const zonesContainer = document.getElementById('dashboard-zones-container');

  metricsContainer.innerHTML = renderLoadingState('Loading dashboard metrics...');
  zonesContainer.innerHTML = renderLoadingState('Loading beach status...');

  try {
    let data;
    if (zoneId) {
      data = await API.getZoneDashboard(zoneId);
      renderZoneSpecificDashboard(data, metricsContainer, zonesContainer);
    } else {
      data = await API.getOverallDashboard();
      renderOverallDashboard(data, metricsContainer, zonesContainer);
    }
  } catch (err) {
    metricsContainer.innerHTML = renderEmptyState('Failed to load dashboard', err.message, '⚠️');
    zonesContainer.innerHTML = renderEmptyState('Dashboard unavailable', 'Unable to retrieve status overview.', '⚠️');
    showToast(err.message, 'error');
  }
}

function renderOverallDashboard(data, metricsContainer, zonesContainer) {
  const totalZones = data?.totalZones || 0;
  const greenZones = data?.greenZones || 0;
  const redZones = data?.redZones || 0;
  const totalSamples = data?.totalWaterSamples || 0;
  const safeSamples = data?.safeSamples || 0;
  const unsafeSamples = data?.unsafeSamples || 0;
  const fin = data?.financialSummary || { totalIncome: 0, totalExpense: 0, netBalance: 0 };

  metricsContainer.innerHTML = `
    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Total Beach Zones</span>
        <div class="stat-icon teal">🏖️</div>
      </div>
      <div class="stat-value">${totalZones}</div>
      <div class="stat-subtext">🟢 ${greenZones} Safe | 🔴 ${redZones} Unsafe</div>
    </div>

    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Water Quality Safe</span>
        <div class="stat-icon green">🟢</div>
      </div>
      <div class="stat-value">${safeSamples}</div>
      <div class="stat-subtext">Out of ${totalSamples} Total Samples</div>
    </div>

    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Unsafe Sample Alerts</span>
        <div class="stat-icon red">🔴</div>
      </div>
      <div class="stat-value">${unsafeSamples}</div>
      <div class="stat-subtext">Exceeded bacterial safety limits</div>
    </div>

    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Net Financial Balance</span>
        <div class="stat-icon blue">💵</div>
      </div>
      <div class="stat-value" style="color: ${fin.netBalance >= 0 ? 'var(--safe-green)' : 'var(--unsafe-red)'};">
        ${formatCurrency(fin.netBalance)}
      </div>
      <div class="stat-subtext">Income: ${formatCurrency(fin.totalIncome)} | Expense: ${formatCurrency(fin.totalExpense)}</div>
    </div>
  `;

  // Render Charts
  renderWaterQualityChart('water-quality-chart-container', safeSamples, unsafeSamples);
  renderFinancialChart('financial-chart-container', fin.totalIncome, fin.totalExpense, fin.netBalance);

  // Render Zones Summary Callout / List
  const latestZone = data?.latestZone;
  const latestSample = data?.latestSample;

  let calloutHTML = '';
  if (latestZone) {
    calloutHTML = `
      <div class="glass-card" style="margin-bottom: 1.5rem; border-left: 4px solid var(--accent-cyan); background: var(--bg-card-hover);">
        <div style="font-size: 0.82rem; font-weight: 700; text-transform: uppercase; color: var(--accent-cyan);">Latest Registered Zone</div>
        <div style="font-size: 1.1rem; font-weight: 700; margin-top: 0.2rem;">${escapeHTML(latestZone.name)} (${escapeHTML(latestZone.analyticAccountCode)})</div>
        <div style="margin-top: 0.4rem;">${renderBadge(latestZone.currentFlagStatus)}</div>
      </div>
    `;
  }

  if (latestSample) {
    calloutHTML += `
      <div class="glass-card" style="margin-bottom: 1.5rem; border-left: 4px solid ${latestSample.isSafe ? 'var(--safe-green)' : 'var(--unsafe-red)'}; background: var(--bg-card-hover);">
        <div style="font-size: 0.82rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted);">Latest Sample Tested</div>
        <div style="font-size: 1rem; font-weight: 700; margin-top: 0.2rem;">
          E. coli: ${latestSample.ecoliCount} CFU/100mL | Enterococci: ${latestSample.enterococciCount} CFU/100mL
        </div>
        <div style="font-size: 0.8rem; color: var(--text-light); margin-top: 0.25rem;">
          Sampled at: ${formatDateTime(latestSample.sampledAt)} | Status: ${renderBadge(latestSample.isSafe)}
        </div>
      </div>
    `;
  }

  // Load all zones for grid display
  API.getZones().then(zones => {
    if (!zones || zones.length === 0) {
      zonesContainer.innerHTML = calloutHTML + renderEmptyState('No Beach Zones Registered', 'Click "Beach Zones" in sidebar to register a new beach location.', '🏖️');
      return;
    }

    let gridHTML = calloutHTML + '<div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1.25rem;">';
    zones.forEach(z => {
      gridHTML += `
        <div class="glass-card glass-card-hover" style="border-top: 4px solid ${z.currentFlagStatus === 'GREEN' ? 'var(--safe-green)' : 'var(--unsafe-red)'};">
          <div style="display: flex; justify-content: space-between; align-items: flex-start;">
            <h4 style="font-size: 1.05rem; font-weight: 700;">${escapeHTML(z.name)}</h4>
            ${renderBadge(z.currentFlagStatus)}
          </div>
          <div style="font-size: 0.82rem; color: var(--text-muted); margin-top: 0.4rem;">Code: ${escapeHTML(z.analyticAccountCode)}</div>
          <button class="btn btn-secondary btn-sm" style="margin-top: 1rem; width: 100%; justify-content: center;" onclick="window.viewZoneDetails(${z.id})">
            View Zone Details
          </button>
        </div>
      `;
    });
    gridHTML += '</div>';
    zonesContainer.innerHTML = gridHTML;
  }).catch(() => {
    zonesContainer.innerHTML = calloutHTML;
  });
}

function renderZoneSpecificDashboard(data, metricsContainer, zonesContainer) {
  const zone = data?.zone;
  const latestSample = data?.latestSample;
  const history = data?.history || [];
  const fin = data?.financialSummary || { totalIncome: 0, totalExpense: 0, netBalance: 0 };

  if (!zone) {
    metricsContainer.innerHTML = renderEmptyState('Zone Not Found', 'The requested zone details are missing.', '❓');
    zonesContainer.innerHTML = '';
    return;
  }

  metricsContainer.innerHTML = `
    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Selected Zone</span>
        <div class="stat-icon teal">🏖️</div>
      </div>
      <div class="stat-value" style="font-size: 1.4rem;">${escapeHTML(zone.name)}</div>
      <div class="stat-subtext">Code: ${escapeHTML(zone.analyticAccountCode)}</div>
    </div>

    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Current Flag</span>
        <div class="stat-icon">${zone.currentFlagStatus === 'GREEN' ? '🟢' : '🔴'}</div>
      </div>
      <div class="stat-value" style="font-size: 1.4rem;">${renderBadge(zone.currentFlagStatus)}</div>
      <div class="stat-subtext">${zone.currentFlagStatus === 'GREEN' ? 'Safe for swimming' : 'Unsafe / Swimming Advisory'}</div>
    </div>

    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Zone Samples Logged</span>
        <div class="stat-icon green">🧪</div>
      </div>
      <div class="stat-value">${history.length}</div>
      <div class="stat-subtext">Recorded sample tests</div>
    </div>

    <div class="glass-card stat-card">
      <div class="stat-header">
        <span class="stat-label">Zone Net Balance</span>
        <div class="stat-icon blue">💵</div>
      </div>
      <div class="stat-value" style="color: ${fin.netBalance >= 0 ? 'var(--safe-green)' : 'var(--unsafe-red)'}; font-size: 1.4rem;">
        ${formatCurrency(fin.netBalance)}
      </div>
      <div class="stat-subtext">Inc: ${formatCurrency(fin.totalIncome)} | Exp: ${formatCurrency(fin.totalExpense)}</div>
    </div>
  `;

  renderFinancialChart('financial-chart-container', fin.totalIncome, fin.totalExpense, fin.netBalance);

  let html = `
    <div class="glass-card" style="margin-bottom: 1.5rem;">
      <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.75rem;">Latest Sample for ${escapeHTML(zone.name)}</h3>
  `;

  if (latestSample) {
    html += `
      <div style="display: flex; gap: 1.5rem; flex-wrap: wrap; font-size: 0.9rem;">
        <div><strong>E. coli:</strong> ${latestSample.ecoliCount} CFU/100mL</div>
        <div><strong>Enterococci:</strong> ${latestSample.enterococciCount} CFU/100mL</div>
        <div><strong>Sampled At:</strong> ${formatDateTime(latestSample.sampledAt)}</div>
        <div><strong>Status:</strong> ${renderBadge(latestSample.isSafe)}</div>
      </div>
    `;
  } else {
    html += `<p style="color: var(--text-muted); font-size: 0.88rem;">No water samples logged for this zone yet.</p>`;
  }
  html += '</div>';

  html += `<h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 1rem;">Water Quality History</h3>`;
  if (history.length === 0) {
    html += renderEmptyState('No Sample History', 'No water sample tests recorded for this zone.', '🧪');
  } else {
    html += `
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Sampled At</th>
              <th>E. coli</th>
              <th>Enterococci</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
    `;
    history.forEach(s => {
      html += `
        <tr>
          <td>#${s.id}</td>
          <td>${formatDateTime(s.sampledAt)}</td>
          <td>${s.ecoliCount} CFU/100mL</td>
          <td>${s.enterococciCount} CFU/100mL</td>
          <td>${renderBadge(s.isSafe)}</td>
        </tr>
      `;
    });
    html += `</tbody></table></div>`;
  }

  zonesContainer.innerHTML = html;
}

/* ==========================================================================
   2. BEACH ZONES VIEW
   ========================================================================== */

async function loadZonesData() {
  const container = document.getElementById('zones-list-container');
  container.innerHTML = renderLoadingState('Loading beach zones...');

  try {
    const zones = await API.getZones();
    Store.setZonesCache(zones);
    populateZoneDropdowns(zones);

    if (!zones || zones.length === 0) {
      container.innerHTML = renderEmptyState(
        'No Beach Zones Found',
        Store.canEdit() ? 'Click "+ Create Beach Zone" button above to add a location.' : 'No beach zones have been registered in the system yet.',
        '🏖️'
      );
      return;
    }

    let html = `
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Zone Name</th>
              <th>Analytic Account Code</th>
              <th>Flag Status</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
    `;

    zones.forEach(z => {
      html += `
        <tr>
          <td><strong>#${z.id}</strong></td>
          <td><span style="font-weight: 600; color: var(--text-main);">${escapeHTML(z.name)}</span></td>
          <td><code>${escapeHTML(z.analyticAccountCode)}</code></td>
          <td>${renderBadge(z.currentFlagStatus)}</td>
          <td style="text-align: right; white-space: nowrap;">
            <button class="btn btn-secondary btn-sm" onclick="window.viewZoneDetails(${z.id})">Details</button>
            ${Store.canEdit() ? `<button class="btn btn-secondary btn-sm" onclick="window.editZone(${z.id})">Edit</button>` : ''}
            ${Store.canDelete() ? `<button class="btn btn-danger btn-sm" onclick="window.deleteZonePrompt(${z.id}, '${escapeHTML(z.name)}')">Delete</button>` : ''}
          </td>
        </tr>
      `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = renderEmptyState('Failed to load zones', err.message, '⚠️');
    showToast(err.message, 'error');
  }
}

window.viewZoneDetails = async (zoneId) => {
  const modalBody = document.getElementById('zone-detail-body');
  modalBody.innerHTML = renderLoadingState('Fetching zone details...');
  openModal('zone-detail-modal-backdrop');

  try {
    const data = await API.getZoneMonitoring(zoneId);
    const zone = data?.zone;
    const latest = data?.latestSample;
    const history = data?.history || [];

    document.getElementById('zone-detail-title').textContent = `${zone?.name || 'Zone'} - Details & Monitoring`;

    let html = `
      <div class="glass-card" style="margin-bottom: 1.25rem;">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <div>
            <div style="font-size:0.8rem; text-transform:uppercase; color:var(--text-muted);">Account Code</div>
            <div style="font-size:1.1rem; font-weight:700;">${escapeHTML(zone?.analyticAccountCode)}</div>
          </div>
          <div>
            <div style="font-size:0.8rem; text-transform:uppercase; color:var(--text-muted); margin-bottom:0.2rem;">Current Flag</div>
            ${renderBadge(zone?.currentFlagStatus)}
          </div>
        </div>
      </div>
    `;

    html += `<h4 style="font-size:1rem; font-weight:700; margin-bottom:0.5rem;">Latest Sample Test</h4>`;
    if (latest) {
      html += `
        <div style="background:var(--bg-card-hover); padding:0.85rem; border-radius:var(--radius-md); border:1px solid var(--border-subtle); margin-bottom:1.25rem; font-size:0.88rem;">
          <div><strong>Status:</strong> ${renderBadge(latest.isSafe)}</div>
          <div><strong>E. coli:</strong> ${latest.ecoliCount} CFU/100mL</div>
          <div><strong>Enterococci:</strong> ${latest.enterococciCount} CFU/100mL</div>
          <div><strong>Sampled:</strong> ${formatDateTime(latest.sampledAt)}</div>
        </div>
      `;
    } else {
      html += `<p style="font-size:0.85rem; color:var(--text-muted); margin-bottom:1.25rem;">No samples recorded for this zone yet.</p>`;
    }

    html += `<h4 style="font-size:1rem; font-weight:700; margin-bottom:0.5rem;">Sample History (${history.length})</h4>`;
    if (history.length === 0) {
      html += `<p style="font-size:0.85rem; color:var(--text-muted);">No history available.</p>`;
    } else {
      html += `
        <div class="table-responsive" style="max-height: 240px; overflow-y: auto;">
          <table class="data-table" style="font-size:0.82rem;">
            <thead>
              <tr>
                <th>Sampled At</th>
                <th>E. coli</th>
                <th>Enterococci</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
      `;
      history.forEach(h => {
        html += `
          <tr>
            <td>${formatDateTime(h.sampledAt)}</td>
            <td>${h.ecoliCount}</td>
            <td>${h.enterococciCount}</td>
            <td>${renderBadge(h.isSafe)}</td>
          </tr>
        `;
      });
      html += `</tbody></table></div>`;
    }

    modalBody.innerHTML = html;
  } catch (err) {
    modalBody.innerHTML = renderEmptyState('Error loading details', err.message, '⚠️');
  }
};

function openZoneModal(zone = null) {
  document.getElementById('zone-modal-title').textContent = zone ? 'Edit Beach Zone' : 'Create Beach Zone';
  document.getElementById('zone-id').value = zone ? zone.id : '';
  document.getElementById('zone-name').value = zone ? zone.name : '';
  document.getElementById('zone-code').value = zone ? zone.analyticAccountCode : '';
  document.getElementById('zone-flag').value = zone ? zone.currentFlagStatus : 'GREEN';
  openModal('zone-modal-backdrop');
}

window.editZone = async (id) => {
  try {
    const zone = await API.getZone(id);
    openZoneModal(zone);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.deleteZonePrompt = (id, name) => {
  showConfirmModal({
    title: 'Delete Beach Zone',
    message: `Are you sure you want to delete beach zone "${name}"? This action cannot be undone.`,
    confirmText: 'Delete Zone',
    onConfirm: async () => {
      await API.deleteZone(id);
      showToast('Beach zone deleted successfully', 'success');
      loadZonesData();
    }
  });
};

async function handleZoneSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('zone-id').value;
  const name = document.getElementById('zone-name').value.trim();
  const analyticAccountCode = document.getElementById('zone-code').value.trim();
  const currentFlagStatus = document.getElementById('zone-flag').value;
  const submitBtn = document.getElementById('zone-submit-btn');

  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    const payload = { name, analyticAccountCode, currentFlagStatus };
    if (id) {
      await API.updateZone(id, payload);
      showToast('Beach zone updated successfully', 'success');
    } else {
      await API.createZone(payload);
      showToast('Beach zone created successfully', 'success');
    }
    closeModal('zone-modal-backdrop');
    loadZonesData();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Save Zone';
  }
}

/* ==========================================================================
   3. WATER SAMPLES VIEW
   ========================================================================== */

let allSamplesList = [];

async function loadSamplesData() {
  const container = document.getElementById('samples-list-container');
  container.innerHTML = renderLoadingState('Loading water samples...');

  try {
    allSamplesList = await API.getSamples() || [];
    renderFilteredSamples();

    // Zone filter listener
    document.getElementById('sample-zone-filter').onchange = () => {
      renderFilteredSamples();
    };
  } catch (err) {
    container.innerHTML = renderEmptyState('Failed to load water samples', err.message, '⚠️');
    showToast(err.message, 'error');
  }
}

function renderFilteredSamples() {
  const container = document.getElementById('samples-list-container');
  const selectedZoneId = document.getElementById('sample-zone-filter').value;

  let samples = allSamplesList;
  if (selectedZoneId) {
    samples = samples.filter(s => String(s.zoneId) === String(selectedZoneId));
  }

  if (!samples || samples.length === 0) {
    container.innerHTML = renderEmptyState(
      'No Water Samples Found',
      selectedZoneId ? 'No water samples logged for the selected zone.' : (Store.canEdit() ? 'Click "+ Log Water Sample" above to record a test.' : 'No water samples recorded in the system.'),
      '🧪'
    );
    return;
  }

  const zonesCache = Store.getZonesCache();

  let html = `
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Zone</th>
            <th>Sampled Date & Time</th>
            <th>E. coli</th>
            <th>Enterococci</th>
            <th>Safety Status</th>
            <th style="text-align: right;">Actions</th>
          </tr>
        </thead>
        <tbody>
  `;

  samples.forEach(s => {
    const zoneObj = zonesCache.find(z => String(z.id) === String(s.zoneId));
    const zoneName = zoneObj ? `${zoneObj.name} (#${s.zoneId})` : `Zone #${s.zoneId}`;

    html += `
      <tr>
        <td><strong>#${s.id}</strong></td>
        <td>${escapeHTML(zoneName)}</td>
        <td>${formatDateTime(s.sampledAt)}</td>
        <td><strong style="color: ${s.ecoliCount > 235 ? 'var(--unsafe-red)' : 'inherit'};">${s.ecoliCount}</strong> CFU/100mL</td>
        <td><strong style="color: ${s.enterococciCount > 104 ? 'var(--unsafe-red)' : 'inherit'};">${s.enterococciCount}</strong> CFU/100mL</td>
        <td>${renderBadge(s.isSafe)}</td>
        <td style="text-align: right; white-space: nowrap;">
          ${Store.canEdit() ? `<button class="btn btn-secondary btn-sm" onclick="window.editSample(${s.id})">Edit</button>` : ''}
          ${Store.canDelete() ? `<button class="btn btn-danger btn-sm" onclick="window.deleteSamplePrompt(${s.id})">Delete</button>` : ''}
        </td>
      </tr>
    `;
  });

  html += `</tbody></table></div>`;
  container.innerHTML = html;
}

function openSampleModal(sample = null) {
  const zones = Store.getZonesCache();
  if (!zones || zones.length === 0) {
    showToast('Please create at least one Beach Zone before logging a sample.', 'warning');
    return;
  }

  document.getElementById('sample-modal-title').textContent = sample ? 'Edit Water Sample' : 'Log Water Sample';
  document.getElementById('sample-id').value = sample ? sample.id : '';
  document.getElementById('sample-zone-id').value = sample ? sample.zoneId : (zones[0]?.id || '');
  document.getElementById('sample-ecoli').value = sample ? sample.ecoliCount : '';
  document.getElementById('sample-enterococci').value = sample ? sample.enterococciCount : '';
  
  // Format current ISO date-time for datetime-local input
  if (sample && sample.sampledAt) {
    document.getElementById('sample-time').value = sample.sampledAt.substring(0, 16);
  } else {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('sample-time').value = now.toISOString().slice(0, 16);
  }

  document.getElementById('sample-safety-preview').style.display = 'none';
  openModal('sample-modal-backdrop');
}

window.editSample = async (id) => {
  try {
    const sample = await API.getSample(id);
    openSampleModal(sample);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.deleteSamplePrompt = (id) => {
  showConfirmModal({
    title: 'Delete Water Sample',
    message: `Are you sure you want to delete water sample #${id}? This action cannot be undone.`,
    confirmText: 'Delete Sample',
    onConfirm: async () => {
      await API.deleteSample(id);
      showToast('Water sample deleted successfully', 'success');
      loadSamplesData();
    }
  });
};

async function handleSampleSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('sample-id').value;
  const zoneId = parseInt(document.getElementById('sample-zone-id').value);
  const ecoliCount = parseInt(document.getElementById('sample-ecoli').value);
  const enterococciCount = parseInt(document.getElementById('sample-enterococci').value);
  const sampledAt = document.getElementById('sample-time').value;
  const submitBtn = document.getElementById('sample-submit-btn');

  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    const payload = { zoneId, ecoliCount, enterococciCount, sampledAt };
    if (id) {
      // Server preserves existing isSafe or update fields
      await API.updateSample(id, payload);
      showToast('Water sample updated successfully', 'success');
    } else {
      // Server calculates isSafe threshold and updates zone flag
      await API.createSample(payload);
      showToast('Water sample logged! Zone flag updated.', 'success');
    }
    closeModal('sample-modal-backdrop');
    loadSamplesData();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Save Sample';
  }
}

/* ==========================================================================
   4. CONTACTS & LEDGER VIEW
   ========================================================================== */

function switchContactsLedgerTab(tabName) {
  document.getElementById('tab-btn-contacts').classList.toggle('active', tabName === 'contacts');
  document.getElementById('tab-btn-ledger').classList.toggle('active', tabName === 'ledger');

  document.getElementById('subview-contacts').style.display = tabName === 'contacts' ? 'block' : 'none';
  document.getElementById('subview-ledger').style.display = tabName === 'ledger' ? 'block' : 'none';

  if (tabName === 'contacts') loadContactsSubView();
  else loadLedgerSubView();
}

async function loadContactsLedgerData() {
  switchContactsLedgerTab('contacts');
}

async function loadContactsSubView() {
  const container = document.getElementById('contacts-list-container');
  container.innerHTML = renderLoadingState('Loading contacts directory...');

  try {
    const contacts = await API.getContacts();
    Store.setContactsCache(contacts);
    populateContactDropdowns(contacts);

    if (!contacts || contacts.length === 0) {
      container.innerHTML = renderEmptyState(
        'No Contacts Found',
        Store.canEdit() ? 'Click "+ Add Contact" button to create a vendor, customer, or internal contact.' : 'No contacts recorded in the system.',
        '📇'
      );
      return;
    }

    let html = `
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Contact Name</th>
              <th>Type</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
    `;

    contacts.forEach(c => {
      const typeBadge = c.type === 'VENDOR' ? 'badge-amber' : c.type === 'CUSTOMER' ? 'badge-green' : 'badge-red';
      html += `
        <tr>
          <td><strong>#${c.id}</strong></td>
          <td><span style="font-weight: 600;">${escapeHTML(c.name)}</span></td>
          <td><span class="badge ${typeBadge}">${escapeHTML(c.type)}</span></td>
          <td style="text-align: right; white-space: nowrap;">
            ${Store.canEdit() ? `<button class="btn btn-secondary btn-sm" onclick="window.editContact(${c.id})">Edit</button>` : ''}
            ${Store.canDelete() ? `<button class="btn btn-danger btn-sm" onclick="window.deleteContactPrompt(${c.id}, '${escapeHTML(c.name)}')">Delete</button>` : ''}
          </td>
        </tr>
      `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = renderEmptyState('Failed to load contacts', err.message, '⚠️');
    showToast(err.message, 'error');
  }
}

function openContactModal(contact = null) {
  document.getElementById('contact-modal-title').textContent = contact ? 'Edit Contact' : 'Add Contact';
  document.getElementById('contact-id').value = contact ? contact.id : '';
  document.getElementById('contact-name').value = contact ? contact.name : '';
  document.getElementById('contact-type').value = contact ? contact.type : 'VENDOR';
  openModal('contact-modal-backdrop');
}

window.editContact = async (id) => {
  try {
    const contact = await API.getContact(id);
    openContactModal(contact);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.deleteContactPrompt = (id, name) => {
  showConfirmModal({
    title: 'Delete Contact',
    message: `Are you sure you want to delete contact "${name}"?`,
    confirmText: 'Delete Contact',
    onConfirm: async () => {
      await API.deleteContact(id);
      showToast('Contact deleted successfully', 'success');
      loadContactsSubView();
    }
  });
};

async function handleContactSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('contact-id').value;
  const name = document.getElementById('contact-name').value.trim();
  const type = document.getElementById('contact-type').value;
  const submitBtn = document.getElementById('contact-submit-btn');

  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    const payload = { name, type };
    if (id) {
      await API.updateContact(id, payload);
      showToast('Contact updated successfully', 'success');
    } else {
      await API.createContact(payload);
      showToast('Contact created successfully', 'success');
    }
    closeModal('contact-modal-backdrop');
    loadContactsSubView();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Save Contact';
  }
}

async function loadLedgerSubView() {
  const banner = document.getElementById('ledger-summary-banner');
  const container = document.getElementById('ledger-list-container');

  banner.innerHTML = renderLoadingState('Calculating summary...');
  container.innerHTML = renderLoadingState('Loading financial ledger entries...');

  try {
    const [entries, summary] = await Promise.all([
      API.getLedgerEntries(),
      API.getLedgerSummary()
    ]);

    // Banner Summary Card
    const inc = summary?.totalIncome || 0;
    const exp = summary?.totalExpense || 0;
    const net = summary?.netBalance || 0;

    banner.innerHTML = `
      <div class="glass-card stat-card">
        <span class="stat-label">Total Income (Permits)</span>
        <div class="stat-value" style="color: var(--safe-green);">${formatCurrency(inc)}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Total Expense (Testing)</span>
        <div class="stat-value" style="color: var(--unsafe-red);">${formatCurrency(exp)}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Net Balance</span>
        <div class="stat-value" style="color: ${net >= 0 ? 'var(--accent-teal)' : 'var(--unsafe-red)'};">${formatCurrency(net)}</div>
      </div>
    `;

    if (!entries || entries.length === 0) {
      container.innerHTML = renderEmptyState(
        'No Ledger Entries',
        Store.canEdit() ? 'Click "+ Log Ledger Entry" button to record a transaction.' : 'No ledger entries recorded.',
        '💵'
      );
      return;
    }

    const zones = Store.getZonesCache();
    const contacts = Store.getContactsCache();

    let html = `
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Zone</th>
              <th>Contact</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Description</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
    `;

    entries.forEach(l => {
      const zObj = zones.find(z => String(z.id) === String(l.zoneId));
      const cObj = contacts.find(c => String(c.id) === String(l.contactId));
      const isIncome = l.type === 'INCOME_PERMIT';

      html += `
        <tr>
          <td><strong>#${l.id}</strong></td>
          <td>${escapeHTML(zObj ? zObj.name : `Zone #${l.zoneId}`)}</td>
          <td>${escapeHTML(cObj ? cObj.name : `Contact #${l.contactId}`)}</td>
          <td><span class="badge ${isIncome ? 'badge-green' : 'badge-red'}">${escapeHTML(l.type)}</span></td>
          <td><strong style="color: ${isIncome ? 'var(--safe-green)' : 'var(--unsafe-red)'};">${formatCurrency(l.amount)}</strong></td>
          <td>${escapeHTML(l.description || '-')}</td>
          <td style="text-align: right; white-space: nowrap;">
            ${Store.canEdit() ? `<button class="btn btn-secondary btn-sm" onclick="window.editLedger(${l.id})">Edit</button>` : ''}
            ${Store.canDelete() ? `<button class="btn btn-danger btn-sm" onclick="window.deleteLedgerPrompt(${l.id})">Delete</button>` : ''}
          </td>
        </tr>
      `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
  } catch (err) {
    banner.innerHTML = '';
    container.innerHTML = renderEmptyState('Failed to load ledger', err.message, '⚠️');
    showToast(err.message, 'error');
  }
}

function openLedgerModal(ledger = null) {
  const zones = Store.getZonesCache();
  const contacts = Store.getContactsCache();

  if (!zones || zones.length === 0 || !contacts || contacts.length === 0) {
    showToast('Please ensure at least one Zone and one Contact exist before logging ledger entries.', 'warning');
    return;
  }

  document.getElementById('ledger-modal-title').textContent = ledger ? 'Edit Ledger Entry' : 'Log Ledger Entry';
  document.getElementById('ledger-id').value = ledger ? ledger.id : '';
  document.getElementById('ledger-zone-id').value = ledger ? ledger.zoneId : (zones[0]?.id || '');
  document.getElementById('ledger-contact-id').value = ledger ? ledger.contactId : (contacts[0]?.id || '');
  document.getElementById('ledger-amount').value = ledger ? ledger.amount : '';
  document.getElementById('ledger-type').value = ledger ? ledger.type : 'INCOME_PERMIT';
  document.getElementById('ledger-description').value = ledger ? ledger.description || '' : '';

  openModal('ledger-modal-backdrop');
}

window.editLedger = async (id) => {
  try {
    const entries = await API.getLedgerEntries();
    const ledger = (entries || []).find(e => String(e.id) === String(id));
    if (ledger) openLedgerModal(ledger);
    else showToast('Ledger entry not found', 'error');
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.deleteLedgerPrompt = (id) => {
  showConfirmModal({
    title: 'Delete Ledger Entry',
    message: `Are you sure you want to delete ledger entry #${id}?`,
    confirmText: 'Delete Entry',
    onConfirm: async () => {
      await API.deleteLedgerEntry(id);
      showToast('Ledger entry deleted successfully', 'success');
      loadLedgerSubView();
    }
  });
};

async function handleLedgerSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('ledger-id').value;
  const zoneId = parseInt(document.getElementById('ledger-zone-id').value);
  const contactId = parseInt(document.getElementById('ledger-contact-id').value);
  const amount = parseFloat(document.getElementById('ledger-amount').value);
  const type = document.getElementById('ledger-type').value;
  const description = document.getElementById('ledger-description').value.trim();
  const submitBtn = document.getElementById('ledger-submit-btn');

  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    const payload = { zoneId, contactId, amount, type, description };
    if (id) {
      await API.updateLedgerEntry(id, payload);
      showToast('Ledger entry updated successfully', 'success');
    } else {
      await API.createLedgerEntry(payload);
      showToast('Ledger entry recorded successfully', 'success');
    }
    closeModal('ledger-modal-backdrop');
    loadLedgerSubView();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Save Entry';
  }
}

/* ==========================================================================
   5. REPORTS VIEW
   ========================================================================== */

function updateReportsFilterVisibility(activeTabId) {
  const safeGrp = document.querySelector('.filter-safe-group');
  const typeGrp = document.querySelector('.filter-type-group');
  const contactGrp = document.querySelector('.filter-contact-group');
  const dateGrps = document.querySelectorAll('.filter-date-group');

  if (activeTabId === 'tab-report-water-quality') {
    safeGrp.style.display = 'none';
    typeGrp.style.display = 'none';
    contactGrp.style.display = 'none';
    dateGrps.forEach(g => g.style.display = 'flex');
  } else if (activeTabId === 'tab-report-samples') {
    safeGrp.style.display = 'flex';
    typeGrp.style.display = 'none';
    contactGrp.style.display = 'none';
    dateGrps.forEach(g => g.style.display = 'flex');
  } else if (activeTabId === 'tab-report-financial') {
    safeGrp.style.display = 'none';
    typeGrp.style.display = 'flex';
    contactGrp.style.display = 'none';
    dateGrps.forEach(g => g.style.display = 'flex');
  } else if (activeTabId === 'tab-report-ledger') {
    safeGrp.style.display = 'none';
    typeGrp.style.display = 'flex';
    contactGrp.style.display = 'flex';
    dateGrps.forEach(g => g.style.display = 'none');
  }

  // Clear previous output
  document.getElementById('reports-output-container').innerHTML = '';
}

function loadReportsView() {
  const activeTab = document.querySelector('#reports-view .tab-btn.active')?.id || 'tab-report-water-quality';
  updateReportsFilterVisibility(activeTab);
  runSelectedReport();
}

async function runSelectedReport() {
  const activeTab = document.querySelector('#reports-view .tab-btn.active')?.id || 'tab-report-water-quality';
  const container = document.getElementById('reports-output-container');
  container.innerHTML = renderLoadingState('Generating report analytics...');

  const zoneId = document.getElementById('report-zone-select').value;
  const from = document.getElementById('report-from-date').value ? `${document.getElementById('report-from-date').value}:00` : '';
  const to = document.getElementById('report-to-date').value ? `${document.getElementById('report-to-date').value}:00` : '';
  const safe = document.getElementById('report-safe-select').value;
  const type = document.getElementById('report-type-select').value;
  const contactId = document.getElementById('report-contact-select').value;

  try {
    if (activeTab === 'tab-report-water-quality') {
      const stats = await API.getWaterQualityReport({ zoneId, from, to });
      renderWaterQualityReportOutput(stats, container);
    } else if (activeTab === 'tab-report-samples') {
      const samples = await API.getSampleReport({ zoneId, from, to, safe });
      renderSamplesReportOutput(samples, container);
    } else if (activeTab === 'tab-report-financial') {
      const report = await API.getFinancialReport({ zoneId, from, to, type });
      renderFinancialReportOutput(report, container);
    } else if (activeTab === 'tab-report-ledger') {
      const entries = await API.getLedgerReport({ zoneId, contactId, type });
      renderLedgerReportOutput(entries, container);
    }
  } catch (err) {
    container.innerHTML = renderEmptyState('Report Generation Error', err.message, '⚠️');
    showToast(err.message, 'error');
  }
}

function renderWaterQualityReportOutput(stats, container) {
  const total = stats?.totalSamples || 0;
  const safe = stats?.safeSamples || 0;
  const unsafe = stats?.unsafeSamples || 0;
  const avgEcoli = stats?.averageEcoli || 0;
  const avgEntero = stats?.averageEnterococci || 0;

  let html = `
    <div class="metrics-grid" style="margin-bottom: 1.5rem;">
      <div class="glass-card stat-card">
        <span class="stat-label">Total Samples Tested</span>
        <div class="stat-value">${total}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Safe Samples</span>
        <div class="stat-value" style="color: var(--safe-green);">${safe}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Unsafe Samples</span>
        <div class="stat-value" style="color: var(--unsafe-red);">${unsafe}</div>
      </div>
    </div>

    <div class="metrics-grid">
      <div class="glass-card stat-card">
        <span class="stat-label">Average E. coli Concentration</span>
        <div class="stat-value" style="color: ${avgEcoli > 235 ? 'var(--unsafe-red)' : 'var(--accent-teal)'};">${avgEcoli} <span style="font-size: 0.9rem;">CFU/100mL</span></div>
        <div class="stat-subtext">Safety Threshold Limit: 235 CFU/100mL</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Average Enterococci Concentration</span>
        <div class="stat-value" style="color: ${avgEntero > 104 ? 'var(--unsafe-red)' : 'var(--accent-teal)'};">${avgEntero} <span style="font-size: 0.9rem;">CFU/100mL</span></div>
        <div class="stat-subtext">Safety Threshold Limit: 104 CFU/100mL</div>
      </div>
    </div>
  `;

  container.innerHTML = html;
}

function renderSamplesReportOutput(samples, container) {
  if (!samples || samples.length === 0) {
    container.innerHTML = renderEmptyState('No Matching Samples', 'No sample records match your filter criteria.', '🧪');
    return;
  }

  const zones = Store.getZonesCache();

  let html = `
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Zone</th>
            <th>Sampled Date & Time</th>
            <th>E. coli</th>
            <th>Enterococci</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
  `;

  samples.forEach(s => {
    const zObj = zones.find(z => String(z.id) === String(s.zoneId));
    html += `
      <tr>
        <td>#${s.id}</td>
        <td>${escapeHTML(zObj ? zObj.name : `Zone #${s.zoneId}`)}</td>
        <td>${formatDateTime(s.sampledAt)}</td>
        <td>${s.ecoliCount} CFU/100mL</td>
        <td>${s.enterococciCount} CFU/100mL</td>
        <td>${renderBadge(s.isSafe)}</td>
      </tr>
    `;
  });

  html += `</tbody></table></div>`;
  container.innerHTML = html;
}

function renderFinancialReportOutput(report, container) {
  const inc = report?.totalIncome || 0;
  const exp = report?.totalExpense || 0;
  const net = report?.netBalance || 0;
  const count = report?.entryCount || 0;

  let html = `
    <div class="glass-card" style="margin-bottom: 1.5rem; background: var(--bg-card-hover);">
      <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem;">
        ℹ️ Note: Financial totals represent matching ledger entries. Date filters validate request parameters.
      </div>
    </div>

    <div class="metrics-grid">
      <div class="glass-card stat-card">
        <span class="stat-label">Matching Transactions</span>
        <div class="stat-value">${count}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Permit Income Total</span>
        <div class="stat-value" style="color: var(--safe-green);">${formatCurrency(inc)}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Lab Expense Total</span>
        <div class="stat-value" style="color: var(--unsafe-red);">${formatCurrency(exp)}</div>
      </div>
      <div class="glass-card stat-card">
        <span class="stat-label">Calculated Net Balance</span>
        <div class="stat-value" style="color: ${net >= 0 ? 'var(--accent-teal)' : 'var(--unsafe-red)'};">${formatCurrency(net)}</div>
      </div>
    </div>
  `;

  container.innerHTML = html;
}

function renderLedgerReportOutput(entries, container) {
  if (!entries || entries.length === 0) {
    container.innerHTML = renderEmptyState('No Ledger Entries Found', 'No ledger records match the selected parameters.', '💵');
    return;
  }

  const zones = Store.getZonesCache();
  const contacts = Store.getContactsCache();

  let html = `
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Zone</th>
            <th>Contact</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Description</th>
          </tr>
        </thead>
        <tbody>
  `;

  entries.forEach(l => {
    const zObj = zones.find(z => String(z.id) === String(l.zoneId));
    const cObj = contacts.find(c => String(c.id) === String(l.contactId));
    const isInc = l.type === 'INCOME_PERMIT';

    html += `
      <tr>
        <td>#${l.id}</td>
        <td>${escapeHTML(zObj ? zObj.name : `Zone #${l.zoneId}`)}</td>
        <td>${escapeHTML(cObj ? cObj.name : `Contact #${l.contactId}`)}</td>
        <td><span class="badge ${isInc ? 'badge-green' : 'badge-red'}">${escapeHTML(l.type)}</span></td>
        <td><strong style="color: ${isInc ? 'var(--safe-green)' : 'var(--unsafe-red)'};">${formatCurrency(l.amount)}</strong></td>
        <td>${escapeHTML(l.description || '-')}</td>
      </tr>
    `;
  });

  html += `</tbody></table></div>`;
  container.innerHTML = html;
}

/* ==========================================================================
   6. USER MANAGEMENT VIEW (ADMIN)
   ========================================================================== */

async function loadUsersData() {
  if (!Store.canManageUsers()) {
    showToast("Forbidden: ADMIN privilege required for user management.", 'error');
    switchView('dashboard');
    return;
  }

  const container = document.getElementById('users-list-container');
  container.innerHTML = renderLoadingState('Loading user accounts...');

  try {
    const users = await API.getUsers();

    if (!users || users.length === 0) {
      container.innerHTML = renderEmptyState('No Users Found', 'No user accounts found in system.', '👥');
      return;
    }

    const currentUsername = Store.getUsername();

    let html = `
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Role</th>
              <th>Enabled Status</th>
              <th>Created At</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
    `;

    users.forEach(u => {
      const isSelf = u.username === currentUsername;
      html += `
        <tr>
          <td><strong>#${u.id}</strong></td>
          <td>
            <span style="font-weight: 600; color: var(--text-main);">${escapeHTML(u.username)}</span>
            ${isSelf ? ' <span style="font-size:0.75rem; color:var(--accent-cyan); font-weight:700;">(You)</span>' : ''}
          </td>
          <td><span class="user-role-badge role-${u.role}">${escapeHTML(u.role)}</span></td>
          <td>
            <span class="badge ${u.enabled ? 'badge-green' : 'badge-red'}">
              ${u.enabled ? 'ENABLED' : 'DISABLED'}
            </span>
          </td>
          <td>${formatDateTime(u.createdAt)}</td>
          <td style="text-align: right; white-space: nowrap;">
            <button class="btn btn-secondary btn-sm" onclick="window.editUser(${u.id})">Edit</button>
            ${!isSelf ? `<button class="btn btn-danger btn-sm" onclick="window.deleteUserPrompt(${u.id}, '${escapeHTML(u.username)}')">Delete</button>` : '<span style="font-size:0.75rem; color:var(--text-light); margin-left:0.5rem;">Self</span>'}
          </td>
        </tr>
      `;
    });

    html += `</tbody></table></div>`;
    container.innerHTML = html;
  } catch (err) {
    container.innerHTML = renderEmptyState('Failed to load users', err.message, '⚠️');
    showToast(err.message, 'error');
  }
}

function openUserModal(user = null) {
  document.getElementById('user-modal-title').textContent = user ? 'Edit User Account' : 'Create User Account';
  document.getElementById('user-id').value = user ? user.id : '';
  document.getElementById('user-username').value = user ? user.username : '';
  document.getElementById('user-username').disabled = !!user; // Username disabled on edit
  document.getElementById('user-password').value = '';
  document.getElementById('user-role').value = user ? user.role : 'VIEWER';
  document.getElementById('user-enabled').checked = user ? user.enabled : true;

  openModal('user-modal-backdrop');
}

window.editUser = async (id) => {
  try {
    const user = await API.getUser(id);
    openUserModal(user);
  } catch (err) {
    showToast(err.message, 'error');
  }
};

window.deleteUserPrompt = (id, username) => {
  showConfirmModal({
    title: 'Delete User Account',
    message: `Are you sure you want to permanently delete user account "${username}"?`,
    confirmText: 'Delete User',
    onConfirm: async () => {
      await API.deleteUser(id);
      showToast('User account deleted successfully', 'success');
      loadUsersData();
    }
  });
};

async function handleUserSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('user-id').value;
  const username = document.getElementById('user-username').value.trim();
  const password = document.getElementById('user-password').value;
  const role = document.getElementById('user-role').value;
  const enabled = document.getElementById('user-enabled').checked;
  const submitBtn = document.getElementById('user-submit-btn');

  submitBtn.disabled = true;
  submitBtn.textContent = 'Saving...';

  try {
    if (id) {
      const payload = { role, enabled };
      if (password) payload.password = password;
      await API.updateUser(id, payload);
      showToast('User account updated successfully', 'success');
    } else {
      const payload = { username, password, role, enabled };
      await API.createUser(payload);
      showToast('User account created successfully', 'success');
    }
    closeModal('user-modal-backdrop');
    loadUsersData();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Save User';
  }
}
