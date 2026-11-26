import axiosClient from './axiosClient';

/**
 * API quản lý hồ sơ cá nhân của Customer (C-05).
 */
export const userApi = {
  updateProfile({ fullName, phone, address }) {
    return axiosClient.put('/users/me', { fullName, phone, address });
  },
};

export default userApi;
