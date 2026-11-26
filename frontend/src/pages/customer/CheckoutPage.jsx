import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cartApi } from '../../api/cartApi';
import { orderApi } from '../../api/orderApi';
import { useAuth } from '../../hooks/useAuth';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import formatVND from '../../utils/formatVND';
import '../../styles/auth.css';

/**
 * Trang Thanh toán (Figma frame 3:286).
 * Không có phí vận chuyển và thuế: Tổng = Tạm tính.
 * Nếu đặt hàng thất bại, giữ nguyên giỏ hàng.
 */
export default function CheckoutPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [cart, setCart] = useState(null);
  const [cartError, setCartError] = useState('');
  const [reload, setReload] = useState(0);
  const [failure, setFailure] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const submitLock = useRef(false);

  const [receiverName, setReceiverName] = useState(user?.fullName || '');
  const [receiverPhone, setReceiverPhone] = useState(user?.phone || '');
  const [shippingAddress, setShippingAddress] = useState(user?.address || '');
  const [paymentMethod, setPaymentMethod] = useState('COD');

  const hasUnavailableItems = cart?.items?.some((item) => item.available === false);

  useEffect(() => {
    let active = true;
    cartApi
      .get()
      .then((data) => {
        if (active) setCart(data);
      })
      .catch((error) => {
        if (active) {
          setCartError(error.response?.data?.message || 'Không thể tải giỏ hàng.');
        }
      });
    return () => {
      active = false;
    };
  }, [reload]);

  async function placeOrder(event) {
    event.preventDefault();
    if (submitLock.current || failure?.reviewRequired || hasUnavailableItems) return;

    const payload = {
      receiverName: receiverName.trim(),
      receiverPhone: receiverPhone.trim(),
      shippingAddress: shippingAddress.trim(),
      paymentMethod,
    };

    if (!payload.receiverName || !payload.receiverPhone || !payload.shippingAddress) {
      setFailure({ message: 'Vui lòng nhập đầy đủ thông tin giao hàng.' });
      return;
    }

    submitLock.current = true;
    setSubmitting(true);
    setFailure(null);
    try {
      const order = await orderApi.checkout(payload);
      window.dispatchEvent(new CustomEvent('keymech:cart-updated'));
      navigate(`/customer/orders/${order.id}/success`, { replace: true });
    } catch (error) {
      const status = error.response?.status;
      const body = error.response?.data;
      const reviewRequired =
        !status || status >= 500 || body?.error === 'EMPTY_CART' || status === 409;
      const uncertain = !status || status >= 500;
      setFailure({
        reviewRequired,
        uncertain,
        message: uncertain
          ? 'Chưa xác định được kết quả đặt hàng. Hãy kiểm tra đơn của tôi và giỏ hàng trước khi thử lại.'
          : body?.message || 'Không thể đặt hàng. Vui lòng kiểm tra lại thông tin.',
      });
    } finally {
      submitLock.current = false;
      setSubmitting(false);
    }
  }

  return (
    <div className="km-checkout">
      <div className="km-steps-crumb">Giỏ hàng → Thanh toán → Hoàn tất</div>
      <h1 className="km-page-title">Thanh toán</h1>

      {cartError ? (
        <ErrorState
          title="Lỗi tải giỏ hàng"
          message={cartError}
          onRetry={() => {
            setCartError('');
            setReload((v) => v + 1);
          }}
        />
      ) : !cart ? (
        <Loading message="Đang tải giỏ hàng..." />
      ) : (cart.items ?? []).length === 0 ? (
        <EmptyState
          title="Giỏ hàng đang trống"
          description="Vui lòng thêm sản phẩm vào giỏ trước khi tiến hành thanh toán."
          actionText="Tiếp tục mua sắm"
          onAction={() => navigate('/products')}
        />
      ) : (
        <form className="km-checkout-grid" onSubmit={placeOrder} noValidate>
          {/* Thông tin giao hàng */}
          <div className="km-panel">
            <h2>Thông tin giao hàng</h2>

            <div className="km-field">
              <label htmlFor="receiverName" className="km-label">
                Tên người nhận <span className="km-required">*</span>
              </label>
              <input
                id="receiverName"
                name="receiverName"
                className="km-input"
                autoComplete="shipping name"
                maxLength={100}
                required
                disabled={submitting}
                value={receiverName}
                onChange={(e) => setReceiverName(e.target.value)}
                placeholder="Nguyễn Minh Anh"
              />
            </div>

            <div className="km-field">
              <label htmlFor="receiverPhone" className="km-label">
                Số điện thoại <span className="km-required">*</span>
              </label>
              <input
                id="receiverPhone"
                name="receiverPhone"
                type="tel"
                className="km-input"
                autoComplete="shipping tel"
                maxLength={15}
                required
                disabled={submitting}
                value={receiverPhone}
                onChange={(e) => setReceiverPhone(e.target.value)}
                placeholder="0901 234 567"
              />
            </div>

            <div className="km-field">
              <label htmlFor="shippingAddress" className="km-label">
                Địa chỉ giao hàng <span className="km-required">*</span>
              </label>
              <textarea
                id="shippingAddress"
                name="shippingAddress"
                className="km-textarea"
                autoComplete="shipping street-address"
                maxLength={255}
                rows={3}
                required
                disabled={submitting}
                value={shippingAddress}
                onChange={(e) => setShippingAddress(e.target.value)}
                placeholder="128 Nguyễn Văn Linh, Đà Nẵng"
              />
            </div>

            <h2 style={{ fontSize: 'var(--text-xl)', marginTop: 'var(--space-2)' }}>
              Phương thức thanh toán
            </h2>

            <label
              className={`km-radio-card ${paymentMethod === 'COD' ? 'km-radio-card--active' : ''}`}
            >
              <input
                type="radio"
                name="paymentMethod"
                value="COD"
                checked={paymentMethod === 'COD'}
                disabled={submitting}
                onChange={() => setPaymentMethod('COD')}
              />
              <span>COD — Thanh toán khi nhận hàng</span>
            </label>

            <label
              className={`km-radio-card ${paymentMethod === 'BANK_TRANSFER' ? 'km-radio-card--active' : ''}`}
            >
              <input
                type="radio"
                name="paymentMethod"
                value="BANK_TRANSFER"
                checked={paymentMethod === 'BANK_TRANSFER'}
                disabled={submitting}
                onChange={() => setPaymentMethod('BANK_TRANSFER')}
              />
              <span>Chuyển khoản ngân hàng</span>
            </label>

            {paymentMethod === 'BANK_TRANSFER' && (
              <div className="km-alert-info">
                <strong>Thông tin chuyển khoản thử nghiệm</strong>
                <div>Ngân hàng: Vietcombank</div>
                <div>Số tài khoản: 0123 456 789</div>
                <div>Chủ tài khoản: KEYMECH</div>
                <div>Chuyển khoản được nhân viên xác nhận sau khi tiếp nhận đơn. Không thực hiện giao dịch thật.</div>
              </div>
            )}

            {failure && (
              <div role="alert" className="km-alert-error">
                <p style={{ margin: '0 0 8px 0' }}>{failure.message}</p>
                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                  {failure.reviewRequired && (
                    <Link to="/cart" className="km-auth-link km-auth-link--sm">
                      Kiểm tra giỏ hàng
                    </Link>
                  )}
                  {failure.uncertain && (
                    <Link to="/customer/orders" className="km-auth-link km-auth-link--sm">
                      Kiểm tra đơn của tôi
                    </Link>
                  )}
                </div>
              </div>
            )}

            {hasUnavailableItems && (
              <div role="alert" className="km-alert-error">
                Có sản phẩm không đủ hàng.{' '}
                <Link to="/cart" className="km-auth-link km-auth-link--sm">
                  Cập nhật giỏ hàng
                </Link>
              </div>
            )}
          </div>

          {/* Tóm tắt đơn hàng (không có phí vận chuyển và thuế) */}
          <aside className="km-panel" aria-labelledby="checkout-summary">
            <h2 id="checkout-summary" style={{ fontSize: '22px' }}>
              Tóm tắt đơn hàng
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {cart.items.map((item) => (
                <div key={item.productId} className="km-checkout-summary-item">
                  <div>
                    <div style={{ fontWeight: 600 }}>
                      {item.name} × {item.quantity}
                    </div>
                    {item.available === false && (
                      <div style={{ color: 'var(--color-danger)', fontSize: 'var(--text-xs)' }}>
                        Không đủ hàng
                      </div>
                    )}
                  </div>
                  <strong>{formatVND(item.subtotal)}</strong>
                </div>
              ))}
            </div>

            <div className="km-summary-row">
              <span>Tạm tính</span>
              <span>{formatVND(cart.totalAmount)}</span>
            </div>

            <div style={{ fontSize: 'var(--text-2xl)', fontWeight: 600 }}>
              Tổng: {formatVND(cart.totalAmount)}
            </div>

            <button
              type="submit"
              className="km-btn km-btn-primary km-btn-full"
              style={{ height: '48px' }}
              disabled={submitting || failure?.reviewRequired || hasUnavailableItems}
            >
              {submitting ? 'Đang đặt hàng...' : 'Đặt hàng'}
            </button>

            <p className="km-auth-footer-note">
              Bằng việc đặt hàng, bạn đồng ý với điều khoản và chính sách bảo mật của KeyMech.
            </p>
          </aside>
        </form>
      )}
    </div>
  );
}
