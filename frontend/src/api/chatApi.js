// API Chatbot (G-04). Backend do Thùy làm; cấu trúc dưới đây theo mục 6 của kế hoạch.
import http from './http';

/** POST /chat: { message } -> { answer, products[], suggestions[], mode: 'ai' | 'rule' } */
export const sendChatMessage = (message) => http.post('/chat', { message }).then((r) => r.data);

/** GET /chat/history: tối đa 20 tin gần nhất trong Session. */
export const getChatHistory = () => http.get('/chat/history').then((r) => r.data);
