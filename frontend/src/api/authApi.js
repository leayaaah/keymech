import axiosClient from './axiosClient';

/**
 * Các API liên quan đến xác thực và thông tin phiên người dùng.
 */
export const authApi = {
  /**
   * Đăng nhập hệ thống
   */
  login(credentials) {
    return axiosClient.post('/auth/login', credentials);
  },

  /**
   * Đăng ký tài khoản khách hàng mới (CUSTOMER)
   */
  register(data) {
    return axiosClient.post('/auth/register', data);
  },

  /**
   * Lấy thông tin tài khoản hiện tại từ Session cookie
   */
  getMe() {
    return axiosClient.get('/auth/me');
  },

  /**
   * Đăng xuất khỏi hệ thống
   */
  logout() {
    return axiosClient.post('/auth/logout');
  },

  /**
   * Đổi mật khẩu tài khoản đang đăng nhập (chỉ Customer)
   */
  changePassword(passwords) {
    return axiosClient.put('/users/me/password', passwords);
  },

  /**
   * Yêu cầu mã xác thực quên mật khẩu (6 chữ số, hiệu lực 10 phút)
   */
  forgotPassword({ email }) {
    return axiosClient.post('/auth/forgot-password', { email });
  },

  /**
   * Đặt lại mật khẩu bằng mã xác thực
   */
  resetPassword({ email, code, newPassword }) {
    return axiosClient.post('/auth/reset-password', { email, code, newPassword });
  },
};

export default authApi;
