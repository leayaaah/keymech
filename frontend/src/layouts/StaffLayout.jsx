import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import '../styles/portal.css';

/**
 * Layout khu vực Nhân viên (Staff & Admin).
 * Không có icon giỏ hàng và Chatbot.
 */
export function StaffLayout() {
  const { user, isAdmin, logout } = useAuth();
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
            <Link to="/staff" className="km-brand" style={{ fontSize: '18px' }}>
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                <rect x="2" y="5" width="20" height="14" rx="2" />
                <path d="M6 9h.01M10 9h.01M14 9h.01M18 9h.01M9 15h6" strokeLinecap="round" />
              </svg>
              <span>KeyMech Staff</span>
            </Link>

            <nav className="km-portal-nav" aria-label="Điều hướng nhân viên">
              <NavLink to="/staff" end className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Tổng quan
              </NavLink>
              <NavLink to="/staff/orders" className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Đơn hàng
              </NavLink>
              <NavLink to="/staff/products" className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Sản phẩm
              </NavLink>
              <NavLink to="/staff/custom-requests" className={({ isActive }) => `km-portal-nav-link ${isActive ? 'active' : ''}`}>
                Yêu cầu Custom
              </NavLink>
              {isAdmin && (
                <NavLink to="/admin" className="km-portal-nav-link">
                  Sang khu Admin →
                </NavLink>
              )}
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

export default StaffLayout;
