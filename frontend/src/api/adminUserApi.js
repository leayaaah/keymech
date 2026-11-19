import axiosClient from './axiosClient';

export const adminUserApi = {
  list(params) {
    return axiosClient.get('/admin/users', { params });
  },
  createStaff(data) {
    return axiosClient.post('/admin/users', data);
  },
  update(id, params) {
    return axiosClient.put(`/admin/users/${id}`, null, { params });
  },
  toggleVisibility(id) {
    return axiosClient.put(`/admin/users/${id}/visibility`);
  },
};
