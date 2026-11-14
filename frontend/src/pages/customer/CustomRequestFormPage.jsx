import '../custom.css';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCustomRequest } from '../../api/customRequestApi';
import {
  CASE_MATERIALS,
  EMPTY_FORM,
  LAYOUTS,
  LIMITS,
  MIN_BUDGET,
  RGB_OPTIONS,
  SWITCH_KINDS,
  apiErrorMessage,
  toRequestBody,
  validateCustomForm,
} from '../../utils/customStatus';

function Field({ id, label, required, error, hint, children }) {
  return (
    <div className={error ? 'cr-field has-error' : 'cr-field'}>
      <label htmlFor={id}>
        {label}
        {required && <span aria-hidden="true"> *</span>}
      </label>
      {children}
      {hint && !error && <small className="cr-muted">{hint}</small>}
      {error && (
        <small id={`${id}-error`} className="cr-error" role="alert">
          {error}
        </small>
      )}
    </div>
  );
}

/** C-03: gửi yêu cầu Custom. Họ tên, email, số điện thoại lấy từ tài khoản nên không phải nhập lại. */
export default function CustomRequestFormPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState(null);

  const set = (name) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    const found = validateCustomForm(form);
    setErrors(found);
    setServerError(null);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      const created = await createCustomRequest(toRequestBody(form));
      navigate(`/custom-requests/${created.id}`, { state: { justCreated: created.requestCode } });
    } catch (err) {
      setServerError(apiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="cr-page">
      <h1>Gửi yêu cầu Custom</h1>
      <p className="cr-muted">
        Mô tả bàn phím bạn muốn. Cửa hàng sẽ xem và gửi báo giá; bạn quyết định đồng ý hay từ chối.
      </p>

      <form className="cr-form" onSubmit={onSubmit} noValidate>
        <Field id="layout" label="Layout" required error={errors.layout}>
          <select id="layout" value={form.layout} onChange={set('layout')} aria-describedby={errors.layout ? 'layout-error' : undefined}>
            <option value="">Chọn layout</option>
            {LAYOUTS.map((l) => (
              <option key={l} value={l}>{l}</option>
            ))}
          </select>
        </Field>

        <Field id="switchKind" label="Loại switch" required error={errors.switchKind}>
          <select id="switchKind" value={form.switchKind} onChange={set('switchKind')}>
            <option value="">Chọn loại switch</option>
            {SWITCH_KINDS.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </Field>

        <Field id="switchName" label="Tên switch" error={errors.switchName} hint="Ví dụ: Gateron Yellow (không bắt buộc)">
          <input id="switchName" type="text" value={form.switchName} onChange={set('switchName')} maxLength={LIMITS.switchType} />
        </Field>

        <Field id="caseMaterial" label="Chất liệu case" error={errors.caseMaterial}>
          <select id="caseMaterial" value={form.caseMaterial} onChange={set('caseMaterial')}>
            <option value="">Chưa chọn</option>
            {CASE_MATERIALS.map((m) => (
              <option key={m} value={m}>{m}</option>
            ))}
          </select>
        </Field>

        <Field id="keycapSet" label="Bộ keycap" error={errors.keycapSet}>
          <input id="keycapSet" type="text" value={form.keycapSet} onChange={set('keycapSet')} maxLength={LIMITS.keycapSet} />
        </Field>

        <Field id="rgbOption" label="Đèn LED" error={errors.rgbOption}>
          <select id="rgbOption" value={form.rgbOption} onChange={set('rgbOption')}>
            <option value="">Chưa chọn</option>
            {RGB_OPTIONS.map((o) => (
              <option key={o} value={o}>{o}</option>
            ))}
          </select>
        </Field>

        <Field id="budget" label="Ngân sách (đồng)" error={errors.budget} hint={`Tối thiểu ${MIN_BUDGET.toLocaleString('vi-VN')}đ nếu nhập`}>
          <input id="budget" type="number" inputMode="numeric" min={MIN_BUDGET} step="1" value={form.budget} onChange={set('budget')} />
        </Field>

        <fieldset className="cr-checks">
          <legend>Tùy chọn</legend>
          <label><input type="checkbox" checked={form.lubing} onChange={set('lubing')} /> Lube switch</label>
          <label><input type="checkbox" checked={form.hotSwap} onChange={set('hotSwap')} /> Hot-swap</label>
        </fieldset>

        <Field id="note" label="Ghi chú" error={errors.note} hint={`${form.note.length}/${LIMITS.note} ký tự`}>
          <textarea id="note" rows={4} value={form.note} onChange={set('note')} maxLength={LIMITS.note} />
        </Field>

        {serverError && <p className="cr-alert cr-alert--error" role="alert">{serverError}</p>}

        <div className="cr-actions">
          <button type="submit" className="cr-btn cr-btn--primary" disabled={submitting}>
            {submitting ? 'Đang gửi…' : 'Gửi yêu cầu'}
          </button>
        </div>
      </form>
    </main>
  );
}
