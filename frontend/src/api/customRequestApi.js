// API Custom (C-03, C-04, C-06, A-07). Mọi lời gọi đi qua instance Axios dùng chung của dự án.
// Giả định: `./http` export default instance Axios có baseURL = VITE_API_URL ("/api") và
// withCredentials = true (cookie JSESSIONID). Nếu file của Trí đặt tên khác, chỉ cần sửa dòng import này.
import http from './http';

const data = (res) => res.data;

// ----- Customer -----
export const createCustomRequest = (body) => http.post('/custom-requests', body).then(data);

export const listMyCustomRequests = (page = 0, size = 10) =>
  http.get('/custom-requests/my', { params: { page, size } }).then(data);

export const getMyCustomRequest = (id) => http.get(`/custom-requests/my/${id}`).then(data);

export const acceptQuote = (id, quotedPrice) =>
  http.put(`/custom-requests/my/${id}/accept`, { quotedPrice }).then(data);

export const declineCustomRequest = (id, note) =>
  http.put(`/custom-requests/my/${id}/decline`, { note: note?.trim() || null }).then(data);

// ----- Staff, Admin -----
export const listStaffCustomRequests = ({ status, page = 0, size = 10 } = {}) =>
  http.get('/staff/custom-requests', { params: { status: status || undefined, page, size } }).then(data);

export const getStaffCustomRequest = (id) => http.get(`/staff/custom-requests/${id}`).then(data);

export const quoteCustomRequest = (id, quotedPrice) =>
  http.put(`/staff/custom-requests/${id}/quote`, { quotedPrice }).then(data);

export const changeCustomStatus = (id, status, note) =>
  http.put(`/staff/custom-requests/${id}/status`, { status, note: note?.trim() || null }).then(data);
