import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
import { toast } from '../../utils/toast';
import { cartApi } from '../../api/cartApi';
import { useAuth } from '../../hooks/useAuth';
import EmptyState from '../../components/EmptyState';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import '../../styles/shop.css';

/**
 * Trang giỏ hàng (Figma frame 3:176).
 * Không có phí vận chuyển và thuế: Tổng cộng = Tạm tính.
 */
export function CartPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [items, setItems] = useState([]);
  const [totalAmount, setTotalAmount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadVersion, setReloadVersion] = useState(0);
  const [updating, setUpdating] = useState(false);

  const loadCart = () => {
    setLoading(true);
    setError(null);
    setReloadVersion((version) => version + 1);
  };

  useEffect(() => {
    let active = true;
    cartApi
      .get()
      .then((data) => {
        if (active) {
          setItems(data?.items ?? []);
          setTotalAmount(data?.totalAmount ?? 0);
        }
      })
      .catch((err) => {
        if (active) {
          setError(err?.response?.data?.message || 'Không thể tải giỏ hàng');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [reloadVersion]);

  const notifyCartChanged = () => {
    window.dispatchEvent(new CustomEvent('keymech:cart-updated'));
  };

  const handleUpdateQty = async (item, nextQty) => {
    if (nextQty < 1) return;
    if (typeof item.stockQuantity === 'number' && nextQty > item.stockQuantity) return;
    setUpdating(true);
    try {
      const updated = await cartApi.updateItem(item.productId, { quantity: nextQty });
      setItems(updated?.items ?? []);
      setTotalAmount(updated?.totalAmount ?? 0);
      notifyCartChanged();
    } catch (err) {
      const status = err?.response?.status;
      if (status !== 409) {
        toast.error(err?.response?.data?.message || 'Không thể cập nhật số lượng');
      }
      loadCart();
    } finally {
      setUpdating(false);
    }
  };

  const handleRemove = async (productId) => {
    setUpdating(true);
    try {
      await cartApi.removeItem(productId);
      toast.success('Đã bỏ sản phẩm khỏi giỏ hàng');
      notifyCartChanged();
      loadCart();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể xóa sản phẩm');
    } finally {
      setUpdating(false);
    }
  };

  const handleClear = async () => {
    if (!window.confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) return;
    setUpdating(true);
    try {
      await cartApi.clear();
      setItems([]);
      setTotalAmount(0);
      notifyCartChanged();
      toast.success('Đã xóa giỏ hàng');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể xóa giỏ hàng');
    } finally {
      setUpdating(false);
    }
  };

  const totalQty = items.reduce((sum, item) => sum + (item.quantity || 0), 0);

  return (
    <div className="km-cart">
      <div className="km-cart-header">
        <h1>Giỏ hàng của bạn</h1>
        <p>
          {totalQty} sản phẩm • Kiểm tra giỏ hàng trước khi thanh toán
        </p>
      </div>

      {loading && <Loading message="Đang tải giỏ hàng..." />}
      {!loading && error && (
        <ErrorState title="Lỗi tải giỏ hàng" message={error} onRetry={loadCart} />
      )}

      {!loading && !error && items.length === 0 && (
        <EmptyState
          title="Giỏ hàng trống"
          description="Bạn chưa thêm sản phẩm nào vào giỏ hàng. Hãy khám phá các mẫu phím cơ và linh kiện mới nhất nhé!"
          actionText="Tiếp tục mua sắm"
          onAction={() => navigate('/products')}
        />
      )}

      {!loading && !error && items.length > 0 && (
        <div className="km-cart-grid">
          {/* Bảng sản phẩm */}
          <div className="km-cart-table-card">
            <div className="km-cart-table-head">
              <span>Sản phẩm</span>
              <span className="km-text-right">Đơn giá</span>
              <span className="km-text-center">Số lượng</span>
              <span className="km-text-right">Thành tiền</span>
            </div>

            {items.map((item) => {
              const maxStock = typeof item.stockQuantity === 'number' ? item.stockQuantity : null;
              const atLimit = maxStock !== null && item.quantity >= maxStock;
              return (
              <div key={item.productId} className="km-cart-row">
                <div className="km-cart-product">
                  <div className="km-cart-thumb">
                    {item.imageUrl ? (
                      <img src={item.imageUrl} alt={item.name} />
                    ) : (
                      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true">
                        <rect x="2" y="6" width="20" height="12" rx="2" />
                        <path d="M6 10h.01M10 10h.01M14 10h.01M8 14h8" strokeLinecap="round" />
                      </svg>
                    )}
                  </div>

                  <div className="km-cart-product-info">
                    <Link to={`/products/${item.productId}`} className="km-cart-product-name">
                      {item.name}
                    </Link>
                    {(item.brand || item.layout || item.switchType) && (
                      <p className="km-cart-product-meta">
                        {[item.brand, item.layout, item.switchType].filter(Boolean).join(' • ')}
                      </p>
                    )}
                    {item.available === false && (
                      <span style={{ fontSize: 'var(--text-xs)', color: 'var(--color-danger)', fontWeight: 600 }}>
                        Không đủ hàng trong kho
                      </span>
                    )}
                    <button
                      type="button"
                      className="km-cart-remove"
                      disabled={updating}
                      onClick={() => handleRemove(item.productId)}
                    >
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                        <path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                      <span>Bỏ khỏi giỏ</span>
                    </button>
                  </div>
                </div>

                <div className="km-text-right">
                  <span className="km-cart-mobile-label">Đơn giá</span>
                  {formatVND(item.price)}
                </div>

                <div className="km-text-center">
                  <span className="km-cart-mobile-label">Số lượng</span>
                  <div className="km-qty-stepper">
                    <button
                      type="button"
                      className="km-qty-btn"
                      aria-label="Giảm số lượng"
                      disabled={updating || item.quantity <= 1}
                      onClick={() => handleUpdateQty(item, item.quantity - 1)}
                    >
                      −
                    </button>
                    <span className="km-qty-value">{item.quantity}</span>
                    <button
                      type="button"
                      className="km-qty-btn"
                      aria-label="Tăng số lượng"
                      disabled={updating || atLimit || item.available === false}
                      onClick={() => handleUpdateQty(item, item.quantity + 1)}
                    >
                      +
                    </button>
                  </div>
                  {maxStock !== null && maxStock > 0 && (
                    <div
                      style={{
                        fontSize: 'var(--text-xs)',
                        color: atLimit ? 'var(--color-warning)' : 'var(--color-text-muted)',
                        marginTop: '4px',
                      }}
                    >
                      Tối đa {maxStock} sản phẩm
                    </div>
                  )}
                </div>

                <div className="km-text-right" style={{ fontWeight: 600 }}>
                  <span className="km-cart-mobile-label">Thành tiền</span>
                  {formatVND(item.subtotal)}
                </div>
              </div>
              );
            })}

            <div className="km-cart-footer-actions">
              <Link to="/products" style={{ color: 'var(--color-primary)', fontWeight: 600 }}>
                Tiếp tục mua sắm
              </Link>
              <button
                type="button"
                className="km-btn km-btn-ghost"
                disabled={updating}
                onClick={handleClear}
              >
                Xóa giỏ hàng
              </button>
            </div>
          </div>

          {/* Tóm tắt đơn hàng (không có phí vận chuyển và thuế) */}
          <aside className="km-cart-summary" aria-label="Tóm tắt đơn hàng">
            <h2>Tóm tắt đơn hàng</h2>
            <div className="km-summary-row">
              <span>Tạm tính</span>
              <span>{formatVND(totalAmount)}</span>
            </div>
            <hr className="km-summary-divider" />
            <div className="km-summary-total">
              <span className="km-summary-total-label">Tổng cộng</span>
              <span className="km-summary-total-value">{formatVND(totalAmount)}</span>
            </div>
            <p className="km-summary-note">
              Giá và tồn kho được kiểm tra lại khi đặt hàng.
            </p>
            <Link to="/checkout" className="km-btn km-btn-primary km-btn-full" style={{ height: '48px' }}>
              Thanh toán
            </Link>
            {!user && (
              <div className="km-guest-info-box">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0, marginTop: '2px' }} aria-hidden="true">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 16v-4M12 8h.01" strokeLinecap="round" />
                </svg>
                <span>Bạn chưa đăng nhập? Giỏ hàng sẽ được giữ lại sau khi đăng nhập.</span>
              </div>
            )}
          </aside>
        </div>
      )}
    </div>
  );
}

export default CartPage;
