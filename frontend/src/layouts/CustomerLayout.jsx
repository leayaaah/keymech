import { useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import '../styles/customer.css';

const NAV_ITEMS = [
  { to: '/customer/orders', label: 'Đơn hàng của tôi' },
  { to: '/customer/custom-requests', label: 'Custom phím' },
  { to: '/customer/profile', label: 'Hồ sơ cá nhân' },
  { to: '/customer/change-password', label: 'Đổi mật khẩu' },
];

/**
 * Layout khu vực tài khoản Customer (Figma frames 3:530–3:566 & 3:6588 mobile menu).
 */
export function CustomerLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const currentSection =
    NAV_ITEMS.find((item) => location.pathname.startsWith(item.to))?.label || 'Tài khoản';

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <div className="km-account-wrap">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
        <nav className="km-breadcrumb" aria-label="Breadcrumb">
          <Link to="/">Trang chủ</Link>
          <span>/</span>
          <span>Tài khoản</span>
          <span>/</span>
          <span>{currentSection}</span>
        </nav>

        <button
          type="button"
          className="km-btn km-btn-soft km-account-mobile-toggle"
          onClick={() => setMenuOpen((o) => !o)}
        >
          {menuOpen ? 'Đóng menu tài khoản' : 'Menu tài khoản'}
        </button>
      </div>

      <div className="km-account-grid">
        <aside className={`km-account-sidebar ${menuOpen ? '' : 'km-account-sidebar--collapsed'}`}>
          <h2 className="km-account-sidebar-title">Tài khoản của bạn</h2>
          <p className="km-account-sidebar-user">{user?.fullName || user?.email}</p>

          <nav className="km-account-nav" aria-label="Điều hướng tài khoản">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) => `km-account-nav-link ${isActive ? 'active' : ''}`}
              >
                {item.label}
              </NavLink>
            ))}
            <button type="button" className="km-account-logout" onClick={handleLogout}>
              Đăng xuất
            </button>
          </nav>
        </aside>

        <section className="km-account-main">
          <Outlet />
        </section>
      </div>
    </div>
  );
}

export default CustomerLayout;
