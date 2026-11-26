import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { cartApi } from '../api/cartApi';
import ChatWidget from '../components/ChatWidget';

/**
 * Layout chính cho cửa hàng (Guest & Customer).
 * Biểu tượng giỏ hàng và Chatbot chỉ hiển thị với Guest và Customer.
 */
export function GuestLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [cartCount, setCartCount] = useState(0);

  // Biểu tượng giỏ hàng và chatbot chỉ hiện với Guest (chưa đăng nhập) hoặc Customer
  const showCartAndChatbot = !user || user.role === 'CUSTOMER';

  useEffect(() => {
    if (!showCartAndChatbot) {
      return;
    }

    let active = true;
    const fetchCartCount = () => {
      cartApi
        .get()
        .then((data) => {
          if (active) {
            const total = (data?.items ?? []).reduce((sum, item) => sum + (item.quantity || 0), 0);
            setCartCount(total);
          }
        })
        .catch(() => {
          if (active) setCartCount(0);
        });
    };

    fetchCartCount();
    window.addEventListener('keymech:cart-updated', fetchCartCount);
    return () => {
      active = false;
      window.removeEventListener('keymech:cart-updated', fetchCartCount);
    };
  }, [showCartAndChatbot, user]);

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <div className="km-shell">
      <header className="km-header">
        <div className="km-header-inner">
          <Link to="/" className="km-brand">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
              <rect x="2" y="5" width="20" height="14" rx="2" />
              <path d="M6 9h.01M10 9h.01M14 9h.01M18 9h.01M6 13h.01M18 13h.01M9 15h6" strokeLinecap="round" />
            </svg>
            <span>KeyMech</span>
          </Link>

          <nav className="km-nav" aria-label="Điều hướng chính">
            <NavLink to="/" end className={({ isActive }) => `km-nav-link ${isActive ? 'active' : ''}`}>
              Trang chủ
            </NavLink>
            <NavLink to="/products" className={({ isActive }) => `km-nav-link ${isActive ? 'active' : ''}`}>
              Sản phẩm
            </NavLink>
            {user?.role === 'CUSTOMER' && (
              <>
                <NavLink to="/customer/orders" className={({ isActive }) => `km-nav-link ${isActive ? 'active' : ''}`}>
                  Đơn hàng của tôi
                </NavLink>
                <NavLink to="/customer/custom-requests" className={({ isActive }) => `km-nav-link ${isActive ? 'active' : ''}`}>
                  Custom phím
                </NavLink>
              </>
            )}
            {user?.role === 'STAFF' && (
              <NavLink to="/staff" className="km-nav-link km-nav-link--accent">
                Khu vực nhân viên
              </NavLink>
            )}
            {user?.role === 'ADMIN' && (
              <NavLink to="/admin" className="km-nav-link km-nav-link--accent">
                Trang quản trị
              </NavLink>
            )}
          </nav>

          <div className="km-header-actions">
            {showCartAndChatbot && (
              <Link to="/cart" className="km-cart-btn" aria-label="Xem giỏ hàng">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                  <circle cx="9" cy="21" r="1" />
                  <circle cx="20" cy="21" r="1" />
                  <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                {cartCount > 0 && <span className="km-cart-badge">{cartCount}</span>}
              </Link>
            )}

            {!user ? (
              <div className="km-auth-links">
                <Link to="/login">Đăng nhập</Link>
                <span className="km-auth-sep">/</span>
                <Link to="/register">Đăng ký</Link>
              </div>
            ) : (
              <div className="km-user-pill">
                <Link
                  to={
                    user.role === 'CUSTOMER'
                      ? '/customer/profile'
                      : user.role === 'ADMIN'
                        ? '/admin'
                        : '/staff'
                  }
                  className="km-user-name"
                >
                  {user.fullName || user.email}
                </Link>
                <button type="button" onClick={handleLogout} className="km-logout-btn">
                  Đăng xuất
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <main className="km-main">
        <Outlet />
      </main>

      {showCartAndChatbot && <ChatWidget />}

      <footer className="km-footer">
        <div className="km-footer-inner">
          <div className="km-footer-brand">
            <div>KeyMech</div>
            <div>Đam mê phím cơ đích thực.</div>
          </div>
          <div>
            <p className="km-footer-col-title">Khám phá</p>
            <p className="km-footer-col-text">Bàn phím • Switch • Keycap</p>
          </div>
          <div>
            <p className="km-footer-col-title">Hỗ trợ</p>
            <p className="km-footer-col-text">hello@keymech.vn • 0901 234 567</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default GuestLayout;
