import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Layout khu vực Quản trị viên cấp cao (Admin).
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
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        backgroundColor: '#0f172a',
        color: '#f8fafc',
      }}
    >
      {/* Admin Header */}
      <header
        style={{
          backgroundColor: '#1e1b4b',
          color: '#ffffff',
          position: 'sticky',
          top: 0,
          zIndex: 100,
          borderBottom: '2px solid #818cf8',
        }}
      >
        <div
          style={{
            maxWidth: '1360px',
            margin: '0 auto',
            padding: '0.85rem 1rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            gap: '1rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem' }}>
            <Link
              to="/admin"
              style={{
                color: '#ffffff',
                textDecoration: 'none',
                fontWeight: 800,
                fontSize: '1.25rem',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
              }}
            >
              ⚡ <span>KeyMech Admin</span>
            </Link>

            <nav
              aria-label="Admin Navigation"
              style={{ display: 'flex', gap: '0.75rem' }}
            >
              <NavLink
                to="/admin"
                end
                style={({ isActive }) => ({
                  color: isActive ? '#a5b4fc' : '#cbd5e1',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  fontWeight: isActive ? 600 : 400,
                  padding: '0.35rem 0.65rem',
                  borderRadius: '4px',
                  backgroundColor: isActive ? 'rgba(165, 180, 252, 0.15)' : 'transparent',
                })}
              >
                Tổng quan Dashboard
              </NavLink>
              <NavLink
                to="/admin/staff"
                style={({ isActive }) => ({
                  color: isActive ? '#a5b4fc' : '#cbd5e1',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  fontWeight: isActive ? 600 : 400,
                  padding: '0.35rem 0.65rem',
                  borderRadius: '4px',
                  backgroundColor: isActive ? 'rgba(165, 180, 252, 0.15)' : 'transparent',
                })}
              >
                Quản lý nhân viên
              </NavLink>
              <NavLink
                to="/admin/reports"
                style={({ isActive }) => ({
                  color: isActive ? '#a5b4fc' : '#cbd5e1',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  fontWeight: isActive ? 600 : 400,
                  padding: '0.35rem 0.65rem',
                  borderRadius: '4px',
                  backgroundColor: isActive ? 'rgba(165, 180, 252, 0.15)' : 'transparent',
                })}
              >
                Báo cáo doanh thu
              </NavLink>
              <NavLink
                to="/staff"
                style={{
                  color: '#93c5fd',
                  textDecoration: 'none',
                  fontSize: '0.9rem',
                  padding: '0.35rem 0.65rem',
                }}
              >
                Sang khu vực Staff →
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
            <span style={{ fontSize: '0.85rem', color: '#c7d2fe' }}>
              {user?.fullName || user?.email} (Admin)
            </span>
            <button
              type="button"
              onClick={handleLogout}
              style={{
                backgroundColor: '#312e81',
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
          maxWidth: '1360px',
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

export default AdminLayout;
