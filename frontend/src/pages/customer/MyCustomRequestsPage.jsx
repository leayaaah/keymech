import '../custom.css';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { listMyCustomRequests } from '../../api/customRequestApi';
import CustomStatusBadge from '../../components/CustomStatusBadge';
import Pagination from '../../components/Pagination';
import useAsyncData from '../../hooks/useAsyncData';
import { apiErrorMessage } from '../../utils/customStatus';
import { formatDateTime, formatVND } from '../../utils/format';

/** C-04: danh sách yêu cầu Custom của tôi. */
export default function MyCustomRequestsPage() {
  const [page, setPage] = useState(0);
  const { data, loading, error, reload } = useAsyncData(() => listMyCustomRequests(page, 10), [page]);

  return (
    <main className="cr-page">
      <div className="cr-page__head">
        <h1>Yêu cầu Custom của tôi</h1>
        <Link to="/custom-requests/new" className="cr-btn cr-btn--primary">Gửi yêu cầu mới</Link>
      </div>

      {loading && <p role="status">Đang tải…</p>}

      {error && (
        <div className="cr-alert cr-alert--error" role="alert">
          {apiErrorMessage(error, 'Không tải được danh sách yêu cầu')}{' '}
          <button type="button" className="cr-link" onClick={reload}>Thử lại</button>
        </div>
      )}

      {data && data.content.length === 0 && (
        <p className="cr-empty">
          Bạn chưa có yêu cầu Custom nào. <Link to="/custom-requests/new">Gửi yêu cầu đầu tiên</Link>.
        </p>
      )}

      {data && data.content.length > 0 && (
        <>
          <ul className="cr-list">
            {data.content.map((r) => (
              <li key={r.id} className="cr-card">
                <div className="cr-card__head">
                  <Link to={`/custom-requests/${r.id}`}><strong>{r.requestCode}</strong></Link>
                  <CustomStatusBadge status={r.status} />
                </div>
                <p>{r.layout} · {r.switchType}</p>
                <p className="cr-muted">
                  Gửi lúc {formatDateTime(r.createdAt)}
                  {r.quotedPrice != null && <> · Báo giá {formatVND(r.quotedPrice)}</>}
                </p>
              </li>
            ))}
          </ul>
          <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
        </>
      )}
    </main>
  );
}
