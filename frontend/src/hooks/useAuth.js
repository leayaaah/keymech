import { useContext } from 'react';
import { AuthContext } from '../context/authContextDef';

/**
 * Custom hook truy cập trạng thái xác thực và thông tin người dùng hiện tại.
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth phải được sử dụng bên trong AuthProvider');
  }
  return context;
}

export default useAuth;
