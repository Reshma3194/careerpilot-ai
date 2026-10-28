import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import * as authService from '../services/authService.js';
import { TOKEN_KEY, USER_KEY } from '../utils/constants.js';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw && localStorage.getItem(TOKEN_KEY) ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);

  const persist = useCallback((auth) => {
    localStorage.setItem(TOKEN_KEY, auth.token);
    const profile = { email: auth.email, fullName: auth.fullName };
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setUser(profile);
  }, []);

  const login = useCallback(
    async (email, password) => persist(await authService.login(email, password)),
    [persist]
  );

  const register = useCallback(
    async (fullName, email, password) => persist(await authService.register(fullName, email, password)),
    [persist]
  );

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, isAuthenticated: Boolean(user), login, register, logout }),
    [user, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
