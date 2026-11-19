import { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { staffOrderApi } from '../../api/staffOrderApi';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { formatDateTime } from '../../utils/formatDate';
import formatVND from '../../utils/formatVND';
import { methodLabels, paymentLabels, statusLabels } from './orderLabels';
import './staffOrders.css';

const nextStatus = {
  NEW: 'CONFIRMED',
  CONFIRMED: 'SHIPPING',
  SHIPPING: 'COMPLETED',
};

export default function StaffOrderDetailPage() {
  const { id } = useParams();
  const { search } = useLocation();
  return (
    <div className="staff-orders">
      <Link to={{ pathname: '/staff/orders', search }}>
        ← Về danh sách đơn hàng
      </Link>
      <OrderDetail key={id} id={id} />
    </div>
  );
}

function OrderDetail({ id }) {
  const [order, setOrder] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [notice, setNotice] = useState(null);
  const [reload, setReload] = useState(0);
  const [busy, setBusy] = useState(false);
  const saving = useRef(false);

  useEffect(() => {
    let active = true;
    staffOrderApi
      .detail(id)
      .then((data) => {
        if (active) setOrder(data);
      })
      .catch((error) => {
        if (active)
          setLoadError(
            error.response?.data?.message || 'Không thể tải chi tiết đơn.'
          );
      });
    return () => {
      active = false;
    };
  }, [id, reload]);

  function refresh() {
    setOrder(null);
    setLoadError('');
    setReload((value) => value + 1);
  }

  async function update(operation) {
    if (saving.current) return;
    saving.current = true;
    setBusy(true);
    setNotice(null);
    try {
      setOrder(await operation());
      setNotice({ message: 'Đã cập nhật đơn hàng.' });
    } catch (error) {
      const status = error.response?.status;
      const shouldRefresh =
        !status || status >= 500 || status === 409 || status === 404;
      setNotice({
        error: true,
        message:
          error.response?.data?.message ||
          'Chưa xác định được kết quả cập nhật.',
      });
      if (shouldRefresh) refresh();
    } finally {
      saving.current = false;
      setBusy(false);
    }
  }

  function changeQuantity(event, item) {
    event.preventDefault();
    const quantity = Number(new FormData(event.currentTarget).get('quantity'));
    if (
      Number.isInteger(quantity) &&
      quantity >= 1 &&
      quantity !== item.quantity
    ) {
      update(() => staffOrderApi.updateQuantity(id, item.id, quantity));
    }
  }

  const canEditItems = order?.status === 'NEW' || order?.status === 'CONFIRMED';
  const canPay =
    order?.status !== 'CANCELLED' && order?.paymentStatus === 'UNPAID';
  const next = nextStatus[order?.status];

  return (
    <>
      {notice && (
        <p role={notice.error ? 'alert' : 'status'}>{notice.message}</p>
      )}
      {loadError ? (
        <ErrorState message={loadError} onRetry={refresh} />
      ) : !order ? (
        <Loading message="Đang tải chi tiết đơn..." />
      ) : (
        <>
          <h1>Đơn {order.orderCode}</h1>
          <p>
            {formatDateTime(order.orderDate)} · {statusLabels[order.status]}
          </p>
          <p>
            {methodLabels[order.paymentMethod]} ·{' '}
            {paymentLabels[order.paymentStatus]}
          </p>
          <p>
            Khách hàng <strong>{order.customerName}</strong> ·{' '}
            {order.customerEmail}
          </p>
          <p>
            Giao cho <strong>{order.receiverName}</strong> ·{' '}
            {order.receiverPhone} · {order.shippingAddress}
          </p>
          <div className="staff-order-controls">
            {next && (
              <button
                type="button"
                disabled={busy}
                onClick={() =>
                  update(() => staffOrderApi.updateStatus(id, next))
                }
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
                disabled={busy}
                onClick={() => {
                  if (
                    window.confirm('Xác nhận đã nhận tiền cho đơn hàng này?')
                  ) {
                    update(() => staffOrderApi.markPaid(id));
                  }
                }}
              >
                Xác nhận đã nhận tiền
              </button>
            )}
            {canEditItems && canPay && (
              <button
                type="button"
                disabled={busy}
                onClick={() => {
                  if (
                    window.confirm('Hủy đơn này và hoàn sản phẩm về tồn kho?')
                  ) {
                    update(() => staffOrderApi.updateStatus(id, 'CANCELLED'));
                  }
                }}
              >
                Hủy đơn
              </button>
            )}
          </div>
          <h2>Sản phẩm</h2>
          <div className="staff-order-table">
            <table>
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th>Đơn giá</th>
                  <th>Số lượng</th>
                  <th>Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                {order.items.map((item) => (
                  <tr key={item.id}>
                    <td>{item.productName}</td>
                    <td>{formatVND(item.unitPrice)}</td>
                    <td>
                      {canEditItems ? (
                        <form
                          className="staff-order-controls"
                          key={item.quantity}
                          onSubmit={(event) => changeQuantity(event, item)}
                        >
                          <input
                            type="number"
                            name="quantity"
                            min={1}
                            max={2147483647}
                            step={1}
                            required
                            defaultValue={item.quantity}
                            disabled={busy}
                            aria-label={`Số lượng ${item.productName}`}
                          />
                          <button
                            type="submit"
                            disabled={busy}
                            aria-label={`Lưu số lượng ${item.productName}`}
                          >
                            Lưu
                          </button>
                        </form>
                      ) : (
                        item.quantity
                      )}
                    </td>
                    <td>{formatVND(item.lineTotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p>
            <strong>Tổng tiền {formatVND(order.totalAmount)}</strong>
          </p>
        </>
      )}
    </>
  );
}
