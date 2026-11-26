// TODO(backend): Xóa mock khi controller /api/admin/categories/** và /api/admin/dashboard hoàn tất.

let nextCatId = 6;

const mockCategories = [
  { id: 1, name: 'Bàn phím cơ', slug: 'ban-phim-co', status: 'ACTIVE', activeProductCount: 12 },
  { id: 2, name: 'Switch', slug: 'switch', status: 'ACTIVE', activeProductCount: 8 },
  { id: 3, name: 'Keycap', slug: 'keycap', status: 'ACTIVE', activeProductCount: 6 },
  { id: 4, name: 'Dụng cụ và phụ kiện', slug: 'dung-cu-phu-kien', status: 'ACTIVE', activeProductCount: 0 },
  { id: 5, name: 'Kit bàn phím', slug: 'kit-ban-phim', status: 'HIDDEN', activeProductCount: 0 },
];

export const adminMock = {
  getDashboard() {
    return Promise.resolve({
      totalOrders: 48,
      pendingOrders: 6,
      totalProducts: 32,
      pendingCustomRequests: 4,
      totalUsers: 125,
    });
  },
  listCategories() {
    return Promise.resolve([...mockCategories]);
  },
  createCategory({ name, slug }) {
    if (
      mockCategories.some(
        (c) => c.name.toLowerCase() === name.toLowerCase() || c.slug.toLowerCase() === slug.toLowerCase()
      )
    ) {
      return Promise.reject({
        response: { status: 409, data: { message: 'Tên hoặc slug danh mục đã tồn tại.' } },
      });
    }
    const created = { id: nextCatId++, name, slug, status: 'ACTIVE', activeProductCount: 0 };
    mockCategories.push(created);
    return Promise.resolve(created);
  },
  updateCategory(id, { name, slug }) {
    const idx = mockCategories.findIndex((c) => String(c.id) === String(id));
    if (idx === -1) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy danh mục.' } } });
    }
    mockCategories[idx] = { ...mockCategories[idx], name, slug };
    return Promise.resolve({ ...mockCategories[idx] });
  },
  toggleCategoryVisibility(id, status) {
    const idx = mockCategories.findIndex((c) => String(c.id) === String(id));
    if (idx === -1) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy danh mục.' } } });
    }
    if (status === 'HIDDEN' && mockCategories[idx].activeProductCount > 0) {
      return Promise.reject({
        response: {
          status: 409,
          data: {
            error: 'CATEGORY_HAS_ACTIVE_PRODUCTS',
            message: `Không thể ẩn danh mục vì vẫn còn ${mockCategories[idx].activeProductCount} sản phẩm đang hiển thị.`,
          },
        },
      });
    }
    mockCategories[idx].status = status;
    return Promise.resolve({ ...mockCategories[idx] });
  },
};
