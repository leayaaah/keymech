import { useEffect, useState } from 'react';
import { toast } from '../utils/toast';
import '../styles/customer.css';

/**
 * Thành phần hiển thị Toast notification toàn cục (Figma frame 3:4540).
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

  return (
    <div className="km-toast-stack" aria-live="polite" aria-label="Thông báo hệ thống">
      {toasts.map((item) => (
        <div key={item.id} role="alert" className={`km-toast km-toast--${item.type || 'info'}`}>
          <div style={{ flex: 1, wordBreak: 'break-word' }}>{item.message}</div>
          <button
            type="button"
            className="km-toast-close"
            onClick={() => removeToast(item.id)}
            aria-label="Đóng thông báo"
          >
            ×
          </button>
        </div>
      ))}
    </div>
  );
}

export default Toast;
