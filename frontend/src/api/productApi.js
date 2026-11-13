import axiosClient from './axiosClient';

/**
 * API cho G-01 (danh sách sản phẩm) và G-02 (chi tiết sản phẩm).
 */
export const productApi = {
  /**
   * GET /api/products — danh sách sản phẩm với lọc & phân trang.
   * @param {object} [params] categoryId, brand, layout, minPrice, maxPrice, keyword, sort, page, size
   */
  list(params = {}) {
    return axiosClient.get('/products', { params });
  },

  /**
   * GET /api/products/{id} — chi tiết sản phẩm.
   */
  detail(id) {
    return axiosClient.get(`/products/${id}`);
  },
};
