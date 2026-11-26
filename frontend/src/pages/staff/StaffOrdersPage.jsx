import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { staffOrderApi } from '../../api/staffOrderApi';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { formatDateTime } from '../../utils/formatDate';
import formatVND from '../../utils/formatVND';
import { orderStatusLabels, paymentStatusLabels } from '../../utils/orderLabels';
import '../../styles/auth.css';
import '../../styles/portal.css';

function statusBadgeClass(status) {
  if (status === 'COMPLETED') return 'km-badge km-badge--success';
  if (status === 'CANCELLED') return 'km-badge km-badge--danger';
  if (status === 'SHIPPING' || status === 'CONFIRMED') return 'km-badge km-badge--info';
  return 'km-badge km-badge--warning';
}

/**
 * Trang Quản lý đơn hàng Staff/Admin (Figma frame 3:103).
 */
export default function StaffOrdersPage() {
  const [params, setParams] = useSearchParams();
  const query = params.toString();

  return (
    <>
      <div className="km-portal-top">
        <h1 className="km-portal-title">Quản lý đơn hàng</h1>
      </div>
      <OrdersList key={query} params={params} setParams={setParams} />
    </>
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
        if (active) {
          setError(err.response?.data?.message || 'Không thể tải danh sách đơn hàng.');
        }
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
      <form className="km-filter-bar" onSubmit={applyFilters}>
        <div className="km-field">
          <label htmlFor="staff-order-kw" className="km-label">
            Mã đơn, họ tên hoặc email
          </label>
          <input
            id="staff-order-kw"
            name="keyword"
            className="km-input"
            defaultValue={keyword}
            placeholder="ORD-..."
          />
        </div>

        <div className="km-field">
          <label htmlFor="staff-order-status" className="km-label">
            Trạng thái
          </label>
          <select id="staff-order-status" name="status" className="km-select" defaultValue={status}>
            <option value="">Tất cả</option>
            {Object.entries(orderStatusLabels).map(([code, label]) => (
              <option key={code} value={code}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div className="km-field">
          <label htmlFor="staff-order-from" className="km-label">
            Từ ngày
          </label>
          <input id="staff-order-from" name="fromDate" type="date" className="km-input" defaultValue={fromDate} />
        </div>

        <div className="km-field">
          <label htmlFor="staff-order-to" className="km-label">
            Đến ngày
          </label>
          <input id="staff-order-to" name="toDate" type="date" className="km-input" defaultValue={toDate} />
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <button type="submit" className="km-btn km-btn-primary">
            Tìm đơn
          </button>
          <button
            type="reset"
            className="km-btn km-btn-soft"
            onClick={() => {
              setFilterError('');
              setParams({});
            }}
          >
            Xóa lọc
          </button>
        </div>
      </form>

      {filterError && (
        <div role="alert" className="km-alert-error">
          {filterError}
        </div>
      )}

      {error ? (
        <ErrorState
          title="Lỗi tải đơn hàng"
          message={error}
          onRetry={() => {
            setError('');
            setRetry((v) => v + 1);
          }}
        />
      ) : !result ? (
        <Loading message="Đang tải danh sách đơn hàng..." />
      ) : (result.content ?? []).length === 0 ? (
        <EmptyState title="Không tìm thấy đơn hàng nào" />
      ) : (
        <>
          <div className="km-table-wrap">
            <table className="km-table" role="table">
              <thead role="rowgroup">
                <tr role="row">
                  <th scope="col" role="columnheader">Mã đơn</th>
                  <th scope="col" role="columnheader">Khách hàng</th>
                  <th scope="col" role="columnheader">Ngày đặt</th>
                  <th scope="col" role="columnheader">Trạng thái</th>
                  <th scope="col" role="columnheader">Thanh toán</th>
                  <th scope="col" role="columnheader">Tổng tiền</th>
                  <th scope="col" role="columnheader">Thao tác</th>
                </tr>
              </thead>
              <tbody role="rowgroup">
                {result.content.map((order) => (
                  <tr role="row" key={order.id}>
                    <td role="cell" data-label="Mã đơn">
                      <strong>{order.orderCode}</strong>
                    </td>
                    <td role="cell" data-label="Khách hàng">
                      <div>{order.customerName}</div>
                      <div className="km-field-hint">{order.customerEmail}</div>
                    </td>
                    <td role="cell" data-label="Ngày đặt">{formatDateTime(order.orderDate)}</td>
                    <td role="cell" data-label="Trạng thái">
                      <span className={statusBadgeClass(order.status)}>
                        {orderStatusLabels[order.status] || order.status}
                      </span>
                    </td>
                    <td role="cell" data-label="Thanh toán">
                      <span className={order.paymentStatus === 'PAID' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
                        {paymentStatusLabels[order.paymentStatus] || order.paymentStatus}
                      </span>
                    </td>
                    <td role="cell" data-label="Tổng tiền">
                      <strong>{formatVND(order.totalAmount)}</strong>
                    </td>
                    <td role="cell" data-label="Thao tác">
                      <Link
                        to={{ pathname: `/staff/orders/${order.id}`, search: params.toString() }}
                        className="km-btn km-btn-soft km-btn-sm"
                      >
                        Chi tiết
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {(result.totalPages > 1 || result.page > 0) && (
            <div style={{ display: 'flex', justifyContent: 'center' }}>
              <nav className="km-pagination" aria-label="Phân trang đơn hàng">
                <button
                  type="button"
                  className="km-page-btn"
                  disabled={result.page === 0}
                  onClick={() => goToPage(result.page - 1)}
                >
                  ‹
                </button>
                <span style={{ padding: '0 8px', fontSize: 'var(--text-sm)' }}>
                  Trang {result.page + 1} / {result.totalPages || 1}
                </span>
                <button
                  type="button"
                  className="km-page-btn"
                  disabled={result.page + 1 >= result.totalPages}
                  onClick={() => goToPage(result.page + 1)}
                >
                  ›
                </button>
              </nav>
            </div>
          )}
        </>
      )}
    </>
  );
}
