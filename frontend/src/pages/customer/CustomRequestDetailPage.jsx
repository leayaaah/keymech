import '../custom.css';
import { useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { acceptQuote, declineCustomRequest, getMyCustomRequest } from '../../api/customRequestApi';
import CustomStatusBadge from '../../components/CustomStatusBadge';
import RequestSpecs from '../../components/RequestSpecs';
import StatusTimeline from '../../components/StatusTimeline';
import useAsyncData from '../../hooks/useAsyncData';
import { LIMITS, apiErrorMessage } from '../../utils/customStatus';
import { formatDateTime, formatVND } from '../../utils/format';

/** C-04 và C-06: chi tiết, tiến độ, đồng ý hoặc từ chối báo giá, hủy yêu cầu. */
export default function CustomRequestDetailPage() {
  const { id } = useParams();
  const location = useLocation();
  const { data, loading, error, reload } = useAsyncData(() => getMyCustomRequest(id), [id]);

  const [declining, setDeclining] = useState(false);
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState(null);

  if (loading && !data) return <main className="cr-page"><p role="status">Đang tải…</p></main>;
  if (error) {
    const notFound = error?.response?.status === 404 || error?.response?.status === 403;
    return (
      <main className="cr-page">
        <p className="cr-alert cr-alert--error" role="alert">
          {notFound ? 'Không tìm thấy yêu cầu này.' : apiErrorMessage(error, 'Không tải được yêu cầu')}
        </p>
        <Link to="/custom-requests">Về danh sách yêu cầu</Link>
      </main>
    );
  }

  const { request: r, history, allowedNext } = data;
  const canAccept = allowedNext.includes('ACCEPTED');
  const canDecline = allowedNext.includes('CANCELLED');
  const declineLabel = r.status === 'QUOTED' ? 'Từ chối báo giá' : 'Hủy yêu cầu';

  const run = async (action) => {
    setBusy(true);
    setActionError(null);
    try {
      await action();
      setDeclining(false);
      setNote('');
      reload();
    } catch (err) {
      setActionError(apiErrorMessage(err));
      // Staff vừa báo lại giá hoặc trạng thái đã đổi: tải lại để khách thấy thông tin mới nhất
      if (err?.response?.status === 409) reload();
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="cr-page">
      <Link to="/custom-requests">← Danh sách yêu cầu</Link>

      {location.state?.justCreated && (
        <p className="cr-alert cr-alert--ok" role="status">
          Đã gửi yêu cầu {location.state.justCreated}. Cửa hàng sẽ báo giá sớm nhất có thể.
        </p>
      )}

      <div className="cr-page__head">
        <h1>{r.requestCode}</h1>
        <CustomStatusBadge status={r.status} />
      </div>
      <p className="cr-muted">Gửi lúc {formatDateTime(r.createdAt)}</p>

      <section aria-labelledby="specs-title">
        <h2 id="specs-title">Thông tin yêu cầu</h2>
        <RequestSpecs request={r} />
      </section>

      {r.quotedPrice != null && (
        <section aria-labelledby="quote-title" className="cr-quote">
          <h2 id="quote-title">Báo giá</h2>
          <p className="cr-quote__price">{formatVND(r.quotedPrice)}</p>
        </section>
      )}

      {(canAccept || canDecline) && (
        <section className="cr-actions" aria-label="Phản hồi của bạn">
          {canAccept && (
            <button
              type="button"
              className="cr-btn cr-btn--primary"
              disabled={busy}
              onClick={() => run(() => acceptQuote(r.id, r.quotedPrice))}
            >
              Đồng ý báo giá {formatVND(r.quotedPrice)}
            </button>
          )}
          {canDecline && !declining && (
            <button type="button" className="cr-btn" disabled={busy} onClick={() => setDeclining(true)}>
              {declineLabel}
            </button>
          )}
        </section>
      )}

      {canDecline && declining && (
        <form
          className="cr-form"
          noValidate
          onSubmit={(e) => {
            e.preventDefault();
            run(() => declineCustomRequest(r.id, note));
          }}
        >
          <div className="cr-field">
            <label htmlFor="decline-note">Lý do (không bắt buộc)</label>
            <textarea id="decline-note" rows={3} maxLength={LIMITS.declineNote} value={note} onChange={(e) => setNote(e.target.value)} />
          </div>
          <div className="cr-actions">
            <button type="submit" className="cr-btn cr-btn--danger" disabled={busy}>
              {busy ? 'Đang xử lý…' : `Xác nhận ${declineLabel.toLowerCase()}`}
            </button>
            <button type="button" className="cr-btn" disabled={busy} onClick={() => setDeclining(false)}>Quay lại</button>
          </div>
        </form>
      )}

      {actionError && <p className="cr-alert cr-alert--error" role="alert">{actionError}</p>}

      <section aria-labelledby="timeline-title">
        <h2 id="timeline-title">Tiến độ</h2>
        <StatusTimeline history={history} />
      </section>
    </main>
  );
}
