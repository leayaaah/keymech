import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import customRequestApi from '../../api/customRequestApi';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import formatVND from '../../utils/formatVND';
import { formatDate } from '../../utils/formatDate';
import { customStatusLabels } from '../../utils/orderLabels';
import { getFeatureErrorMessage } from '../../utils/apiError';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';

const LAYOUT_OPTIONS = ['60%', '65%', '75%', 'TKL', 'Full-size'];
const SWITCH_TYPES = ['Linear', 'Tactile', 'Clicky'];
const CASE_MATERIALS = ['Nhựa', 'Nhôm', 'Gỗ', 'Acrylic'];
const LED_TYPES = ['Không', 'RGB', 'Đơn sắc'];
const MIN_BUDGET = 500000;
const MAX_NOTE_LENGTH = 1000;

function customBadgeClass(status) {
  if (status === 'COMPLETED' || status === 'ACCEPTED') return 'km-badge km-badge--success';
  if (status === 'CANCELLED' || status === 'REJECTED') return 'km-badge km-badge--danger';
  if (status === 'QUOTED' || status === 'IN_PROGRESS') return 'km-badge km-badge--info';
  return 'km-badge km-badge--warning';
}

/**
 * Trang Custom phím của Customer (Figma frames 3:546 & 3:550).
 * Quy tắc: Ngân sách KHÔNG bắt buộc (≥ 500.000 ₫ nếu nhập), ghi chú ≤ 1000 ký tự,
 * tên/email/SĐT chỉ đọc từ tài khoản.
 */
export function CustomerCustomRequestsPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [tab, setTab] = useState('list');
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  // Form state
  const [form, setForm] = useState({
    layout: '75%',
    switchType: 'Linear',
    switchName: '',
    caseMaterial: 'Nhôm',
    keycapSet: '',
    ledType: 'RGB',
    lubeSwitch: false,
    hotSwap: true,
    budget: '',
    note: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (tab !== 'list') return;
    let active = true;
    customRequestApi
      .listMine()
      .then((res) => {
        if (active) {
          setRequests(res?.content ?? []);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(getFeatureErrorMessage(err, 'Không thể tải danh sách yêu cầu Custom.'));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [tab, reloadKey]);

  const validateForm = () => {
    const next = {};
    if (!form.layout) next.layout = 'Vui lòng chọn layout.';
    if (!form.switchType) next.switchType = 'Vui lòng chọn loại switch.';
    if (!form.switchName.trim()) next.switchName = 'Vui lòng nhập tên switch mong muốn.';

    if (form.budget !== '') {
      const budgetNum = Number(form.budget);
      if (Number.isNaN(budgetNum) || budgetNum < MIN_BUDGET) {
        next.budget = 'Ngân sách dự kiến phải từ 500.000 ₫ trở lên (hoặc để trống).';
      }
    }

    if (form.note.length > MAX_NOTE_LENGTH) {
      next.note = `Ghi chú không được vượt quá ${MAX_NOTE_LENGTH} ký tự.`;
    }

    setFormErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setSubmitting(true);
    try {
      const created = await customRequestApi.create({
        layout: form.layout,
        switchType: form.switchType,
        switchName: form.switchName.trim(),
        caseMaterial: form.caseMaterial,
        keycapSet: form.keycapSet.trim() || null,
        ledType: form.ledType,
        lubeSwitch: form.lubeSwitch,
        hotSwap: form.hotSwap,
        budget: form.budget !== '' ? Number(form.budget) : null,
        note: form.note.trim() || null,
      });
      toast.success('Đã gửi yêu cầu Custom thành công!');
      if (created?.id) {
        navigate(`/customer/custom-requests/${created.id}`);
      } else {
        setTab('list');
        setLoading(true);
        setReloadKey((k) => k + 1);
      }
    } catch (err) {
      toast.error(getFeatureErrorMessage(err, 'Không thể gửi yêu cầu Custom.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div>
        <h1 className="km-account-heading">Custom phím</h1>
        <p className="km-auth-subtitle">
          Cùng KeyMech tạo nên bộ bàn phím mang dấu ấn riêng của bạn.
        </p>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
        <button
          type="button"
          className={`km-btn ${tab === 'create' ? 'km-btn-primary' : 'km-btn-soft'}`}
          onClick={() => setTab('create')}
        >
          Gửi yêu cầu mới
        </button>
        <button
          type="button"
          className={`km-btn ${tab === 'list' ? 'km-btn-primary' : 'km-btn-soft'}`}
          onClick={() => {
            setTab('list');
            setLoading(true);
            setReloadKey((k) => k + 1);
          }}
        >
          Yêu cầu của tôi
        </button>
      </div>

      {tab === 'create' ? (
        <>
          <div className="km-account-card">
            <h2 style={{ fontSize: 'var(--text-lg)' }}>Thông tin từ tài khoản • Chỉ đọc</h2>
            <p className="km-auth-subtitle">
              {[user?.fullName, user?.email, user?.phone].filter(Boolean).join('  •  ')}
            </p>
          </div>

          <form className="km-account-card" onSubmit={handleSubmit} noValidate>
            <h2>Cấu hình mong muốn</h2>
            <p className="km-field-hint">Các trường có dấu * là bắt buộc.</p>

            <div className="km-form-row-2">
              <div className="km-field">
                <label htmlFor="custom-layout" className="km-label">
                  Layout <span className="km-required">*</span>
                </label>
                <select
                  id="custom-layout"
                  className="km-select"
                  value={form.layout}
                  onChange={(e) => setForm((p) => ({ ...p, layout: e.target.value }))}
                >
                  {LAYOUT_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
                <p className="km-field-hint">60% • 65% • 75% • TKL • Full-size</p>
              </div>

              <div className="km-field">
                <label htmlFor="custom-switch-type" className="km-label">
                  Loại switch <span className="km-required">*</span>
                </label>
                <select
                  id="custom-switch-type"
                  className="km-select"
                  value={form.switchType}
                  onChange={(e) => setForm((p) => ({ ...p, switchType: e.target.value }))}
                >
                  {SWITCH_TYPES.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
                <p className="km-field-hint">Linear • Tactile • Clicky</p>
              </div>
            </div>

            <div className="km-form-row-2">
              <div className="km-field">
                <label htmlFor="custom-switch-name" className="km-label">
                  Tên switch <span className="km-required">*</span>
                </label>
                <input
                  id="custom-switch-name"
                  type="text"
                  className={`km-input ${formErrors.switchName ? 'km-input--error' : ''}`}
                  placeholder="Gateron Milky Yellow Pro"
                  value={form.switchName}
                  onChange={(e) => {
                    setForm((p) => ({ ...p, switchName: e.target.value }));
                    if (formErrors.switchName) setFormErrors((p) => ({ ...p, switchName: '' }));
                  }}
                />
                {formErrors.switchName && <p className="km-field-error">{formErrors.switchName}</p>}
              </div>

              <div className="km-field">
                <label htmlFor="custom-case" className="km-label">
                  Chất liệu case
                </label>
                <select
                  id="custom-case"
                  className="km-select"
                  value={form.caseMaterial}
                  onChange={(e) => setForm((p) => ({ ...p, caseMaterial: e.target.value }))}
                >
                  {CASE_MATERIALS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="km-form-row-2">
              <div className="km-field">
                <label htmlFor="custom-keycap" className="km-label">
                  Bộ keycap
                </label>
                <input
                  id="custom-keycap"
                  type="text"
                  className="km-input"
                  placeholder="PBT Cherry profile • Trắng / xanh navy"
                  value={form.keycapSet}
                  onChange={(e) => setForm((p) => ({ ...p, keycapSet: e.target.value }))}
                />
              </div>

              <div className="km-field">
                <label htmlFor="custom-led" className="km-label">
                  Đèn LED
                </label>
                <select
                  id="custom-led"
                  className="km-select"
                  value={form.ledType}
                  onChange={(e) => setForm((p) => ({ ...p, ledType: e.target.value }))}
                >
                  {LED_TYPES.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div style={{ display: 'flex', gap: '24px', flexWrap: 'wrap' }}>
              <label className="km-filter-option">
                <input
                  type="checkbox"
                  checked={form.lubeSwitch}
                  onChange={(e) => setForm((p) => ({ ...p, lubeSwitch: e.target.checked }))}
                />
                <span>Lube switch</span>
              </label>
              <label className="km-filter-option">
                <input
                  type="checkbox"
                  checked={form.hotSwap}
                  onChange={(e) => setForm((p) => ({ ...p, hotSwap: e.target.checked }))}
                />
                <span>Hot-swap</span>
              </label>
            </div>

            <div className="km-field">
              <label htmlFor="custom-budget" className="km-label">
                Ngân sách dự kiến (₫) — tùy chọn
              </label>
              <input
                id="custom-budget"
                type="number"
                min={MIN_BUDGET}
                step={50000}
                className={`km-input ${formErrors.budget ? 'km-input--error' : ''}`}
                placeholder="Từ 500.000 ₫ (không bắt buộc)"
                value={form.budget}
                onChange={(e) => {
                  setForm((p) => ({ ...p, budget: e.target.value }));
                  if (formErrors.budget) setFormErrors((p) => ({ ...p, budget: '' }));
                }}
              />
              {formErrors.budget && <p className="km-field-error">{formErrors.budget}</p>}
            </div>

            <div className="km-field">
              <label htmlFor="custom-note" className="km-label">
                Ghi chú thêm ({form.note.length}/{MAX_NOTE_LENGTH})
              </label>
              <textarea
                id="custom-note"
                rows={4}
                maxLength={MAX_NOTE_LENGTH}
                className={`km-textarea ${formErrors.note ? 'km-input--error' : ''}`}
                placeholder="Ưu tiên tiếng gõ trầm, phù hợp văn phòng..."
                value={form.note}
                onChange={(e) => setForm((p) => ({ ...p, note: e.target.value }))}
              />
              {formErrors.note && <p className="km-field-error">{formErrors.note}</p>}
            </div>

            <div>
              <button type="submit" className="km-btn km-btn-primary" disabled={submitting}>
                {submitting ? 'Đang gửi yêu cầu...' : 'Gửi yêu cầu Custom'}
              </button>
            </div>
          </form>
        </>
      ) : loading ? (
        <Loading message="Đang tải yêu cầu Custom..." />
      ) : error ? (
        <ErrorState
          title="Lỗi tải yêu cầu Custom"
          message={error}
          onRetry={() => {
            setLoading(true);
            setError('');
            setReloadKey((k) => k + 1);
          }}
        />
      ) : requests.length === 0 ? (
        <EmptyState
          title="Chưa có yêu cầu Custom nào"
          description="Bạn có thể gửi yêu cầu cấu hình bàn phím riêng để nhận báo giá từ đội ngũ KeyMech."
          actionText="Gửi yêu cầu mới"
          onAction={() => setTab('create')}
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {requests.map((req) => (
            <article key={req.id} className="km-account-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 'var(--text-lg)' }}>
                    {req.requestCode || `CUS-${req.id}`}
                  </div>
                  <div className="km-field-hint">
                    Ngày gửi: {formatDate(req.createdAt)} • {req.layout} • {req.switchType} ({req.switchName})
                  </div>
                </div>
                <span className={customBadgeClass(req.status)}>
                  {customStatusLabels[req.status] || req.status}
                </span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px', paddingTop: '8px', borderTop: '1px solid var(--color-border-subtle)' }}>
                <div style={{ fontWeight: 600 }}>
                  {req.quotedPrice
                    ? `Báo giá: ${formatVND(req.quotedPrice)}`
                    : req.budget
                      ? `Ngân sách: ${formatVND(req.budget)}`
                      : 'Chưa có báo giá'}
                </div>
                <Link to={`/customer/custom-requests/${req.id}`} className="km-btn km-btn-soft">
                  Xem chi tiết
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </>
  );
}

export default CustomerCustomRequestsPage;
