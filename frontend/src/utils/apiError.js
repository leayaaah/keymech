export function getFeatureErrorMessage(err, fallback = 'Đã có lỗi xảy ra. Vui lòng thử lại.') {
  return err?.response?.data?.message || fallback;
}
