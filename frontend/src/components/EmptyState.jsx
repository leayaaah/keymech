/**
 * Thành phần hiển thị trạng thái danh sách trống.
 */
export function EmptyState({
  title = 'Không tìm thấy sản phẩm',
  description = 'Hiện tại chưa có dữ liệu hiển thị hoặc không khớp với bộ lọc của bạn.',
  actionText,
  onAction,
}) {
  return (
    <div
      role="region"
      aria-label={title}
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 'var(--space-12) var(--space-6)',
        textAlign: 'center',
        backgroundColor: 'var(--color-surface)',
        borderRadius: 'var(--radius-lg)',
        border: '1px dashed var(--color-border-strong)',
      }}
    >
      <div
        aria-hidden="true"
        style={{
          width: '56px',
          height: '56px',
          borderRadius: 'var(--radius-pill)',
          backgroundColor: 'var(--color-bg)',
          color: 'var(--color-text-muted)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 'var(--space-4)',
        }}
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="8" />
          <path d="m21 21-4.3-4.3" strokeLinecap="round" />
        </svg>
      </div>
      <h3 style={{ fontSize: 'var(--text-xl)', fontWeight: 600, color: 'var(--color-text)', margin: '0 0 var(--space-2)' }}>
        {title}
      </h3>
      {description && (
        <p style={{ fontSize: 'var(--text-base)', color: 'var(--color-text-muted)', maxWidth: '420px', margin: actionText ? '0 0 var(--space-5)' : 0 }}>
          {description}
        </p>
      )}
      {actionText && onAction && (
        <button type="button" className="km-btn km-btn-primary" onClick={onAction}>
          {actionText}
        </button>
      )}
    </div>
  );
}

export default EmptyState;
