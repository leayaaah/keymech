import axiosClient from './axiosClient';

/**
 * API cho luồng Custom bàn phím cơ (Customer & Staff).
 */
export const customRequestApi = {
  listComponents() {
    return axiosClient.get('/custom-requests/components');
  },

  create(payload) {
    return axiosClient.post('/custom-requests', payload);
  },

  listMine(page = 0, size = 10) {
    return axiosClient.get('/custom-requests/my', { params: { page, size } });
  },

  getMine(id) {
    return axiosClient.get(`/custom-requests/my/${encodeURIComponent(id)}`);
  },

  acceptQuote(id, { quotedPrice }) {
    return axiosClient.put(`/custom-requests/my/${encodeURIComponent(id)}/accept`, { quotedPrice });
  },

  decline(id, { note } = {}) {
    return axiosClient.put(`/custom-requests/my/${encodeURIComponent(id)}/decline`, { note });
  },

  listForStaff(params = {}) {
    return axiosClient.get('/staff/custom-requests', { params });
  },

  getForStaff(id) {
    return axiosClient.get(`/staff/custom-requests/${encodeURIComponent(id)}`);
  },

  sendQuote(id, { quotedPrice, quoteNote }) {
    return axiosClient.put(`/staff/custom-requests/${encodeURIComponent(id)}/quote`, {
      quotedPrice,
      quoteNote,
    });
  },

  updateStatusForStaff(id, { status, note }) {
    return axiosClient.put(`/staff/custom-requests/${encodeURIComponent(id)}/status`, {
      status,
      note,
    });
  },
};

export default customRequestApi;
