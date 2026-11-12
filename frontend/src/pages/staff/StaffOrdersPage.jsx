import EmptyState from '../../components/EmptyState';

/**
 * Trang quản lý đơn hàng dành cho Nhân viên.
 */
export function StaffOrdersPage() {
  const orders = [];

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
        Danh sách đơn hàng toàn hệ thống
      </h1>
      <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>
        Khu vực nhân viên xử lý đơn, cập nhật trạng thái đơn hàng (CONFIRMED, SHIPPING, COMPLETED, CANCELLED).
      </p>

      {orders.length === 0 ? (
        <EmptyState
          title="Không có đơn hàng nào cần xử lý"
          description="Hiện tại tất cả đơn hàng đều đã được xử lý hoặc chưa phát sinh đơn mới."
        />
      ) : (
        <div>
          <p>Bảng danh sách đơn hàng...</p>
        </div>
      )}
    </div>
  );
}

export default StaffOrdersPage;
