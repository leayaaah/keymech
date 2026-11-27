import axiosClient from './axiosClient';

function buildProductFormData(payload, imageFile) {
  const formData = new FormData();
  Object.entries(payload).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      formData.append(key, String(value));
    }
  });
  if (imageFile) {
    formData.append('image', imageFile);
  }
  return formData;
}

/**
 * API quản lý sản phẩm dành cho Staff và Admin (A-01, A-02).
 */
export const staffProductApi = {
  list(params = {}) {
    return axiosClient.get('/staff/products', { params });
  },

  detail(id) {
    return axiosClient.get(`/staff/products/${encodeURIComponent(id)}`);
  },

  create(payload, imageFile = null) {
    return axiosClient.post('/staff/products', buildProductFormData(payload, imageFile));
  },

  update(id, payload, imageFile = null) {
    // Form sửa KHÔNG gửi tồn kho — tránh ghi đè giá trị khách vừa checkout (xem UpdateProductInput).
    const { stockQuantity: _stockQuantity, ...rest } = payload;
    return axiosClient.put(
      `/staff/products/${encodeURIComponent(id)}`,
      buildProductFormData(rest, imageFile)
    );
  },

  /** A-01: điều chỉnh tồn kho — thao tác chủ đích, riêng biệt với form sửa. */
  updateStock(id, stockQuantity) {
    return axiosClient.patch(`/staff/products/${encodeURIComponent(id)}/stock`, { stockQuantity });
  },

  toggleVisibility(id, status) {
    return axiosClient.put(`/admin/products/${encodeURIComponent(id)}/visibility`, { status });
  },
};

export default staffProductApi;
