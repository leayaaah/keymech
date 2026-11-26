import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import customRequestApi from '../../api/customRequestApi';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import formatVND from '../../utils/formatVND';
import { formatDateTime } from '../../utils/formatDate';
import { customStatusLabels } from '../../utils/orderLabels';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/portal.css';

/**
 * Trang Chi tiết yêu cầu Custom phía Staff/Admin (Figma frames 3:108, 3:5903, 3:5989, 3:6075).
 * Luồng trạng thái theo kế hoạch mục 9:
 * - PENDING → QUOTED (nhập báo giá) hoặc REJECTED (từ chối kèm ghi chú)
 * - ACCEPTED → IN_PROGRESS (Bắt đầu lắp ráp)
 * - IN_PROGRESS → COMPLETED (Hoàn thành)
 */
export function StaffCustomRequestDetailPage() {
  const { id } = useParams();
  const [req, setReq] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const [quotedPrice, setQuotedPrice] = useState('');
  const [quoteNote, setQuoteNote] = useState('');
  const [rejectNote, setRejectNote] = useState('');
  const [statusNote, setStatusNote] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let active = true;
    customRequestApi
      .getForStaff(id)
      .then((data) => {
        if (!active) return;
        setReq(data);
        setQuotedPrice(data?.quotedPrice != null ? String(data.quotedPrice) : '');
        setQuoteNote(data?.quoteNote || '');
        setError('');
      })
      .catch((err) => {
        if (active) {
          setError(err?.response?.data?.message || 'Không thể tải yêu cầu Custom.');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [id, reloadKey]);

  const handleSendQuote = async (e) => {
    e.preventDefault();
    const priceNum = Number(quotedPrice);
    if (!quotedPrice || Number.isNaN(priceNum) || priceNum <= 0) {
      toast.error('Vui lòng nhập giá báo hợp lệ (> 0).');
      return;
    }
    setBusy(true);
    try {
      const updated = await customRequestApi.sendQuote(req.id, {
        quotedPrice: priceNum,
        quoteNote: quoteNote.trim(),
      });
      setReq(updated);
      toast.success('Đã gửi báo giá cho khách hàng.');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể gửi báo giá.');
    } finally {
      setBusy(false);
    }
  };

  const handleTransition = async (nextStatus, noteText) => {
    if (nextStatus === 'REJECTED' && !noteText.trim()) {
      toast.error('Vui lòng nhập lý do từ chối.');
      return;
    }
    setBusy(true);
    try {
      const updated = await customRequestApi.updateStatusForStaff(req.id, {
        status: nextStatus,
        note: noteText.trim(),
      });
      setReq(updated);
      setRejectNote('');
      setStatusNote('');
      toast.success(`Đã chuyển trạng thái sang "${customStatusLabels[nextStatus]}".`);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể cập nhật trạng thái.');
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Loading message="Đang tải yêu cầu Custom..." />;
  if (error || !req) {
    return (
      <ErrorState
        title="Lỗi tải yêu cầu Custom"
        message={error || 'Không tìm thấy yêu cầu.'}
        onRetry={() => {
          setLoading(true);
          setError('');
          setReloadKey((k) => k + 1);
        }}
      />
    );
  }

  return (
    <>
      <Link to="/staff/custom-requests" className="km-auth-link km-auth-link--sm">
        ← Về danh sách yêu cầu Custom
      </Link>

      <div className="km-portal-top">
        <div>
          <h1 className="km-portal-title">{req.requestCode || `CUS-${req.id}`}</h1>
          <p className="km-auth-subtitle">
            Khách hàng: {req.customerName} • {req.customerEmail}
            {req.customerPhone ? ` • ${req.customerPhone}` : ''}
          </p>
        </div>
        <span className="km-badge km-badge--info">
          {customStatusLabels[req.status] || req.status}
        </span>
      </div>

      <div className="km-form-row-2">
        <section className="km-panel" style={{ padding: 'var(--space-6)' }}>
          <h2 style={{ fontSize: 'var(--text-xl)' }}>Cấu hình yêu cầu</h2>
          <div>Layout: {req.layout}</div>
          <div>
            Switch: {req.switchType} • {req.switchName}
          </div>
          {req.caseMaterial && <div>Case: {req.caseMaterial}</div>}
          {req.keycapSet && <div>Keycap: {req.keycapSet}</div>}
          <div>
            Lube: {req.lubeSwitch ? 'Có' : 'Không'} • Hot-swap: {req.hotSwap ? 'Có' : 'Không'}
          </div>
          {req.ledType && <div>Đèn LED: {req.ledType}</div>}
          {req.budget != null && <div>Ngân sách dự kiến: {formatVND(req.budget)}</div>}
          {req.quotedPrice != null && (
            <div style={{ fontWeight: 600, color: 'var(--color-primary)' }}>
              Giá đã báo: {formatVND(req.quotedPrice)}
            </div>
          )}
          {req.note && <p className="km-field-hint">Ghi chú khách: {req.note}</p>}
        </section>

        <section className="km-panel" style={{ padding: 'var(--space-6)' }}>
          <h2 style={{ fontSize: 'var(--text-xl)' }}>Lịch sử trạng thái</h2>
          {(req.history ?? []).length === 0 ? (
            <p className="km-field-hint">Chưa có lịch sử.</p>
          ) : (
            (req.history ?? []).map((h) => (
              <div key={h.id} style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ color: 'var(--color-primary)', fontSize: 'var(--text-xs)', fontWeight: 600 }}>
                  ● {formatDateTime(h.createdAt)}
                </span>
                <strong style={{ fontSize: 'var(--text-sm)' }}>
                  {h.fromStatus ? `${customStatusLabels[h.fromStatus] || h.fromStatus} → ` : ''}
                  {customStatusLabels[h.toStatus] || h.toStatus}
                </strong>
                {h.note && <span className="km-field-hint">{h.note}</span>}
              </div>
            ))
          )}
        </section>
      </div>

      {/* Form báo giá & từ chối khi PENDING (hoặc cập nhật báo giá khi QUOTED) */}
      {(req.status === 'PENDING' || req.status === 'QUOTED') && (
        <div className="km-form-row-2">
          <form className="km-panel" style={{ padding: 'var(--space-6)' }} onSubmit={handleSendQuote}>
            <h2 style={{ fontSize: 'var(--text-xl)' }}>
              {req.status === 'PENDING' ? 'Lập báo giá' : 'Cập nhật báo giá'}
            </h2>
            <div className="km-field">
              <label htmlFor="staff-quote-price" className="km-label">
                Giá báo (₫) <span className="km-required">*</span>
              </label>
              <input
                id="staff-quote-price"
                type="number"
                min={1}
                step={10000}
                className="km-input"
                value={quotedPrice}
                onChange={(e) => setQuotedPrice(e.target.value)}
                placeholder="2650000"
              />
            </div>
            <div className="km-field">
              <label htmlFor="staff-quote-note" className="km-label">
                Ghi chú báo giá
              </label>
              <textarea
                id="staff-quote-note"
                rows={3}
                className="km-textarea"
                value={quoteNote}
                onChange={(e) => setQuoteNote(e.target.value)}
                placeholder="Chi tiết linh kiện, công lắp ráp, thời gian dự kiến..."
              />
            </div>
            <div>
              <button type="submit" className="km-btn km-btn-primary" disabled={busy}>
                {busy ? 'Đang gửi...' : 'Gửi báo giá'}
              </button>
            </div>
          </form>

          {req.status === 'PENDING' && (
            <div className="km-panel" style={{ padding: 'var(--space-6)' }}>
              <h2 style={{ fontSize: 'var(--text-xl)' }}>Từ chối yêu cầu</h2>
              <div className="km-field">
                <label htmlFor="staff-reject-note" className="km-label">
                  Lý do từ chối <span className="km-required">*</span>
                </label>
                <textarea
                  id="staff-reject-note"
                  rows={3}
                  className="km-textarea"
                  value={rejectNote}
                  onChange={(e) => setRejectNote(e.target.value)}
                  placeholder="Ví dụ: Linh kiện yêu cầu hiện ngừng sản xuất..."
                />
              </div>
              <div>
                <button
                  type="button"
                  className="km-btn km-btn-danger"
                  disabled={busy}
                  onClick={() => handleTransition('REJECTED', rejectNote)}
                >
                  Từ chối yêu cầu
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Chuyển trạng thái lắp ráp */}
      {(req.status === 'ACCEPTED' || req.status === 'IN_PROGRESS') && (
        <div className="km-panel" style={{ padding: 'var(--space-6)' }}>
          <h2 style={{ fontSize: 'var(--text-xl)' }}>Cập nhật tiến độ lắp ráp</h2>
          <div className="km-field">
            <label htmlFor="staff-progress-note" className="km-label">
              Ghi chú tiến độ
            </label>
            <input
              id="staff-progress-note"
              className="km-input"
              value={statusNote}
              onChange={(e) => setStatusNote(e.target.value)}
              placeholder="Ghi chú tùy chọn..."
            />
          </div>
          <div>
            {req.status === 'ACCEPTED' && (
              <button
                type="button"
                className="km-btn km-btn-primary"
                disabled={busy}
                onClick={() => handleTransition('IN_PROGRESS', statusNote || 'Bắt đầu lắp ráp.')}
              >
                Bắt đầu lắp ráp
              </button>
            )}
            {req.status === 'IN_PROGRESS' && (
              <button
                type="button"
                className="km-btn km-btn-primary"
                disabled={busy}
                onClick={() => handleTransition('COMPLETED', statusNote || 'Hoàn thành lắp ráp và bàn giao.')}
              >
                Hoàn thành lắp ráp
              </button>
            )}
          </div>
        </div>
      )}
    </>
  );
}

export default StaffCustomRequestDetailPage;
