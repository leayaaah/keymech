import EmptyState from '../../components/EmptyState';

/**
 * Trang quản lý yêu cầu Custom bàn phím cơ của khách hàng.
 */
export function CustomerCustomRequestsPage() {
  const requests = [];

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
        Yêu cầu Custom bàn phím
      </h1>

      {requests.length === 0 ? (
        <EmptyState
          title="Chưa có yêu cầu Custom nào"
          description="Bạn có thể tạo yêu cầu cấu hình phím cơ riêng (kit, switch, keycap, mod âm) để nhận báo giá từ thợ phím KeyMech."
          actionText="Tạo yêu cầu Custom mới"
          onAction={() => alert('Chức năng tạo yêu cầu Custom sẽ mở ở Sprint sau.')}
        />
      ) : (
        <div>
          <p>Danh sách yêu cầu custom...</p>
        </div>
      )}
    </div>
  );
}

export default CustomerCustomRequestsPage;
