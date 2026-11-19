import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { cartApi } from '../../api/cartApi';
import { orderApi } from '../../api/orderApi';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import formatVND from '../../utils/formatVND';
import { paymentMethodLabels } from '../../utils/orderLabels';
import './customerOrders.css';

export default function CheckoutPage() {
  const [cart, setCart] = useState(null);
  const [cartError, setCartError] = useState('');
  const [reload, setReload] = useState(0);
  const [failure, setFailure] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const submitLock = useRef(false);
  const navigate = useNavigate();
  const hasUnavailableItems = cart?.items.some((item) => !item.available);

  useEffect(() => {
    let active = true;
    cartApi
      .get()
      .then((data) => {
        if (active) setCart(data);
      })
      .catch((error) => {
        if (active)
          setCartError(
            error.response?.data?.message || 'Không thể tải giỏ hàng.'
          );
      });
    return () => {
      active = false;
    };
  }, [reload]);

  async function placeOrder(event) {
    event.preventDefault();
    if (submitLock.current || failure?.reviewRequired || hasUnavailableItems)
      return;

    const fields = new FormData(event.currentTarget);
    const data = {
      receiverName: fields.get('receiverName').trim(),
      receiverPhone: fields.get('receiverPhone').trim(),
      shippingAddress: fields.get('shippingAddress').trim(),
      paymentMethod: fields.get('paymentMethod'),
    };
    if (!data.receiverName || !data.receiverPhone || !data.shippingAddress) {
      setFailure({ message: 'Vui lòng nhập đầy đủ thông tin giao hàng.' });
      return;
    }

    submitLock.current = true;
    setSubmitting(true);
    setFailure(null);
    try {
      const order = await orderApi.checkout(data);
      navigate(`/customer/orders/${order.id}/success`, { replace: true });
    } catch (error) {
      const status = error.response?.status;
      const body = error.response?.data;
      const reviewRequired =
        !status ||
        status >= 500 ||
        body?.error === 'EMPTY_CART' ||
        status === 409;
      const uncertain = !status || status >= 500;
      setFailure({
        reviewRequired,
        uncertain,
        message: uncertain
          ? 'Chưa xác định được kết quả đặt hàng. Hãy kiểm tra đơn của tôi và giỏ hàng trước khi thử lại.'
          : body?.message ||
            'Không thể đặt hàng. Vui lòng kiểm tra lại thông tin.',
      });
    } finally {
      submitLock.current = false;
      setSubmitting(false);
    }
  }

  return (
    <div className="customer-order-page">
      <Link to="/cart">← Về giỏ hàng</Link>
      <h1>Thanh toán</h1>
      {cartError ? (
        <ErrorState
          message={cartError}
          onRetry={() => {
            setCartError('');
            setReload((value) => value + 1);
          }}
        />
      ) : !cart ? (
        <Loading message="Đang tải giỏ hàng..." />
      ) : cart.items.length === 0 ? (
        <p>
          Giỏ hàng đang trống. <Link to="/products">Tiếp tục mua sắm</Link>
        </p>
      ) : (
        <div className="checkout-grid">
          <form className="order-panel" onSubmit={placeOrder}>
            <h2>Thông tin giao hàng</h2>
            <fieldset disabled={submitting}>
              <label htmlFor="receiverName">Tên người nhận</label>
              <input
                id="receiverName"
                name="receiverName"
                autoComplete="shipping name"
                maxLength={100}
                required
              />
              <label htmlFor="receiverPhone">Số điện thoại</label>
              <input
                id="receiverPhone"
                name="receiverPhone"
                type="tel"
                autoComplete="shipping tel"
                maxLength={15}
                required
              />
              <label htmlFor="shippingAddress">Địa chỉ giao hàng</label>
              <textarea
                id="shippingAddress"
                name="shippingAddress"
                autoComplete="shipping street-address"
                maxLength={255}
                rows={3}
                required
              />
              <label htmlFor="paymentMethod">Phương thức thanh toán</label>
              <select
                id="paymentMethod"
                name="paymentMethod"
                defaultValue="COD"
              >
                {Object.entries(paymentMethodLabels).map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
              <p>Chuyển khoản được nhân viên xác nhận sau khi tiếp nhận đơn.</p>
              {failure && (
                <div role="alert" className="order-alert">
                  <p>{failure.message}</p>
                  {failure.reviewRequired && (
                    <Link to="/cart">Kiểm tra giỏ hàng</Link>
                  )}
                  {failure.uncertain && (
                    <Link to="/customer/orders">Kiểm tra đơn của tôi</Link>
                  )}
                </div>
              )}
              {hasUnavailableItems && (
                <p role="alert">
                  Có sản phẩm không đủ hàng.{' '}
                  <Link to="/cart">Cập nhật giỏ hàng</Link>
                </p>
              )}
              <button
                className="order-primary"
                disabled={
                  submitting || failure?.reviewRequired || hasUnavailableItems
                }
              >
                {submitting ? 'Đang đặt hàng…' : 'Đặt hàng'}
              </button>
            </fieldset>
          </form>
          <section className="order-panel" aria-labelledby="checkout-summary">
            <h2 id="checkout-summary">Tóm tắt đơn hàng</h2>
            <ul className="order-lines">
              {cart.items.map((item) => (
                <li key={item.productId}>
                  <span>
                    {item.name} × {item.quantity}
                    {!item.available && ' · Không đủ hàng'}
                  </span>
                  <strong>{formatVND(item.subtotal)}</strong>
                </li>
              ))}
            </ul>
            <p>
              <strong>Tạm tính {formatVND(cart.totalAmount)}</strong>
            </p>
            <p>Giá và tồn kho được kiểm tra lại khi đặt hàng.</p>
          </section>
        </div>
      )}
    </div>
  );
}
