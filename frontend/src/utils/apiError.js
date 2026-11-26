const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

/**
 * Chuẩn hóa thông báo lỗi cho các màn hình phụ thuộc endpoint backend chưa hoàn thiện.
 * Khi VITE_USE_MOCK !== 'true' và server trả 404/405 -> thông báo rõ ràng thay vì màn trống.
 */
export function getFeatureErrorMessage(err, fallback = 'Đã có lỗi xảy ra. Vui lòng thử lại.') {
  const status = err?.response?.status;
  if (!USE_MOCK && (status === 404 || status === 405)) {
    return 'Chức năng này chưa có trên máy chủ';
  }
  return err?.response?.data?.message || fallback;
}
