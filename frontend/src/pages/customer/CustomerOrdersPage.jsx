import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import EmptyState from '../../components/EmptyState';
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

export default function CustomerOrdersPage() {
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
        if (active)
          setError(err.response?.data?.message || 'Không thể tải đơn hàng.');
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
    <div className="customer-order-page">
      <h1>Đơn của tôi</h1>
      {error ? (
        <ErrorState
          message={error}
          onRetry={() => {
            setError('');
            setReload((value) => value + 1);
          }}
        />
      ) : !result ? (
        <Loading message="Đang tải đơn hàng..." />
      ) : result.totalElements === 0 ? (
        <EmptyState title="Bạn chưa có đơn hàng nào" />
      ) : (
        <>
          <ul className="order-list">
            {result.content.map((order) => (
              <li key={order.id} className="order-panel">
                <h2>
                  <Link to={`/customer/orders/${order.id}`}>
                    {order.orderCode}
                  </Link>
                </h2>
                <p>
                  {formatDateTime(order.orderDate)} ·{' '}
                  {orderStatusLabels[order.status]}
                </p>
                <p>
                  {paymentMethodLabels[order.paymentMethod]} ·{' '}
                  {paymentStatusLabels[order.paymentStatus]}
                </p>
                <strong>{formatVND(order.totalAmount)}</strong>
              </li>
            ))}
          </ul>
          {result.totalPages > 1 && (
            <nav className="order-pagination" aria-label="Phân trang đơn hàng">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => goToPage(page - 1)}
              >
                Trang trước
              </button>
              <span>
                Trang {page + 1} / {result.totalPages}
              </span>
              <button
                type="button"
                disabled={page + 1 >= result.totalPages}
                onClick={() => goToPage(page + 1)}
              >
                Trang sau
              </button>
            </nav>
          )}
        </>
      )}
      <Link to="/products">Tiếp tục mua sắm</Link>
    </div>
  );
}
