import { Link } from 'react-router-dom';

/**
 * Trang Đăng ký tài khoản (Khung ban đầu cho chore/frontend-skeleton).
 */
export function RegisterPage() {
  return (
    <div
      style={{
        maxWidth: '460px',
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
        Đăng ký tài khoản
      </h1>
      <p style={{ textAlign: 'center', color: '#6b7280', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
        Khung giao diện đang sẵn sàng. Tính năng đăng ký chi tiết sẽ hoàn thiện ở nhánh feature/C-01-login-ui.
      </p>
      <div style={{ textAlign: 'center' }}>
        <Link to="/login" style={{ color: '#2563eb', fontSize: '0.9rem' }}>
          Đã có tài khoản? Đăng nhập
        </Link>
      </div>
    </div>
  );
}

export default RegisterPage;
