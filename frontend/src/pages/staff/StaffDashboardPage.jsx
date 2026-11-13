import { Link } from 'react-router-dom';

/**
 * Trang tổng quan dành cho Nhân viên (Staff).
 */
export function StaffDashboardPage() {
  return (
    <div>
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 700,
          color: '#1e293b',
          marginBottom: '1rem',
        }}
      >
        Bàn làm việc Nhân viên (Staff)
      </h1>
      <p style={{ color: '#64748b', marginBottom: '2rem' }}>
        Theo dõi đơn hàng cần xử lý, cập nhật trạng thái kho sản phẩm và duyệt các yêu cầu Custom phím từ khách hàng.
      </p>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: '1.5rem',
        }}
      >
        <div
          style={{
            backgroundColor: '#ffffff',
            padding: '1.5rem',
            borderRadius: '8px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          }}
        >
          <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.5rem' }}>
            📦 Xử lý đơn hàng
          </h2>
          <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '1rem' }}>
            Duyệt đơn hàng mới, cập nhật mã vận đơn và chuyển trạng thái giao hàng.
          </p>
          <Link
            to="/staff/orders"
            style={{
              color: '#2563eb',
              textDecoration: 'none',
              fontWeight: 500,
              fontSize: '0.9rem',
            }}
          >
            Quản lý đơn hàng →
          </Link>
        </div>

        <div
          style={{
            backgroundColor: '#ffffff',
            padding: '1.5rem',
            borderRadius: '8px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          }}
        >
          <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.5rem' }}>
            ⌨️ Danh mục sản phẩm
          </h2>
          <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '1rem' }}>
            Xem tồn kho, ẩn/hiện sản phẩm và cập nhật mô tả chi tiết.
          </p>
          <Link
            to="/staff/products"
            style={{
              color: '#2563eb',
              textDecoration: 'none',
              fontWeight: 500,
              fontSize: '0.9rem',
            }}
          >
            Quản lý sản phẩm →
          </Link>
        </div>

        <div
          style={{
            backgroundColor: '#ffffff',
            padding: '1.5rem',
            borderRadius: '8px',
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          }}
        >
          <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: '#1e293b', marginBottom: '0.5rem' }}>
            🔧 Duyệt Custom phím
          </h2>
          <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '1rem' }}>
            Tiếp nhận cấu hình custom từ khách, báo giá và cập nhật tiến độ ráp phím.
          </p>
          <Link
            to="/staff/custom-requests"
            style={{
              color: '#2563eb',
              textDecoration: 'none',
              fontWeight: 500,
              fontSize: '0.9rem',
            }}
          >
            Duyệt yêu cầu Custom →
          </Link>
        </div>
      </div>
    </div>
  );
}

export default StaffDashboardPage;
