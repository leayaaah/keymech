import { Routes, Route } from 'react-router-dom';

// Layouts
import GuestLayout from '../layouts/GuestLayout';
import CustomerLayout from '../layouts/CustomerLayout';
import StaffLayout from '../layouts/StaffLayout';
import AdminLayout from '../layouts/AdminLayout';

// Route Guard
import ProtectedRoute from './ProtectedRoute';

// Guest / Shop Pages
import HomePage from '../pages/guest/HomePage';
import ProductListPage from '../pages/guest/ProductListPage';
import ProductDetailPage from '../pages/guest/ProductDetailPage';
import CartPage from '../pages/guest/CartPage';
import LoginPage from '../pages/guest/LoginPage';
import RegisterPage from '../pages/guest/RegisterPage';

// Customer Pages
import CustomerProfilePage from '../pages/customer/CustomerProfilePage';
import CustomerOrdersPage from '../pages/customer/CustomerOrdersPage';
import CustomerOrderDetailPage from '../pages/customer/CustomerOrderDetailPage';
import CustomerCustomRequestsPage from '../pages/customer/CustomerCustomRequestsPage';
import ChangePasswordPage from '../pages/customer/ChangePasswordPage';

// Staff Pages
import StaffDashboardPage from '../pages/staff/StaffDashboardPage';
import StaffOrdersPage from '../pages/staff/StaffOrdersPage';
import StaffProductsPage from '../pages/staff/StaffProductsPage';
import StaffCustomRequestsPage from '../pages/staff/StaffCustomRequestsPage';

// Admin Pages
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import AdminStaffPage from '../pages/admin/AdminStaffPage';
import AdminReportsPage from '../pages/admin/AdminReportsPage';

// Common / Error Pages
import ForbiddenPage from '../pages/ForbiddenPage';
import NotFoundPage from '../pages/NotFoundPage';

/**
 * Cấu hình toàn bộ Routes cho ứng dụng KeyMech.
 * Tuân thủ phân quyền: Guest, Customer, Staff, Admin.
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* Khu vực Cửa hàng (Guest & Customer) */}
      <Route element={<GuestLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />

        {/* Giỏ hàng: Cho phép Guest & Customer; Chặn Staff & Admin */}
        <Route element={<ProtectedRoute blockStaffAdmin={true} />}>
          <Route path="/cart" element={<CartPage />} />
        </Route>

        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Khu vực khách hàng cá nhân: Chỉ Customer */}
        <Route element={<ProtectedRoute roles={['CUSTOMER']} />}>
          <Route path="/customer" element={<CustomerLayout />}>
            <Route path="profile" element={<CustomerProfilePage />} />
            <Route path="orders" element={<CustomerOrdersPage />} />
            <Route path="orders/:id" element={<CustomerOrderDetailPage />} />
            <Route
              path="custom-requests"
              element={<CustomerCustomRequestsPage />}
            />
            <Route path="change-password" element={<ChangePasswordPage />} />
          </Route>
        </Route>
      </Route>

      {/* Khu vực Nhân viên: Staff & Admin */}
      <Route element={<ProtectedRoute roles={['STAFF', 'ADMIN']} />}>
        <Route path="/staff" element={<StaffLayout />}>
          <Route index element={<StaffDashboardPage />} />
          <Route path="orders" element={<StaffOrdersPage />} />
          <Route path="products" element={<StaffProductsPage />} />
          <Route path="custom-requests" element={<StaffCustomRequestsPage />} />
        </Route>
      </Route>

      {/* Khu vực Quản trị viên: Chỉ Admin */}
      <Route element={<ProtectedRoute roles={['ADMIN']} />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="staff" element={<AdminStaffPage />} />
          <Route path="reports" element={<AdminReportsPage />} />
        </Route>
      </Route>

      {/* Báo lỗi quyền truy cập và 404 */}
      <Route path="/forbidden" element={<ForbiddenPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default AppRoutes;
