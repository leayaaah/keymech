// TODO(backend): Xóa mock khi controller /api/custom-requests/** và /api/staff/custom-requests/** hoàn tất.

let nextId = 3;

const mockRequests = [
  {
    id: 1,
    requestCode: 'CUS-2026031801',
    customerName: 'Nguyễn Minh Anh',
    customerEmail: 'minhanh@example.com',
    customerPhone: '0901234567',
    layout: '75%',
    switchType: 'Linear',
    switchName: 'Gateron Milky Yellow Pro',
    caseMaterial: 'Nhôm',
    keycapSet: 'PBT Cherry profile • Trắng / xanh navy',
    ledType: 'RGB',
    lubeSwitch: true,
    hotSwap: true,
    budget: 2500000,
    note: 'Ưu tiên tiếng gõ trầm, phù hợp văn phòng.',
    status: 'QUOTED',
    quotedPrice: 2650000,
    quoteNote: 'Đã bao gồm linh kiện, lube switch và công lắp ráp. Dự kiến hoàn thành 5–7 ngày.',
    createdAt: '2026-03-17T18:35:00',
    history: [
      {
        id: 2,
        fromStatus: 'PENDING',
        toStatus: 'QUOTED',
        actorName: 'Nhân viên Hoàng Nam',
        note: 'Đã chọn linh kiện, bảo hành 12 tháng.',
        createdAt: '2026-03-18T09:10:00',
      },
      {
        id: 1,
        fromStatus: null,
        toStatus: 'PENDING',
        actorName: 'Nguyễn Minh Anh',
        note: 'Đã gửi yêu cầu Custom.',
        createdAt: '2026-03-17T18:35:00',
      },
    ],
  },
  {
    id: 2,
    requestCode: 'CUS-2026031902',
    customerName: 'Nguyễn Minh Anh',
    customerEmail: 'minhanh@example.com',
    customerPhone: '0901234567',
    layout: '65%',
    switchType: 'Tactile',
    switchName: 'Boba U4T',
    caseMaterial: 'Nhựa',
    keycapSet: 'GMK Clone Minimal',
    ledType: 'Không',
    lubeSwitch: false,
    hotSwap: true,
    budget: null,
    note: 'Gõ phản hồi rõ, nhỏ gọn mang đi làm.',
    status: 'PENDING',
    quotedPrice: null,
    quoteNote: null,
    createdAt: '2026-03-19T10:00:00',
    history: [
      {
        id: 3,
        fromStatus: null,
        toStatus: 'PENDING',
        actorName: 'Nguyễn Minh Anh',
        note: 'Đã gửi yêu cầu Custom.',
        createdAt: '2026-03-19T10:00:00',
      },
    ],
  },
];

export const customMock = {
  listMine() {
    return Promise.resolve({
      content: [...mockRequests],
      page: 0,
      size: 10,
      totalElements: mockRequests.length,
      totalPages: 1,
    });
  },
  getById(id) {
    const found = mockRequests.find((r) => String(r.id) === String(id));
    if (!found) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy yêu cầu Custom.' } } });
    }
    return Promise.resolve({ ...found, history: [...found.history] });
  },
  create(payload) {
    const item = {
      id: nextId++,
      requestCode: `CUS-2026032${nextId}`,
      customerName: 'Khách hàng',
      customerEmail: 'customer@example.com',
      customerPhone: '',
      ...payload,
      status: 'PENDING',
      quotedPrice: null,
      quoteNote: null,
      createdAt: new Date().toISOString(),
      history: [
        {
          id: Date.now(),
          fromStatus: null,
          toStatus: 'PENDING',
          actorName: 'Khách hàng',
          note: 'Đã gửi yêu cầu Custom.',
          createdAt: new Date().toISOString(),
        },
      ],
    };
    mockRequests.unshift(item);
    return Promise.resolve(item);
  },
  acceptQuote(id, { quotedPrice }) {
    const found = mockRequests.find((r) => String(r.id) === String(id));
    if (!found) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy yêu cầu Custom.' } } });
    }
    if (Number(quotedPrice) !== Number(found.quotedPrice)) {
      return Promise.reject({
        response: {
          status: 409,
          data: { error: 'QUOTE_CHANGED', message: 'Báo giá đã thay đổi. Vui lòng tải lại báo giá mới nhất.' },
        },
      });
    }
    found.status = 'ACCEPTED';
    found.history.unshift({
      id: Date.now(),
      fromStatus: 'QUOTED',
      toStatus: 'ACCEPTED',
      actorName: found.customerName,
      note: 'Khách đồng ý báo giá.',
      createdAt: new Date().toISOString(),
    });
    return Promise.resolve({ ...found });
  },
  cancel(id, { note } = {}) {
    const found = mockRequests.find((r) => String(r.id) === String(id));
    if (!found) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy yêu cầu Custom.' } } });
    }
    const prev = found.status;
    found.status = 'CANCELLED';
    found.history.unshift({
      id: Date.now(),
      fromStatus: prev,
      toStatus: 'CANCELLED',
      actorName: found.customerName,
      note: note || 'Khách hủy yêu cầu.',
      createdAt: new Date().toISOString(),
    });
    return Promise.resolve({ ...found });
  },
  listForStaff(params = {}) {
    const filtered = params.status
      ? mockRequests.filter((r) => r.status === params.status)
      : mockRequests;
    return Promise.resolve({
      content: [...filtered],
      page: 0,
      size: 10,
      totalElements: filtered.length,
      totalPages: 1,
    });
  },
  sendQuote(id, { quotedPrice, quoteNote }) {
    const found = mockRequests.find((r) => String(r.id) === String(id));
    if (!found) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy yêu cầu Custom.' } } });
    }
    const prev = found.status;
    found.status = 'QUOTED';
    found.quotedPrice = Number(quotedPrice);
    found.quoteNote = quoteNote || '';
    found.history.unshift({
      id: Date.now(),
      fromStatus: prev,
      toStatus: 'QUOTED',
      actorName: 'Nhân viên',
      note: quoteNote || 'Đã gửi báo giá.',
      createdAt: new Date().toISOString(),
    });
    return Promise.resolve({ ...found });
  },
  updateStatusForStaff(id, { status, note }) {
    const found = mockRequests.find((r) => String(r.id) === String(id));
    if (!found) {
      return Promise.reject({ response: { status: 404, data: { message: 'Không tìm thấy yêu cầu Custom.' } } });
    }
    const prev = found.status;
    found.status = status;
    found.history.unshift({
      id: Date.now(),
      fromStatus: prev,
      toStatus: status,
      actorName: 'Nhân viên',
      note: note || '',
      createdAt: new Date().toISOString(),
    });
    return Promise.resolve({ ...found });
  },
};
