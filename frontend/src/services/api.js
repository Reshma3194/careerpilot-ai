
import axios from 'axios';
import { TOKEN_KEY, USER_KEY } from '../utils/constants.js';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const url = error.config?.url ?? '';
    const isAuthCall =
      url.includes('/auth/login') || url.includes('/auth/register');

    if (error.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }

    return Promise.reject(error);
  }
);

/** Unwraps the backend's { success, message, data } envelope. */
export const unwrap = (promise) => promise.then((res) => res.data.data);

/** Turns any axios error into a message a person can act on. */
export function errorMessage(error) {
  if (error.response?.data?.message) return error.response.data.message;
  if (error.code === 'ERR_NETWORK') {
    return 'Cannot reach the server. Check your connection and try again.';
  }
  return 'Something went wrong. Try again.';
}

export default api;
