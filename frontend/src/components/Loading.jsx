/**
 * Thành phần hiển thị trạng thái đang tải (Loading Spinner).
 */
export function Loading({ fullPage = false, message = 'Đang tải dữ liệu...' }) {
  const content = (
    <div
      role="status"
      aria-live="polite"
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 'var(--space-8)',
        minHeight: fullPage ? '100vh' : '200px',
        color: 'var(--color-text-muted)',
      }}
    >
      <div
        style={{
          width: '36px',
          height: '36px',
          border: '3px solid var(--color-border)',
          borderTopColor: 'var(--color-primary)',
          borderRadius: 'var(--radius-pill)',
          animation: 'km-spin 0.8s linear infinite',
        }}
      />
      {message && (
        <p style={{ marginTop: 'var(--space-4)', fontSize: 'var(--text-base)', fontWeight: 500 }}>
          {message}
        </p>
      )}
      <style>{`
        @keyframes km-spin {
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
          inset: 0,
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
