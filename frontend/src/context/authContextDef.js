import { createContext } from 'react';

export const AuthContext = createContext({
  user: null,
  loading: true,
  login: async () => {},
  register: async () => {},
  logout: async () => {},
  refreshUser: async () => {},
  changePassword: async () => {},
  isCustomer: false,
  isStaff: false,
  isAdmin: false,
  isStaffOrAdmin: false,
});

export default AuthContext;
