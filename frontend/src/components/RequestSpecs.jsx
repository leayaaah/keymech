import { formatVND } from '../utils/format';

const yesNo = (v) => (v ? 'Có' : 'Không');

/** Bảng thông số của yêu cầu Custom, dùng chung cho trang khách và trang Staff. */
export default function RequestSpecs({ request: r }) {
  const rows = [
    ['Layout', r.layout],
    ['Switch', r.switchType],
    ['Chất liệu case', r.caseMaterial],
    ['Bộ keycap', r.keycapSet],
    ['Lube switch', yesNo(r.lubing)],
    ['Hot-swap', yesNo(r.hotSwap)],
    ['Đèn LED', r.rgbOption],
    ['Ngân sách', r.budget != null ? formatVND(r.budget) : null],
  ].filter(([, v]) => v != null && v !== '');

  return (
    <dl className="cr-specs">
      {rows.map(([k, v]) => (
        <div key={k} className="cr-specs__row">
          <dt>{k}</dt>
          <dd>{v}</dd>
        </div>
      ))}
      {r.note && (
        <div className="cr-specs__row cr-specs__row--wide">
          <dt>Ghi chú</dt>
          <dd className="cr-prewrap">{r.note}</dd>
        </div>
      )}
    </dl>
  );
}
