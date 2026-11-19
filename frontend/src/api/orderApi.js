import axiosClient from './axiosClient';

export const orderApi = {
  checkout(data) {
    return axiosClient.post('/orders/checkout', data);
  },
  listMine(page) {
    return axiosClient.get('/orders/my', { params: { page, size: 10 } });
  },
  getMine(id) {
    return axiosClient.get(`/orders/my/${encodeURIComponent(id)}`);
  },
};
