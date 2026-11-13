import { Link } from 'react-router-dom';

/**
 * Trang hiển thị lỗi 403 - Không đủ quyền truy cập.
 */
export function ForbiddenPage() {
  return (
    <div
      role="region"
      aria-label="Không đủ quyền truy cập"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '60vh',
        padding: '2rem',
        textAlign: 'center',
      }}
    >
      <div
        aria-hidden="true"
        style={{
          fontSize: '3.5rem',
          marginBottom: '1rem',
        }}
      >
        🚫
      </div>
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '0.75rem',
        }}
      >
        403 - Bạn không đủ quyền
      </h1>
      <p
        style={{
          color: '#4b5563',
          maxWidth: '460px',
          marginBottom: '1.5rem',
          lineHeight: 1.5,
        }}
      >
        Tài khoản hiện tại của bạn không có quyền truy cập vào khu vực này.
        Vui lòng liên hệ quản trị viên hoặc quay lại trang chủ.
      </p>
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        <Link
          to="/"
          style={{
            padding: '0.6rem 1.25rem',
            backgroundColor: '#2563eb',
            color: '#ffffff',
            borderRadius: '6px',
            textDecoration: 'none',
            fontWeight: 500,
          }}
        >
          Về trang chủ
        </Link>
      </div>
    </div>
  );
}

export default ForbiddenPage;
