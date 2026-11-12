import axios from 'axios';
import { toast } from '../utils/toast';

const baseURL = import.meta.env.VITE_API_URL || '/api';

const axiosClient = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
axiosClient.interceptors.request.use(
  (config) => {
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
axiosClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response ? error.response.status : null;
    const data = error.response ? error.response.data : null;
    const configUrl = error.config ? error.config.url || '' : '';

    const serverMessage =
      data && data.message ? data.message : 'Đã có lỗi xảy ra. Vui lòng thử lại.';

    switch (status) {
      case 401: {
        // Không tự động chuyển hướng nếu lời gọi là kiểm tra phiên /auth/me hoặc đang thực hiện /auth/login
        const isAuthCheck =
          configUrl.includes('/auth/me') || configUrl.includes('/auth/login');
        if (!isAuthCheck) {
          const currentPath = window.location.pathname;
          if (currentPath !== '/login') {
            toast.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
          }
        }
        break;
      }
      case 403: {
        toast.error(serverMessage || 'Bạn không có quyền thực hiện thao tác này.');
        break;
      }
      case 409: {
        toast.error(serverMessage);
        break;
      }
      case 429: {
        toast.error(serverMessage || 'Bạn thao tác quá nhanh, vui lòng chờ trong giây lát.');
        break;
      }
      default: {
        // Các mã lỗi khác sẽ do component gọi API bắt nếu cần xử lý riêng
        break;
      }
    }

    return Promise.reject(error);
  }
);

export default axiosClient;
