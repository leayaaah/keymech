// TODO(backend): Xóa mock khi controller /api/staff/products/** hoàn tất.

let nextProductId = 100;

const mockProducts = [
  {
    id: 1,
    name: 'Keychron K2 Pro',
    brand: 'Keychron',
    categoryId: 1,
    categoryName: 'Bàn phím cơ',
    layout: '75%',
    switchType: 'Linear',
    price: 2200000,
    stockQuantity: 18,
    status: 'ACTIVE',
    description: 'Bàn phím cơ không dây 75% hỗ trợ QMK/VIA.',
    specs: 'Bluetooth 5.1 / USB-C, 84 phím, Hot-swap',
    imageUrl: '',
  },
  {
    id: 2,
    name: 'Akko 5075B Plus',
    brand: 'Akko',
    categoryId: 1,
    categoryName: 'Bàn phím cơ',
    layout: '75%',
    switchType: 'Tactile',
    price: 1890000,
    stockQuantity: 12,
    status: 'ACTIVE',
    description: 'Gasket mount 75% kèm núm xoay.',
    specs: '3 mode kết nối, RGB, Hot-swap 5 pin',
    imageUrl: '',
  },
  {
    id: 3,
    name: 'Leopold FC750R',
    brand: 'Leopold',
    categoryId: 1,
    categoryName: 'Bàn phím cơ',
    layout: 'TKL',
    switchType: 'Linear',
    price: 3250000,
    stockQuantity: 0,
    status: 'HIDDEN',
    description: 'Phím cơ TKL build chắc chắn.',
    specs: 'USB-C, PBT Double-shot',
    imageUrl: '',
  },
];

export const staffProductMock = {
  list(params = {}) {
    const kw = (params.keyword || '').toLowerCase();
    const filtered = mockProducts.filter(
      (p) => !kw || p.name.toLowerCase().includes(kw) || p.brand.toLowerCase().includes(kw)
    );
    return Promise.resolve({
      content: [...filtered],
      page: 0,
      size: 12,
      totalElements: filtered.length,
      totalPages: 1,
    });
  },
  detail(id) {
    const found = mockProducts.find((p) => String(p.id) === String(id));
    if (!found) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy sản phẩm.' } } });
    }
    return Promise.resolve({ ...found });
  },
  create(payload) {
    const item = { id: nextProductId++, status: 'ACTIVE', ...payload };
    mockProducts.unshift(item);
    return Promise.resolve(item);
  },
  update(id, payload) {
    const idx = mockProducts.findIndex((p) => String(p.id) === String(id));
    if (idx === -1) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy sản phẩm.' } } });
    }
    mockProducts[idx] = { ...mockProducts[idx], ...payload };
    return Promise.resolve({ ...mockProducts[idx] });
  },
  toggleVisibility(id, status) {
    const idx = mockProducts.findIndex((p) => String(p.id) === String(id));
    if (idx === -1) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy sản phẩm.' } } });
    }
    mockProducts[idx].status = status;
    return Promise.resolve({ ...mockProducts[idx] });
  },
};
