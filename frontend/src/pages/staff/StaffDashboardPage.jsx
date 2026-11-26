import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import '../../styles/portal.css';

/**
 * Trang Tổng quan nhân viên (Figma frame 3:102).
 */
export function StaffDashboardPage() {
  const { user, isAdmin } = useAuth();

  return (
    <>
      <div>
        <h1 className="km-portal-title">Tổng quan nhân viên</h1>
        <p className="km-auth-subtitle">
          Xin chào {user?.fullName || user?.email}. Chọn nghiệp vụ cần xử lý bên dưới.
        </p>
      </div>

      <div className="km-kpi-grid">
        <article className="km-kpi-card">
          <span className="km-kpi-label">Đơn hàng</span>
          <strong style={{ fontSize: 'var(--text-xl)' }}>Quản lý đơn hàng</strong>
          <p className="km-field-hint">
            Xác nhận đơn mới, sửa số lượng (Mới/Đã xác nhận), xác nhận thanh toán, giao hàng hoặc hủy đơn.
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '8px' }}>
            <Link to="/staff/orders" className="km-btn km-btn-primary km-btn-sm">
              Mở đơn hàng →
            </Link>
          </div>
        </article>

        <article className="km-kpi-card">
          <span className="km-kpi-label">Kho sản phẩm</span>
          <strong style={{ fontSize: 'var(--text-xl)' }}>Quản lý sản phẩm</strong>
          <p className="km-field-hint">
            {isAdmin
              ? 'Thêm, chỉnh sửa thông tin, tồn kho và ẩn/hiện sản phẩm (quyền Quản trị viên).'
              : 'Thêm, chỉnh sửa thông tin và cập nhật số lượng tồn kho sản phẩm.'}
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '8px' }}>
            <Link to="/staff/products" className="km-btn km-btn-primary km-btn-sm">
              Mở sản phẩm →
            </Link>
          </div>
        </article>

        <article className="km-kpi-card">
          <span className="km-kpi-label">Dịch vụ Custom</span>
          <strong style={{ fontSize: 'var(--text-xl)' }}>Yêu cầu Custom phím</strong>
          <p className="km-field-hint">
            Tiếp nhận cấu hình khách gửi, lập báo giá, từ chối hoặc cập nhật tiến độ lắp ráp.
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '8px' }}>
            <Link to="/staff/custom-requests" className="km-btn km-btn-primary km-btn-sm">
              Mở Custom phím →
            </Link>
          </div>
        </article>
      </div>
    </>
  );
}

export default StaffDashboardPage;
