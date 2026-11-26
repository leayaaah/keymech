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
import '../../styles/customer.css';

function customBadgeClass(status) {
  if (status === 'COMPLETED' || status === 'ACCEPTED') return 'km-badge km-badge--success';
  if (status === 'CANCELLED' || status === 'REJECTED') return 'km-badge km-badge--danger';
  if (status === 'QUOTED' || status === 'IN_PROGRESS') return 'km-badge km-badge--info';
  return 'km-badge km-badge--warning';
}

/**
 * Trang chi tiết yêu cầu Custom phía Customer (Figma frames 3:554, 3:558, 3:4299–3:4548).
 * - Đồng ý báo giá gửi kèm quotedPrice; nếu 409 QUOTE_CHANGED → hiện nút "Tải lại báo giá".
 * - Từ chối báo giá → CANCELLED với ghi chú "Khách từ chối báo giá".
 * - Chỉ cho hủy/từ chối khi ở trạng thái PENDING hoặc QUOTED.
 */
export function CustomerCustomRequestDetailPage() {
  const { id } = useParams();
  const [req, setReq] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const [modal, setModal] = useState(null); // 'accept' | 'decline' | 'cancel' | null
  const [quoteChangedError, setQuoteChangedError] = useState(false);
  const [acting, setActing] = useState(false);

  const reload = () => {
    setLoading(true);
    setError('');
    setQuoteChangedError(false);
    setReloadKey((k) => k + 1);
  };

  useEffect(() => {
    let active = true;
    customRequestApi
      .getMine(id)
      .then((data) => {
        if (active) {
          setReq(data);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(err?.response?.data?.message || 'Không thể tải chi tiết yêu cầu Custom.');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [id, reloadKey]);

  const handleAccept = async () => {
    if (!req || acting) return;
    setActing(true);
    try {
      const updated = await customRequestApi.acceptQuote(req.id, {
        quotedPrice: req.quotedPrice,
      });
      setReq(updated);
      setModal(null);
      toast.success('Đã xác nhận đồng ý báo giá!');
    } catch (err) {
      const code = err?.response?.data?.error;
      if (err?.response?.status === 409 && code === 'QUOTE_CHANGED') {
        setQuoteChangedError(true);
        setModal(null);
      } else {
        toast.error(err?.response?.data?.message || 'Không thể xác nhận báo giá.');
      }
    } finally {
      setActing(false);
    }
  };

  const handleDeclineOrCancel = async (isDecline) => {
    if (!req || acting) return;
    setActing(true);
    try {
      const updated = await customRequestApi.cancel(req.id, {
        note: isDecline ? 'Khách từ chối báo giá' : 'Khách hủy yêu cầu Custom',
      });
      setReq(updated);
      setModal(null);
      toast.success(isDecline ? 'Đã từ chối báo giá.' : 'Đã hủy yêu cầu Custom.');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể thực hiện thao tác.');
    } finally {
      setActing(false);
    }
  };

  if (loading) return <Loading message="Đang tải yêu cầu Custom..." />;
  if (error || !req) {
    return (
      <ErrorState
        title="Lỗi tải yêu cầu Custom"
        message={error || 'Không tìm thấy yêu cầu.'}
        onRetry={reload}
      />
    );
  }

  const canCancel = req.status === 'PENDING' || req.status === 'QUOTED';

  return (
    <>
      <Link to="/customer/custom-requests" className="km-auth-link km-auth-link--sm">
        ← Quay lại danh sách yêu cầu Custom
      </Link>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <h1 className="km-account-heading" style={{ fontSize: 'var(--text-2xl)' }}>
          {req.requestCode || `CUS-${req.id}`}
        </h1>
        <span className={customBadgeClass(req.status)}>
          {customStatusLabels[req.status] || req.status}
        </span>
      </div>

      {quoteChangedError && (
        <div role="alert" className="km-alert-error" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <span>Báo giá đã thay đổi trong lúc bạn xem. Vui lòng tải lại báo giá mới nhất trước khi đồng ý.</span>
          <button type="button" className="km-btn km-btn-primary" onClick={reload}>
            Tải lại báo giá
          </button>
        </div>
      )}

      <div className="km-form-row-2">
        <section className="km-account-card">
          <h2>Cấu hình của bạn</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: 'var(--text-base)' }}>
            <div>Layout: {req.layout}</div>
            <div>
              Switch: {req.switchType} • {req.switchName}
            </div>
            {req.caseMaterial && <div>Case: {req.caseMaterial}</div>}
            {req.keycapSet && <div>Keycap: {req.keycapSet}</div>}
            <div>
              Lube switch: {req.lubeSwitch ? 'Có' : 'Không'} • Hot-swap: {req.hotSwap ? 'Có' : 'Không'}
            </div>
            {req.ledType && <div>Đèn LED: {req.ledType}</div>}
            {req.budget != null && <div>Ngân sách: {formatVND(req.budget)}</div>}
          </div>
          {req.note && (
            <p className="km-field-hint" style={{ fontSize: 'var(--text-sm)' }}>
              Ghi chú: {req.note}
            </p>
          )}
        </section>

        <section className="km-account-card">
          <h2>Lịch sử xử lý</h2>
          {(req.history ?? []).length === 0 ? (
            <p className="km-field-hint">Chưa có lịch sử cập nhật.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {req.history.map((h) => (
                <div key={h.id} style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                  <span style={{ color: 'var(--color-primary)', fontSize: 'var(--text-xs)', fontWeight: 600 }}>
                    ● {formatDateTime(h.createdAt)}
                  </span>
                  <strong style={{ fontSize: 'var(--text-sm)' }}>
                    {h.fromStatus ? `${customStatusLabels[h.fromStatus] || h.fromStatus} → ` : ''}
                    {customStatusLabels[h.toStatus] || h.toStatus}
                  </strong>
                  {h.actorName && <span className="km-field-hint">{h.actorName}</span>}
                  {h.note && <span className="km-field-hint">{h.note}</span>}
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* Báo giá chính thức */}
      {req.status === 'QUOTED' && req.quotedPrice != null && (
        <section
          className="km-account-card"
          style={{ backgroundColor: 'var(--color-primary-soft)', borderColor: 'var(--color-primary-tint)' }}
        >
          <span style={{ fontWeight: 600, color: 'var(--color-primary-hover)' }}>
            Báo giá dành cho bạn
          </span>
          <div style={{ fontSize: 'var(--text-4xl)', fontWeight: 600, color: 'var(--color-primary)' }}>
            {formatVND(req.quotedPrice)}
          </div>
          {req.quoteNote && <p style={{ margin: 0, color: 'var(--color-text)' }}>{req.quoteNote}</p>}
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <button type="button" className="km-btn km-btn-primary" onClick={() => setModal('accept')}>
              Đồng ý báo giá
            </button>
            <button type="button" className="km-btn km-btn-soft" onClick={() => setModal('decline')}>
              Từ chối báo giá
            </button>
          </div>
        </section>
      )}

      {req.status === 'PENDING' && (
        <div className="km-alert-info">
          Yêu cầu đang chờ nhân viên KeyMech tiếp nhận và lên báo giá chi tiết.
        </div>
      )}

      {req.status === 'ACCEPTED' && (
        <div className="km-alert-success">
          Bạn đã đồng ý báo giá {req.quotedPrice ? formatVND(req.quotedPrice) : ''}. KeyMech sẽ sớm bắt đầu lắp ráp.
        </div>
      )}

      {req.status === 'CANCELLED' && (
        <div className="km-alert-error">
          Yêu cầu Custom này đã bị hủy.
        </div>
      )}

      {canCancel && (
        <div>
          <button
            type="button"
            className="km-btn km-btn-ghost"
            style={{ color: 'var(--color-danger)' }}
            onClick={() => setModal('cancel')}
          >
            Hủy yêu cầu Custom
          </button>
        </div>
      )}

      {/* Modals (Figma 3:4507, 3:4518, 3:4529) */}
      {modal && (
        <div className="km-modal-backdrop" role="dialog" aria-modal="true">
          <div className="km-modal">
            {modal === 'accept' && (
              <>
                <h2>Đồng ý báo giá?</h2>
                <p className="km-auth-subtitle">
                  Bạn xác nhận đồng ý mức báo giá <strong>{formatVND(req.quotedPrice)}</strong> cho yêu cầu{' '}
                  <strong>{req.requestCode}</strong>?
                </p>
                <div className="km-modal-actions">
                  <button type="button" className="km-btn km-btn-soft" onClick={() => setModal(null)} disabled={acting}>
                    Quay lại
                  </button>
                  <button type="button" className="km-btn km-btn-primary" onClick={handleAccept} disabled={acting}>
                    {acting ? 'Đang xác nhận...' : 'Xác nhận đồng ý'}
                  </button>
                </div>
              </>
            )}

            {modal === 'decline' && (
              <>
                <h2>Từ chối báo giá?</h2>
                <p className="km-auth-subtitle">
                  Từ chối báo giá sẽ chuyển yêu cầu sang trạng thái <strong>Đã hủy</strong> với ghi chú &ldquo;Khách từ chối báo giá&rdquo;.
                </p>
                <div className="km-modal-actions">
                  <button type="button" className="km-btn km-btn-soft" onClick={() => setModal(null)} disabled={acting}>
                    Quay lại
                  </button>
                  <button
                    type="button"
                    className="km-btn km-btn-primary"
                    style={{ backgroundColor: 'var(--color-danger)' }}
                    onClick={() => handleDeclineOrCancel(true)}
                    disabled={acting}
                  >
                    {acting ? 'Đang xử lý...' : 'Xác nhận từ chối'}
                  </button>
                </div>
              </>
            )}

            {modal === 'cancel' && (
              <>
                <h2>Hủy yêu cầu Custom?</h2>
                <p className="km-auth-subtitle">
                  Bạn có chắc muốn hủy yêu cầu Custom này? Thao tác không thể hoàn tác.
                </p>
                <div className="km-modal-actions">
                  <button type="button" className="km-btn km-btn-soft" onClick={() => setModal(null)} disabled={acting}>
                    Quay lại
                  </button>
                  <button
                    type="button"
                    className="km-btn km-btn-primary"
                    style={{ backgroundColor: 'var(--color-danger)' }}
                    onClick={() => handleDeclineOrCancel(false)}
                    disabled={acting}
                  >
                    {acting ? 'Đang hủy...' : 'Xác nhận hủy'}
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </>
  );
}

export default CustomerCustomRequestDetailPage;
