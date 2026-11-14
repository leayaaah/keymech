import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import Loading from '../components/Loading';
import ForbiddenPage from '../pages/ForbiddenPage';

/**
 * Component bảo vệ các tuyến đường yêu cầu vai trò hoặc đăng nhập.
 *
 * @param {Object} props
 * @param {string[]} [props.roles] Danh sách vai trò được phép (ví dụ: ['CUSTOMER'], ['STAFF', 'ADMIN'], ['ADMIN'])
 * @param {boolean} [props.blockStaffAdmin=false] Áp dụng cho các khu vực như giỏ hàng (Guest & Customer được phép, Staff & Admin bị chặn)
 */
export function ProtectedRoute({ roles, blockStaffAdmin = false }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <Loading fullPage message="Đang kiểm tra quyền truy cập..." />;
  }

  // Trường hợp đặc thù của giỏ hàng: Guest và Customer được vào, nhưng Staff và Admin bị chặn
  if (blockStaffAdmin && user) {
    if (user.role === 'STAFF' || user.role === 'ADMIN') {
      return <ForbiddenPage />;
    }
  }

  // Nếu route có yêu cầu danh sách roles cụ thể
  if (roles && roles.length > 0) {
    // Chưa đăng nhập -> Chuyển hướng sang trang đăng nhập và lưu vị trí trước đó
    if (!user) {
      return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // Đã đăng nhập nhưng không khớp vai trò được cho phép
    if (!roles.includes(user.role)) {
      return <ForbiddenPage />;
    }
  }

  return <Outlet />;
}

export default ProtectedRoute;
