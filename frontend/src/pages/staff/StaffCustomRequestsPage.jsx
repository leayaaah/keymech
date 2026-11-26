import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import customRequestApi from '../../api/customRequestApi';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import formatVND from '../../utils/formatVND';
import { formatDate } from '../../utils/formatDate';
import { customStatusLabels } from '../../utils/orderLabels';
import { getFeatureErrorMessage } from '../../utils/apiError';
import '../../styles/auth.css';
import '../../styles/portal.css';

function customBadgeClass(status) {
  if (status === 'COMPLETED' || status === 'ACCEPTED') return 'km-badge km-badge--success';
  if (status === 'CANCELLED' || status === 'REJECTED') return 'km-badge km-badge--danger';
  if (status === 'QUOTED' || status === 'IN_PROGRESS') return 'km-badge km-badge--info';
  return 'km-badge km-badge--warning';
}

/**
 * Trang Danh sách yêu cầu Custom cho Staff/Admin (Figma frame 3:107).
 */
export function StaffCustomRequestsPage() {
  const [statusFilter, setStatusFilter] = useState('');
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;
    customRequestApi
      .listForStaff({ status: statusFilter || undefined })
      .then((res) => {
        if (active) {
          setRequests(res?.content ?? []);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(getFeatureErrorMessage(err, 'Không thể tải danh sách yêu cầu Custom.'));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [statusFilter, reloadKey]);

  return (
    <>
      <div className="km-portal-top">
        <h1 className="km-portal-title">Yêu cầu Custom phím</h1>
      </div>

      <div className="km-filter-bar">
        <div className="km-field" style={{ maxWidth: '280px' }}>
          <label htmlFor="staff-custom-status" className="km-label">
            Lọc theo trạng thái
          </label>
          <select
            id="staff-custom-status"
            className="km-select"
            value={statusFilter}
            onChange={(e) => {
              setLoading(true);
              setStatusFilter(e.target.value);
            }}
          >
            <option value="">Tất cả trạng thái</option>
            {Object.entries(customStatusLabels).map(([code, label]) => (
              <option key={code} value={code}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <Loading message="Đang tải yêu cầu Custom..." />
      ) : error ? (
        <ErrorState
          title="Lỗi tải yêu cầu Custom"
          message={error}
          onRetry={() => {
            setLoading(true);
            setError('');
            setReloadKey((k) => k + 1);
          }}
        />
      ) : requests.length === 0 ? (
        <EmptyState title="Không có yêu cầu Custom nào" />
      ) : (
        <div className="km-table-wrap">
          <table className="km-table">
            <thead>
              <tr>
                <th>Mã yêu cầu</th>
                <th>Khách hàng</th>
                <th>Cấu hình</th>
                <th>Báo giá / Ngân sách</th>
                <th>Ngày gửi</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((req) => (
                <tr key={req.id}>
                  <td>
                    <strong>{req.requestCode || `CUS-${req.id}`}</strong>
                  </td>
                  <td>
                    <div>{req.customerName}</div>
                    <div className="km-field-hint">{req.customerEmail}</div>
                  </td>
                  <td>
                    {req.layout} • {req.switchType} ({req.switchName})
                  </td>
                  <td>
                    {req.quotedPrice
                      ? formatVND(req.quotedPrice)
                      : req.budget
                        ? `NS: ${formatVND(req.budget)}`
                        : '—'}
                  </td>
                  <td>{formatDate(req.createdAt)}</td>
                  <td>
                    <span className={customBadgeClass(req.status)}>
                      {customStatusLabels[req.status] || req.status}
                    </span>
                  </td>
                  <td>
                    <Link to={`/staff/custom-requests/${req.id}`} className="km-btn km-btn-soft km-btn-sm">
                      Xử lý
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default StaffCustomRequestsPage;
