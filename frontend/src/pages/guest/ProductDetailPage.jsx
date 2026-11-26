import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import ProductCard from '../../components/ProductCard';
import NotFoundPage from '../NotFoundPage';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';
import { productApi } from '../../api/productApi';
import { cartApi } from '../../api/cartApi';
import '../../styles/shop.css';

const SPEC_LABELS = {
  connectivity: 'Kết nối',
  keys: 'Số phím',
  case: 'Chất liệu vỏ',
  hotSwap: 'Hot-swap',
  backlight: 'Đèn nền',
  material: 'Chất liệu',
  profile: 'Profile',
};

const SWITCH_DESCRIPTIONS = {
  Tactile: 'Có một khấc nhẹ khi nhấn, giúp bạn cảm nhận điểm kích hoạt.',
  Linear: 'Hành trình phím trơn và đều, không có khấc phản hồi.',
  Clicky: 'Có khấc phản hồi và tiếng click rõ khi nhấn.',
};

function readSpecRows(specs) {
  if (!specs?.trim()) return [];

  try {
    const parsed = JSON.parse(specs);
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
      return Object.entries(parsed).flatMap(([key, value]) => {
        if (value == null || value === '') return [];
        const label = Object.hasOwn(SPEC_LABELS, key) ? SPEC_LABELS[key] : key;
        if (typeof value === 'boolean') return [[key, label, value ? 'Có' : 'Không']];
        return [[key, label, typeof value === 'object' ? JSON.stringify(value) : value]];
      });
    }
  } catch {
    // The API also supports plain text in specs.
  }

  return [['specs', 'Thông số khác', specs]];
}

/**
 * Trang chi tiết sản phẩm (Figma frames 3:154 & 3:382).
 * - Sản phẩm bị ẩn / không tồn tại (404) → hiển thị trang 404.
 * - Vai trò Staff / Admin → ẩn nút mua hàng, hiện thông báo chế độ xem.
 */
export function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isStaffOrAdmin } = useAuth();

  const [product, setProduct] = useState(null);
  const [related, setRelated] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let ignore = false;
    const productId = parseInt(id, 10);

    productApi
      .detail(productId)
      .then((data) => {
        if (ignore) return;
        setProduct(data);
        setNotFound(false);
        setError(null);
        setQuantity(1);

        setRelated([]);
        const filters = data.layout ? { layout: data.layout } : {};
        productApi.list({ page: 0, size: data.layout ? 5 : 24, ...filters })
          .then((res) => {
            if (ignore) return;
            const items = (res?.content ?? []).filter((item) => item.id !== data.id);
            const similar = data.switchType
              ? items.filter((item) => item.switchType === data.switchType)
              : [];
            const recommendations = data.layout
              ? [...similar, ...items.filter((item) => !similar.includes(item))]
              : similar;
            setRelated(recommendations.slice(0, 4));
          })
          .catch(() => {
            if (!ignore) setRelated([]);
          });
      })
      .catch((err) => {
        if (ignore) return;
        if (err?.response?.status === 404) {
          setNotFound(true);
        } else {
          setError(err?.response?.data?.message || 'Không thể tải thông tin sản phẩm');
        }
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [id, reloadKey]);

  const stock = product?.stockQuantity ?? 0;

  useEffect(() => {
    if (!loading && !notFound && !error && String(product?.id) === String(Number(id))) {
      document.title = `${product.name} | KeyMech`;
    } else {
      document.title = notFound ? 'Không tìm thấy sản phẩm | KeyMech' : 'Chi tiết sản phẩm | KeyMech';
    }
  }, [id, product, loading, notFound, error]);

  const handleAddToCart = async (goToCart = false) => {
    if (!product || stock <= 0 || adding) return;
    setAdding(true);
    try {
      await cartApi.addItem({ productId: product.id, quantity });
      window.dispatchEvent(new CustomEvent('keymech:cart-updated'));
      if (goToCart) {
        navigate('/cart');
      } else {
        toast.success(`Đã thêm ${quantity} sản phẩm vào giỏ hàng`);
      }
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể thêm vào giỏ hàng');
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return <Loading message="Đang tải chi tiết sản phẩm..." />;
  }

  if (notFound) {
    return <NotFoundPage />;
  }

  if (error || !product) {
    return (
      <ErrorState
        title="Lỗi tải sản phẩm"
        message={error || 'Không tìm thấy sản phẩm'}
        onRetry={() => {
          setLoading(true);
          setError(null);
          setReloadKey((k) => k + 1);
        }}
      />
    );
  }

  const specRows = readSpecRows(product.specs);

  return (
    <div className={`km-detail${isStaffOrAdmin ? '' : ' km-detail--purchasable'}`}>
      <nav className="km-breadcrumb" aria-label="Breadcrumb">
        <Link to="/">Trang chủ</Link>
        <span>/</span>
        <Link to="/products">Sản phẩm</Link>
        <span>/</span>
        <span>{product.name}</span>
      </nav>

      {/* Top 2-column section */}
      <section className="km-detail-top">
        <div className="km-detail-media">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} />
          ) : (
            <svg width="72" height="72" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true">
              <rect x="2" y="6" width="20" height="12" rx="2" />
              <path d="M6 10h.01M10 10h.01M14 10h.01M18 10h.01M8 14h8" strokeLinecap="round" />
            </svg>
          )}
        </div>

        <div className="km-detail-buy">
          <p className="km-detail-brand">
            {[product.brand, product.categoryName].filter(Boolean).join('  /  ')}
          </p>
          <h1 className="km-page-title">{product.name}</h1>
          {product.descriptionShort && (
            <p className="km-detail-short">{product.descriptionShort}</p>
          )}
          <p className="km-detail-price">{formatVND(product.price)}</p>
          <p className={`km-detail-stock ${stock <= 0 ? 'km-detail-stock--out' : ''}`}>
            {stock > 0 ? `● Còn ${stock} sản phẩm` : '● Hết hàng'}
          </p>

          {isStaffOrAdmin ? (
            <div className="km-staff-notice" role="status">
              Chế độ xem nhân viên / quản trị. Không hỗ trợ mua hàng.
            </div>
          ) : (
            <>
              <div role="group" aria-labelledby="detail-qty-label">
                <span id="detail-qty-label" className="km-detail-qty-label">
                  Số lượng
                </span>
                <div className="km-qty-stepper">
                  <button
                    type="button"
                    className="km-qty-btn"
                    aria-label="Giảm số lượng"
                    disabled={quantity <= 1 || stock <= 0}
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  >
                    −
                  </button>
                  <span className="km-qty-value" aria-live="polite">{quantity}</span>
                  <button
                    type="button"
                    className="km-qty-btn"
                    aria-label="Tăng số lượng"
                    disabled={quantity >= stock || stock <= 0}
                    onClick={() => setQuantity((q) => Math.min(stock, q + 1))}
                  >
                    +
                  </button>
                </div>
              </div>

              <div className="km-detail-actions">
                <button
                  type="button"
                  className="km-btn km-btn-primary"
                  disabled={adding || stock <= 0}
                  onClick={() => handleAddToCart(false)}
                >
                  {adding ? 'Đang thêm...' : stock > 0 ? 'Thêm vào giỏ' : 'Hết hàng'}
                </button>
                <button
                  type="button"
                  className="km-btn km-btn-soft"
                  disabled={adding || stock <= 0}
                  onClick={() => handleAddToCart(true)}
                >
                  Mua ngay
                </button>
              </div>
            </>
          )}

          <div className="km-trust-list">
            <span>✓ Hàng chính hãng</span>
            <span>✓ Hỗ trợ kỹ thuật tận tâm</span>
            <span>✓ Giao hàng toàn quốc</span>
          </div>
        </div>
      </section>

      {/* Mô tả & Thông số */}
      <section className="km-detail-desc-card">
        <h2>Mô tả sản phẩm</h2>
        <p style={{ margin: 0, lineHeight: 1.6, fontSize: 'var(--text-md)' }}>
          {product.description || 'Thông tin mô tả chi tiết đang được cập nhật.'}
        </p>

        <h2>Thông số kỹ thuật</h2>
        {product.brand && (
          <div className="km-spec-row">
            <span className="km-spec-key">Thương hiệu</span>
            <span>{product.brand}</span>
          </div>
        )}
        {product.layout && (
          <div className="km-spec-row">
            <span className="km-spec-key">Layout</span>
            <span>{product.layout}</span>
          </div>
        )}
        {product.switchType && (
          <div className="km-spec-row">
            <span className="km-spec-key">Switch</span>
            <div>
              {product.switchType}
              {Object.hasOwn(SWITCH_DESCRIPTIONS, product.switchType) && (
                <details className="km-switch-help">
                  <summary>Loại switch này có gì khác?</summary>
                  <p>{SWITCH_DESCRIPTIONS[product.switchType]}</p>
                </details>
              )}
            </div>
          </div>
        )}
        {specRows.map(([key, label, value]) => (
          <div key={key} className="km-spec-row">
            <span className="km-spec-key">{label}</span>
            <span>{value}</span>
          </div>
        ))}
      </section>

      {/* Sản phẩm liên quan */}
      {related.length > 0 && (
        <section>
          <div className="km-section-header">
            <h2 className="km-section-title">Sản phẩm liên quan</h2>
          </div>
          <div className="km-product-grid-4">
            {related.map((item) => (
              <ProductCard key={item.id} product={item} />
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

export default ProductDetailPage;
