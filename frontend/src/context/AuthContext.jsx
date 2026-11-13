import { useState, useEffect } from 'react';
import { AuthContext } from './authContextDef';
import authApi from '../api/authApi';
import axiosClient from '../api/axiosClient';

export { AuthContext };

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Khôi phục phiên làm việc khi người dùng vào trang
  useEffect(() => {
    let isMounted = true;

    authApi
      .getMe()
      .then((data) => {
        if (isMounted) {
          setUser(data);
        }
      })
      .catch(() => {
        // 401 hoặc chưa có session cookie -> Guest
        if (isMounted) {
          setUser(null);
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const refreshUser = async () => {
    try {
      const data = await authApi.getMe();
      setUser(data);
      return data;
    } catch {
      setUser(null);
      return null;
    }
  };

  // Đăng nhập
  const login = async (email, password) => {
    setLoading(true);
    try {
      const loggedUser = await authApi.login({ email, password });
      setUser(loggedUser);

      // Nếu là Customer, đồng bộ giỏ hàng từ Session
      if (loggedUser && loggedUser.role === 'CUSTOMER') {
        try {
          await axiosClient.get('/cart');
        } catch {
          // Bỏ qua lỗi nếu backend cart chưa hoàn thiện
        }
      }

      return loggedUser;
    } finally {
      setLoading(false);
    }
  };

  // Đăng ký
  const register = async (registerData) => {
    setLoading(true);
    try {
      return await authApi.register(registerData);
    } finally {
      setLoading(false);
    }
  };

  // Đăng xuất
  const logout = async () => {
    setLoading(true);
    try {
      await authApi.logout();
    } catch {
      // Cho dù backend lỗi hoặc hết phiên, client vẫn xóa trạng thái user
    } finally {
      setUser(null);
      setLoading(false);
    }
  };

  // Đổi mật khẩu
  const changePassword = async ({ oldPassword, newPassword }) => {
    return await authApi.changePassword({ oldPassword, newPassword });
  };

  const isCustomer = user?.role === 'CUSTOMER';
  const isStaff = user?.role === 'STAFF';
  const isAdmin = user?.role === 'ADMIN';
  const isStaffOrAdmin = isStaff || isAdmin;

  const value = {
    user,
    setUser,
    loading,
    setLoading,
    login,
    register,
    logout,
    refreshUser,
    changePassword,
    isCustomer,
    isStaff,
    isAdmin,
    isStaffOrAdmin,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export default AuthProvider;
