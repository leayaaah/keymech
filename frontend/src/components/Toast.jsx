import { useEffect, useState } from 'react';
import { toast } from '../utils/toast';

/**
 * Thành phần hiển thị Toast notification toàn cục.
 */
export function Toast() {
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    const unsubscribe = toast.subscribe((newToast) => {
      setToasts((prev) => [...prev, newToast]);

      if (newToast.duration && newToast.duration > 0) {
        setTimeout(() => {
          setToasts((prev) => prev.filter((item) => item.id !== newToast.id));
        }, newToast.duration);
      }
    });

    return unsubscribe;
  }, []);

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((item) => item.id !== id));
  };

  if (toasts.length === 0) return null;

  const getTypeStyle = (type) => {
    switch (type) {
      case 'success':
        return {
          bg: '#ecfdf5',
          border: '#10b981',
          text: '#065f46',
          icon: '✅',
        };
      case 'error':
        return {
          bg: '#fef2f2',
          border: '#ef4444',
          text: '#991b1b',
          icon: '❌',
        };
      case 'warning':
        return {
          bg: '#fffbeb',
          border: '#f59e0b',
          text: '#92400e',
          icon: '⚠️',
        };
      default:
        return {
          bg: '#eff6ff',
          border: '#3b82f6',
          text: '#1e40af',
          icon: 'ℹ️',
        };
    }
  };

  return (
    <div
      aria-live="polite"
      aria-label="Thông báo hệ thống"
      style={{
        position: 'fixed',
        top: '20px',
        right: '20px',
        zIndex: 10000,
        display: 'flex',
        flexDirection: 'column',
        gap: '10px',
        maxWidth: '380px',
        width: 'calc(100% - 40px)',
        pointerEvents: 'none',
      }}
    >
      {toasts.map((item) => {
        const style = getTypeStyle(item.type);
        return (
          <div
            key={item.id}
            role="alert"
            style={{
              pointerEvents: 'auto',
              display: 'flex',
              alignItems: 'flex-start',
              gap: '10px',
              padding: '12px 16px',
              backgroundColor: style.bg,
              borderLeft: `4px solid ${style.border}`,
              borderRadius: '6px',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
              color: style.text,
              fontSize: '0.9rem',
              lineHeight: 1.4,
            }}
          >
            <span aria-hidden="true" style={{ fontSize: '1.1rem' }}>
              {style.icon}
            </span>
            <div style={{ flex: 1, wordBreak: 'break-word' }}>{item.message}</div>
            <button
              type="button"
              onClick={() => removeToast(item.id)}
              aria-label="Đóng thông báo"
              style={{
                background: 'none',
                border: 'none',
                color: style.text,
                fontSize: '1.1rem',
                cursor: 'pointer',
                padding: '0 4px',
                lineHeight: 1,
              }}
            >
              ×
            </button>
          </div>
        );
      })}
    </div>
  );
}

export default Toast;
