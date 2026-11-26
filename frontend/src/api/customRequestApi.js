import axiosClient from './axiosClient';
import { customMock } from './mock/customMock';

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true';

/**
 * API cho luồng Custom bàn phím cơ (Customer & Staff).
 * TODO(backend): Backend chưa có controller /api/custom-requests/** và /api/staff/custom-requests/**.
 * Mock chỉ kích hoạt khi VITE_USE_MOCK=true (mặc định false).
 */
export const customRequestApi = {
  // TODO(backend): POST /api/custom-requests
  create(payload) {
    if (USE_MOCK) return customMock.create(payload);
    return axiosClient.post('/custom-requests', payload);
  },

  // TODO(backend): GET /api/custom-requests/my
  listMine(page = 0, size = 10) {
    if (USE_MOCK) return customMock.listMine();
    return axiosClient.get('/custom-requests/my', { params: { page, size } });
  },

  // TODO(backend): GET /api/custom-requests/my/{id}
  getMine(id) {
    if (USE_MOCK) return customMock.getById(id);
    return axiosClient.get(`/custom-requests/my/${encodeURIComponent(id)}`);
  },

  // TODO(backend): PUT /api/custom-requests/my/{id}/accept (kèm quotedPrice)
  acceptQuote(id, { quotedPrice }) {
    if (USE_MOCK) return customMock.acceptQuote(id, { quotedPrice });
    return axiosClient.put(`/custom-requests/my/${encodeURIComponent(id)}/accept`, { quotedPrice });
  },

  // TODO(backend): PUT /api/custom-requests/my/{id}/decline (dùng cho cả hủy PENDING và từ chối báo giá QUOTED -> CANCELLED)
  decline(id, { note } = {}) {
    if (USE_MOCK) return customMock.decline(id, { note });
    return axiosClient.put(`/custom-requests/my/${encodeURIComponent(id)}/decline`, { note });
  },

  // TODO(backend): GET /api/staff/custom-requests
  listForStaff(params = {}) {
    if (USE_MOCK) return customMock.listForStaff(params);
    return axiosClient.get('/staff/custom-requests', { params });
  },

  // TODO(backend): not in plan section 6, needs team approval
  getForStaff(id) {
    if (USE_MOCK) return customMock.getById(id);
    return axiosClient.get(`/staff/custom-requests/${encodeURIComponent(id)}`);
  },

  // TODO(backend): PUT /api/staff/custom-requests/{id}/quote
  sendQuote(id, { quotedPrice, quoteNote }) {
    if (USE_MOCK) return customMock.sendQuote(id, { quotedPrice, quoteNote });
    return axiosClient.put(`/staff/custom-requests/${encodeURIComponent(id)}/quote`, {
      quotedPrice,
      quoteNote,
    });
  },

  // TODO(backend): PUT /api/staff/custom-requests/{id}/status
  updateStatusForStaff(id, { status, note }) {
    if (USE_MOCK) return customMock.updateStatusForStaff(id, { status, note });
    return axiosClient.put(`/staff/custom-requests/${encodeURIComponent(id)}/status`, {
      status,
      note,
    });
  },
};

export default customRequestApi;
