import { Link } from 'react-router-dom';
import EmptyState from '../../components/EmptyState';

/**
 * Trang danh sách đơn hàng đã mua của khách hàng.
 */
export function CustomerOrdersPage() {
  const orders = [];

  return (
    <div>
      <h1
        style={{
          fontSize: '1.4rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '1rem',
        }}
      >
        Lịch sử đơn hàng
      </h1>

      {orders.length === 0 ? (
        <EmptyState
          title="Bạn chưa có đơn hàng nào"
          description="Khám phá các mẫu phím cơ cao cấp và đặt hàng ngay hôm nay!"
          actionText="Mua sắm ngay"
          onAction={() => (window.location.href = '/products')}
        />
      ) : (
        <div>
          <p>Danh sách đơn hàng...</p>
        </div>
      )}

      <div style={{ marginTop: '1rem' }}>
        <Link to="/products" style={{ color: '#2563eb', fontSize: '0.9rem' }}>
          ← Mua sắm thêm sản phẩm
        </Link>
      </div>
    </div>
  );
}

export default CustomerOrdersPage;
