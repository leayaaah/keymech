import { Link } from 'react-router-dom';

/**
 * Trang Đăng nhập (Khung ban đầu cho chore/frontend-skeleton).
 */
export function LoginPage() {
  return (
    <div
      style={{
        maxWidth: '420px',
        margin: '2rem auto',
        padding: '2rem',
        backgroundColor: '#ffffff',
        borderRadius: '8px',
        boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
      }}
    >
      <h1
        style={{
          fontSize: '1.5rem',
          fontWeight: 700,
          textAlign: 'center',
          marginBottom: '1rem',
          color: '#111827',
        }}
      >
        Đăng nhập KeyMech
      </h1>
      <p style={{ textAlign: 'center', color: '#6b7280', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
        Khung giao diện đang sẵn sàng. Tính năng đăng nhập chi tiết sẽ hoàn thiện ở nhánh feature/C-01-login-ui.
      </p>
      <div style={{ textAlign: 'center' }}>
        <Link to="/register" style={{ color: '#2563eb', fontSize: '0.9rem' }}>
          Chưa có tài khoản? Đăng ký ngay
        </Link>
      </div>
    </div>
  );
}

export default LoginPage;
