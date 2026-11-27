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
    return axiosClient.put(
      `/staff/products/${encodeURIComponent(id)}`,
      buildProductFormData(payload, imageFile)
    );
  },

  toggleVisibility(id, status) {
    return axiosClient.put(`/admin/products/${encodeURIComponent(id)}/visibility`, { status });
  },
};

export default staffProductApi;
