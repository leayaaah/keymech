import axiosClient from './axiosClient';

/**
 * Các API liên quan đến xác thực và thông tin phiên người dùng.
 */
export const authApi = {
  /**
   * Đăng nhập hệ thống
   * @param {Object} credentials
   * @param {string} credentials.email
   * @param {string} credentials.password
   * @returns {Promise<Object>} Thông tin tài khoản trả về từ server
   */
  login(credentials) {
    return axiosClient.post('/auth/login', credentials);
  },

  /**
   * Đăng ký tài khoản khách hàng mới (CUSTOMER)
   * @param {Object} data
   * @param {string} data.email
   * @param {string} data.password
   * @param {string} data.fullName
   * @param {string} [data.phone]
   * @param {string} [data.address]
   * @returns {Promise<Object>}
   */
  register(data) {
    return axiosClient.post('/auth/register', data);
  },

  /**
   * Lấy thông tin tài khoản hiện tại từ Session cookie
   * @returns {Promise<Object>}
   */
  getMe() {
    return axiosClient.get('/auth/me');
  },

  /**
   * Đăng xuất khỏi hệ thống
   * @returns {Promise<void>}
   */
  logout() {
    return axiosClient.post('/auth/logout');
  },

  /**
   * Đổi mật khẩu tài khoản đang đăng nhập
   * @param {Object} passwords
   * @param {string} passwords.oldPassword
   * @param {string} passwords.newPassword
   * @returns {Promise<Object>}
   */
  changePassword(passwords) {
    return axiosClient.put('/users/me/password', passwords);
  },
};

export default authApi;
