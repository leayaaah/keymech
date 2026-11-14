import { Link } from 'react-router-dom';

/**
 * Trang hiển thị lỗi 404 - Không tìm thấy trang.
 */
export function NotFoundPage() {
  return (
    <div
      role="region"
      aria-label="Trang không tồn tại"
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
        🔍
      </div>
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '0.75rem',
        }}
      >
        404 - Trang không tồn tại
      </h1>
      <p
        style={{
          color: '#4b5563',
          maxWidth: '460px',
          marginBottom: '1.5rem',
          lineHeight: 1.5,
        }}
      >
        Đường dẫn bạn yêu cầu không tồn tại hoặc đã được chuyển sang địa chỉ
        khác.
      </p>
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
  );
}

export default NotFoundPage;
