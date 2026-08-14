import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadProfile = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setLoading(false);
      return;
    }
    try {
      const { data } = await api.get('/users/me');
      setUser(data.data);
    } catch (err) {
      localStorage.clear();
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const login = (authResponse) => {
    localStorage.setItem('accessToken', authResponse.accessToken);
    localStorage.setItem('refreshToken', authResponse.refreshToken);
    setUser({
      id: authResponse.userId,
      fullName: authResponse.fullName,
      email: authResponse.email,
      roles: authResponse.roles,
    });
  };

  // Used after Google/GitHub OAuth2 redirect — we only get raw tokens back in the
  // URL, not a full user object, so we store the tokens then fetch /users/me.
  const loginWithTokens = async (accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    setLoading(true);
    await loadProfile();
  };

  const logout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (e) {
      // ignore — clear local state regardless
    }
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, login, loginWithTokens, logout, loading, refreshProfile: loadProfile }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
