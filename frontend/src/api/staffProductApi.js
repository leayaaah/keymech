import axiosClient from './axiosClient';
import { staffProductMock } from './mock/staffProductMock';

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

/**
 * API quản lý sản phẩm dành cho Staff & Admin (A-01..A-03).
 * TODO(backend): Backend chưa có controller /api/staff/products/** và /api/admin/products/{id}/visibility.
 * Mock chỉ kích hoạt khi VITE_USE_MOCK=true (mặc định false).
 */
export const staffProductApi = {
  // TODO(backend): GET /api/staff/products
  list(params = {}) {
    if (USE_MOCK) return staffProductMock.list(params);
    return axiosClient.get('/staff/products', { params });
  },

  // TODO(backend): GET /api/staff/products/{id}
  detail(id) {
    if (USE_MOCK) return staffProductMock.detail(id);
    return axiosClient.get(`/staff/products/${encodeURIComponent(id)}`);
  },

  // TODO(backend): POST /api/staff/products
  create(payload) {
    if (USE_MOCK) return staffProductMock.create(payload);
    return axiosClient.post('/staff/products', payload);
  },

  // TODO(backend): PUT /api/staff/products/{id}
  update(id, payload) {
    if (USE_MOCK) return staffProductMock.update(id, payload);
    return axiosClient.put(`/staff/products/${encodeURIComponent(id)}`, payload);
  },

  // TODO(backend): POST /api/staff/products/{id}/image (multipart/form-data)
  uploadImage(id, file) {
    if (USE_MOCK) return Promise.resolve({ imageUrl: '' });
    const formData = new FormData();
    formData.append('file', file);
    return axiosClient.post(`/staff/products/${encodeURIComponent(id)}/image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // TODO(backend): PUT /api/admin/products/{id}/visibility (Chỉ ADMIN)
  toggleVisibility(id, status) {
    if (USE_MOCK) return staffProductMock.toggleVisibility(id, status);
    return axiosClient.put(`/admin/products/${encodeURIComponent(id)}/visibility`, { status });
  },
};

export default staffProductApi;
