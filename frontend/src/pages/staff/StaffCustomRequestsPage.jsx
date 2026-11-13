import EmptyState from '../../components/EmptyState';

/**
 * Trang duyệt yêu cầu Custom phím dành cho Nhân viên.
 */
export function StaffCustomRequestsPage() {
  const requests = [];

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
        Yêu cầu Custom bàn phím cần duyệt
      </h1>
      <p style={{ color: '#64748b', marginBottom: '1.5rem' }}>
        Khu vực thợ phím kiểm tra cấu hình, gửi báo giá và theo dõi trạng thái gia công.
      </p>

      {requests.length === 0 ? (
        <EmptyState
          title="Không có yêu cầu Custom nào đang chờ"
          description="Hiện tại không có yêu cầu Custom nào ở trạng thái chờ duyệt."
        />
      ) : (
        <div>
          <p>Danh sách yêu cầu custom...</p>
        </div>
      )}
    </div>
  );
}

export default StaffCustomRequestsPage;
