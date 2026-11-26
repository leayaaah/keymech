import { Link } from 'react-router-dom';

/**
 * Trang hiển thị lỗi 404 - Không tìm thấy trang / sản phẩm đã ẩn.
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
        padding: 'var(--space-8)',
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: '56px', fontWeight: 700, color: 'var(--color-primary)', lineHeight: 1 }}>
        404
      </div>
      <h1 style={{ fontSize: 'var(--text-3xl)', fontWeight: 600, color: 'var(--color-text)', margin: 'var(--space-3) 0 var(--space-2)' }}>
        Trang không tồn tại
      </h1>
      <p style={{ color: 'var(--color-text-muted)', maxWidth: '460px', margin: '0 0 var(--space-6)', lineHeight: 1.5 }}>
        Đường dẫn hoặc sản phẩm bạn yêu cầu không tồn tại hoặc hiện không khả dụng.
      </p>
      <Link to="/" className="km-btn km-btn-primary">
        Về trang chủ
      </Link>
    </div>
  );
}

export default NotFoundPage;
