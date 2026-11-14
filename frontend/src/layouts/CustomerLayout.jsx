import { NavLink, Outlet } from 'react-router-dom';

/**
 * Layout cho khu vực khách hàng cá nhân (Customer).
 */
export function CustomerLayout() {
  const navItems = [
    { to: '/customer/profile', label: 'Thông tin cá nhân' },
    { to: '/customer/orders', label: 'Lịch sử đơn hàng' },
    { to: '/customer/custom-requests', label: 'Yêu cầu Custom' },
    { to: '/customer/change-password', label: 'Đổi mật khẩu' },
  ];

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: '1.5rem',
        alignItems: 'start',
      }}
    >
      {/* Sidebar menu */}
      <aside
        style={{
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          padding: '1.25rem',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
        }}
      >
        <h2
          style={{
            fontSize: '1.1rem',
            fontWeight: 700,
            marginBottom: '1rem',
            color: '#111827',
          }}
        >
          Tài khoản của tôi
        </h2>
        <nav
          aria-label="Menu tài khoản"
          style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}
        >
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                display: 'block',
                padding: '0.65rem 0.85rem',
                borderRadius: '6px',
                textDecoration: 'none',
                fontSize: '0.925rem',
                fontWeight: isActive ? 600 : 400,
                color: isActive ? '#2563eb' : '#4b5563',
                backgroundColor: isActive ? '#eff6ff' : 'transparent',
              })}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      {/* Main panel */}
      <section
        style={{
          gridColumn: 'span 2',
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          padding: '1.5rem',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          minHeight: '400px',
        }}
      >
        <Outlet />
      </section>
    </div>
  );
}

export default CustomerLayout;
