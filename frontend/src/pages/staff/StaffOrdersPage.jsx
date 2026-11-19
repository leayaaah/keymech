import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { staffOrderApi } from '../../api/staffOrderApi';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { formatDateTime } from '../../utils/formatDate';
import formatVND from '../../utils/formatVND';
import { paymentLabels, statusLabels } from './orderLabels';
import './staffOrders.css';

export default function StaffOrdersPage() {
  const [params, setParams] = useSearchParams();
  const query = params.toString();

  return (
    <div className="staff-orders">
      <h1>Quản lý đơn hàng</h1>
      <OrdersList key={query} params={params} setParams={setParams} />
    </div>
  );
}

function OrdersList({ params, setParams }) {
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  const [filterError, setFilterError] = useState('');
  const page = params.get('page') || '0';
  const keyword = params.get('keyword') || '';
  const status = params.get('status') || '';
  const fromDate = params.get('fromDate') || '';
  const toDate = params.get('toDate') || '';

  useEffect(() => {
    let active = true;
    staffOrderApi
      .list({
        page,
        size: 10,
        keyword: keyword || undefined,
        status: status || undefined,
        fromDate: fromDate || undefined,
        toDate: toDate || undefined,
      })
      .then((data) => {
        if (active) setResult(data);
      })
      .catch((err) => {
        if (active)
          setError(
            err.response?.data?.message || 'Không thể tải danh sách đơn.'
          );
      });
    return () => {
      active = false;
    };
  }, [page, keyword, status, fromDate, toDate, retry]);

  function applyFilters(event) {
    event.preventDefault();
    const fields = new FormData(event.currentTarget);
    if (
      fields.get('fromDate') &&
      fields.get('toDate') &&
      fields.get('fromDate') > fields.get('toDate')
    ) {
      setFilterError('Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.');
      return;
    }
    setFilterError('');
    const next = new URLSearchParams();
    for (const [name, value] of fields) {
      if (value.trim()) next.set(name, value.trim());
    }
    setParams(next);
  }

  function goToPage(number) {
    const next = new URLSearchParams(params);
    if (number) next.set('page', number);
    else next.delete('page');
    setParams(next);
  }

  return (
    <>
      <form className="staff-order-controls" onSubmit={applyFilters}>
        <label>
          Mã đơn, họ tên hoặc email
          <input name="keyword" defaultValue={keyword} />
        </label>
        <label>
          Trạng thái
          <select name="status" defaultValue={status}>
            <option value="">Tất cả</option>
            {Object.entries(statusLabels).map(([code, label]) => (
              <option key={code} value={code}>
                {label}
              </option>
            ))}
          </select>
        </label>
        <label>
          Từ ngày
          <input name="fromDate" type="date" defaultValue={fromDate} />
        </label>
        <label>
          Đến ngày
          <input name="toDate" type="date" defaultValue={toDate} />
        </label>
        <button type="submit">Tìm đơn</button>
        <button
          type="reset"
          onClick={() => {
            setFilterError('');
            setParams({});
          }}
        >
          Xóa bộ lọc
        </button>
      </form>
      {filterError && <p role="alert">{filterError}</p>}
      {error ? (
        <ErrorState
          message={error}
          onRetry={() => {
            setError('');
            setRetry((value) => value + 1);
          }}
        />
      ) : !result ? (
        <Loading message="Đang tải đơn hàng..." />
      ) : (
        <>
          {result.content.length === 0 ? (
            <EmptyState title="Không có đơn hàng trên trang này" />
          ) : (
            <div className="staff-order-table">
              <table>
                <thead>
                  <tr>
                    <th>Mã đơn</th>
                    <th>Khách hàng</th>
                    <th>Ngày đặt</th>
                    <th>Trạng thái</th>
                    <th>Thanh toán</th>
                    <th>Tổng tiền</th>
                  </tr>
                </thead>
                <tbody>
                  {result.content.map((order) => (
                    <tr key={order.id}>
                      <td>
                        <Link
                          to={{
                            pathname: `/staff/orders/${order.id}`,
                            search: params.toString(),
                          }}
                        >
                          {order.orderCode}
                        </Link>
                      </td>
                      <td>
                        {order.customerName}
                        <br />
                        {order.customerEmail}
                      </td>
                      <td>{formatDateTime(order.orderDate)}</td>
                      <td>{statusLabels[order.status]}</td>
                      <td>{paymentLabels[order.paymentStatus]}</td>
                      <td>{formatVND(order.totalAmount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {(result.totalPages > 1 || result.page > 0) && (
            <nav
              className="staff-order-controls"
              aria-label="Phân trang đơn hàng"
            >
              <button
                type="button"
                disabled={result.page === 0}
                onClick={() => goToPage(result.page - 1)}
              >
                Trang trước
              </button>
              <span>Trang {result.page + 1}</span>
              <button
                type="button"
                disabled={result.page + 1 >= result.totalPages}
                onClick={() => goToPage(result.page + 1)}
              >
                Trang sau
              </button>
            </nav>
          )}
        </>
      )}
    </>
  );
}
