/**
 * Tiện ích định dạng ngày giờ dùng chung cho toàn bộ dự án KeyMech.
 */

/**
 * Định dạng chuỗi ngày tháng theo chuẩn Việt Nam (DD/MM/YYYY).
 *
 * @param {string|Date} dateInput Chuỗi ISO hoặc đối tượng Date
 * @returns {string} Chuỗi ngày định dạng DD/MM/YYYY
 */
export function formatDate(dateInput) {
  if (!dateInput) return '';
  const date = new Date(dateInput);
  if (isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
}

/**
 * Định dạng ngày và giờ theo chuẩn Việt Nam (HH:mm DD/MM/YYYY).
 *
 * @param {string|Date} dateInput Chuỗi ISO hoặc đối tượng Date
 * @returns {string} Chuỗi ngày giờ
 */
export function formatDateTime(dateInput) {
  if (!dateInput) return '';
  const date = new Date(dateInput);
  if (isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
}

export default formatDate;
