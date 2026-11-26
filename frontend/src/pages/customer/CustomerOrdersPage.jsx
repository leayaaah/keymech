import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { formatDate } from '../../utils/formatDate';
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
 * Trang Đơn hàng của tôi (Figma frames 3:358, 3:530, 3:534).
 */
export default function CustomerOrdersPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [reload, setReload] = useState(0);

  useEffect(() => {
    let active = true;
    orderApi
      .listMine(page)
      .then((data) => {
        if (active) setResult(data);
      })
      .catch((err) => {
        if (active) {
          setError(err.response?.data?.message || 'Không thể tải danh sách đơn hàng.');
        }
      });
    return () => {
      active = false;
    };
  }, [page, reload]);

  function goToPage(nextPage) {
    setResult(null);
    setPage(nextPage);
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-6)' }}>
      <h1 style={{ fontSize: 'var(--text-4xl)', fontWeight: 600, margin: 0 }}>
        Đơn hàng của tôi
      </h1>

      {error ? (
        <ErrorState
          title="Lỗi tải đơn hàng"
          message={error}
          onRetry={() => {
            setError('');
            setReload((v) => v + 1);
          }}
        />
      ) : !result ? (
        <Loading message="Đang tải đơn hàng..." />
      ) : (result.content ?? []).length === 0 ? (
        <EmptyState
          title="Bạn chưa có đơn hàng nào"
          description="Khi bạn đặt mua bàn phím cơ hoặc linh kiện, đơn hàng sẽ hiển thị tại đây."
          actionText="Khám phá sản phẩm"
          onAction={() => navigate('/products')}
        />
      ) : (
        <>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
            {result.content.map((order) => (
              <article key={order.id} className="km-panel" style={{ padding: 'var(--space-6)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 'var(--text-lg)' }}>
                      {order.orderCode}
                    </div>
                    <div className="km-field-hint">
                      Ngày đặt: {formatDate(order.orderDate)} • {paymentMethodLabels[order.paymentMethod] || order.paymentMethod}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
                    <span className={statusBadgeClass(order.status)}>
                      {orderStatusLabels[order.status] || order.status}
                    </span>
                    <span className={order.paymentStatus === 'PAID' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
                      {paymentStatusLabels[order.paymentStatus] || order.paymentStatus}
                    </span>
                  </div>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px', paddingTop: '8px', borderTop: '1px solid var(--color-border-subtle)' }}>
                  <div style={{ fontSize: 'var(--text-xl)', fontWeight: 600 }}>
                    {formatVND(order.totalAmount)}
                  </div>
                  <Link to={`/customer/orders/${order.id}`} className="km-btn km-btn-soft">
                    Xem chi tiết đơn hàng
                  </Link>
                </div>
              </article>
            ))}
          </div>

          {result.totalPages > 1 && (
            <nav className="km-pagination" aria-label="Phân trang đơn hàng">
              <button
                type="button"
                className="km-page-btn"
                disabled={page === 0}
                onClick={() => goToPage(page - 1)}
              >
                ‹
              </button>
              <span style={{ fontSize: 'var(--text-sm)', padding: '0 8px' }}>
                Trang {page + 1} / {result.totalPages}
              </span>
              <button
                type="button"
                className="km-page-btn"
                disabled={page + 1 >= result.totalPages}
                onClick={() => goToPage(page + 1)}
              >
                ›
              </button>
            </nav>
          )}
        </>
      )}
    </div>
  );
}
