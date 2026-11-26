import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import staffProductApi from '../../api/staffProductApi';
import { useAuth } from '../../hooks/useAuth';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import formatVND from '../../utils/formatVND';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/portal.css';

/**
 * Trang Quản lý sản phẩm (Figma frames 3:105 Staff, 3:3832 & 3:6352 Admin).
 * Quy tắc: Chỉ ADMIN mới có quyền Ẩn/Hiện sản phẩm; Staff chỉ thêm & chỉnh sửa.
 */
export function StaffProductsPage() {
  const { isAdmin } = useAuth();
  const [keyword, setKeyword] = useState('');
  const [appliedKw, setAppliedKw] = useState('');
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);
  const [togglingId, setTogglingId] = useState(null);

  useEffect(() => {
    let active = true;
    staffProductApi
      .list({ keyword: appliedKw || undefined, page: 0, size: 20 })
      .then((res) => {
        if (active) {
          setProducts(res?.content ?? []);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(err?.response?.data?.message || 'Không thể tải danh sách sản phẩm.');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [appliedKw, reloadKey]);

  const handleToggleVisibility = async (product) => {
    if (!isAdmin || togglingId) return;
    const nextStatus = product.status === 'ACTIVE' ? 'HIDDEN' : 'ACTIVE';
    setTogglingId(product.id);
    try {
      const updated = await staffProductApi.toggleVisibility(product.id, nextStatus);
      setProducts((prev) =>
        prev.map((p) => (p.id === product.id ? { ...p, status: updated?.status || nextStatus } : p))
      );
      toast.success(
        nextStatus === 'HIDDEN'
          ? `Đã ẩn sản phẩm "${product.name}".`
          : `Đã hiển thị lại sản phẩm "${product.name}".`
      );
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể đổi trạng thái hiển thị.');
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <>
      <div className="km-portal-top">
        <div>
          <h1 className="km-portal-title">
            {isAdmin ? 'Quản lý sản phẩm · Quản trị viên' : 'Quản lý sản phẩm'}
          </h1>
          <p className="km-auth-subtitle">
            Xem toàn bộ sản phẩm (kể cả đã ẩn), cập nhật thông tin và tồn kho.
          </p>
        </div>
        <Link to="/staff/products/new" className="km-btn km-btn-primary">
          + Thêm sản phẩm
        </Link>
      </div>

      <form
        className="km-filter-bar"
        onSubmit={(e) => {
          e.preventDefault();
          setLoading(true);
          setAppliedKw(keyword.trim());
        }}
      >
        <div className="km-field">
          <label htmlFor="staff-prod-kw" className="km-label">
            Tìm kiếm sản phẩm
          </label>
          <input
            id="staff-prod-kw"
            type="text"
            className="km-input"
            placeholder="Tên sản phẩm hoặc thương hiệu..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>
        <button type="submit" className="km-btn km-btn-primary">
          Tìm kiếm
        </button>
      </form>

      {loading ? (
        <Loading message="Đang tải danh sách sản phẩm..." />
      ) : error ? (
        <ErrorState
          title="Lỗi tải sản phẩm"
          message={error}
          onRetry={() => {
            setLoading(true);
            setError('');
            setReloadKey((k) => k + 1);
          }}
        />
      ) : products.length === 0 ? (
        <EmptyState title="Không tìm thấy sản phẩm" />
      ) : (
        <div className="km-table-wrap">
          <table className="km-table">
            <thead>
              <tr>
                <th>Sản phẩm</th>
                <th>Thương hiệu</th>
                <th>Giá bán</th>
                <th>Tồn kho</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {products.map((p) => (
                <tr key={p.id}>
                  <td>
                    <strong>{p.name}</strong>
                    <div className="km-field-hint">
                      {[p.categoryName, p.layout, p.switchType].filter(Boolean).join(' • ')}
                    </div>
                  </td>
                  <td>{p.brand}</td>
                  <td>{formatVND(p.price)}</td>
                  <td>{p.stockQuantity}</td>
                  <td>
                    <span className={p.status === 'ACTIVE' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
                      {p.status === 'ACTIVE' ? 'Đang hiện' : 'Đã ẩn'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                      <Link to={`/staff/products/${p.id}/edit`} className="km-btn km-btn-soft km-btn-sm">
                        Sửa
                      </Link>
                      {isAdmin && (
                        <button
                          type="button"
                          className={`km-btn km-btn-sm ${p.status === 'ACTIVE' ? 'km-btn-danger' : 'km-btn-primary'}`}
                          disabled={togglingId === p.id}
                          onClick={() => handleToggleVisibility(p)}
                        >
                          {p.status === 'ACTIVE' ? 'Ẩn' : 'Hiện'}
                        </button>
                      )}
                    </div>
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

export default StaffProductsPage;
