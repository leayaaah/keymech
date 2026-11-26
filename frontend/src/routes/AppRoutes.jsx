import { useEffect } from 'react';
import { Routes, Route, useParams } from 'react-router-dom';

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
import ForgotPasswordPage from '../pages/guest/ForgotPasswordPage';
import ResetPasswordPage from '../pages/guest/ResetPasswordPage';

// Customer Pages
import CustomerProfilePage from '../pages/customer/CustomerProfilePage';
import CheckoutPage from '../pages/customer/CheckoutPage';
import CustomerOrdersPage from '../pages/customer/CustomerOrdersPage';
import CustomerOrderDetailPage from '../pages/customer/CustomerOrderDetailPage';
import CustomerCustomRequestsPage from '../pages/customer/CustomerCustomRequestsPage';
import CustomerCustomRequestDetailPage from '../pages/customer/CustomerCustomRequestDetailPage';
import ChangePasswordPage from '../pages/customer/ChangePasswordPage';

// Staff Pages
import StaffDashboardPage from '../pages/staff/StaffDashboardPage';
import StaffOrdersPage from '../pages/staff/StaffOrdersPage';
import StaffOrderDetailPage from '../pages/staff/StaffOrderDetailPage';
import StaffProductsPage from '../pages/staff/StaffProductsPage';
import StaffProductFormPage from '../pages/staff/StaffProductFormPage';
import StaffCustomRequestsPage from '../pages/staff/StaffCustomRequestsPage';
import StaffCustomRequestDetailPage from '../pages/staff/StaffCustomRequestDetailPage';

// Admin Pages
import AdminDashboardPage from '../pages/admin/AdminDashboardPage';
import AdminStaffPage from '../pages/admin/AdminStaffPage';
import AdminCategoriesPage from '../pages/admin/AdminCategoriesPage';
import AdminReportsPage from '../pages/admin/AdminReportsPage';

// Common / Error Pages
import ForbiddenPage from '../pages/ForbiddenPage';
import NotFoundPage from '../pages/NotFoundPage';

function PageTitle({ title, children }) {
  useEffect(() => {
    document.title = `${title} | KeyMech`;
  }, [title]);

  return children;
}

function ProductDetailRoute() {
  const { id } = useParams();
  return <ProductDetailPage key={id} />;
}

/**
 * Cấu hình toàn bộ Routes cho ứng dụng KeyMech.
 * Tuân thủ phân quyền: Guest, Customer, Staff, Admin.
 */
export function AppRoutes() {
  return (
    <Routes>
      {/* Khu vực Cửa hàng (Guest & Customer) */}
      <Route element={<GuestLayout />}>
        <Route path="/" element={<PageTitle title="Trang chủ"><HomePage /></PageTitle>} />
        <Route path="/products" element={<PageTitle title="Sản phẩm"><ProductListPage /></PageTitle>} />
        <Route path="/products/:id" element={<ProductDetailRoute />} />

        {/* Giỏ hàng: Cho phép Guest & Customer; Chặn Staff & Admin */}
        <Route element={<ProtectedRoute blockStaffAdmin={true} />}>
          <Route path="/cart" element={<PageTitle title="Giỏ hàng"><CartPage /></PageTitle>} />
        </Route>

        <Route path="/login" element={<PageTitle title="Đăng nhập"><LoginPage /></PageTitle>} />
        <Route path="/register" element={<PageTitle title="Đăng ký"><RegisterPage /></PageTitle>} />
        <Route path="/forgot-password" element={<PageTitle title="Quên mật khẩu"><ForgotPasswordPage /></PageTitle>} />
        <Route path="/reset-password" element={<PageTitle title="Đặt lại mật khẩu"><ResetPasswordPage /></PageTitle>} />

        {/* Khu vực khách hàng cá nhân: Chỉ Customer */}
        <Route element={<ProtectedRoute roles={['CUSTOMER']} />}>
          <Route path="/checkout" element={<PageTitle title="Thanh toán"><CheckoutPage /></PageTitle>} />
          <Route path="/customer" element={<CustomerLayout />}>
            <Route path="profile" element={<PageTitle title="Hồ sơ cá nhân"><CustomerProfilePage /></PageTitle>} />
            <Route path="orders" element={<PageTitle title="Đơn hàng của tôi"><CustomerOrdersPage /></PageTitle>} />
            <Route path="orders/:id" element={<PageTitle title="Chi tiết đơn hàng"><CustomerOrderDetailPage /></PageTitle>} />
            <Route path="orders/:id/success" element={<PageTitle title="Đặt hàng thành công"><CustomerOrderDetailPage success /></PageTitle>} />
            <Route
              path="custom-requests"
              element={<PageTitle title="Custom phím"><CustomerCustomRequestsPage /></PageTitle>}
            />
            <Route
              path="custom-requests/:id"
              element={<PageTitle title="Chi tiết yêu cầu Custom"><CustomerCustomRequestDetailPage /></PageTitle>}
            />
            <Route path="change-password" element={<PageTitle title="Đổi mật khẩu"><ChangePasswordPage /></PageTitle>} />
          </Route>
        </Route>
      </Route>

      {/* Khu vực Nhân viên: Staff & Admin */}
      <Route element={<ProtectedRoute roles={['STAFF', 'ADMIN']} />}>
        <Route path="/staff" element={<StaffLayout />}>
          <Route index element={<PageTitle title="Tổng quan nhân viên"><StaffDashboardPage /></PageTitle>} />
          <Route path="orders" element={<PageTitle title="Quản lý đơn hàng"><StaffOrdersPage /></PageTitle>} />
          <Route path="orders/:id" element={<PageTitle title="Xử lý đơn hàng"><StaffOrderDetailPage /></PageTitle>} />
          <Route path="products" element={<PageTitle title="Quản lý sản phẩm"><StaffProductsPage /></PageTitle>} />
          <Route path="products/new" element={<PageTitle title="Thêm sản phẩm"><StaffProductFormPage /></PageTitle>} />
          <Route path="products/:id/edit" element={<PageTitle title="Chỉnh sửa sản phẩm"><StaffProductFormPage /></PageTitle>} />
          <Route path="custom-requests" element={<PageTitle title="Yêu cầu Custom"><StaffCustomRequestsPage /></PageTitle>} />
          <Route path="custom-requests/:id" element={<PageTitle title="Xử lý yêu cầu Custom"><StaffCustomRequestDetailPage /></PageTitle>} />
        </Route>
      </Route>

      {/* Khu vực Quản trị viên: Chỉ Admin */}
      <Route element={<ProtectedRoute roles={['ADMIN']} />}>
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<PageTitle title="Tổng quan quản trị"><AdminDashboardPage /></PageTitle>} />
          <Route path="staff" element={<PageTitle title="Quản lý người dùng"><AdminStaffPage /></PageTitle>} />
          <Route path="categories" element={<PageTitle title="Quản lý danh mục"><AdminCategoriesPage /></PageTitle>} />
          <Route path="reports" element={<PageTitle title="Báo cáo doanh thu"><AdminReportsPage /></PageTitle>} />
        </Route>
      </Route>

      {/* Báo lỗi quyền truy cập và 404 */}
      <Route path="/forbidden" element={<ForbiddenPage />} />
      <Route path="*" element={<PageTitle title="Không tìm thấy trang"><NotFoundPage /></PageTitle>} />
    </Routes>
  );
}

export default AppRoutes;
