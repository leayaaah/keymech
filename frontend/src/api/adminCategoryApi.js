import axiosClient from './axiosClient';
import { adminMock } from './mock/adminMock';

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

/**
 * API quản trị danh mục và tổng quan Admin (A-07).
 * TODO(backend): Backend chưa có controller /api/admin/categories/** và /api/admin/dashboard.
 * Mock chỉ kích hoạt khi VITE_USE_MOCK=true (mặc định false).
 */
export const adminCategoryApi = {
  // TODO(backend): GET /api/admin/dashboard
  getDashboard() {
    if (USE_MOCK) return adminMock.getDashboard();
    return axiosClient.get('/admin/dashboard');
  },

  // TODO(backend): GET /api/admin/categories
  list() {
    if (USE_MOCK) return adminMock.listCategories();
    return axiosClient.get('/admin/categories');
  },

  // TODO(backend): POST /api/admin/categories
  create({ name, slug }) {
    if (USE_MOCK) return adminMock.createCategory({ name, slug });
    return axiosClient.post('/admin/categories', { name, slug });
  },

  // TODO(backend): PUT /api/admin/categories/{id}/visibility (409 CATEGORY_NOT_EMPTY nếu còn sản phẩm ACTIVE)
  toggleVisibility(id, status) {
    if (USE_MOCK) return adminMock.toggleCategoryVisibility(id, status);
    return axiosClient.put(`/admin/categories/${encodeURIComponent(id)}/visibility`, { status });
  },
};

export default adminCategoryApi;
