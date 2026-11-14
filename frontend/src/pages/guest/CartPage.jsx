import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
import { toast } from '../../utils/toast';
import { cartApi } from '../../api/cartApi';
import EmptyState from '../../components/EmptyState';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';

export function CartPage() {
  const [items, setItems] = useState([]);
  const [totalAmount, setTotalAmount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(null); // productId đang cập nhật

  const loadCart = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await cartApi.get();
      setItems(data.items ?? []);
      setTotalAmount(data.totalAmount ?? 0);
      setLoading(false);
    } catch (err) {
      setError(err?.message || 'Không thể tải giỏ hàng');
      setLoading(false);
    }
  };

  useEffect(() => { loadCart(); }, []);

  const handleIncrease = async (item) => {
    setUpdating(item.productId);
    try {
      const updated = await cartApi.updateItem(item.productId, { quantity: item.quantity + 1 });
      setItems(updated.items ?? []);
      setTotalAmount(updated.totalAmount ?? 0);
    } catch (err) {
      toast.error(err?.message || 'Không thể cập nhật');
    } finally {
      setUpdating(null);
    }
  };

  const handleDecrease = async (item) => {
    if (item.quantity <= 1) return;
    setUpdating(item.productId);
    try {
      const updated = await cartApi.updateItem(item.productId, { quantity: item.quantity - 1 });
      setItems(updated.items ?? []);
      setTotalAmount(updated.totalAmount ?? 0);
    } catch (err) {
      toast.error(err?.message || 'Không thể cập nhật');
    } finally {
      setUpdating(null);
    }
  };

  const handleRemove = async (productId) => {
    try {
      await cartApi.removeItem(productId);
      toast.success('Đã xóa sản phẩm khỏi giỏ');
      loadCart();
    } catch (err) {
      toast.error(err?.message || 'Không thể xóa');
    }
  };

  const handleClear = async () => {
    if (!window.confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) return;
    try {
      await cartApi.clear();
      setItems([]);
      setTotalAmount(0);
      toast.success('Đã xóa giỏ hàng');
    } catch (err) {
      toast.error(err?.message || 'Không thể xóa giỏ');
    }
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#111827', margin: 0 }}>Giỏ hàng của bạn</h1>
        {items.length > 0 && (
          <button onClick={handleClear} style={{ padding: '0.4rem 0.8rem', border: '1px solid #ef4444', color: '#ef4444', background: '#fff', borderRadius: '6px', cursor: 'pointer', fontSize: '0.85rem' }}>
            Xóa giỏ
          </button>
        )}
      </div>

      {loading && <Loading message="Đang tải giỏ hàng..." />}
      {!loading && error && <ErrorState title="Lỗi tải giỏ hàng" message={error} onRetry={loadCart} />}

      {!loading && !error && items.length === 0 && (
        <EmptyState
          title="Giỏ hàng trống"
          description="Bạn chưa thêm sản phẩm nào vào giỏ hàng. Hãy khám phá các mẫu phím cơ và linh kiện mới nhất nhé!"
          actionText="Tiếp tục mua sắm"
          onAction={() => (window.location.href = '/products')}
        />
      )}

      {!loading && !error && items.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 320px', gap: '1.5rem', alignItems: 'start' }}>
          {/* Items list */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {items.map((item) => (
              <div key={item.productId} style={{ display: 'flex', gap: '1rem', padding: '1rem', backgroundColor: '#fff', borderRadius: '8px', boxShadow: '0 1px 2px rgba(0,0,0,0.08)', alignItems: 'center' }}>
                {/* Thumbnail */}
                <div style={{ width: 80, height: 80, flexShrink: 0, borderRadius: '6px', backgroundColor: '#f3f4f6', overflow: 'hidden' }}>
                  {item.imageUrl ? (
                    <img src={item.imageUrl} alt={item.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : <span style={{ fontSize: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>⌨️</span>}
                </div>

                {/* Info */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <h3 style={{ margin: '0 0 0.25rem', fontSize: '1rem', fontWeight: 600, color: '#111827' }}>{item.name}</h3>
                  <div style={{ fontSize: '0.85rem', color: '#6b7280' }}>Đơn giá: {formatVND(item.price)}</div>
                  {!item.available && (
                    <span style={{ fontSize: '0.8rem', color: '#dc2626' }}>⚠ Không đủ hàng</span>
                  )}
                </div>

                {/* Quantity controls */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <button onClick={() => handleDecrease(item)} disabled={updating === item.productId} style={qtyBtnStyle(updating === item.productId)}>−</button>
                  <span style={{ width: 32, textAlign: 'center', fontWeight: 600 }}>{item.quantity}</span>
                  <button onClick={() => handleIncrease(item)} disabled={updating === item.productId} style={qtyBtnStyle(updating === item.productId)}>+</button>
                </div>

                {/* Subtotal */}
                <div style={{ textAlign: 'right', minWidth: 100 }}>
                  <div style={{ fontWeight: 700, color: '#dc2626' }}>{formatVND(item.subtotal)}</div>
                </div>

                {/* Remove */}
                <button onClick={() => handleRemove(item.productId)} disabled={updating === item.productId} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem', color: '#9ca3af', padding: '0.25rem' }}>✕</button>
              </div>
            ))}
          </div>

          {/* Summary card */}
          <div style={{ padding: '1.25rem', backgroundColor: '#fff', borderRadius: '8px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', position: 'sticky', top: '1rem' }}>
            <h2 style={{ margin: '0 0 1rem', fontSize: '1.1rem', fontWeight: 700, color: '#111827' }}>Tóm tắt đơn hàng</h2>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.9rem' }}>
              <span style={{ color: '#6b7280' }}>Thành tiền ({items.reduce((s, i) => s + i.quantity, 0)} món)</span>
              <span>{formatVND(totalAmount)}</span>
            </div>
            <hr style={{ border: 'none', borderTop: '1px solid #e5e7eb', margin: '0.75rem 0' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '1.1rem' }}>
              <span>Tổng cộng</span>
              <span style={{ color: '#dc2626' }}>{formatVND(totalAmount)}</span>
            </div>
            <Link to="/products" style={{ display: 'block', textAlign: 'center', padding: '0.75rem', backgroundColor: '#2563eb', color: '#fff', borderRadius: '6px', textDecoration: 'none', fontWeight: 600, marginTop: '1rem' }}>
              Tiếp tục mua sắm
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

function qtyBtnStyle(disabled) {
  return {
    width: 28, height: 28, border: '1px solid #d1d5db', background: disabled ? '#f3f4f6' : '#fff',
    borderRadius: 4, cursor: disabled ? 'not-allowed' : 'pointer', fontSize: '1rem',
    color: disabled ? '#9ca3af' : '#374151', display: 'flex', alignItems: 'center', justifyContent: 'center',
  };
}

export default CartPage;
