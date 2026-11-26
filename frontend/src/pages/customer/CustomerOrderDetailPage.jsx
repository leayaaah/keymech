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
import '../../styles/auth.css';

function statusBadgeClass(status) {
  if (status === 'COMPLETED') return 'km-badge km-badge--success';
  if (status === 'CANCELLED') return 'km-badge km-badge--danger';
  if (status === 'SHIPPING' || status === 'CONFIRMED') return 'km-badge km-badge--info';
  return 'km-badge km-badge--warning';
}

/**
 * Trang Đặt hàng thành công (success=true, Figma 3:310)
 * và Trang Chi tiết đơn hàng của Customer (Figma 3:538 & 3:542).
 */
export default function CustomerOrderDetailPage({ success = false }) {
  const { id } = useParams();
  return <OrderDetail key={`${id}-${success}`} id={id} success={success} />;
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
        if (active) {
          setError({
            status: err.response?.status,
            message: err.response?.data?.message || 'Không thể tải chi tiết đơn hàng.',
          });
        }
      });
    return () => {
      active = false;
    };
  }, [id, reload]);

  if (error) {
    return (
      <ErrorState
        title="Lỗi tải đơn hàng"
        message={error.message}
        onRetry={
          !error.status || error.status >= 500
            ? () => {
                setError(null);
                setReload((v) => v + 1);
              }
            : undefined
        }
      />
    );
  }

  if (!order) {
    return <Loading message="Đang tải chi tiết đơn hàng..." />;
  }

  if (success) {
    return (
      <div className="km-success-card">
        <div className="km-success-icon" aria-hidden="true">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M20 6L9 17l-5-5" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
        <h1 className="km-page-heading">Đặt hàng thành công!</h1>
        <p className="km-auth-subtitle" style={{ fontSize: 'var(--text-lg)' }}>
          Cảm ơn {order.receiverName} đã chọn KeyMech. Chúng tôi sẽ sớm xác nhận đơn hàng của bạn.
        </p>
        <p className="km-success-code">Mã đơn: {order.orderCode}</p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', fontSize: 'var(--text-lg)' }}>
          {(order.items ?? []).map((item) => (
            <div key={item.id}>
              {item.productName} × {item.quantity} • {formatVND(item.lineTotal)}
            </div>
          ))}
          <div>
            {paymentMethodLabels[order.paymentMethod] || order.paymentMethod} •{' '}
            {paymentStatusLabels[order.paymentStatus] || order.paymentStatus}
          </div>
          <div>
            {order.receiverName} • {order.receiverPhone}
          </div>
          <div>{order.shippingAddress}</div>
        </div>

        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginTop: '8px' }}>
          <Link to={`/customer/orders/${order.id}`} className="km-btn km-btn-primary">
            Xem đơn hàng
          </Link>
          <Link to="/products" className="km-btn km-btn-soft">
            Tiếp tục mua sắm
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
      <Link to="/customer/orders" className="km-auth-link km-auth-link--sm">
        ← Quay lại đơn hàng của tôi
      </Link>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h1 style={{ fontSize: 'var(--text-3xl)', margin: '0 0 4px 0' }}>
            Đơn hàng {order.orderCode}
          </h1>
          <p className="km-auth-subtitle">Đặt lúc {formatDateTime(order.orderDate)}</p>
        </div>
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <span className={statusBadgeClass(order.status)}>
            {orderStatusLabels[order.status] || order.status}
          </span>
          <span className={order.paymentStatus === 'PAID' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
            {paymentStatusLabels[order.paymentStatus] || order.paymentStatus}
          </span>
        </div>
      </div>

      {order.status === 'CANCELLED' ? (
        <div className="km-alert-error">
          Đơn hàng này đã bị hủy. Tồn kho đã được hoàn lại.
        </div>
      ) : (
        <p className="km-status-flow">
          Mới → Đã xác nhận → Đang giao → Hoàn thành
        </p>
      )}

      <section className="km-panel" style={{ padding: 'var(--space-6)' }}>
        <h2 style={{ fontSize: 'var(--text-xl)' }}>Thông tin giao hàng & Thanh toán</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px' }}>
          <div>
            <div className="km-field-hint">Người nhận</div>
            <strong>{order.receiverName}</strong>
          </div>
          <div>
            <div className="km-field-hint">Số điện thoại</div>
            <strong>{order.receiverPhone}</strong>
          </div>
          <div>
            <div className="km-field-hint">Phương thức thanh toán</div>
            <strong>{paymentMethodLabels[order.paymentMethod] || order.paymentMethod}</strong>
          </div>
          <div style={{ gridColumn: '1 / -1' }}>
            <div className="km-field-hint">Địa chỉ giao hàng</div>
            <strong>{order.shippingAddress}</strong>
          </div>
        </div>
      </section>

      <section className="km-panel" style={{ padding: 'var(--space-6)' }}>
        <h2 style={{ fontSize: 'var(--text-xl)' }}>Sản phẩm trong đơn</h2>
        {(order.items ?? []).map((item) => (
          <div key={item.id} className="km-checkout-summary-item">
            <div>
              <strong>{item.productName}</strong>
              <div className="km-field-hint">
                Đơn giá tại thời điểm mua: {formatVND(item.unitPrice)} × {item.quantity}
              </div>
            </div>
            <strong>{formatVND(item.lineTotal)}</strong>
          </div>
        ))}
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 'var(--text-xl)', fontWeight: 600, paddingTop: '8px' }}>
          <span>Tổng thanh toán</span>
          <span>{formatVND(order.totalAmount)}</span>
        </div>
      </section>
    </div>
  );
}
