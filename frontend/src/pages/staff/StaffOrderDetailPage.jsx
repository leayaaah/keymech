import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { staffOrderApi } from '../../api/staffOrderApi';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { formatDateTime } from '../../utils/formatDate';
import formatVND from '../../utils/formatVND';
import {
  orderStatusLabels,
  paymentMethodLabels,
  paymentStatusLabels,
} from '../../utils/orderLabels';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';
import '../../styles/portal.css';

const nextStatusMap = {
  NEW: 'CONFIRMED',
  CONFIRMED: 'SHIPPING',
  SHIPPING: 'COMPLETED',
};

/**
 * Trang Chi tiết đơn hàng Staff/Admin (Figma frames 3:104 & 3:109 modal hủy đơn).
 * - Chỉ sửa số lượng khi NEW hoặc CONFIRMED.
 * - Chỉ hủy khi UNPAID; nếu 409 PAYMENT_NOT_EDITABLE → hiện đúng thông báo quy định.
 */
export default function StaffOrderDetailPage() {
  const { id } = useParams();
  const { search } = useLocation();
  return (
    <>
      <Link to={{ pathname: '/staff/orders', search }} className="km-auth-link km-auth-link--sm">
        ← Về danh sách đơn hàng
      </Link>
      <OrderDetail key={id} id={id} />
    </>
  );
}

function OrderDetail({ id }) {
  const [order, setOrder] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [notice, setNotice] = useState(null);
  const [reload, setReload] = useState(0);
  const [busy, setBusy] = useState(false);
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const saving = useRef(false);

  useEffect(() => {
    let active = true;
    staffOrderApi
      .detail(id)
      .then((data) => {
        if (active) setOrder(data);
      })
      .catch((error) => {
        if (active) {
          setLoadError(error.response?.data?.message || 'Không thể tải chi tiết đơn hàng.');
        }
      });
    return () => {
      active = false;
    };
  }, [id, reload]);

  function refresh() {
    setOrder(null);
    setLoadError('');
    setReload((v) => v + 1);
  }

  async function update(operation) {
    if (saving.current) return;
    saving.current = true;
    setBusy(true);
    setNotice(null);
    try {
      const updated = await operation();
      setOrder(updated);
      setCancelModalOpen(false);
      toast.success('Đã cập nhật đơn hàng.');
    } catch (error) {
      const status = error.response?.status;
      const body = error.response?.data;
      const isPaidLock = status === 409 && body?.error === 'PAYMENT_NOT_EDITABLE';
      const msg = isPaidLock
        ? 'Đơn hàng đã thanh toán, không thể hủy hoặc sửa số lượng.'
        : body?.message || 'Không thể cập nhật đơn hàng.';
      setNotice({ error: true, message: msg });
      setCancelModalOpen(false);
      if (!status || status >= 500 || status === 409 || status === 404) {
        refresh();
      }
    } finally {
      saving.current = false;
      setBusy(false);
    }
  }

  function changeQuantity(event, item) {
    event.preventDefault();
    const quantity = Number(new FormData(event.currentTarget).get('quantity'));
    if (Number.isInteger(quantity) && quantity >= 1 && quantity !== item.quantity) {
      update(() => staffOrderApi.updateQuantity(id, item.id, quantity));
    }
  }

  if (loadError) {
    return <ErrorState title="Lỗi tải đơn hàng" message={loadError} onRetry={refresh} />;
  }
  if (!order) {
    return <Loading message="Đang tải chi tiết đơn hàng..." />;
  }

  const canEditItems =
    (order.status === 'NEW' || order.status === 'CONFIRMED') &&
    order.paymentStatus === 'UNPAID';
  const canPay = order.status !== 'CANCELLED' && order.paymentStatus === 'UNPAID';
  const canCancel =
    (order.status === 'NEW' || order.status === 'CONFIRMED') &&
    order.paymentStatus === 'UNPAID';
  const next = nextStatusMap[order.status];

  return (
    <>
      {notice && (
        <div role={notice.error ? 'alert' : 'status'} className={notice.error ? 'km-alert-error' : 'km-alert-success'}>
          {notice.message}
        </div>
      )}

      <div className="km-portal-top">
        <div>
          <h1 className="km-portal-title">Đơn hàng {order.orderCode}</h1>
          <p className="km-auth-subtitle">Đặt lúc {formatDateTime(order.orderDate)}</p>
        </div>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <span className="km-badge km-badge--info">
            {orderStatusLabels[order.status] || order.status}
          </span>
          <span className={order.paymentStatus === 'PAID' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
            {paymentStatusLabels[order.paymentStatus] || order.paymentStatus}
          </span>
        </div>
      </div>

      {/* Hành động */}
      <div className="km-panel" style={{ padding: 'var(--space-5)' }}>
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          {next && (
            <button
              type="button"
              className="km-btn km-btn-primary"
              disabled={busy}
              onClick={() => update(() => staffOrderApi.updateStatus(id, next))}
            >
              {next === 'CONFIRMED'
                ? 'Xác nhận đơn'
                : next === 'SHIPPING'
                  ? 'Bắt đầu giao'
                  : 'Hoàn thành đơn'}
            </button>
          )}
          {canPay && (
            <button
              type="button"
              className="km-btn km-btn-soft"
              disabled={busy}
              onClick={() => update(() => staffOrderApi.markPaid(id))}
            >
              Xác nhận đã thanh toán
            </button>
          )}
          {canCancel && (
            <button
              type="button"
              className="km-btn km-btn-danger"
              disabled={busy}
              onClick={() => setCancelModalOpen(true)}
            >
              Hủy đơn
            </button>
          )}
        </div>
      </div>

      {/* Thông tin khách hàng & giao hàng */}
      <div className="km-form-row-2">
        <section className="km-panel" style={{ padding: 'var(--space-5)' }}>
          <h2 style={{ fontSize: 'var(--text-xl)' }}>Khách hàng</h2>
          <div>
            <strong>{order.customerName}</strong>
          </div>
          <div className="km-field-hint">{order.customerEmail}</div>
          <div>Phương thức: {paymentMethodLabels[order.paymentMethod] || order.paymentMethod}</div>
        </section>

        <section className="km-panel" style={{ padding: 'var(--space-5)' }}>
          <h2 style={{ fontSize: 'var(--text-xl)' }}>Người nhận</h2>
          <div>
            <strong>{order.receiverName}</strong> • {order.receiverPhone}
          </div>
          <div>{order.shippingAddress}</div>
        </section>
      </div>

      {/* Bảng sản phẩm */}
      <div className="km-table-wrap">
        <table className="km-table" role="table">
          <thead role="rowgroup">
            <tr role="row">
              <th scope="col" role="columnheader">Sản phẩm</th>
              <th scope="col" role="columnheader">Đơn giá</th>
              <th scope="col" role="columnheader">Số lượng</th>
              <th scope="col" role="columnheader">Thành tiền</th>
            </tr>
          </thead>
          <tbody role="rowgroup">
            {(order.items ?? []).map((item) => (
              <tr role="row" key={item.id}>
                <td role="cell" data-label="Sản phẩm">{item.productName}</td>
                <td role="cell" data-label="Đơn giá">{formatVND(item.unitPrice)}</td>
                <td role="cell" data-label="Số lượng">
                  {canEditItems ? (
                    <form
                      key={item.quantity}
                      style={{ display: 'inline-flex', gap: '8px', alignItems: 'center' }}
                      onSubmit={(event) => changeQuantity(event, item)}
                    >
                      <input
                        type="number"
                        name="quantity"
                        min={1}
                        step={1}
                        required
                        defaultValue={item.quantity}
                        disabled={busy}
                        className="km-input"
                        style={{ width: '84px', padding: '6px 10px' }}
                        aria-label={`Số lượng ${item.productName}`}
                      />
                      <button type="submit" className="km-btn km-btn-soft km-btn-sm" disabled={busy}>
                        Lưu
                      </button>
                    </form>
                  ) : (
                    item.quantity
                  )}
                </td>
                <td role="cell" data-label="Thành tiền">
                  <strong>{formatVND(item.lineTotal)}</strong>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div style={{ textAlign: 'right', fontSize: 'var(--text-2xl)', fontWeight: 600 }}>
        Tổng cộng: {formatVND(order.totalAmount)}
      </div>

      {/* Modal Xác nhận hủy đơn (Figma 3:109) */}
      {cancelModalOpen && (
        <div className="km-modal-backdrop" role="dialog" aria-modal="true">
          <div className="km-modal">
            <h2>Xác nhận hủy đơn hàng?</h2>
            <p className="km-auth-subtitle">
              Hủy đơn <strong>{order.orderCode}</strong> sẽ hoàn trả toàn bộ số lượng sản phẩm về tồn kho. Thao tác này không thể hoàn tác.
            </p>
            <div className="km-modal-actions">
              <button
                type="button"
                className="km-btn km-btn-soft"
                disabled={busy}
                onClick={() => setCancelModalOpen(false)}
              >
                Quay lại
              </button>
              <button
                type="button"
                className="km-btn km-btn-danger"
                disabled={busy}
                onClick={() => update(() => staffOrderApi.updateStatus(id, 'CANCELLED'))}
              >
                {busy ? 'Đang hủy...' : 'Xác nhận hủy đơn'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
