import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Layout chính cho cửa hàng (Guest & Customer).
 * Biểu tượng giỏ hàng và Chatbot chỉ hiển thị với Guest và Customer.
 */
export function GuestLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Biểu tượng giỏ hàng và chatbot chỉ hiện với Guest (chưa đăng nhập) hoặc Customer
  const showCartAndChatbot = !user || user.role === 'CUSTOMER';

  const handleLogout = async () => {
    await logout();
    navigate('/');
  };

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        backgroundColor: '#f3f4f6',
      }}
    >
      {/* Header */}
      <header
        style={{
          backgroundColor: '#111827',
          color: '#ffffff',
          position: 'sticky',
          top: 0,
          zIndex: 100,
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        }}
      >
        <div
          style={{
            maxWidth: '1200px',
            margin: '0 auto',
            padding: '0.75rem 1rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '1rem',
            flexWrap: 'wrap',
          }}
        >
          {/* Logo & Brand */}
          <Link
            to="/"
            style={{
              color: '#ffffff',
              textDecoration: 'none',
              fontWeight: 800,
              fontSize: '1.35rem',
              letterSpacing: '-0.5px',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
            }}
          >
            <span role="img" aria-label="Keyboard">
              ⌨️
            </span>{' '}
            KeyMech
          </Link>

          {/* Navigation links */}
          <nav
            aria-label="Điều hướng chính"
            style={{ display: 'flex', gap: '1.25rem', alignItems: 'center' }}
          >
            <Link
              to="/"
              style={{
                color: '#e5e7eb',
                textDecoration: 'none',
                fontSize: '0.925rem',
              }}
            >
              Trang chủ
            </Link>
            <Link
              to="/products"
              style={{
                color: '#e5e7eb',
                textDecoration: 'none',
                fontSize: '0.925rem',
              }}
            >
              Sản phẩm
            </Link>
            {user?.role === 'CUSTOMER' && (
              <>
                <Link
                  to="/customer/orders"
                  style={{
                    color: '#e5e7eb',
                    textDecoration: 'none',
                    fontSize: '0.925rem',
                  }}
                >
                  Đơn hàng của tôi
                </Link>
                <Link
                  to="/customer/custom-requests"
                  style={{
                    color: '#e5e7eb',
                    textDecoration: 'none',
                    fontSize: '0.925rem',
                  }}
                >
                  Custom phím
                </Link>
              </>
            )}
            {user?.role === 'STAFF' && (
              <Link
                to="/staff"
                style={{
                  color: '#60a5fa',
                  textDecoration: 'none',
                  fontSize: '0.925rem',
                  fontWeight: 600,
                }}
              >
                Khu vực nhân viên
              </Link>
            )}
            {user?.role === 'ADMIN' && (
              <Link
                to="/admin"
                style={{
                  color: '#f87171',
                  textDecoration: 'none',
                  fontSize: '0.925rem',
                  fontWeight: 600,
                }}
              >
                Trang quản trị
              </Link>
            )}
          </nav>

          {/* Action buttons (Cart & Auth) */}
          <div
            style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}
          >
            {showCartAndChatbot && (
              <Link
                to="/cart"
                aria-label="Xem giỏ hàng"
                style={{
                  color: '#ffffff',
                  textDecoration: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                  backgroundColor: '#1f2937',
                  padding: '0.45rem 0.85rem',
                  borderRadius: '6px',
                  fontSize: '0.9rem',
                }}
              >
                <span aria-hidden="true">🛒</span>
                <span>Giỏ hàng</span>
              </Link>
            )}

            {!user ? (
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <Link
                  to="/login"
                  style={{
                    color: '#ffffff',
                    textDecoration: 'none',
                    padding: '0.45rem 0.85rem',
                    borderRadius: '6px',
                    fontSize: '0.9rem',
                    border: '1px solid #374151',
                  }}
                >
                  Đăng nhập
                </Link>
                <Link
                  to="/register"
                  style={{
                    backgroundColor: '#2563eb',
                    color: '#ffffff',
                    textDecoration: 'none',
                    padding: '0.45rem 0.85rem',
                    borderRadius: '6px',
                    fontSize: '0.9rem',
                    fontWeight: 500,
                  }}
                >
                  Đăng ký
                </Link>
              </div>
            ) : (
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                }}
              >
                <Link
                  to={
                    user.role === 'CUSTOMER'
                      ? '/customer/profile'
                      : user.role === 'ADMIN'
                        ? '/admin'
                        : '/staff'
                  }
                  style={{
                    color: '#93c5fd',
                    fontSize: '0.875rem',
                    textDecoration: 'none',
                    fontWeight: 500,
                  }}
                >
                  {user.fullName || user.email} ({user.role})
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  style={{
                    backgroundColor: '#374151',
                    color: '#ffffff',
                    border: 'none',
                    padding: '0.45rem 0.75rem',
                    borderRadius: '6px',
                    fontSize: '0.85rem',
                    cursor: 'pointer',
                  }}
                >
                  Đăng xuất
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main style={{ flex: 1, maxWidth: '1200px', width: '100%', margin: '0 auto', padding: '1.5rem 1rem' }}>
        <Outlet />
      </main>

      {/* Floating Chatbot Button (Only for Guest and Customer) */}
      {showCartAndChatbot && (
        <div
          role="region"
          aria-label="Trợ lý ảo tư vấn KeyMech"
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            zIndex: 90,
          }}
        >
          <button
            type="button"
            aria-label="Mở cửa sổ Chatbot tư vấn"
            onClick={() => alert('Tính năng Chatbot tư vấn AI sẽ được mở ở Sprint sau.')}
            style={{
              width: '54px',
              height: '54px',
              borderRadius: '50%',
              backgroundColor: '#2563eb',
              color: '#ffffff',
              border: 'none',
              boxShadow: '0 4px 10px rgba(37, 99, 235, 0.4)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '1.5rem',
            }}
          >
            💬
          </button>
        </div>
      )}

      {/* Footer */}
      <footer
        style={{
          backgroundColor: '#111827',
          color: '#9ca3af',
          padding: '2rem 1rem',
          marginTop: 'auto',
          fontSize: '0.875rem',
          textAlign: 'center',
        }}
      >
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <p style={{ margin: '0 0 0.5rem 0' }}>
            © 2026 KeyMech - Nền tảng thương mại điện tử & Custom bàn phím cơ.
          </p>
          <p style={{ margin: 0, fontSize: '0.8rem', color: '#6b7280' }}>
            Dự án Nhóm 4 | Hỗ trợ responsive từ 360px | Bảo mật phân quyền nghiêm ngặt.
          </p>
        </div>
      </footer>
    </div>
  );
}

export default GuestLayout;
