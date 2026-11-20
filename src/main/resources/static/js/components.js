/**
 * Municipal Smart Public Beach Safety & Water Quality Monitoring System
 * UI Components & Helper Module
 */

// XSS Sanitizer
export function escapeHTML(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Toast Notifications
export function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  
  const icon = type === 'success' ? '✅' : type === 'error' ? '⚠️' : type === 'warning' ? '🔔' : 'ℹ️';
  toast.innerHTML = `
    <span>${icon}</span>
    <div style="flex:1;">${escapeHTML(message)}</div>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Confirmation Modal Dialog
export function showConfirmModal({ title = 'Confirm Action', message = 'Are you sure?', confirmText = 'Confirm', onConfirm }) {
  const backdrop = document.getElementById('confirm-modal-backdrop');
  if (!backdrop) return;

  document.getElementById('confirm-modal-title').textContent = title;
  document.getElementById('confirm-modal-message').textContent = message;
  
  const confirmBtn = document.getElementById('confirm-modal-submit-btn');
  confirmBtn.textContent = confirmText;

  // Clone button to strip previous event listeners
  const newConfirmBtn = confirmBtn.cloneNode(true);
  confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

  newConfirmBtn.addEventListener('click', async () => {
    newConfirmBtn.disabled = true;
    newConfirmBtn.textContent = 'Processing...';
    try {
      if (onConfirm) await onConfirm();
      closeModal('confirm-modal-backdrop');
    } catch (err) {
      showToast(err.message || 'Operation failed', 'error');
    } finally {
      newConfirmBtn.disabled = false;
      newConfirmBtn.textContent = confirmText;
    }
  });

  openModal('confirm-modal-backdrop');
}

// Modal Helpers
export function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

export function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

// Loading State Generator
export function renderLoadingState(message = 'Loading data...') {
  return `
    <div class="state-box">
      <div class="spinner"></div>
      <div class="state-title">${escapeHTML(message)}</div>
      <div class="state-description">Please wait while fetching information from server...</div>
    </div>
  `;
}

// Empty State Generator
export function renderEmptyState(title = 'No records found', description = 'There are currently no items to display.', icon = '🔍') {
  return `
    <div class="state-box">
      <div class="state-icon">${icon}</div>
      <div class="state-title">${escapeHTML(title)}</div>
      <div class="state-description">${escapeHTML(description)}</div>
    </div>
  `;
}

// Badge Generator
export function renderBadge(status) {
  if (status === 'GREEN' || status === true) {
    return `<span class="badge badge-green">🟢 SAFE</span>`;
  } else if (status === 'RED' || status === false) {
    return `<span class="badge badge-red">🔴 UNSAFE / CLOSURE</span>`;
  }
  return `<span class="badge badge-amber">${escapeHTML(status || 'UNKNOWN')}</span>`;
}

// Formatters
export function formatCurrency(amount) {
  const val = Number(amount) || 0;
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
}

export function formatDateTime(isoStr) {
  if (!isoStr) return 'N/A';
  try {
    const date = new Date(isoStr);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return isoStr;
  }
}

// Native SVG Donut Chart Renderer
export function renderWaterQualityChart(containerId, safeCount = 0, unsafeCount = 0) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const total = safeCount + unsafeCount;
  if (total === 0) {
    container.innerHTML = `<div style="text-align:center; color: var(--text-light); font-size: 0.85rem;">No sample data for visual chart</div>`;
    return;
  }

  const safePercent = Math.round((safeCount / total) * 100);
  const unsafePercent = 100 - safePercent;

  container.innerHTML = `
    <div style="display:flex; align-items:center; justify-content:space-around; width:100%; height:100%;">
      <svg width="150" height="150" viewBox="0 0 36 36" style="transform: rotate(-90deg);">
        <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="var(--border-subtle)" stroke-width="4.5" />
        <!-- Safe Segment -->
        <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="var(--safe-green)" stroke-width="4.5" stroke-dasharray="${safePercent}, 100" />
      </svg>
      <div style="display:flex; flex-direction:column; gap:0.6rem; font-size:0.88rem;">
        <div style="display:flex; align-items:center; gap:0.5rem;">
          <span style="width:12px; height:12px; background:var(--safe-green); border-radius:3px;"></span>
          <strong>Safe:</strong> ${safeCount} (${safePercent}%)
        </div>
        <div style="display:flex; align-items:center; gap:0.5rem;">
          <span style="width:12px; height:12px; background:var(--unsafe-red); border-radius:3px;"></span>
          <strong>Unsafe:</strong> ${unsafeCount} (${unsafePercent}%)
        </div>
        <div style="font-size:0.78rem; color:var(--text-light); margin-top:0.2rem;">Total Samples: ${total}</div>
      </div>
    </div>
  `;
}

// Native SVG Financial Summary Bar Chart Renderer
export function renderFinancialChart(containerId, income = 0, expense = 0, net = 0) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const maxVal = Math.max(Math.abs(income), Math.abs(expense), Math.abs(net), 100);
  const incH = Math.round((Math.abs(income) / maxVal) * 120);
  const expH = Math.round((Math.abs(expense) / maxVal) * 120);
  const netH = Math.round((Math.abs(net) / maxVal) * 120);

  container.innerHTML = `
    <div style="display:flex; flex-direction:column; width:100%; height:100%; justify-content:flex-end;">
      <div style="display:flex; align-items:flex-end; justify-content:space-around; height:140px; padding-bottom:0.5rem; border-bottom:1px solid var(--border-subtle);">
        <div style="display:flex; flex-direction:column; align-items:center; gap:0.3rem;">
          <span style="font-size:0.75rem; font-weight:700; color:var(--safe-green);">${formatCurrency(income)}</span>
          <div style="width:36px; height:${Math.max(incH, 4)}px; background:var(--safe-green); border-radius:4px 4px 0 0;"></div>
        </div>
        <div style="display:flex; flex-direction:column; align-items:center; gap:0.3rem;">
          <span style="font-size:0.75rem; font-weight:700; color:var(--unsafe-red);">${formatCurrency(expense)}</span>
          <div style="width:36px; height:${Math.max(expH, 4)}px; background:var(--unsafe-red); border-radius:4px 4px 0 0;"></div>
        </div>
        <div style="display:flex; flex-direction:column; align-items:center; gap:0.3rem;">
          <span style="font-size:0.75rem; font-weight:700; color:${net >= 0 ? 'var(--accent-teal)' : 'var(--unsafe-red)'};">${formatCurrency(net)}</span>
          <div style="width:36px; height:${Math.max(netH, 4)}px; background:${net >= 0 ? 'var(--accent-teal)' : 'var(--unsafe-red)'}; border-radius:4px 4px 0 0;"></div>
        </div>
      </div>
      <div style="display:flex; justify-content:space-around; font-size:0.78rem; font-weight:600; color:var(--text-muted); padding-top:0.4rem;">
        <span>Income</span>
        <span>Expense</span>
        <span>Net Balance</span>
      </div>
    </div>
  `;
}
