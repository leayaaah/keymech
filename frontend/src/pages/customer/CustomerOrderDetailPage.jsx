import { useParams, Link } from 'react-router-dom';

/**
 * Trang chi tiết đơn hàng của khách hàng.
 */
export function CustomerOrderDetailPage() {
  const { id } = useParams();

  return (
    <div>
      <Link
        to="/customer/orders"
        style={{
          color: '#2563eb',
          textDecoration: 'none',
          fontSize: '0.9rem',
          display: 'inline-block',
          marginBottom: '1rem',
        }}
      >
        ← Quay lại danh sách đơn hàng
      </Link>
      <h1
        style={{
          fontSize: '1.4rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '1rem',
        }}
      >
        Chi tiết đơn hàng #{id}
      </h1>
      <p style={{ color: '#4b5563', fontSize: '0.9rem' }}>
        Thông tin chi tiết đơn hàng và theo dõi vận chuyển (Tracking) sẽ được kết nối ở module C-04.
      </p>
    </div>
  );
}

export default CustomerOrderDetailPage;
