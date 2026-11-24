# Ma trận phân quyền trên bản tích hợp

Ngày 25/11/2026, nhánh `chore/C-01-role-permission-matrix` từ `origin/main` tại `e4659ae` (đã có PR #17). Đối chiếu với [kế hoạch nhóm, mục 7 và 11](Ke_hoach_du_an_Web_TMDT_ban_phim_co_Nhom_4_v3.md) và [phân công Sprint 3](Phan_cong_Nhom4_deadline_30-11.md).

Chạy Chrome qua Vite, Spring Boot và MariaDB 11.8.5 cô lập. Mỗi vai trò dùng một phiên trình duyệt riêng, trừ luồng Guest chuyển sang Customer dùng cùng phiên. Mã `401` và `403` ở các API ưu tiên được kiểm tra cùng mã lỗi `UNAUTHORIZED` và `FORBIDDEN` trong JSON.

## API

| Yêu cầu | Guest | Customer | Staff | Admin |
| --- | --- | --- | --- | --- |
| `GET /api/products` | 200 | 200 | 200 | 200 |
| `GET /api/chat/history` | 200 | 200 | 200 | 200 |
| `POST /api/auth/register` | 201, tạo Customer | 403 | 403 | 403 |
| `GET /api/auth/me` | 401 | 200, `CUSTOMER` | 200, `STAFF` | 200, `ADMIN` |
| `GET /api/cart` | 200 | 200 | 403 | 403 |
| `POST /api/cart/items` | 200 | 200 | 403 | 403 |
| `PUT /api/cart/items/{productId}` | 200 | 200 | 403 | 403 |
| `DELETE /api/cart/items/{productId}` | 200 | 200 | 403 | 403 |
| `DELETE /api/cart` | 200 | 200 | 403 | 403 |
| `POST /api/orders/checkout` | 401 | 201 khi giỏ hợp lệ | 403 | 403 |
| `GET /api/orders/my` | 401 | 200 | 403 | 403 |
| `PUT /api/users/me` | 401 | 200 | 403 | 403 |
| `GET /api/staff/orders` | 401 | 403 | 200 | 200 |
| `GET /api/admin/users` | 401 | 403 | 403 | 200 |

`GET /api/auth/me` đi qua matcher công khai ở cả bốn vai trò. Guest nhận `401` từ nghiệp vụ xác định người dùng hiện tại, nên dấu cho phép trong bảng matcher của kế hoạch không đồng nghĩa với HTTP `200`.

## Giao diện

| Đường dẫn | Guest | Customer | Staff | Admin |
| --- | --- | --- | --- | --- |
| `/cart` | Mở giỏ | Mở giỏ | Trang 403 | Trang 403 |
| `/checkout` | Chuyển đến đăng nhập | Mở thanh toán | Trang 403 | Trang 403 |
| `/staff/orders` | Chuyển đến đăng nhập | Trang 403 | Mở quản lý đơn | Mở quản lý đơn |
| `/admin/staff` | Chuyển đến đăng nhập | Trang 403 | Trang 403 | Mở quản lý người dùng |

## Luồng hồi quy đã chạy

- **TC-SEC-07.** Guest thêm sản phẩm vào giỏ từ trang chi tiết, cập nhật số lượng thành 2 rồi chọn **Thanh toán**. Đăng nhập Customer làm đổi `JSESSIONID`, giữ nguyên toàn bộ dữ liệu giỏ và đưa người dùng về `/checkout`. Đặt COD trả `201`, mở trang thành công và xóa giỏ. MariaDB có đơn thử `ORD-UBCWTEZNF7` ở trạng thái `NEW`, dòng hàng số lượng 2, tồn kho sản phẩm giảm từ 25 xuống 23.
- **TC-SEC-19 và biến thể Admin.** Chrome xác nhận `JSESSIONID` đổi khi phiên Guest có giỏ đăng nhập Staff hoặc Admin. `AuthServiceTest` kiểm tra trực tiếp thuộc tính giỏ bị xóa cho cả hai vai trò. Các yêu cầu giỏ và checkout sau đăng nhập trả `403`.
- **TC-SEC-08, 10, 11, 14, 15, 16 và 18.** Chạy các yêu cầu đại diện trong ma trận API và mở trực tiếp các trang Staff, Admin tương ứng. Admin vào được khu Staff. Staff bị từ chối ở khu Admin.
- Toàn bộ `mvn test` trong `backend` đạt **90/90** trên MariaDB 11.8.5. Dịch vụ SMTP cục bộ nhận email xác nhận của ca đặt hàng trên Chrome.

## Chạy lại

1. Nạp `database/01-schema.sql` và `database/02-data.sql` vào schema MariaDB riêng. Chuẩn bị tài khoản thử Customer, Staff và Admin. Dùng SMTP cục bộ cho ca đặt hàng.
2. Khởi động backend ở cổng `8080` và frontend ở `http://localhost:5173`. Mở phiên riêng cho từng vai trò. Dùng cùng cookie phiên cho các request trước và sau đăng nhập trong ca Guest có giỏ.
3. Thực hiện các dòng trong bảng bằng payload hợp lệ và hai luồng đăng nhập ở trên. Sau checkout, kiểm tra `orders`, `order_items`, `products.stock_quantity` và `GET /api/cart` của cùng phiên. Chạy `mvn test` với cấu hình kết nối schema thử để lặp lại các kiểm tra ở backend.

Bản tích hợp này chưa có controller cho API Custom và `GET /api/categories` theo kế hoạch. Các phần đó cần được bổ sung vào ma trận chạy thực tế khi endpoint được tích hợp.
