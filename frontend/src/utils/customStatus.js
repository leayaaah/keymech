// Hằng số và validate cho yêu cầu Custom. Quy tắc khớp CreateCustomRequest ở backend (mục 9 kế hoạch).

export const STATUS_LABEL = {
  PENDING: 'Chờ tiếp nhận',
  QUOTED: 'Đã báo giá',
  ACCEPTED: 'Khách đã đồng ý',
  IN_PROGRESS: 'Đang lắp ráp',
  COMPLETED: 'Hoàn thành',
  REJECTED: 'Từ chối',
  CANCELLED: 'Đã hủy',
};

// Nhóm màu hiển thị: không chỉ dựa vào màu, nhãn chữ luôn có.
export const STATUS_TONE = {
  PENDING: 'neutral',
  QUOTED: 'info',
  ACCEPTED: 'info',
  IN_PROGRESS: 'info',
  COMPLETED: 'success',
  REJECTED: 'danger',
  CANCELLED: 'muted',
};

export const STATUS_OPTIONS = Object.keys(STATUS_LABEL);

export const LAYOUTS = ['60%', '65%', '75%', 'TKL', 'Fullsize'];
export const SWITCH_KINDS = ['Linear', 'Tactile', 'Clicky'];
export const CASE_MATERIALS = ['Nhựa', 'Nhôm', 'Gỗ', 'Acrylic'];
export const RGB_OPTIONS = ['Không', 'RGB', 'Đơn sắc'];

export const MIN_BUDGET = 500000;
export const LIMITS = { switchType: 80, keycapSet: 120, note: 1000, declineNote: 500, statusNote: 500 };

export const EMPTY_FORM = {
  layout: '',
  switchKind: '',
  switchName: '',
  caseMaterial: '',
  keycapSet: '',
  lubing: false,
  hotSwap: true,
  rgbOption: '',
  budget: '',
  note: '',
};

/** Ghép loại và tên switch thành một chuỗi, ví dụ "Linear - Gateron Yellow" (tối đa 80 ký tự). */
export function joinSwitch(kind, name) {
  const n = (name || '').trim();
  return n ? `${kind} - ${n}` : kind;
}

/** Trả về object lỗi theo tên trường; rỗng nghĩa là hợp lệ. Thông báo tiếng Việt. */
export function validateCustomForm(f) {
  const errors = {};
  if (!f.layout) errors.layout = 'Vui lòng chọn layout';
  else if (!LAYOUTS.includes(f.layout)) errors.layout = 'Layout không hợp lệ';

  if (!f.switchKind) errors.switchKind = 'Vui lòng chọn loại switch';
  else if (joinSwitch(f.switchKind, f.switchName).length > LIMITS.switchType) {
    errors.switchName = `Loại và tên switch tối đa ${LIMITS.switchType} ký tự`;
  }

  if (f.caseMaterial && !CASE_MATERIALS.includes(f.caseMaterial)) errors.caseMaterial = 'Chất liệu case không hợp lệ';
  if (f.rgbOption && !RGB_OPTIONS.includes(f.rgbOption)) errors.rgbOption = 'Tùy chọn đèn LED không hợp lệ';
  if (f.keycapSet.length > LIMITS.keycapSet) errors.keycapSet = `Bộ keycap tối đa ${LIMITS.keycapSet} ký tự`;
  if (f.note.length > LIMITS.note) errors.note = `Ghi chú tối đa ${LIMITS.note} ký tự`;

  if (String(f.budget).trim() !== '') {
    const b = Number(f.budget);
    if (!Number.isFinite(b)) errors.budget = 'Ngân sách phải là số';
    else if (b < MIN_BUDGET) errors.budget = 'Ngân sách tối thiểu 500.000đ';
  }
  return errors;
}

/** Chuyển state của form thành body gửi API: trường tùy chọn để trống thì gửi null, không gửi chuỗi rỗng. */
export function toRequestBody(f) {
  const orNull = (v) => (v && String(v).trim() !== '' ? String(v).trim() : null);
  return {
    layout: f.layout,
    switchType: joinSwitch(f.switchKind, f.switchName),
    caseMaterial: orNull(f.caseMaterial),
    keycapSet: orNull(f.keycapSet),
    lubing: f.lubing,
    hotSwap: f.hotSwap,
    rgbOption: orNull(f.rgbOption),
    budget: String(f.budget).trim() === '' ? null : Number(f.budget),
    note: orNull(f.note),
  };
}

/** Lấy thông báo lỗi từ phản hồi API (định dạng {status, error, message, timestamp}). */
export function apiErrorMessage(err, fallback = 'Có lỗi xảy ra, vui lòng thử lại') {
  return err?.response?.data?.message || fallback;
}
