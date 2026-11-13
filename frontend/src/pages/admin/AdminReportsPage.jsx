import EmptyState from '../../components/EmptyState';

/**
 * Trang báo cáo doanh thu và số liệu hệ thống dành cho Quản trị viên.
 */
export function AdminReportsPage() {
  return (
    <div>
      <h1
        style={{
          fontSize: '1.75rem',
          fontWeight: 800,
          color: '#ffffff',
          marginBottom: '0.5rem',
        }}
      >
        Báo cáo doanh thu & Thống kê kinh doanh
      </h1>
      <p style={{ color: '#94a3b8', marginBottom: '1.5rem' }}>
        Biểu đồ và bảng thống kê kết nối với các endpoint `/api/admin/reports/**`.
      </p>

      <EmptyState
        title="Đang đồng bộ số liệu báo cáo"
        description="Dữ liệu báo cáo doanh số và đơn hàng sẽ được tích hợp ở các Sprint tiếp theo."
      />
    </div>
  );
}

export default AdminReportsPage;
