import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import '../styles/portal.css';

/**
 * Layout khu vực Quản trị viên (Admin).
 * Không có icon giỏ hàng và Chatbot.
 */
export function AdminLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="km-portal-shell">
      <header className="km-portal-header">
        <div className="km-portal-header-inner">
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-6)', flexWrap: 'wrap' }}>
            <Link to="/admin" className="km-brand" style={{ fontSize: '18px' }}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                <rect x="2" y="5" width="20" height="14" rx="2" />
                <path d="M6 9h.01M10 9h.01M14 9h.01M18 9h.01M9 15h6" strokeLinecap="round" />
              </svg>
              <span>KeyMech Admin</span>
            </Link>

            <nav className="km-portal-nav" aria-label="Điều hướng quản trị">
              <NavLink to="/admin" end className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Dashboard
              </NavLink>
              <NavLink to="/admin/staff" className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Người dùng
              </NavLink>
              <NavLink to="/admin/categories" className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Danh mục
              </NavLink>
              <NavLink to="/staff/products" className="km-portal-nav-link">
                Sản phẩm
              </NavLink>
              <NavLink to="/staff" className="km-portal-nav-link">
                Khu Staff →
              </NavLink>
            </nav>
          </div>

          <div className="km-user-pill">
            <Link to="/" className="km-nav-link">
              ← Cửa hàng
            </Link>
            <span className="km-user-name" style={{ fontSize: 'var(--text-sm)' }}>
              {user?.fullName || user?.email}
            </span>
            <button type="button" onClick={handleLogout} className="km-logout-btn">
              Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <main className="km-portal-main">
        <Outlet />
      </main>
    </div>
  );
}

export default AdminLayout;
