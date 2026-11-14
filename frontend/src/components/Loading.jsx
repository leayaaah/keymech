/**
 * Thành phần hiển thị trạng thái đang tải (Loading Spinner).
 *
 * @param {Object} props
 * @param {boolean} [props.fullPage=false] Hiển thị toàn màn hình hay trong khung chứa
 * @param {string} [props.message='Đang tải dữ liệu...'] Thông điệp kèm theo
 */
export function Loading({ fullPage = false, message = 'Đang tải dữ liệu...' }) {
  const content = (
    <div
      role="status"
      aria-live="polite"
      className="loading-container"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '2rem',
        minHeight: fullPage ? '100vh' : '200px',
        color: '#4b5563',
      }}
    >
      <div
        className="spinner"
        style={{
          width: '40px',
          height: '40px',
          border: '4px solid #e5e7eb',
          borderTop: '4px solid #2563eb',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      {message && (
        <p style={{ marginTop: '1rem', fontSize: '0.95rem', fontWeight: 500 }}>
          {message}
        </p>
      )}
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );

  if (fullPage) {
    return (
      <div
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(255, 255, 255, 0.85)',
          zIndex: 9999,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {content}
      </div>
    );
  }

  return content;
}

export default Loading;
