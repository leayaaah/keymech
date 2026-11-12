import EmptyState from '../../components/EmptyState';

/**
 * Trang quản lý tài khoản nhân viên (Staff) dành cho Quản trị viên.
 */
export function AdminStaffPage() {
  const staffList = [];

  return (
    <div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.5rem',
        }}
      >
        <div>
          <h1
            style={{
              fontSize: '1.75rem',
              fontWeight: 800,
              color: '#ffffff',
              margin: '0 0 0.25rem 0',
            }}
          >
            Quản lý tài khoản Nhân viên (Staff)
          </h1>
          <p style={{ color: '#94a3b8', margin: 0, fontSize: '0.9rem' }}>
            Chỉ Admin có quyền tạo tài khoản nhân viên mới và gán vai trò STAFF.
          </p>
        </div>

        <button
          type="button"
          onClick={() => alert('Chức năng tạo Staff sẽ mở ở Sprint sau.')}
          style={{
            backgroundColor: '#4f46e5',
            color: '#ffffff',
            border: 'none',
            padding: '0.6rem 1.25rem',
            borderRadius: '6px',
            fontWeight: 600,
            cursor: 'pointer',
            fontSize: '0.9rem',
          }}
        >
          + Thêm nhân viên mới
        </button>
      </div>

      {staffList.length === 0 ? (
        <EmptyState
          title="Chưa có nhân viên nào trong danh sách"
          description="Nhấn nút '+ Thêm nhân viên mới' để tạo tài khoản Staff phục vụ vận hành."
        />
      ) : (
        <div>
          <p>Danh sách nhân viên...</p>
        </div>
      )}
    </div>
  );
}

export default AdminStaffPage;
