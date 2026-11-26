import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import staffProductApi from '../../api/staffProductApi';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import { getFeatureErrorMessage } from '../../utils/apiError';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/portal.css';

const CATEGORIES = [
  { id: 1, name: 'Bàn phím cơ' },
  { id: 2, name: 'Switch' },
  { id: 3, name: 'Keycap' },
  { id: 4, name: 'Dụng cụ và phụ kiện' },
  { id: 5, name: 'Kit bàn phím' },
];

const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024;

function validateImageFile(file) {
  if (!file) return '';
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    return 'Ảnh sản phẩm chỉ chấp nhận định dạng JPG, PNG hoặc WebP.';
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) {
    return 'Dung lượng ảnh tối đa là 2 MB.';
  }
  return '';
}

/**
 * Trang Thêm / Chỉnh sửa sản phẩm (Figma frame 3:106).
 * Quy tắc: giá > 0, tồn kho ≥ 0, hỗ trợ upload ảnh vào uploads/.
 */
export function StaffProductFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: '',
    brand: '',
    categoryId: '1',
    layout: '75%',
    switchType: 'Linear',
    price: '',
    stockQuantity: '0',
    description: '',
    specs: '',
  });
  const [imageFile, setImageFile] = useState(null);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(isEdit);
  const [loadError, setLoadError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    let active = true;
    staffProductApi
      .detail(id)
      .then((data) => {
        if (!active) return;
        setForm({
          name: data?.name || '',
          brand: data?.brand || '',
          categoryId: String(data?.categoryId || 1),
          layout: data?.layout || '',
          switchType: data?.switchType || '',
          price: data?.price != null ? String(data.price) : '',
          stockQuantity: data?.stockQuantity != null ? String(data.stockQuantity) : '0',
          description: data?.description || '',
          specs: data?.specs || '',
        });
      })
      .catch((err) => {
        if (active) setLoadError(getFeatureErrorMessage(err, 'Không thể tải thông tin sản phẩm.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [id, isEdit]);

  const validate = () => {
    const next = {};
    if (!form.name.trim()) next.name = 'Vui lòng nhập tên sản phẩm.';
    if (!form.brand.trim()) next.brand = 'Vui lòng nhập thương hiệu.';
    const priceNum = Number(form.price);
    if (!form.price || Number.isNaN(priceNum) || priceNum <= 0) {
      next.price = 'Giá bán phải lớn hơn 0.';
    }
    const stockNum = Number(form.stockQuantity);
    if (form.stockQuantity === '' || !Number.isInteger(stockNum) || stockNum < 0) {
      next.stockQuantity = 'Tồn kho phải là số nguyên ≥ 0.';
    }
    const imageError = validateImageFile(imageFile);
    if (imageError) {
      next.image = imageError;
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setSubmitting(true);
    try {
      const payload = {
        name: form.name.trim(),
        brand: form.brand.trim(),
        categoryId: Number(form.categoryId),
        layout: form.layout.trim() || null,
        switchType: form.switchType.trim() || null,
        price: Number(form.price),
        stockQuantity: Number(form.stockQuantity),
        description: form.description.trim() || null,
        specs: form.specs.trim() || null,
      };

      if (isEdit) {
        await staffProductApi.update(id, payload, imageFile);
      } else {
        await staffProductApi.create(payload, imageFile);
      }

      toast.success(isEdit ? 'Đã cập nhật sản phẩm.' : 'Đã thêm sản phẩm mới.');
      navigate('/staff/products');
    } catch (err) {
      toast.error(getFeatureErrorMessage(err, 'Không thể lưu sản phẩm.'));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Loading message="Đang tải sản phẩm..." />;
  if (loadError) return <ErrorState title="Lỗi tải sản phẩm" message={loadError} />;

  return (
    <>
      <Link to="/staff/products" className="km-auth-link km-auth-link--sm">
        ← Quay lại danh sách sản phẩm
      </Link>
      <h1 className="km-portal-title">{isEdit ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}</h1>

      <form className="km-panel" style={{ maxWidth: '820px' }} onSubmit={handleSubmit} noValidate>
        <div className="km-form-row-2">
          <div className="km-field">
            <label htmlFor="prod-name" className="km-label">
              Tên sản phẩm <span className="km-required">*</span>
            </label>
            <input
              id="prod-name"
              className={`km-input ${errors.name ? 'km-input--error' : ''}`}
              value={form.name}
              onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
            />
            {errors.name && <p className="km-field-error">{errors.name}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="prod-brand" className="km-label">
              Thương hiệu <span className="km-required">*</span>
            </label>
            <input
              id="prod-brand"
              className={`km-input ${errors.brand ? 'km-input--error' : ''}`}
              value={form.brand}
              onChange={(e) => setForm((p) => ({ ...p, brand: e.target.value }))}
            />
            {errors.brand && <p className="km-field-error">{errors.brand}</p>}
          </div>
        </div>

        <div className="km-form-row-2">
          <div className="km-field">
            <label htmlFor="prod-cat" className="km-label">
              Danh mục
            </label>
            <select
              id="prod-cat"
              className="km-select"
              value={form.categoryId}
              onChange={(e) => setForm((p) => ({ ...p, categoryId: e.target.value }))}
            >
              {CATEGORIES.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="km-field">
            <label htmlFor="prod-layout" className="km-label">
              Layout
            </label>
            <input
              id="prod-layout"
              className="km-input"
              placeholder="75%, TKL, 65%..."
              value={form.layout}
              onChange={(e) => setForm((p) => ({ ...p, layout: e.target.value }))}
            />
          </div>
        </div>

        <div className="km-form-row-2">
          <div className="km-field">
            <label htmlFor="prod-price" className="km-label">
              Giá bán (₫) <span className="km-required">*</span>
            </label>
            <input
              id="prod-price"
              type="number"
              min={1}
              className={`km-input ${errors.price ? 'km-input--error' : ''}`}
              value={form.price}
              onChange={(e) => setForm((p) => ({ ...p, price: e.target.value }))}
            />
            {errors.price && <p className="km-field-error">{errors.price}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="prod-stock" className="km-label">
              Tồn kho <span className="km-required">*</span>
            </label>
            <input
              id="prod-stock"
              type="number"
              min={0}
              step={1}
              className={`km-input ${errors.stockQuantity ? 'km-input--error' : ''}`}
              value={form.stockQuantity}
              onChange={(e) => setForm((p) => ({ ...p, stockQuantity: e.target.value }))}
            />
            {errors.stockQuantity && <p className="km-field-error">{errors.stockQuantity}</p>}
          </div>
        </div>

        <div className="km-field">
          <label htmlFor="prod-switch" className="km-label">
            Loại switch
          </label>
          <input
            id="prod-switch"
            className="km-input"
            placeholder="Linear, Tactile, Clicky..."
            value={form.switchType}
            onChange={(e) => setForm((p) => ({ ...p, switchType: e.target.value }))}
          />
        </div>

        <div className="km-field">
          <label htmlFor="prod-image" className="km-label">
            Ảnh sản phẩm (JPG/PNG/WebP, tối đa 2 MB)
          </label>
          <input
            id="prod-image"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className={`km-input ${errors.image ? 'km-input--error' : ''}`}
            onChange={(e) => {
              const file = e.target.files?.[0] || null;
              const imgErr = validateImageFile(file);
              if (imgErr) {
                setImageFile(null);
                setErrors((p) => ({ ...p, image: imgErr }));
              } else {
                setImageFile(file);
                setErrors((p) => ({ ...p, image: '' }));
              }
            }}
          />
          <p className="km-field-hint">Định dạng JPG, PNG hoặc WebP, tối đa 2 MB.</p>
          {errors.image && <p className="km-field-error">{errors.image}</p>}
        </div>

        <div className="km-field">
          <label htmlFor="prod-desc" className="km-label">
            Mô tả sản phẩm
          </label>
          <textarea
            id="prod-desc"
            rows={3}
            className="km-textarea"
            value={form.description}
            onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
          />
        </div>

        <div className="km-field">
          <label htmlFor="prod-specs" className="km-label">
            Thông số kỹ thuật
          </label>
          <textarea
            id="prod-specs"
            rows={3}
            className="km-textarea"
            value={form.specs}
            onChange={(e) => setForm((p) => ({ ...p, specs: e.target.value }))}
          />
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <button type="submit" className="km-btn km-btn-primary" disabled={submitting}>
            {submitting ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Tạo sản phẩm'}
          </button>
          <Link to="/staff/products" className="km-btn km-btn-soft">
            Hủy
          </Link>
        </div>
      </form>
    </>
  );
}

export default StaffProductFormPage;
