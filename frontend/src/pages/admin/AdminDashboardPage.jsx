import { Link } from 'react-router-dom';

/**
 * Trang tổng quan dành cho Quản trị viên (Admin).
 */
export function AdminDashboardPage() {
  return (
    <div>
      <h1
        style={{
          fontSize: '1.85rem',
          fontWeight: 800,
          color: '#ffffff',
          marginBottom: '0.5rem',
        }}
      >
        Bảng điều khiển Quản trị viên (Admin)
      </h1>
      <p style={{ color: '#94a3b8', marginBottom: '2rem' }}>
        Theo dõi toàn diện các chỉ số kinh doanh, quản trị tài khoản nhân sự và báo cáo hệ thống KeyMech.
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
            backgroundColor: '#1e293b',
            border: '1px solid #334155',
            padding: '1.5rem',
            borderRadius: '8px',
          }}
        >
          <div style={{ color: '#818cf8', fontSize: '0.85rem', fontWeight: 600, textTransform: 'uppercase' }}>
            Nhân sự
          </div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, margin: '0.5rem 0', color: '#ffffff' }}>
            Quản lý tài khoản Staff
          </h2>
          <p style={{ color: '#94a3b8', fontSize: '0.9rem', marginBottom: '1.25rem' }}>
            Tạo tài khoản nhân viên mới, phân công nhiệm vụ và quản lý trạng thái hoạt động.
          </p>
          <Link
            to="/admin/staff"
            style={{
              color: '#a5b4fc',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '0.9rem',
            }}
          >
            Vào trang quản lý nhân viên →
          </Link>
        </div>

        <div
          style={{
            backgroundColor: '#1e293b',
            border: '1px solid #334155',
            padding: '1.5rem',
            borderRadius: '8px',
          }}
        >
          <div style={{ color: '#34d399', fontSize: '0.85rem', fontWeight: 600, textTransform: 'uppercase' }}>
            Kinh doanh
          </div>
          <h2 style={{ fontSize: '1.3rem', fontWeight: 700, margin: '0.5rem 0', color: '#ffffff' }}>
            Báo cáo doanh thu & Đơn hàng
          </h2>
          <p style={{ color: '#94a3b8', fontSize: '0.9rem', marginBottom: '1.25rem' }}>
            Thống kê doanh thu theo thời gian, tỷ lệ đơn hàng hoàn thành và phân tích xu hướng mua sắm.
          </p>
          <Link
            to="/admin/reports"
            style={{
              color: '#6ee7b7',
              textDecoration: 'none',
              fontWeight: 600,
              fontSize: '0.9rem',
            }}
          >
            Xem báo cáo chi tiết →
          </Link>
        </div>
      </div>
    </div>
  );
}

export default AdminDashboardPage;
