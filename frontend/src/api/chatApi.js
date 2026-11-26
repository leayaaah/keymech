import axiosClient from './axiosClient';

/**
 * API Chatbot tư vấn sản phẩm KeyMech (G-04).
 */
export const chatApi = {
  sendMessage(message) {
    return axiosClient.post('/chat', { message });
  },
  getHistory() {
    return axiosClient.get('/chat/history');
  },
};

export default chatApi;
