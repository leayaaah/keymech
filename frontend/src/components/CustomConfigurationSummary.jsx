import formatVND from '../utils/formatVND';

const SLOT_LABELS = { KIT: 'Kit', SWITCH: 'Switch', KEYCAP: 'Keycap', STABILIZER: 'Stabilizer' };

export default function CustomConfigurationSummary({ request }) {
  const parts = request.components ?? [];
  const subtotal = parts.reduce((sum, part) => sum + part.unitPrice * part.quantity, 0);

  return (
    <>
      <div>Layout: {request.layout}</div>
      {parts.length > 0 ? (
        <>
          {parts.map((part) => (
            <div key={part.slot}>
              {SLOT_LABELS[part.slot]}: {part.name} · {formatVND(part.unitPrice)} × {part.quantity}
            </div>
          ))}
          <div>Hot-swap: {request.hotSwap ? 'Có' : 'Không'}</div>
          <div>Tạm tính linh kiện lúc gửi: <strong>{formatVND(subtotal)}</strong></div>
        </>
      ) : (
        <>
          <div>Switch: {[request.switchType, request.switchName].filter(Boolean).join(' • ')}</div>
          {request.caseMaterial && <div>Case: {request.caseMaterial}</div>}
          {request.keycapSet && <div>Keycap: {request.keycapSet}</div>}
          <div>Lube switch: {request.lubeSwitch ? 'Có' : 'Không'} • Hot-swap: {request.hotSwap ? 'Có' : 'Không'}</div>
          {request.ledType && <div>Đèn LED: {request.ledType}</div>}
        </>
      )}
      {request.budget != null && <div>Ngân sách dự kiến: {formatVND(request.budget)}</div>}
    </>
  );
}
