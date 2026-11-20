/**
 * Municipal Smart Public Beach Safety & Water Quality Monitoring System
 * API Client Module
 */

import { Store } from './store.js';

const API_BASE = '/api';

export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

async function request(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const token = Store.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);

    // 204 No Content
    if (response.status === 204) {
      return null;
    }

    let data = null;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      try {
        data = await response.json();
      } catch (e) {
        data = null;
      }
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      // 401 Session Expired or Invalid Credentials
      if (response.status === 401) {
        // If not login request itself, clear auth state and trigger session expire handler
        if (!endpoint.includes('/auth/login')) {
          Store.clearAuth();
          if (window.handleUnauthorized) {
            window.handleUnauthorized();
          }
        }
        const msg = typeof data === 'string' ? data : (data?.message || 'Session expired or invalid credentials.');
        throw new ApiError(msg, 401, data);
      }

      // 403 Forbidden
      if (response.status === 403) {
        const msg = typeof data === 'string' ? data : (data?.message || "You don't have permission to perform this action.");
        throw new ApiError(msg, 403, data);
      }

      // 404 Not Found
      if (response.status === 404) {
        const msg = typeof data === 'string' ? data : (data?.message || 'Requested resource not found.');
        throw new ApiError(msg, 404, data);
      }

      // 400 Bad Request
      if (response.status === 400) {
        const msg = typeof data === 'string' ? data : (data?.message || 'Invalid request parameters or payload.');
        throw new ApiError(msg, 400, data);
      }

      // 500 Server Error
      if (response.status >= 500) {
        const msg = typeof data === 'string' ? data : (data?.message || 'Internal server error occurred.');
        throw new ApiError(msg, response.status, data);
      }

      const msg = typeof data === 'string' ? data : (data?.message || `API error (${response.status})`);
      throw new ApiError(msg, response.status, data);
    }

    return data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    // Network or parse error
    throw new ApiError(error.message || 'Network communication error.', 0);
  }
}

export const API = {
  // Auth
  login(username, password) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
  },

  register(username, password) {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
  },

  // Users (ADMIN)
  getUsers() {
    return request('/users');
  },

  getUser(id) {
    return request(`/users/${id}`);
  },

  createUser(userData) {
    return request('/users', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  },

  updateUser(id, userData) {
    return request(`/users/${id}`, {
      method: 'PUT',
      body: JSON.stringify(userData)
    });
  },

  deleteUser(id) {
    return request(`/users/${id}`, {
      method: 'DELETE'
    });
  },

  // Beach Zones
  getZones() {
    return request('/beach/zones/getAll');
  },

  getZone(id) {
    return request(`/beach/zones/get/${id}`);
  },

  createZone(zoneData) {
    return request('/beach/zones/create', {
      method: 'POST',
      body: JSON.stringify(zoneData)
    });
  },

  updateZone(id, zoneData) {
    return request(`/beach/zones/update/${id}`, {
      method: 'PUT',
      body: JSON.stringify(zoneData)
    });
  },

  deleteZone(id) {
    return request(`/beach/zones/delete/${id}`, {
      method: 'DELETE'
    });
  },

  // Water Samples
  getSamples() {
    return request('/beach/samples/getAll');
  },

  getSample(id) {
    return request(`/beach/samples/get/${id}`);
  },

  createSample(sampleData) {
    return request('/beach/samples/create', {
      method: 'POST',
      body: JSON.stringify(sampleData)
    });
  },

  updateSample(id, sampleData) {
    return request(`/beach/samples/update/${id}`, {
      method: 'PUT',
      body: JSON.stringify(sampleData)
    });
  },

  deleteSample(id) {
    return request(`/beach/samples/delete/${id}`, {
      method: 'DELETE'
    });
  },

  // Contacts
  getContacts() {
    return request('/contacts/getAll');
  },

  getContact(id) {
    return request(`/contacts/get/${id}`);
  },

  createContact(contactData) {
    return request('/contacts/create', {
      method: 'POST',
      body: JSON.stringify(contactData)
    });
  },

  updateContact(id, contactData) {
    return request(`/contacts/update/${id}`, {
      method: 'PUT',
      body: JSON.stringify(contactData)
    });
  },

  deleteContact(id) {
    return request(`/contacts/delete/${id}`, {
      method: 'DELETE'
    });
  },

  // Ledger
  getLedgerEntries() {
    return request('/ledger/getAll');
  },

  getLedgerSummary() {
    return request('/ledger/summary');
  },

  getZoneLedgerSummary(zoneId) {
    return request(`/ledger/summary/zone/${zoneId}`);
  },

  createLedgerEntry(ledgerData) {
    return request('/ledger/create', {
      method: 'POST',
      body: JSON.stringify(ledgerData)
    });
  },

  updateLedgerEntry(id, ledgerData) {
    return request(`/ledger/update/${id}`, {
      method: 'PUT',
      body: JSON.stringify(ledgerData)
    });
  },

  deleteLedgerEntry(id) {
    return request(`/ledger/delete/${id}`, {
      method: 'DELETE'
    });
  },

  // Monitoring
  getLatestMonitoringSample(zoneId) {
    return request(`/beach/monitoring/latest/${zoneId}`);
  },

  getWaterStatus(zoneId) {
    return request(`/beach/monitoring/status/${zoneId}`);
  },

  getWaterHistory(zoneId) {
    return request(`/beach/monitoring/history/${zoneId}`);
  },

  getZoneMonitoring(zoneId) {
    return request(`/beach/monitoring/zone/${zoneId}`);
  },

  // Dashboard
  getOverallDashboard() {
    return request('/beach/dashboard');
  },

  getZoneDashboard(zoneId) {
    return request(`/beach/dashboard/zone/${zoneId}`);
  },

  // Reports
  getSampleReport(params = {}) {
    const query = new URLSearchParams();
    if (params.zoneId) query.append('zoneId', params.zoneId);
    if (params.from) query.append('from', params.from);
    if (params.to) query.append('to', params.to);
    if (params.safe !== undefined && params.safe !== '') query.append('safe', params.safe);
    const qStr = query.toString();
    return request(`/beach/reports/samples${qStr ? `?${qStr}` : ''}`);
  },

  getWaterQualityReport(params = {}) {
    const query = new URLSearchParams();
    if (params.zoneId) query.append('zoneId', params.zoneId);
    if (params.from) query.append('from', params.from);
    if (params.to) query.append('to', params.to);
    const qStr = query.toString();
    return request(`/beach/reports/water-quality${qStr ? `?${qStr}` : ''}`);
  },

  getFinancialReport(params = {}) {
    const query = new URLSearchParams();
    if (params.zoneId) query.append('zoneId', params.zoneId);
    if (params.from) query.append('from', params.from);
    if (params.to) query.append('to', params.to);
    if (params.type) query.append('type', params.type);
    const qStr = query.toString();
    return request(`/beach/reports/financial${qStr ? `?${qStr}` : ''}`);
  },

  getLedgerReport(params = {}) {
    const query = new URLSearchParams();
    if (params.zoneId) query.append('zoneId', params.zoneId);
    if (params.contactId) query.append('contactId', params.contactId);
    if (params.type) query.append('type', params.type);
    const qStr = query.toString();
    return request(`/beach/reports/ledger${qStr ? `?${qStr}` : ''}`);
  }
};
