import axiosClient from './axiosClient';

/**
 * API quản trị danh mục và tổng quan Admin (A-08, A-09).
 */
export const adminCategoryApi = {
  getDashboard() {
    return axiosClient.get('/admin/dashboard');
  },

  list() {
    return axiosClient.get('/admin/categories');
  },

  create({ name, slug }) {
    return axiosClient.post('/admin/categories', { name, slug });
  },

  toggleVisibility(id, status) {
    return axiosClient.put(`/admin/categories/${encodeURIComponent(id)}/visibility`, { status });
  },
};

export default adminCategoryApi;
