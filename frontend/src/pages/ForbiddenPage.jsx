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
        padding: 'var(--space-8)',
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: '56px', fontWeight: 700, color: 'var(--color-danger)', lineHeight: 1 }}>
        403
      </div>
      <h1 style={{ fontSize: 'var(--text-3xl)', fontWeight: 600, color: 'var(--color-text)', margin: 'var(--space-3) 0 var(--space-2)' }}>
        Bạn không đủ quyền truy cập
      </h1>
      <p style={{ color: 'var(--color-text-muted)', maxWidth: '460px', margin: '0 0 var(--space-6)', lineHeight: 1.5 }}>
        Tài khoản hiện tại của bạn không có quyền truy cập vào khu vực này.
      </p>
      <Link to="/" className="km-btn km-btn-primary">
        Về trang chủ
      </Link>
    </div>
  );
}

export default ForbiddenPage;
