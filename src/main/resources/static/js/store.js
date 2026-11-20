/**
 * Municipal Smart Public Beach Safety & Water Quality Monitoring System
 * Store: State & LocalStorage Manager
 */

const TOKEN_KEY = 'bs_jwt_token';
const USERNAME_KEY = 'bs_username';
const ROLE_KEY = 'bs_user_role';
const THEME_KEY = 'bs_theme_pref';

export const Store = {
  // Authentication State
  getToken() {
    return localStorage.getItem(TOKEN_KEY) || null;
  },

  getUsername() {
    return localStorage.getItem(USERNAME_KEY) || '';
  },

  getRole() {
    return localStorage.getItem(ROLE_KEY) || 'VIEWER';
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  setAuth(token, username, role) {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    if (username) localStorage.setItem(USERNAME_KEY, username);
    if (role) localStorage.setItem(ROLE_KEY, role);
  },

  clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(ROLE_KEY);
  },

  // Role Permission Helpers (for UI display control)
  isAdmin() {
    return this.getRole() === 'ADMIN';
  },

  isOperator() {
    return this.getRole() === 'OPERATOR';
  },

  canEdit() {
    const role = this.getRole();
    return role === 'ADMIN' || role === 'OPERATOR';
  },

  canDelete() {
    return this.getRole() === 'ADMIN';
  },

  canManageUsers() {
    return this.getRole() === 'ADMIN';
  },

  // Theme Management
  getTheme() {
    return localStorage.getItem(THEME_KEY) || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  },

  setTheme(theme) {
    localStorage.setItem(THEME_KEY, theme);
    document.documentElement.setAttribute('data-theme', theme);
  },

  initTheme() {
    const current = this.getTheme();
    document.documentElement.setAttribute('data-theme', current);
  },

  // Active View State
  activeView: 'dashboard',

  // Cached Reference Data
  cachedZones: [],
  cachedContacts: [],

  setZonesCache(zones) {
    this.cachedZones = Array.isArray(zones) ? zones : [];
  },

  getZonesCache() {
    return this.cachedZones;
  },

  setContactsCache(contacts) {
    this.cachedContacts = Array.isArray(contacts) ? contacts : [];
  },

  getContactsCache() {
    return this.cachedContacts;
  }
};
