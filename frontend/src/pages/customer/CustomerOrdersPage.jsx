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

const STATUS_FILTER_LABELS = ['Đang xử lý', 'Đang giao', 'Hoàn thành', 'Đã hủy'];

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
    <div className="km-order-list">
      <h1 className="km-page-title">
        Đơn hàng của tôi
      </h1>
      <div>
        <div className="km-order-filters" role="group" aria-label="Lọc theo trạng thái đơn hàng" aria-describedby="order-filter-hint">
          <button type="button" className="km-btn km-btn-soft" aria-pressed="true">Tất cả</button>
          {STATUS_FILTER_LABELS.map((label) => (
            <button key={label} type="button" className="km-btn km-btn-ghost" disabled>{label}</button>
          ))}
        </div>
        <p id="order-filter-hint" className="km-field-hint">Bộ lọc trạng thái đang được phát triển.</p>
      </div>

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
          <div className="km-order-cards">
            {result.content.map((order) => (
              <article key={order.id} className="km-panel km-order-card">
                <div className="km-order-card-row">
                  <div>
                    <div className="km-order-code">
                      {order.orderCode}
                    </div>
                    <div className="km-field-hint">
                      Ngày đặt: {formatDate(order.orderDate)} • {paymentMethodLabels[order.paymentMethod] || order.paymentMethod}
                    </div>
                  </div>
                  <div className="km-order-badges">
                    <span className={statusBadgeClass(order.status)}>
                      {orderStatusLabels[order.status] || order.status}
                    </span>
                    {order.status !== 'CANCELLED' && (
                      <span className={order.paymentStatus === 'PAID' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
                        {paymentStatusLabels[order.paymentStatus] || order.paymentStatus}
                      </span>
                    )}
                  </div>
                </div>

                <div className="km-order-preview">
                  <div className="km-order-preview-thumb" aria-hidden="true">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                      <rect x="2" y="6" width="20" height="12" rx="2" />
                      <path d="M6 10h.01M10 10h.01M14 10h.01M18 10h.01M8 14h8" strokeLinecap="round" />
                    </svg>
                  </div>
                  <div>
                    <strong>Sản phẩm trong đơn</strong>
                    <p className="km-field-hint">Xem tên và số lượng trong chi tiết đơn hàng.</p>
                  </div>
                </div>

                <div className="km-order-card-row km-order-card-footer">
                  <div className="km-order-total">
                    {formatVND(order.totalAmount)}
                  </div>
                  <div className="km-order-actions">
                    {order.status === 'COMPLETED' && (
                      <span className="km-order-rebuy">
                        <button type="button" className="km-btn km-btn-ghost" disabled aria-describedby={`rebuy-${order.id}`}>Mua lại</button>
                        <small id={`rebuy-${order.id}`}>Sắp có</small>
                      </span>
                    )}
                    <Link to={`/customer/orders/${order.id}`} className="km-btn km-btn-soft">
                      Xem chi tiết
                    </Link>
                  </div>
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
