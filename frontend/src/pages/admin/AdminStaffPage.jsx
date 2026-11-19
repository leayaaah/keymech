import { useEffect, useRef, useState } from 'react';
import { adminUserApi } from '../../api/adminUserApi';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import Loading from '../../components/Loading';
import './adminUsers.css';

const roleLabels = {
  CUSTOMER: 'Khách hàng',
  STAFF: 'Nhân viên',
  ADMIN: 'Quản trị viên',
};

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
        if (active)
          setError(
            err.response?.data?.message || 'Không thể tải danh sách người dùng.'
          );
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
    setReload((value) => value + 1);
  }

  async function mutate(operation, successMessage) {
    if (saving.current) return;
    saving.current = true;
    setBusy(true);
    setNotice(null);
    try {
      await operation();
      setEditor(null);
      setNotice({ message: successMessage });
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
      mutate(
        () => adminUserApi.update(editor.id, details),
        'Đã lưu thông tin người dùng.'
      );
    } else {
      const email = fields.get('email').trim();
      const password = fields.get('password');
      if (!email || !password.trim()) {
        setNotice({
          error: true,
          message: 'Vui lòng nhập email và mật khẩu ban đầu.',
        });
        return;
      }
      mutate(
        () => adminUserApi.createStaff({ ...details, email, password }),
        'Đã tạo tài khoản Staff.'
      );
    }
  }

  function toggle(user) {
    const label = user.enabled ? 'Ẩn' : 'Hiện';
    if (window.confirm(`${label} tài khoản ${user.fullName}?`)) {
      mutate(
        () => adminUserApi.toggleVisibility(user.id),
        `Đã ${label.toLowerCase()} tài khoản.`
      );
    }
  }

  return (
    <div className="admin-users">
      <h1>Quản lý người dùng</h1>
      <form className="admin-user-controls" onSubmit={applyFilters}>
        <label>
          Họ tên
          <input type="search" name="keyword" />
        </label>
        <label>
          Vai trò
          <select name="role" defaultValue="">
            <option value="">Tất cả</option>
            <option value="CUSTOMER">Khách hàng</option>
            <option value="STAFF">Nhân viên</option>
          </select>
        </label>
        <button type="submit" disabled={busy}>
          Tìm người dùng
        </button>
        <button
          type="button"
          disabled={busy}
          onClick={() => {
            setEditor({});
            setNotice(null);
          }}
        >
          Thêm nhân viên
        </button>
      </form>
      {notice && (
        <p role={notice.error ? 'alert' : 'status'}>{notice.message}</p>
      )}
      {editor && (
        <form
          className="admin-user-editor"
          key={editor.id || 'create'}
          onSubmit={saveUser}
        >
          <h2>
            {editor.id ? 'Sửa thông tin người dùng' : 'Tạo tài khoản Staff'}
          </h2>
          <fieldset disabled={busy}>
            <label>
              Họ tên
              <input
                name="fullName"
                autoComplete="off"
                required
                maxLength={100}
                defaultValue={editor.fullName || ''}
              />
            </label>
            {!editor.id && (
              <>
                <label>
                  Email
                  <input
                    name="email"
                    type="email"
                    required
                    maxLength={150}
                    autoComplete="off"
                  />
                </label>
                <label>
                  Mật khẩu ban đầu
                  <input
                    name="password"
                    type="password"
                    minLength={8}
                    autoComplete="new-password"
                    required
                  />
                </label>
              </>
            )}
            <label>
              Điện thoại
              <input
                name="phone"
                type="tel"
                maxLength={15}
                defaultValue={editor.phone || ''}
              />
            </label>
            <label>
              Địa chỉ
              <textarea
                name="address"
                rows={2}
                maxLength={255}
                defaultValue={editor.address || ''}
              />
            </label>
            <div className="admin-user-controls">
              <button type="submit">
                {editor.id ? 'Lưu thông tin' : 'Tạo Staff'}
              </button>
              <button type="button" onClick={() => setEditor(null)}>
                Đóng
              </button>
            </div>
          </fieldset>
        </form>
      )}
      {error ? (
        <ErrorState message={error} onRetry={refresh} />
      ) : !result ? (
        <Loading message="Đang tải danh sách người dùng..." />
      ) : (
        <>
          {result.content.length === 0 ? (
            <EmptyState title="Không có người dùng trên trang này" />
          ) : (
            <div className="admin-user-table">
              <table>
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
                      <td>{user.fullName}</td>
                      <td>{user.email}</td>
                      <td>{roleLabels[user.role]}</td>
                      <td>{user.enabled ? 'Đang hiện' : 'Đã ẩn'}</td>
                      <td>
                        {user.role !== 'ADMIN' && (
                          <div className="admin-user-controls">
                            <button
                              type="button"
                              disabled={busy}
                              onClick={() => {
                                setEditor(user);
                                setNotice(null);
                              }}
                              aria-label={`Sửa ${user.fullName}`}
                            >
                              Sửa
                            </button>
                            <button
                              type="button"
                              disabled={busy}
                              onClick={() => toggle(user)}
                              aria-label={`${user.enabled ? 'Ẩn' : 'Hiện'} ${user.fullName}`}
                            >
                              {user.enabled ? 'Ẩn' : 'Hiện'}
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {(result.totalPages > 1 || query.page > 0) && (
            <nav
              className="admin-user-controls"
              aria-label="Phân trang người dùng"
            >
              <button
                type="button"
                disabled={query.page === 0 || busy}
                onClick={() => load({ ...query, page: query.page - 1 })}
              >
                Trang trước
              </button>
              <span>Trang {query.page + 1}</span>
              <button
                type="button"
                disabled={query.page + 1 >= result.totalPages || busy}
                onClick={() => load({ ...query, page: query.page + 1 })}
              >
                Trang sau
              </button>
            </nav>
          )}
        </>
      )}
    </div>
  );
}
