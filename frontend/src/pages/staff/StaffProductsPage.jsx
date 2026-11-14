import EmptyState from '../../components/EmptyState';

/**
 * Trang quản lý sản phẩm dành cho Nhân viên.
 */
export function StaffProductsPage() {
  const products = [];

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
        Quản lý danh mục & kho hàng
      </h1>
      <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>
        Khu vực xem toàn bộ sản phẩm (kể cả sản phẩm đã ẩn HIDDEN), cập nhật thông tin và điều chỉnh tồn kho.
      </p>

      {products.length === 0 ? (
        <EmptyState
          title="Chưa tải sản phẩm"
          description="Sẽ kết nối với API `/api/staff/products`."
        />
      ) : (
        <div>
          <p>Danh sách sản phẩm...</p>
        </div>
      )}
    </div>
  );
}

export default StaffProductsPage;
