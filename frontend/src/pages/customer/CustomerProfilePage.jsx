import { useAuth } from '../../hooks/useAuth';

/**
 * Trang hồ sơ cá nhân của khách hàng.
 */
export function CustomerProfilePage() {
  const { user } = useAuth();

  return (
    <div>
      <h1
        style={{
          fontSize: '1.4rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '1rem',
        }}
      >
        Hồ sơ cá nhân
      </h1>
      <p style={{ color: '#4b5563', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
        Quản lý thông tin tài khoản và địa chỉ giao hàng của bạn.
      </p>

      <div
        style={{
          display: 'grid',
          gap: '1rem',
          maxWidth: '500px',
        }}
      >
        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#374151', marginBottom: '0.25rem' }}>
            Họ và tên
          </label>
          <input
            type="text"
            readOnly
            value={user?.fullName || 'Khách hàng KeyMech'}
            style={{
              width: '100%',
              padding: '0.5rem 0.75rem',
              borderRadius: '6px',
              border: '1px solid #d1d5db',
              backgroundColor: '#f9fafb',
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#374151', marginBottom: '0.25rem' }}>
            Email
          </label>
          <input
            type="email"
            readOnly
            value={user?.email || 'customer@example.com'}
            style={{
              width: '100%',
              padding: '0.5rem 0.75rem',
              borderRadius: '6px',
              border: '1px solid #d1d5db',
              backgroundColor: '#f9fafb',
            }}
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, color: '#374151', marginBottom: '0.25rem' }}>
            Vai trò hệ thống
          </label>
          <input
            type="text"
            readOnly
            value={user?.role || 'CUSTOMER'}
            style={{
              width: '100%',
              padding: '0.5rem 0.75rem',
              borderRadius: '6px',
              border: '1px solid #d1d5db',
              backgroundColor: '#f9fafb',
            }}
          />
        </div>
      </div>
    </div>
  );
}

export default CustomerProfilePage;
