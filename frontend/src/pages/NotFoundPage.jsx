import { Link } from 'react-router-dom';

/**
 * Trang hiển thị lỗi 404 - Không tìm thấy trang / sản phẩm đã ẩn.
 */
export function NotFoundPage() {
  return (
    <div
      role="region"
      aria-label="Trang không tồn tại"
      className="km-error-page"
    >
      <div className="km-error-page-code">
        404
      </div>
      <h1>
        Trang không tồn tại
      </h1>
      <p>
        Đường dẫn hoặc sản phẩm bạn yêu cầu không tồn tại hoặc hiện không khả dụng.
      </p>
      <Link to="/" className="km-btn km-btn-primary">
        Về trang chủ
      </Link>
    </div>
  );
}

export default NotFoundPage;
