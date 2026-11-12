/**
 * Định dạng số tiền thành chuỗi tiền tệ VND theo quy ước chung của dự án.
 * Ví dụ: 1250000 -> "1.250.000 ₫"
 *
 * @param {number|string} amount Số tiền cần định dạng
 * @returns {string} Chuỗi hiển thị tiền tệ VND
 */
export function formatVND(amount) {
  if (amount === null || amount === undefined || isNaN(Number(amount))) {
    return '0 ₫';
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  }).format(Number(amount));
}

export default formatVND;
