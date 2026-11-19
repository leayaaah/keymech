import axiosClient from './axiosClient';

export const staffOrderApi = {
  list(params) {
    return axiosClient.get('/staff/orders', { params });
  },
  detail(id) {
    return axiosClient.get(`/staff/orders/${id}`);
  },
  updateQuantity(id, itemId, quantity) {
    return axiosClient.put(`/staff/orders/${id}/items/${itemId}`, { quantity });
  },
  updateStatus(id, status) {
    return axiosClient.put(`/staff/orders/${id}/status`, { status });
  },
  markPaid(id) {
    return axiosClient.put(`/staff/orders/${id}/payment`, {
      paymentStatus: 'PAID',
    });
  },
};
