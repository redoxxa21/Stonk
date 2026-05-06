import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { apiClient } from '../lib/apiClient';
import { clearSession, loadSession, saveSession } from '../lib/authStorage';

const AuthContext = createContext(null);

async function loadProfile(username) {
  return apiClient.get(`/users/username/${encodeURIComponent(username)}`);
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => loadSession());
  const [user, setUser] = useState(null);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    let alive = true;

    async function hydrate() {
      if (!session?.token || !session?.username) {
        if (alive) setHydrated(true);
        return;
      }

      try {
        const profile = await loadProfile(session.username);
        if (!alive) return;
        setUser(profile);
        const nextSession = {
          token: session.token,
          userId: session.userId || profile.id,
          username: session.username,
          role: session.role || profile.role,
        };
        setSession(nextSession);
        saveSession(nextSession);
      } catch {
        if (!alive) return;
        clearSession();
        setSession(null);
        setUser(null);
      } finally {
        if (alive) setHydrated(true);
      }
    }

    hydrate();
    return () => {
      alive = false;
    };
  }, []);

  const auth = useMemo(() => {
    async function authenticate(path, body) {
      const response = await apiClient.post(path, body);
      const nextSession = {
        token: response.token,
        userId: response.userId,
        username: response.username,
        role: response.role,
      };
      saveSession(nextSession);
      setSession(nextSession);
      const profile = await loadProfile(response.username);
      setUser(profile);
      return { auth: response, user: profile };
    }

    return {
      session,
      user,
      hydrated,
      async login(body) {
        return authenticate('/auth/login', body);
      },
      async register(body) {
        return authenticate('/auth/register', body);
      },
      async refreshUser() {
        if (!session?.username) return null;
        const profile = await loadProfile(session.username);
        setUser(profile);
        return profile;
      },
      async updateProfile(id, body) {
        const updated = await apiClient.put(`/users/${id}`, body);
        setUser(updated);
        if (updated.username && session) {
          const nextSession = { ...session, username: updated.username, role: updated.role || session.role };
          saveSession(nextSession);
          setSession(nextSession);
        }
        return updated;
      },
      logout() {
        clearSession();
        setSession(null);
        setUser(null);
      },
    };
  }, [session, user, hydrated]);

  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return value;
}
