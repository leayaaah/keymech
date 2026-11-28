import axiosClient from './axiosClient';

/**
 * API Chatbot tư vấn sản phẩm KeyMech (G-04).
 */
export const chatApi = {
  sendMessage(message, context) {
    return axiosClient.post('/chat', { message, context });
  },
  getHistory() {
    return axiosClient.get('/chat/history');
  },
};

export default chatApi;
