import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Layout khu vực Nhân viên (Staff).
 * Không có icon giỏ hàng và Chatbot.
 */
export function StaffLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
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
      {/* Staff Header */}
      <header
        style={{
          backgroundColor: '#1e293b',
          color: '#ffffff',
          position: 'sticky',
          top: 0,
          zIndex: 100,
          borderBottom: '2px solid #3b82f6',
        }}
      >
        <div
          style={{
            maxWidth: '1280px',
            margin: '0 auto',
            padding: '0.75rem 1rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
            <Link
              to="/staff"
              style={{
                color: '#ffffff',
                textDecoration: 'none',
                fontWeight: 700,
                fontSize: '1.2rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
              }}
            >
              🛠️ <span>KeyMech Staff Portal</span>
            </Link>

            <nav
              aria-label="Staff Menu"
              style={{ display: 'flex', gap: '0.75rem' }}
            >
              <NavLink
                to="/staff/orders"
                style={({ isActive }) => ({
                  color: isActive ? '#60a5fa' : '#cbd5e1',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  fontWeight: isActive ? 600 : 400,
                  padding: '0.35rem 0.65rem',
                  borderRadius: '4px',
                  backgroundColor: isActive ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
                })}
              >
                Quản lý đơn hàng
              </NavLink>
              <NavLink
                to="/staff/products"
                style={({ isActive }) => ({
                  color: isActive ? '#60a5fa' : '#cbd5e1',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  fontWeight: isActive ? 600 : 400,
                  padding: '0.35rem 0.65rem',
                  borderRadius: '4px',
                  backgroundColor: isActive ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
                })}
              >
                Quản lý sản phẩm
              </NavLink>
              <NavLink
                to="/staff/custom-requests"
                style={({ isActive }) => ({
                  color: isActive ? '#60a5fa' : '#cbd5e1',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  fontWeight: isActive ? 600 : 400,
                  padding: '0.35rem 0.65rem',
                  borderRadius: '4px',
                  backgroundColor: isActive ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
                })}
              >
                Duyệt Custom phím
              </NavLink>
            </nav>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <Link
              to="/"
              style={{
                color: '#94a3b8',
                fontSize: '0.85rem',
                textDecoration: 'none',
              }}
            >
              ← Về Cửa hàng
            </Link>
            <span style={{ fontSize: '0.85rem', color: '#cbd5e1' }}>
              {user?.fullName || user?.email} (Staff)
            </span>
            <button
              type="button"
              onClick={handleLogout}
              style={{
                backgroundColor: '#334155',
                color: '#ffffff',
                border: 'none',
                padding: '0.4rem 0.75rem',
                borderRadius: '4px',
                fontSize: '0.85rem',
                cursor: 'pointer',
              }}
            >
              Đăng xuất
            </button>
          </div>
        </div>
      </header>

      {/* Main content */}
      <main
        style={{
          flex: 1,
          maxWidth: '1280px',
          width: '100%',
          margin: '0 auto',
          padding: '1.5rem 1rem',
        }}
      >
        <Outlet />
      </main>
    </div>
  );
}

export default StaffLayout;
