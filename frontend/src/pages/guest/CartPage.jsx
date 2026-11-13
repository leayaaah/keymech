import { Link } from 'react-router-dom';
import EmptyState from '../../components/EmptyState';

/**
 * Trang giỏ hàng (áp dụng cho Guest và Customer; Staff và Admin bị chặn truy cập).
 */
export function CartPage() {
  const cartItems = [];

  return (
    <div>
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '1.5rem',
        }}
      >
        Giỏ hàng của bạn
      </h1>

      {cartItems.length === 0 ? (
        <EmptyState
          title="Giỏ hàng trống"
          description="Bạn chưa thêm sản phẩm nào vào giỏ hàng. Hãy khám phá các mẫu phím cơ và linh kiện mới nhất nhé!"
          actionText="Tiếp tục mua sắm"
          onAction={() => (window.location.href = '/products')}
        />
      ) : (
        <div>
          {/* Sẽ gắn kết nối Session Cart ở module G-03 */}
          <p>Danh sách sản phẩm trong giỏ...</p>
        </div>
      )}

      <div style={{ marginTop: '1.5rem' }}>
        <Link
          to="/products"
          style={{
            color: '#2563eb',
            textDecoration: 'none',
            fontSize: '0.9rem',
            fontWeight: 500,
          }}
        >
          ← Tiếp tục xem sản phẩm
        </Link>
      </div>
    </div>
  );
}

export default CartPage;
