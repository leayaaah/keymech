import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import adminCategoryApi from '../../api/adminCategoryApi';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import { getFeatureErrorMessage } from '../../utils/apiError';
import formatVND from '../../utils/formatVND';
import { customStatusLabels } from '../../utils/orderLabels';
import '../../styles/portal.css';

/**
 * Trang Dashboard quản trị viên (Figma frame 3:3825).
 */
export function AdminDashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;
    adminCategoryApi
      .getDashboard()
      .then((data) => {
        if (active) {
          setStats(data);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(getFeatureErrorMessage(err, 'Không thể tải thống kê tổng quan.'));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [reloadKey]);

  const metrics = stats && [
    ['Tổng đơn hàng', stats.totalOrders],
    ['Đơn chờ xử lý', stats.pendingOrders],
    ['Đơn hôm nay', stats.ordersToday],
    ['Đơn tháng này', stats.ordersThisMonth],
    ['Doanh thu hôm nay', formatVND(stats.revenueToday)],
    ['Doanh thu tháng này', formatVND(stats.revenueThisMonth)],
    ['Sản phẩm', stats.totalProducts],
    ['Người dùng', stats.totalUsers],
    ['Người dùng mới hôm nay', stats.newUsersToday],
    ['Người dùng mới tháng này', stats.newUsersThisMonth],
  ];

  return (
    <>
      <div>
        <h1 className="km-portal-title">Dashboard quản trị</h1>
        <p className="km-auth-subtitle">
          Tổng quan hệ thống KeyMech, quản lý người dùng, danh mục và sản phẩm.
        </p>
      </div>

      {loading && <Loading message="Đang tải thống kê tổng quan..." />}

      {!loading && error && (
        <ErrorState
          title="Chưa thể tải thống kê tổng quan"
          message={error}
          onRetry={() => {
            setLoading(true);
            setError('');
            setReloadKey((k) => k + 1);
          }}
        />
      )}

      {!loading && !error && metrics && (
        <>
          <div className="km-kpi-grid">
            {metrics.map(([label, value]) => (
              <div className="km-kpi-card" key={label}>
                <span className="km-kpi-label">{label}</span>
                <span className="km-kpi-value">{value}</span>
              </div>
            ))}
          </div>
          <section aria-labelledby="custom-stats">
            <h2 id="custom-stats">Yêu cầu Custom theo trạng thái</h2>
            <div className="km-kpi-grid">
              {Object.entries(customStatusLabels).map(([status, label]) => (
                <div className="km-kpi-card" key={status}>
                  <span className="km-kpi-label">{label}</span>
                  <span className="km-kpi-value">{stats.customRequestsByStatus[status]}</span>
                </div>
              ))}
            </div>
          </section>
        </>
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
            Thêm mới và ẩn/hiện danh mục (chặn ẩn nếu còn sản phẩm đang hiển thị).
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
