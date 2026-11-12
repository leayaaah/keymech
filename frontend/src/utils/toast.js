/**
 * Quản lý thông báo Toast đơn giản, không phụ thuộc thư viện ngoài.
 */

let listeners = [];
let nextId = 1;

export const toast = {
  /**
   * Đăng ký lắng nghe sự kiện thêm toast
   * @param {Function} listener
   */
  subscribe(listener) {
    listeners.push(listener);
    return () => {
      listeners = listeners.filter((l) => l !== listener);
    };
  },

  /**
   * Phát thông báo
   * @param {Object} item
   * @param {'success'|'error'|'warning'|'info'} item.type
   * @param {string} item.message
   * @param {number} [item.duration] Thời gian hiển thị (ms), mặc định 3500ms
   */
  show(item) {
    const toastItem = {
      id: nextId++,
      type: item.type || 'info',
      message: item.message || '',
      duration: item.duration || 3500,
    };
    listeners.forEach((listener) => listener(toastItem));
  },

  success(message, duration) {
    this.show({ type: 'success', message, duration });
  },

  error(message, duration) {
    this.show({ type: 'error', message, duration });
  },

  warning(message, duration) {
    this.show({ type: 'warning', message, duration });
  },

  info(message, duration) {
    this.show({ type: 'info', message, duration });
  },
};

export default toast;
