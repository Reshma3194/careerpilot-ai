import api, { unwrap } from './api.js';

export const login = (email, password) => unwrap(api.post('/auth/login', { email, password }));

export const register = (fullName, email, password) =>
  unwrap(api.post('/auth/register', { fullName, email, password }));

export const fetchMe = () => unwrap(api.get('/auth/me'));
