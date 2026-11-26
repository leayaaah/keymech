import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import adminCategoryApi from '../../api/adminCategoryApi';
import '../../styles/portal.css';

/**
 * Trang Dashboard quản trị viên (Figma frame 3:3825).
 */
export function AdminDashboardPage() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    let active = true;
    adminCategoryApi
      .getDashboard()
      .then((data) => {
        if (active) setStats(data);
      })
      .catch(() => {
        // Backend dashboard chưa triển khai -> hiển thị thẻ điều hướng bình thường
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <>
      <div>
        <h1 className="km-portal-title">Dashboard quản trị</h1>
        <p className="km-auth-subtitle">
          Tổng quan hệ thống KeyMech, quản lý người dùng, danh mục và sản phẩm.
        </p>
      </div>

      {stats && (
        <div className="km-kpi-grid">
          <div className="km-kpi-card">
            <span className="km-kpi-label">Tổng đơn hàng</span>
            <span className="km-kpi-value">{stats.totalOrders ?? 0}</span>
          </div>
          <div className="km-kpi-card">
            <span className="km-kpi-label">Đơn chờ xử lý</span>
            <span className="km-kpi-value">{stats.pendingOrders ?? 0}</span>
          </div>
          <div className="km-kpi-card">
            <span className="km-kpi-label">Sản phẩm</span>
            <span className="km-kpi-value">{stats.totalProducts ?? 0}</span>
          </div>
          <div className="km-kpi-card">
            <span className="km-kpi-label">Custom chờ duyệt</span>
            <span className="km-kpi-value">{stats.pendingCustomRequests ?? 0}</span>
          </div>
          <div className="km-kpi-card">
            <span className="km-kpi-label">Người dùng</span>
            <span className="km-kpi-value">{stats.totalUsers ?? 0}</span>
          </div>
        </div>
      )}

      <div className="km-kpi-grid">
        <article className="km-kpi-card">
          <span className="km-kpi-label">Nhân sự & Khách hàng</span>
          <strong style={{ fontSize: 'var(--text-xl)' }}>Quản lý người dùng</strong>
          <p className="km-field-hint">
            Tạo tài khoản nhân viên (vai trò cố định STAFF), sửa thông tin và ẩn/hiện tài khoản (không tác động ADMIN).
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '8px' }}>
            <Link to="/admin/staff" className="km-btn km-btn-primary km-btn-sm">
              Mở quản lý người dùng →
            </Link>
          </div>
        </article>

        <article className="km-kpi-card">
          <span className="km-kpi-label">Phân loại</span>
          <strong style={{ fontSize: 'var(--text-xl)' }}>Quản lý danh mục</strong>
          <p className="km-field-hint">
            Thêm, sửa và ẩn/hiện danh mục (chặn ẩn nếu còn sản phẩm đang hiển thị).
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '8px' }}>
            <Link to="/admin/categories" className="km-btn km-btn-primary km-btn-sm">
              Mở quản lý danh mục →
            </Link>
          </div>
        </article>

        <article className="km-kpi-card">
          <span className="km-kpi-label">Kho hàng</span>
          <strong style={{ fontSize: 'var(--text-xl)' }}>Quản lý sản phẩm</strong>
          <p className="km-field-hint">
            Xem, sửa và ẩn/hiện sản phẩm trên cửa hàng (quyền Quản trị viên).
          </p>
          <div style={{ marginTop: 'auto', paddingTop: '8px' }}>
            <Link to="/staff/products" className="km-btn km-btn-primary km-btn-sm">
              Mở quản lý sản phẩm →
            </Link>
          </div>
        </article>
      </div>
    </>
  );
}

export default AdminDashboardPage;
