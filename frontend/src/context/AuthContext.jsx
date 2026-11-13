import { useState } from 'react';
import { AuthContext } from './authContextDef';

export { AuthContext };

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(false);

  const login = async () => {};
  const logout = async () => {
    setUser(null);
  };
  const refreshUser = async () => {};

  const value = {
    user,
    setUser,
    loading,
    setLoading,
    login,
    logout,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
