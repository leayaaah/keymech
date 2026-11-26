import { Link } from 'react-router-dom';

/**
 * Trang hiển thị lỗi 403 - Không đủ quyền truy cập.
 */
export function ForbiddenPage() {
  return (
    <div
      role="region"
      aria-label="Không đủ quyền truy cập"
      className="km-error-page"
    >
      <div className="km-error-page-code km-error-page-code--forbidden">
        403
      </div>
      <h1>
        Bạn không đủ quyền truy cập
      </h1>
      <p>
        Tài khoản hiện tại của bạn không có quyền truy cập vào khu vực này.
      </p>
      <Link to="/" className="km-btn km-btn-primary">
        Về trang chủ
      </Link>
    </div>
  );
}

export default ForbiddenPage;
