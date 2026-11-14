import '../custom.css';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { changeCustomStatus, getStaffCustomRequest, quoteCustomRequest } from '../../api/customRequestApi';
import CustomStatusBadge from '../../components/CustomStatusBadge';
import RequestSpecs from '../../components/RequestSpecs';
import StatusTimeline from '../../components/StatusTimeline';
import useAsyncData from '../../hooks/useAsyncData';
import { LIMITS, STATUS_LABEL, apiErrorMessage } from '../../utils/customStatus';
import { formatDateTime, formatVND } from '../../utils/format';

/** A-07: báo giá và chuyển trạng thái yêu cầu Custom. */
export default function StaffCustomRequestDetailPage() {
  const { id } = useParams();
  const { data, loading, error, reload } = useAsyncData(() => getStaffCustomRequest(id), [id]);

  const [price, setPrice] = useState('');
  const [nextStatus, setNextStatus] = useState('');
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(null); // { type: 'ok' | 'error', text }

  if (loading && !data) return <main className="cr-page"><p role="status">Đang tải…</p></main>;
  if (error) {
    return (
      <main className="cr-page">
        <p className="cr-alert cr-alert--error" role="alert">
          {error?.response?.status === 404 ? 'Không tìm thấy yêu cầu này.' : apiErrorMessage(error, 'Không tải được yêu cầu')}
        </p>
        <Link to="/staff/custom-requests">Về danh sách yêu cầu</Link>
      </main>
    );
  }

  const { request: r, history, allowedNext } = data;
  const canQuote = r.status === 'PENDING' || r.status === 'QUOTED';
  const priceNumber = Number(price);
  const priceInvalid = price.trim() === '' || !Number.isFinite(priceNumber) || priceNumber < 1;
  // Chuyển sang "Đã báo giá" cần có báo giá trước; giao diện nhắc, backend mới là nơi bắt buộc.
  const needsQuoteFirst = nextStatus === 'QUOTED' && r.quotedPrice == null;

  const run = async (action, okText) => {
    setBusy(true);
    setMessage(null);
    try {
      await action();
      setMessage({ type: 'ok', text: okText });
      setPrice('');
      setNextStatus('');
      setNote('');
      reload();
    } catch (err) {
      setMessage({ type: 'error', text: apiErrorMessage(err) });
      if (err?.response?.status === 409) reload();   // trạng thái đã đổi ở nơi khác
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="cr-page cr-page--wide">
      <Link to="/staff/custom-requests">← Danh sách yêu cầu</Link>

      <div className="cr-page__head">
        <h1>{r.requestCode}</h1>
        <CustomStatusBadge status={r.status} />
      </div>
      <p className="cr-muted">Gửi lúc {formatDateTime(r.createdAt)} · Cập nhật {formatDateTime(r.updatedAt)}</p>

      {r.customer && (
        <section aria-labelledby="customer-title">
          <h2 id="customer-title">Khách hàng</h2>
          <p>
            {r.customer.fullName}
            <br />
            <span className="cr-muted">{r.customer.email}{r.customer.phone ? ` · ${r.customer.phone}` : ''}</span>
          </p>
        </section>
      )}

      <section aria-labelledby="specs-title">
        <h2 id="specs-title">Thông tin yêu cầu</h2>
        <RequestSpecs request={r} />
      </section>

      <section aria-labelledby="quote-title">
        <h2 id="quote-title">Báo giá</h2>
        <p>
          Giá hiện tại: <strong>{r.quotedPrice != null ? formatVND(r.quotedPrice) : 'Chưa báo giá'}</strong>
        </p>
        {canQuote ? (
          <form
            className="cr-form cr-form--row"
            noValidate
            onSubmit={(e) => {
              e.preventDefault();
              if (!priceInvalid) run(() => quoteCustomRequest(r.id, priceNumber), 'Đã lưu báo giá.');
            }}
          >
            <div className="cr-field">
              <label htmlFor="quote-price">Giá báo mới (đồng)</label>
              <input id="quote-price" type="number" min="1" step="1" inputMode="numeric" value={price} onChange={(e) => setPrice(e.target.value)} />
            </div>
            <button type="submit" className="cr-btn" disabled={busy || priceInvalid}>Lưu báo giá</button>
          </form>
        ) : (
          <p className="cr-muted">Không thể đổi báo giá ở trạng thái này.</p>
        )}
        {r.status === 'QUOTED' && (
          <p className="cr-muted">Báo lại giá khi đang "Đã báo giá": khách sẽ phải đồng ý đúng giá mới.</p>
        )}
      </section>

      <section aria-labelledby="status-title">
        <h2 id="status-title">Chuyển trạng thái</h2>
        {allowedNext.length === 0 ? (
          <p className="cr-muted">Yêu cầu đã ở trạng thái cuối, không chuyển tiếp được.</p>
        ) : (
          <form
            className="cr-form"
            noValidate
            onSubmit={(e) => {
              e.preventDefault();
              if (nextStatus) run(() => changeCustomStatus(r.id, nextStatus, note), 'Đã cập nhật trạng thái.');
            }}
          >
            <div className="cr-field">
              <label htmlFor="next-status">Trạng thái mới</label>
              <select id="next-status" value={nextStatus} onChange={(e) => setNextStatus(e.target.value)}>
                <option value="">Chọn trạng thái</option>
                {allowedNext.map((s) => (
                  <option key={s} value={s}>{STATUS_LABEL[s]}</option>
                ))}
              </select>
              {needsQuoteFirst && <small className="cr-error" role="alert">Hãy lưu báo giá trước khi chuyển sang "Đã báo giá".</small>}
            </div>
            <div className="cr-field">
              <label htmlFor="status-note">Ghi chú gửi cho khách (không bắt buộc)</label>
              <textarea id="status-note" rows={3} maxLength={LIMITS.statusNote} value={note} onChange={(e) => setNote(e.target.value)} />
            </div>
            <div className="cr-actions">
              <button type="submit" className="cr-btn cr-btn--primary" disabled={busy || !nextStatus || needsQuoteFirst}>
                {busy ? 'Đang xử lý…' : 'Cập nhật trạng thái'}
              </button>
            </div>
          </form>
        )}
      </section>

      {message && (
        <p className={`cr-alert ${message.type === 'ok' ? 'cr-alert--ok' : 'cr-alert--error'}`} role={message.type === 'ok' ? 'status' : 'alert'}>
          {message.text}
        </p>
      )}

      <section aria-labelledby="timeline-title">
        <h2 id="timeline-title">Lịch sử</h2>
        <StatusTimeline history={history} />
      </section>
    </main>
  );
}
