import axiosClient from './axiosClient';

/**
 * API cho G-03 (giỏ hàng Session-based).
 */
export const cartApi = {
  /** GET /api/cart */
  get() {
    return axiosClient.get('/cart');
  },

  /** POST /api/cart/items */
  addItem({ productId, quantity }) {
    return axiosClient.post('/cart/items', { productId, quantity });
  },

  /** PUT /api/cart/items/{productId} */
  updateItem(productId, { quantity }) {
    return axiosClient.put(`/cart/items/${productId}`, { quantity });
  },

  /** DELETE /api/cart/items/{productId} */
  removeItem(productId) {
    return axiosClient.delete(`/cart/items/${productId}`);
  },

  /** DELETE /api/cart */
  clear() {
    return axiosClient.delete('/cart');
  },
};
