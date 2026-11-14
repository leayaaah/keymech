<<<<<<< HEAD
import '../custom.css';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { listStaffCustomRequests } from '../../api/customRequestApi';
import CustomStatusBadge from '../../components/CustomStatusBadge';
import Pagination from '../../components/Pagination';
import useAsyncData from '../../hooks/useAsyncData';
import { STATUS_LABEL, STATUS_OPTIONS, apiErrorMessage } from '../../utils/customStatus';
import { formatDateTime, formatVND } from '../../utils/format';

/** A-07: danh sách mọi yêu cầu Custom, lọc theo trạng thái (Staff và Admin). */
export default function StaffCustomRequestsPage() {
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const { data, loading, error, reload } = useAsyncData(
    () => listStaffCustomRequests({ status, page, size: 10 }),
    [status, page],
  );

  return (
    <main className="cr-page cr-page--wide">
      <div className="cr-page__head">
        <h1>Yêu cầu Custom</h1>
        <div className="cr-field cr-field--inline">
          <label htmlFor="status-filter">Trạng thái</label>
          <select id="status-filter" value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
            <option value="">Tất cả</option>
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>{STATUS_LABEL[s]}</option>
            ))}
          </select>
        </div>
      </div>

      {loading && <p role="status">Đang tải…</p>}

      {error && (
        <div className="cr-alert cr-alert--error" role="alert">
          {apiErrorMessage(error, 'Không tải được danh sách yêu cầu')}{' '}
          <button type="button" className="cr-link" onClick={reload}>Thử lại</button>
        </div>
      )}

      {data && data.content.length === 0 && <p className="cr-empty">Không có yêu cầu nào{status ? ' ở trạng thái này' : ''}.</p>}

      {data && data.content.length > 0 && (
        <>
          <div className="cr-table-wrap">
            <table className="cr-table">
              <thead>
                <tr>
                  <th scope="col">Mã</th>
                  <th scope="col">Khách hàng</th>
                  <th scope="col">Cấu hình</th>
                  <th scope="col">Ngân sách</th>
                  <th scope="col">Báo giá</th>
                  <th scope="col">Trạng thái</th>
                  <th scope="col">Gửi lúc</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((r) => (
                  <tr key={r.id}>
                    <td><Link to={`/staff/custom-requests/${r.id}`}>{r.requestCode}</Link></td>
                    <td>{r.customer?.fullName}</td>
                    <td>{r.layout} · {r.switchType}</td>
                    <td>{r.budget != null ? formatVND(r.budget) : '—'}</td>
                    <td>{r.quotedPrice != null ? formatVND(r.quotedPrice) : '—'}</td>
                    <td><CustomStatusBadge status={r.status} /></td>
                    <td>{formatDateTime(r.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
        </>
      )}
    </main>
  );
}
=======
import EmptyState from '../../components/EmptyState';

/**
 * Trang duyệt yêu cầu Custom phím dành cho Nhân viên.
 */
export function StaffCustomRequestsPage() {
  const requests = [];

  return (
    <div>
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: '#1e293b',
          marginBottom: '1rem',
        }}
      >
        Yêu cầu Custom bàn phím cần duyệt
      </h1>
      <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>
        Khu vực thợ phím kiểm tra cấu hình, gửi báo giá và theo dõi trạng thái gia công.
      </p>

      {requests.length === 0 ? (
        <EmptyState
          title="Không có yêu cầu Custom nào đang chờ"
          description="Hiện tại không có yêu cầu Custom nào ở trạng thái chờ duyệt."
        />
      ) : (
        <div>
          <p>Danh sách yêu cầu custom...</p>
        </div>
      )}
    </div>
  );
}

export default StaffCustomRequestsPage;
>>>>>>> origin/main
