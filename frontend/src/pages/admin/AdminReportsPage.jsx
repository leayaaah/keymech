import EmptyState from '../../components/EmptyState';

/**
 * Trang báo cáo doanh thu và số liệu hệ thống dành cho Quản trị viên.
 */
export function AdminReportsPage() {
  return (
    <div>
      <h1 className="km-portal-title">
        Báo cáo doanh thu & Thống kê kinh doanh
      </h1>
      <EmptyState
        title="Chưa có dữ liệu báo cáo"
        description="Dữ liệu báo cáo doanh số và đơn hàng sẽ được tích hợp ở các Sprint tiếp theo."
      />
    </div>
  );
}

export default AdminReportsPage;
