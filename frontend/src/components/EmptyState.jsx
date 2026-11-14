/**
 * Thành phần hiển thị khi danh sách hoặc dữ liệu bị trống.
 *
 * @param {Object} props
 * @param {string} [props.title='Không tìm thấy sản phẩm'] Tiêu đề chính
 * @param {string} [props.description] Mô tả thêm chi tiết
 * @param {string} [props.actionText] Nhãn của nút hành động (nếu có)
 * @param {Function} [props.onAction] Callback khi nhấn nút hành động
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
      className="empty-state"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '3rem 1.5rem',
        textAlign: 'center',
        backgroundColor: '#f9fafb',
        borderRadius: '8px',
        border: '1px dashed #d1d5db',
        margin: '1.5rem 0',
      }}
    >
      <div
        aria-hidden="true"
        style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          backgroundColor: '#e5e7eb',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1rem',
          fontSize: '24px',
          color: '#6b7280',
        }}
      >
        🔍
      </div>
      <h3
        style={{
          fontSize: '1.15rem',
          fontWeight: 600,
          color: '#1f2937',
          marginBottom: '0.5rem',
        }}
      >
        {title}
      </h3>
      {description && (
        <p
          style={{
            fontSize: '0.925rem',
            color: '#6b7280',
            maxWidth: '420px',
            marginBottom: actionText ? '1.25rem' : 0,
          }}
        >
          {description}
        </p>
      )}
      {actionText && onAction && (
        <button
          type="button"
          onClick={onAction}
          style={{
            padding: '0.5rem 1.25rem',
            backgroundColor: '#2563eb',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            fontSize: '0.9rem',
            fontWeight: 500,
            cursor: 'pointer',
          }}
        >
          {actionText}
        </button>
      )}
    </div>
  );
}

export default EmptyState;
