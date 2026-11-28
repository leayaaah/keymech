import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import customRequestApi from '../../api/customRequestApi';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import PageNavigation from '../../components/PageNavigation';
import formatVND from '../../utils/formatVND';
import { formatDate } from '../../utils/formatDate';
import { customStatusLabels } from '../../utils/orderLabels';
import { getFeatureErrorMessage } from '../../utils/apiError';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';

const MIN_BUDGET = 500000;
const MAX_NOTE_LENGTH = 1000;

function ComponentSelect({ id, label, options, value, onChange, error, disabled }) {
  return (
    <div className="km-field">
      <label htmlFor={id} className="km-label">{label} <span className="km-required">*</span></label>
      <select
        id={id}
        className={`km-select ${error ? 'km-input--error' : ''}`}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        disabled={disabled}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${id}-error` : undefined}
      >
        <option value="">Chọn {label.toLowerCase()}</option>
        {options.map((part) => (
          <option key={part.productId} value={part.productId}>
            {part.name} — {formatVND(part.unitPrice)}{part.quantity > 1 ? ` × ${part.quantity}` : ''}
          </option>
        ))}
      </select>
      {error && <p id={`${id}-error`} className="km-field-error">{error}</p>}
    </div>
  );
}

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
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [catalog, setCatalog] = useState({ options: null, error: '' });
  const [catalogRevision, setCatalogRevision] = useState(0);

  // Form state
  const [form, setForm] = useState({
    kitProductId: '',
    switchProductId: '',
    keycapProductId: '',
    stabilizerProductId: '',
    budget: '',
    note: '',
  });
  const [formErrors, setFormErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);

  const kits = catalog.options ?? [];
  const kitOption = kits.find((option) => option.kit.productId === Number(form.kitProductId));
  const switchOption = kitOption?.switches.find((part) => part.productId === Number(form.switchProductId));
  const keycapOption = kitOption?.keycaps.find((part) => part.productId === Number(form.keycapProductId));
  const stabilizerOption = kitOption?.stabilizers.find((part) => part.productId === Number(form.stabilizerProductId));
  const subtotal = [kitOption?.kit, switchOption, keycapOption, stabilizerOption]
    .reduce((sum, part) => sum + (part ? part.unitPrice * part.quantity : 0), 0);

  useEffect(() => {
    if (tab !== 'create') return;
    let active = true;
    customRequestApi.listComponents().then((options) => {
      if (active) setCatalog({ options, error: '' });
    }).catch((err) => {
      if (active) setCatalog({ options: null, error: getFeatureErrorMessage(err, 'Không thể tải linh kiện.') });
    });
    return () => { active = false; };
  }, [tab, catalogRevision]);

  const reloadCatalog = () => {
    setCatalog({ options: null, error: '' });
    setCatalogRevision((current) => current + 1);
  };

  const updatePart = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
    setFormErrors((current) => ({ ...current, [field]: '' }));
  };

  const updateKit = (kitProductId) => {
    setForm((current) => ({ ...current, kitProductId, switchProductId: '', keycapProductId: '', stabilizerProductId: '' }));
    setFormErrors((current) => ({ ...current, kitProductId: '', switchProductId: '', keycapProductId: '', stabilizerProductId: '' }));
  };

  useEffect(() => {
    if (tab !== 'list') return;
    let active = true;
    customRequestApi
      .listMine(page)
      .then((res) => {
        if (active) {
          setRequests(res?.content ?? []);
          setTotalPages(res?.totalPages ?? 0);
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
  }, [tab, page, reloadKey]);

  const validateForm = () => {
    const next = {};
    if (!kitOption) next.kitProductId = 'Vui lòng chọn kit còn hàng.';
    if (kitOption && !switchOption) next.switchProductId = 'Vui lòng chọn switch tương thích.';
    if (kitOption && !keycapOption) next.keycapProductId = 'Vui lòng chọn bộ keycap tương thích.';
    if (kitOption && !kitOption.stabilizersIncluded && !stabilizerOption) {
      next.stabilizerProductId = 'Vui lòng chọn stabilizer tương thích.';
    }

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
        kitProductId: kitOption.kit.productId,
        switchProductId: switchOption.productId,
        keycapProductId: keycapOption.productId,
        stabilizerProductId: stabilizerOption?.productId ?? null,
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
      if ([400, 409].includes(err?.response?.status)) reloadCatalog();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div>
        <h1 className="km-page-title">Custom phím</h1>
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
            <button
              type="button"
              className="km-btn km-btn-soft"
              onClick={() => window.dispatchEvent(new Event('keymech:chat-compatibility'))}
            >
              Hỏi về tương thích
            </button>

            {catalog.error ? (
              <ErrorState title="Lỗi tải linh kiện" message={catalog.error} onRetry={reloadCatalog} />
            ) : catalog.options === null ? (
              <Loading message="Đang tải linh kiện..." />
            ) : kits.length === 0 ? (
              <p className="km-field-hint" role="status">Shop hiện chưa có bộ linh kiện tương thích và đủ hàng để gửi yêu cầu.</p>
            ) : (
              <>
                <ComponentSelect
                  id="custom-kit"
                  label="Kit bàn phím"
                  options={kits.map((option) => option.kit)}
                  value={kitOption ? form.kitProductId : ''}
                  onChange={updateKit}
                  error={formErrors.kitProductId}
                  disabled={submitting}
                />
                {kitOption && (
                  <>
                    <p className="km-field-hint">
                      Layout {kitOption.layout}. Vỏ, plate và PCB đi kèm kit.
                      {kitOption.hotSwap && ' PCB hot-swap cho phép thay switch tương thích mà không cần hàn.'}
                    </p>
                    <div className="km-form-row-2">
                      <ComponentSelect
                        id="custom-switch"
                        label="Switch"
                        options={kitOption.switches}
                        value={switchOption ? form.switchProductId : ''}
                        onChange={(value) => updatePart('switchProductId', value)}
                        error={formErrors.switchProductId}
                        disabled={submitting}
                      />
                      <ComponentSelect
                        id="custom-keycap"
                        label="Bộ keycap"
                        options={kitOption.keycaps}
                        value={keycapOption ? form.keycapProductId : ''}
                        onChange={(value) => updatePart('keycapProductId', value)}
                        error={formErrors.keycapProductId}
                        disabled={submitting}
                      />
                    </div>
                    {kitOption.stabilizersIncluded ? (
                      <p className="km-field-hint">Kit đã có stabilizer.</p>
                    ) : (
                      <ComponentSelect
                        id="custom-stabilizer"
                        label="Stabilizer"
                        options={kitOption.stabilizers}
                        value={stabilizerOption ? form.stabilizerProductId : ''}
                        onChange={(value) => updatePart('stabilizerProductId', value)}
                        error={formErrors.stabilizerProductId}
                        disabled={submitting}
                      />
                    )}
                    <p className="km-field-hint" aria-live="polite">Tạm tính linh kiện đã chọn: <strong>{formatVND(subtotal)}</strong></p>
                  </>
                )}
              </>
            )}

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
                aria-describedby="custom-note-hint"
                placeholder="Ví dụ: Muốn gõ êm và dễ thay switch."
                value={form.note}
                onChange={(e) => setForm((p) => ({ ...p, note: e.target.value }))}
              />
              <p id="custom-note-hint" className="km-field-hint">
                Ghi thêm nhu cầu về âm thanh hoặc cách lắp để nhân viên xem khi báo giá.
              </p>
              {formErrors.note && <p className="km-field-error">{formErrors.note}</p>}
            </div>

            <div>
              <button type="submit" className="km-btn km-btn-primary" disabled={submitting || kits.length === 0}>
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
                    Ngày gửi: {formatDate(req.createdAt)} • {[req.layout, req.switchType, req.switchName].filter(Boolean).join(' • ')}
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
          <PageNavigation page={page} totalPages={totalPages} label="Phân trang yêu cầu của tôi"
            onChange={(next) => {
              setLoading(true);
              setPage(next);
            }}
          />
        </div>
      )}
    </>
  );
}

export default CustomerCustomRequestsPage;
