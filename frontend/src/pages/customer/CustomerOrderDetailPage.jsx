import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { formatDateTime } from '../../utils/formatDate';
import formatVND from '../../utils/formatVND';
import {
  orderStatusLabels,
  paymentMethodLabels,
  paymentStatusLabels,
} from '../../utils/orderLabels';
import './customerOrders.css';

export default function CustomerOrderDetailPage({ success = false }) {
  const { id } = useParams();
  return <OrderDetail key={id} id={id} success={success} />;
}

function OrderDetail({ id, success }) {
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(null);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    let active = true;
    orderApi
      .getMine(id)
      .then((data) => {
        if (active) setOrder(data);
      })
      .catch((err) => {
        if (active)
          setError({
            status: err.response?.status,
            message:
              err.response?.data?.message || 'Không thể tải chi tiết đơn hàng.',
          });
      });
    return () => {
      active = false;
    };
  }, [id, reload]);

  return (
    <div className="customer-order-page">
      <Link to="/customer/orders">← Về đơn của tôi</Link>
      {error ? (
        <ErrorState
          message={error.message}
          onRetry={
            !error.status || error.status >= 500
              ? () => {
                  setError(null);
                  setReload((value) => value + 1);
                }
              : undefined
          }
        />
      ) : !order ? (
        <Loading message="Đang tải chi tiết đơn..." />
      ) : (
        <>
          <h1>
            {success
              ? 'Đặt hàng thành công'
              : `Chi tiết đơn ${order.orderCode}`}
          </h1>
          {success && (
            <p>
              Mã đơn hàng <strong>{order.orderCode}</strong>
            </p>
          )}
          <section className="order-panel" aria-labelledby="order-info">
            <h2 id="order-info">Thông tin đơn hàng</h2>
            <dl className="order-facts">
              <dt>Ngày đặt</dt>
              <dd>{formatDateTime(order.orderDate)}</dd>
              <dt>Trạng thái</dt>
              <dd>{orderStatusLabels[order.status]}</dd>
              <dt>Phương thức thanh toán</dt>
              <dd>{paymentMethodLabels[order.paymentMethod]}</dd>
              <dt>Thanh toán</dt>
              <dd>{paymentStatusLabels[order.paymentStatus]}</dd>
              <dt>Người nhận</dt>
              <dd>{order.receiverName}</dd>
              <dt>Điện thoại</dt>
              <dd>{order.receiverPhone}</dd>
              <dt>Địa chỉ giao hàng</dt>
              <dd>{order.shippingAddress}</dd>
            </dl>
          </section>
          <section className="order-panel" aria-labelledby="order-items">
            <h2 id="order-items">Sản phẩm</h2>
            <ul className="order-lines">
              {order.items.map((item) => (
                <li key={item.id}>
                  <span>
                    {item.productName}
                    <br />
                    {item.quantity} × {formatVND(item.unitPrice)}
                  </span>
                  <strong>{formatVND(item.lineTotal)}</strong>
                </li>
              ))}
            </ul>
            <p>
              <strong>Tổng tiền {formatVND(order.totalAmount)}</strong>
            </p>
          </section>
          {success && (
            <Link className="order-primary" to="/customer/orders">
              Xem đơn của tôi
            </Link>
          )}
        </>
      )}
    </div>
  );
}
