/**
 * Thành phần hiển thị khi tải dữ liệu bị lỗi.
 *
 * @param {Object} props
 * @param {string} [props.title='Đã xảy ra lỗi'] Tiêu đề lỗi
 * @param {string} [props.message='Không thể tải dữ liệu từ máy chủ. Vui lòng thử lại sau.'] Chi tiết lỗi
 * @param {Function} [props.onRetry] Hàm gọi lại khi nhấn nút "Thử lại"
 */
export function ErrorState({
  title = 'Đã xảy ra lỗi',
  message = 'Không thể tải dữ liệu từ máy chủ. Vui lòng thử lại sau.',
  onRetry,
}) {
  return (
    <div
      role="alert"
      className="error-state"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '3rem 1.5rem',
        textAlign: 'center',
        backgroundColor: '#fef2f2',
        borderRadius: '8px',
        border: '1px solid #fecaca',
        margin: '1.5rem 0',
      }}
    >
      <div
        aria-hidden="true"
        style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          backgroundColor: '#fee2e2',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1rem',
          fontSize: '24px',
          color: '#dc2626',
        }}
      >
        ⚠️
      </div>
      <h3
        style={{
          fontSize: '1.15rem',
          fontWeight: 600,
          color: '#991b1b',
          marginBottom: '0.5rem',
        }}
      >
        {title}
      </h3>
      <p
        style={{
          fontSize: '0.925rem',
          color: '#b91c1c',
          maxWidth: '440px',
          marginBottom: onRetry ? '1.25rem' : 0,
        }}
      >
        {message}
      </p>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          style={{
            padding: '0.5rem 1.25rem',
            backgroundColor: '#dc2626',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            fontSize: '0.9rem',
            fontWeight: 500,
            cursor: 'pointer',
          }}
        >
          Thử lại
        </button>
      )}
    </div>
  );
}

export default ErrorState;
