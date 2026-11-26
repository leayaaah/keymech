import { useEffect, useState } from 'react';
import adminCategoryApi from '../../api/adminCategoryApi';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';
import '../../styles/portal.css';

function toSlug(str) {
  return str
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'd')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '');
}

/**
 * Trang Quản lý danh mục Admin (Figma frames 3:3828, 3:3829, 3:3830).
 * - Tên và slug không trùng.
 * - Không thể ẩn danh mục khi còn sản phẩm ACTIVE (409 -> hiện Modal "Không thể ẩn danh mục").
 */
export function AdminCategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const [editor, setEditor] = useState(null); // null | { id?, name, slug }
  const [saving, setSaving] = useState(false);
  const [blockedModal, setBlockedModal] = useState(null); // string message on 409

  useEffect(() => {
    let active = true;
    adminCategoryApi
      .list()
      .then((res) => {
        if (active) {
          setCategories(Array.isArray(res) ? res : res?.content ?? []);
          setError('');
        }
      })
      .catch((err) => {
        if (active) {
          setError(err?.response?.data?.message || 'Không thể tải danh sách danh mục.');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [reloadKey]);

  const handleSave = async (e) => {
    e.preventDefault();
    const name = editor.name.trim();
    const slug = (editor.slug.trim() || toSlug(name)).toLowerCase();
    if (!name || !slug) {
      toast.error('Vui lòng nhập đầy đủ tên và slug danh mục.');
      return;
    }

    setSaving(true);
    try {
      if (editor.id) {
        await adminCategoryApi.update(editor.id, { name, slug });
        toast.success('Đã cập nhật danh mục.');
      } else {
        await adminCategoryApi.create({ name, slug });
        toast.success('Đã thêm danh mục mới.');
      }
      setEditor(null);
      setLoading(true);
      setReloadKey((k) => k + 1);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể lưu danh mục (tên hoặc slug có thể bị trùng).');
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (cat) => {
    const nextStatus = cat.status === 'ACTIVE' ? 'HIDDEN' : 'ACTIVE';
    setSaving(true);
    try {
      const updated = await adminCategoryApi.toggleVisibility(cat.id, nextStatus);
      setCategories((prev) =>
        prev.map((c) => (c.id === cat.id ? { ...c, status: updated?.status || nextStatus } : c))
      );
      toast.success(
        nextStatus === 'HIDDEN'
          ? `Đã ẩn danh mục "${cat.name}".`
          : `Đã hiển thị lại danh mục "${cat.name}".`
      );
    } catch (err) {
      if (err?.response?.status === 409) {
        setBlockedModal(
          err?.response?.data?.message ||
            `Không thể ẩn danh mục "${cat.name}" vì vẫn còn sản phẩm đang hiển thị.`
        );
      } else {
        toast.error(err?.response?.data?.message || 'Không thể cập nhật trạng thái danh mục.');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="km-portal-top">
        <div>
          <h1 className="km-portal-title">Quản lý danh mục</h1>
          <p className="km-auth-subtitle">
            Thêm, sửa và ẩn/hiện danh mục sản phẩm (không xóa vật lý).
          </p>
        </div>
        <button
          type="button"
          className="km-btn km-btn-primary"
          onClick={() => setEditor({ name: '', slug: '' })}
        >
          + Thêm danh mục
        </button>
      </div>

      {loading ? (
        <Loading message="Đang tải danh mục..." />
      ) : error ? (
        <ErrorState
          title="Lỗi tải danh mục"
          message={error}
          onRetry={() => {
            setLoading(true);
            setError('');
            setReloadKey((k) => k + 1);
          }}
        />
      ) : categories.length === 0 ? (
        <EmptyState title="Chưa có danh mục nào" />
      ) : (
        <div className="km-table-wrap">
          <table className="km-table">
            <thead>
              <tr>
                <th>Tên danh mục</th>
                <th>Slug</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((cat) => (
                <tr key={cat.id}>
                  <td>
                    <strong>{cat.name}</strong>
                  </td>
                  <td>
                    <code>{cat.slug}</code>
                  </td>
                  <td>
                    <span className={cat.status === 'ACTIVE' ? 'km-badge km-badge--success' : 'km-badge km-badge--neutral'}>
                      {cat.status === 'ACTIVE' ? 'Đang hiện' : 'Đã ẩn'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button
                        type="button"
                        className="km-btn km-btn-soft km-btn-sm"
                        disabled={saving}
                        onClick={() => setEditor({ id: cat.id, name: cat.name, slug: cat.slug })}
                      >
                        Sửa
                      </button>
                      <button
                        type="button"
                        className={`km-btn km-btn-sm ${cat.status === 'ACTIVE' ? 'km-btn-danger' : 'km-btn-primary'}`}
                        disabled={saving}
                        onClick={() => handleToggle(cat)}
                      >
                        {cat.status === 'ACTIVE' ? 'Ẩn' : 'Hiện'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Thêm / Sửa danh mục (Figma 3:3829) */}
      {editor && (
        <div className="km-modal-backdrop" role="dialog" aria-modal="true">
          <form className="km-modal" onSubmit={handleSave}>
            <h2>{editor.id ? 'Chỉnh sửa danh mục' : 'Thêm danh mục mới'}</h2>
            <div className="km-field">
              <label htmlFor="cat-name" className="km-label">
                Tên danh mục <span className="km-required">*</span>
              </label>
              <input
                id="cat-name"
                className="km-input"
                required
                value={editor.name}
                onChange={(e) => {
                  const val = e.target.value;
                  setEditor((prev) => ({
                    ...prev,
                    name: val,
                    slug: prev.id ? prev.slug : toSlug(val),
                  }));
                }}
                placeholder="Bàn phím cơ"
              />
            </div>

            <div className="km-field">
              <label htmlFor="cat-slug" className="km-label">
                Slug (duy nhất) <span className="km-required">*</span>
              </label>
              <input
                id="cat-slug"
                className="km-input"
                required
                value={editor.slug}
                onChange={(e) => setEditor((prev) => ({ ...prev, slug: e.target.value }))}
                placeholder="ban-phim-co"
              />
            </div>

            <div className="km-modal-actions">
              <button type="button" className="km-btn km-btn-soft" disabled={saving} onClick={() => setEditor(null)}>
                Hủy
              </button>
              <button type="submit" className="km-btn km-btn-primary" disabled={saving}>
                {saving ? 'Đang lưu...' : 'Lưu danh mục'}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Modal 409 Không thể ẩn danh mục (Figma 3:3830) */}
      {blockedModal && (
        <div className="km-modal-backdrop" role="alertdialog" aria-modal="true">
          <div className="km-modal">
            <h2>Không thể ẩn danh mục</h2>
            <p className="km-auth-subtitle">{blockedModal}</p>
            <div className="km-modal-actions">
              <button type="button" className="km-btn km-btn-primary" onClick={() => setBlockedModal(null)}>
                Đã hiểu
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default AdminCategoriesPage;
