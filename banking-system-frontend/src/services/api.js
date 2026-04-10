import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'https://mary-project-production.up.railway.app/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ========================
// JWT Interceptor — attach token to every request
// ========================
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ========================
// Auth API calls
// ========================
export const registerUser = (userData) => api.post('/auth/register', userData);
export const loginUser = (credentials) => api.post('/auth/login', credentials);

// ========================
// Account API calls
// ========================
export const getAccountsByUser = (userId) => api.get(`/accounts/${userId}`);
export const deposit = (accountId, amount) => api.post(`/accounts/${accountId}/deposit?amount=${amount}`);

// ========================
// Transfer API calls
// ========================
export const transfer = (transferData) => api.post('/transfer', transferData);

// ========================
// Helper: Auth utilities
// ========================
export const saveAuthData = (data) => {
  localStorage.setItem('token', data.token);
  localStorage.setItem('userId', data.userId);
  localStorage.setItem('userName', data.name);
  localStorage.setItem('userEmail', data.email);
};

export const clearAuthData = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('userName');
  localStorage.removeItem('userEmail');
};

export const isAuthenticated = () => {
  return !!localStorage.getItem('token');
};

export const getStoredUserId = () => {
  return localStorage.getItem('userId');
};

export const getStoredUserName = () => {
  return localStorage.getItem('userName');
};

export default api;
