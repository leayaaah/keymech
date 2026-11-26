import { useEffect, useRef, useState } from 'react';
import { adminUserApi } from '../../api/adminUserApi';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';
import '../../styles/portal.css';

const roleLabels = {
  CUSTOMER: 'Khách hàng',
  STAFF: 'Nhân viên',
  ADMIN: 'Quản trị viên',
};

/**
 * Trang Quản lý người dùng & Tạo tài khoản nhân viên (Figma frames 3:3826, 3:3827, 3:6220).
 * Quy tắc:
 * - Không sửa, ẩn hoặc đặt lại mật khẩu tài khoản ADMIN.
 * - Tài khoản mới tạo có vai trò cố định STAFF.
 */
export default function AdminStaffPage() {
  const [query, setQuery] = useState({ keyword: '', role: '', page: 0 });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [reload, setReload] = useState(0);
  const [editor, setEditor] = useState(null);
  const [notice, setNotice] = useState(null);
  const [busy, setBusy] = useState(false);
  const saving = useRef(false);

  useEffect(() => {
    let active = true;
    adminUserApi
      .list({
        keyword: query.keyword || undefined,
        role: query.role || undefined,
        page: query.page,
        size: 20,
        sort: 'id,asc',
      })
      .then((data) => {
        if (active) setResult(data);
      })
      .catch((err) => {
        if (active) {
          setError(err.response?.data?.message || 'Không thể tải danh sách người dùng.');
        }
      });
    return () => {
      active = false;
    };
  }, [query, reload]);

  function load(nextQuery) {
    setResult(null);
    setError('');
    setQuery(nextQuery);
  }

  function applyFilters(event) {
    event.preventDefault();
    const fields = new FormData(event.currentTarget);
    load({
      keyword: fields.get('keyword').trim(),
      role: fields.get('role'),
      page: 0,
    });
  }

  function refresh() {
    setError('');
    setResult(null);
    setReload((v) => v + 1);
  }

  async function mutate(operation, successMessage) {
    if (saving.current) return;
    saving.current = true;
    setBusy(true);
    setNotice(null);
    try {
      await operation();
      setEditor(null);
      toast.success(successMessage);
      refresh();
    } catch (err) {
      const status = err.response?.status;
      const uncertain = !status || status >= 500;
      setNotice({
        error: true,
        message: uncertain
          ? 'Chưa xác định được kết quả. Hãy kiểm tra danh sách trước khi thao tác lại.'
          : err.response?.data?.message || 'Không thể cập nhật người dùng.',
      });
      if (uncertain || status === 404 || status === 409) {
        if (uncertain || status === 404) setEditor(null);
        refresh();
      }
    } finally {
      saving.current = false;
      setBusy(false);
    }
  }

  function saveUser(event) {
    event.preventDefault();
    const fields = new FormData(event.currentTarget);
    const details = {
      fullName: fields.get('fullName').trim(),
      phone: fields.get('phone').trim(),
      address: fields.get('address').trim(),
    };
    if (!details.fullName) {
      setNotice({ error: true, message: 'Vui lòng nhập họ tên.' });
      return;
    }

    if (editor.id) {
      mutate(() => adminUserApi.update(editor.id, details), 'Đã lưu thông tin người dùng.');
    } else {
      const email = fields.get('email').trim();
      const password = fields.get('password');
      if (!email || !password || password.length < 8) {
        setNotice({
          error: true,
          message: 'Vui lòng nhập email hợp lệ và mật khẩu ban đầu từ 8 ký tự trở lên.',
        });
        return;
      }
      mutate(
        () => adminUserApi.createStaff({ ...details, email, password }),
        'Đã tạo tài khoản nhân viên (STAFF).'
      );
    }
  }

  function toggle(user) {
    if (user.role === 'ADMIN') return;
    const label = user.enabled ? 'Ẩn' : 'Hiện';
    if (window.confirm(`${label} tài khoản ${user.fullName}?`)) {
      mutate(
        () => adminUserApi.toggleVisibility(user.id),
        `Đã ${label.toLowerCase()} tài khoản ${user.fullName}.`
      );
    }
  }

  return (
    <>
      <div className="km-portal-top">
        <div>
          <h1 className="km-portal-title">Quản lý người dùng</h1>
          <p className="km-auth-subtitle">
            Tạo tài khoản nhân viên (vai trò cố định STAFF) và quản lý trạng thái tài khoản.
          </p>
        </div>
        <button
          type="button"
          className="km-btn km-btn-primary"
          disabled={busy}
          onClick={() => {
            setEditor({});
            setNotice(null);
          }}
        >
          + Tạo tài khoản nhân viên
        </button>
      </div>

      <form className="km-filter-bar" onSubmit={applyFilters}>
        <div className="km-field">
          <label htmlFor="admin-user-kw" className="km-label">
            Họ tên
          </label>
          <input id="admin-user-kw" type="search" name="keyword" className="km-input" placeholder="Tìm theo họ tên..." />
        </div>

        <div className="km-field" style={{ maxWidth: '220px' }}>
          <label htmlFor="admin-user-role" className="km-label">
            Vai trò
          </label>
          <select id="admin-user-role" name="role" className="km-select" defaultValue="">
            <option value="">Tất cả</option>
            <option value="CUSTOMER">Khách hàng</option>
            <option value="STAFF">Nhân viên</option>
          </select>
        </div>

        <button type="submit" className="km-btn km-btn-primary" disabled={busy}>
          Tìm kiếm
        </button>
      </form>

      {notice && (
        <div role={notice.error ? 'alert' : 'status'} className={notice.error ? 'km-alert-error' : 'km-alert-success'}>
          {notice.message}
        </div>
      )}

      {error ? (
        <ErrorState title="Lỗi tải người dùng" message={error} onRetry={refresh} />
      ) : !result ? (
        <Loading message="Đang tải danh sách người dùng..." />
      ) : (result.content ?? []).length === 0 ? (
        <EmptyState title="Không tìm thấy người dùng nào" />
      ) : (
        <>
          <div className="km-table-wrap">
            <table className="km-table">
              <thead>
                <tr>
                  <th>Họ tên</th>
                  <th>Email</th>
                  <th>Vai trò</th>
                  <th>Trạng thái</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {result.content.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <strong>{user.fullName}</strong>
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <span className={user.role === 'ADMIN' ? 'km-badge km-badge--warning' : 'km-badge km-badge--info'}>
                        {roleLabels[user.role] || user.role}
                      </span>
                    </td>
                    <td>
                      <span className={user.enabled ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
                        {user.enabled ? 'Đang hoạt động' : 'Đã ẩn'}
                      </span>
                    </td>
                    <td>
                      {user.role !== 'ADMIN' ? (
                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            type="button"
                            className="km-btn km-btn-soft km-btn-sm"
                            disabled={busy}
                            onClick={() => {
                              setEditor(user);
                              setNotice(null);
                            }}
                          >
                            Sửa
                          </button>
                          <button
                            type="button"
                            className={`km-btn km-btn-sm ${user.enabled ? 'km-btn-danger' : 'km-btn-primary'}`}
                            disabled={busy}
                            onClick={() => toggle(user)}
                          >
                            {user.enabled ? 'Ẩn' : 'Hiện'}
                          </button>
                        </div>
                      ) : (
                        <span className="km-field-hint">Được bảo vệ</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {(result.totalPages > 1 || query.page > 0) && (
            <div style={{ display: 'flex', justifyContent: 'center' }}>
              <nav className="km-pagination" aria-label="Phân trang người dùng">
                <button
                  type="button"
                  className="km-page-btn"
                  disabled={query.page === 0 || busy}
                  onClick={() => load({ ...query, page: query.page - 1 })}
                >
                  ‹
                </button>
                <span style={{ padding: '0 8px', fontSize: 'var(--text-sm)' }}>
                  Trang {query.page + 1} / {result.totalPages || 1}
                </span>
                <button
                  type="button"
                  className="km-page-btn"
                  disabled={query.page + 1 >= result.totalPages || busy}
                  onClick={() => load({ ...query, page: query.page + 1 })}
                >
                  ›
                </button>
              </nav>
            </div>
          )}
        </>
      )}

      {/* Modal Tạo Staff / Sửa thông tin (Figma 3:3827) */}
      {editor && (
        <div className="km-modal-backdrop" role="dialog" aria-modal="true">
          <form className="km-modal" key={editor.id || 'create'} onSubmit={saveUser}>
            <h2>{editor.id ? 'Chỉnh sửa người dùng' : 'Tạo tài khoản nhân viên'}</h2>
            {!editor.id && (
              <p className="km-field-hint">Vai trò được gán cố định là Nhân viên (STAFF).</p>
            )}

            <div className="km-field">
              <label htmlFor="modal-fullname" className="km-label">
                Họ tên <span className="km-required">*</span>
              </label>
              <input
                id="modal-fullname"
                name="fullName"
                className="km-input"
                required
                maxLength={100}
                defaultValue={editor.fullName || ''}
              />
            </div>

            {!editor.id && (
              <>
                <div className="km-field">
                  <label htmlFor="modal-email" className="km-label">
                    Email <span className="km-required">*</span>
                  </label>
                  <input
                    id="modal-email"
                    name="email"
                    type="email"
                    className="km-input"
                    required
                    maxLength={150}
                  />
                </div>
                <div className="km-field">
                  <label htmlFor="modal-password" className="km-label">
                    Mật khẩu ban đầu (≥ 8 ký tự) <span className="km-required">*</span>
                  </label>
                  <input
                    id="modal-password"
                    name="password"
                    type="password"
                    className="km-input"
                    minLength={8}
                    autoComplete="new-password"
                    required
                  />
                </div>
              </>
            )}

            <div className="km-field">
              <label htmlFor="modal-phone" className="km-label">
                Số điện thoại
              </label>
              <input
                id="modal-phone"
                name="phone"
                type="tel"
                className="km-input"
                maxLength={15}
                defaultValue={editor.phone || ''}
              />
            </div>

            <div className="km-field">
              <label htmlFor="modal-address" className="km-label">
                Địa chỉ
              </label>
              <textarea
                id="modal-address"
                name="address"
                rows={2}
                className="km-textarea"
                maxLength={255}
                defaultValue={editor.address || ''}
              />
            </div>

            <div className="km-modal-actions">
              <button type="button" className="km-btn km-btn-soft" disabled={busy} onClick={() => setEditor(null)}>
                Đóng
              </button>
              <button type="submit" className="km-btn km-btn-primary" disabled={busy}>
                {busy ? 'Đang lưu...' : editor.id ? 'Lưu thông tin' : 'Tạo tài khoản Staff'}
              </button>
            </div>
          </form>
        </div>
      )}
    </>
  );
}
