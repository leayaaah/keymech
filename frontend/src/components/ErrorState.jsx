/**
 * Thành phần hiển thị lỗi tải dữ liệu kèm nút thử lại.
 */
export function ErrorState({
  title = 'Đã xảy ra lỗi',
  message = 'Không thể tải dữ liệu từ máy chủ. Vui lòng thử lại sau.',
  onRetry,
}) {
  return (
    <div
      role="alert"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 'var(--space-10) var(--space-6)',
        textAlign: 'center',
        backgroundColor: 'var(--color-surface)',
        borderRadius: 'var(--radius-lg)',
        border: '1px solid var(--color-danger-soft)',
      }}
    >
      <div
        aria-hidden="true"
        style={{
          width: '56px',
          height: '56px',
          borderRadius: 'var(--radius-pill)',
          backgroundColor: 'var(--color-danger-soft)',
          color: 'var(--color-danger)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 'var(--space-4)',
        }}
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 8v4M12 16h.01" strokeLinecap="round" />
        </svg>
      </div>
      <h3 style={{ fontSize: 'var(--text-xl)', fontWeight: 600, color: 'var(--color-danger)', margin: '0 0 var(--space-2)' }}>
        {title}
      </h3>
      <p style={{ fontSize: 'var(--text-base)', color: 'var(--color-text-muted)', maxWidth: '440px', margin: onRetry ? '0 0 var(--space-5)' : 0 }}>
        {message}
      </p>
      {onRetry && (
        <button type="button" className="km-btn km-btn-primary" onClick={onRetry}>
          Thử lại
        </button>
      )}
    </div>
  );
}

export default ErrorState;
