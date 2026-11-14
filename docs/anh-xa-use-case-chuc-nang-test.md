# Bảng ánh xạ: use case, chức năng, API, test case

Soạn bởi Phương (Sprint 1). SRS là chuẩn (R16). **Cột UC chưa điền** vì SRS (23 use case) không nằm trong kho mã: chỉ biết UC-23 là Quên mật khẩu. Cả nhóm điền cột này từ SRS rồi đối chiếu; chức năng nào không có UC tương ứng hoặc ngược lại thì ghi vào cột ghi chú và sửa kế hoạch hoặc SRS.

Cột test chỉ ghi ID đã có trong kế hoạch bản 3 (mục 11) hoặc đã viết trong mã. Ô `chưa có` là chỗ nhóm cần bổ sung test case.

| Chức năng | Tên | UC | Người giữ | API chính | Test case |
| --- | --- | --- | --- | --- | --- |
| G-01 | Xem danh sách sản phẩm | | Thuận | `GET /api/products` | TC-TRK-01 (tracking), chưa có test lọc |
| G-02 | Xem chi tiết sản phẩm | | Thuận | `GET /api/products/{id}` | TC-HID-02, TC-TRK-01 |
| G-03 | Giỏ hàng | | Thuận | `/api/cart/**` | TC-CART-06, TC-CART-07, TC-SEC-15, TC-HID-03 |
| G-04 | Chatbot | | Thùy (widget: Phương) | `POST /api/chat`, `GET /api/chat/history` | TC-BOT-08 đến TC-BOT-18, TC-IDX-01, TC-IDX-02 |
| G-05 | Đăng ký | | Trí | `POST /api/auth/register` | TC-SEC-12, TC-SEC-18 |
| G-06 | Quên mật khẩu | UC-23 | Trí (Could) | `POST /api/auth/forgot-password`, `/reset-password` | TC-PWD-02, TC-PWD-03, TC-PWD-04 |
| C-01 | Đăng nhập, đăng xuất | | Trí | `POST /api/auth/login`, `/logout`, `GET /me` | TC-SEC-07, TC-SEC-19, TC-TRK-02 |
| C-02 | Đặt hàng và thanh toán | | Thùy | `POST /api/orders/checkout` | TC-PAY-07, TC-PAY-08, TC-SEC-14, TC-TRK-04 |
| C-03 | Gửi yêu cầu Custom | | **Phương** | `POST /api/custom-requests` | `CustomRequestServiceTest` (C-03); test ID TC-CUS-01 đến 07 của nhóm: chưa có |
| C-04 | Xem đơn hàng và tiến độ Custom | | Thùy (đơn), **Phương** (Custom) | `GET /api/custom-requests/my`, `/my/{id}` | TC-CUS-09 |
| C-05 | Đổi mật khẩu | | Trí | `PUT /api/users/me/password` | TC-PWD-01 |
| C-06 | Phản hồi báo giá Custom | | **Phương** | `PUT .../my/{id}/accept`, `/decline` | TC-CUS-10, TC-CUS-11, TC-CUS-13 |
| A-01 | Staff thêm, sửa sản phẩm | | Thuận | `/api/staff/products/**` | chưa có |
| A-02 | Admin ẩn/hiện sản phẩm | | Thuận | `PUT /api/admin/products/{id}/visibility` | TC-HID-02, TC-BOT-09 |
| A-03 | Admin quản lý người dùng | | Trí | `/api/admin/users/**` | TC-HID-01, TC-SEC-13, TC-SEC-17 |
| A-04 | Tìm kiếm và lọc đơn | | Thùy | `GET /api/staff/orders` | chưa có |
| A-05 | Sửa số lượng mặt hàng trong đơn | | Thùy | `PUT /api/staff/orders/{id}/items/{itemId}` | chưa có |
| A-06 | Cập nhật trạng thái đơn và thanh toán | | Thùy | `PUT .../status`, `.../payment` | TC-PAY-09, TC-PAY-10 |
| A-07 | Quản lý yêu cầu Custom | | **Phương** | `/api/staff/custom-requests/**` | TC-CUS-08, TC-CUS-12; `CustomRequestServiceTest`, `CustomStatusPolicyTest` (A-07) |
| A-08 | Dashboard | | Thuận | `GET /api/admin/dashboard` | chưa có |
| A-09 | Admin quản lý danh mục | | Thuận | `/api/admin/categories/**` | TC-HID-04 |

## Test case do Phương viết trong mã

| Nhóm | Lớp test | Phủ |
| --- | --- | --- |
| Custom | `CustomStatusPolicyTest` | Bảng chuyển trạng thái, bước của khách và của Staff, TC-CUS-12 |
| Custom | `CustomRequestServiceTest` | C-03, C-06, A-07, TC-CUS-09 đến TC-CUS-13 ở mức Service |
| Tracking | `ActivityTrackerTest` | TC-TRK-01, TC-TRK-02, TC-TRK-03, TC-TRK-05 |
| Tracking | `ActivityWriterTest` | Ghi dòng, lọc trùng 60 giây, nuốt lỗi |
| Tracking | `ProfileServiceTest`, `InterestProfileBuilderTest` | Hồ sơ quan tâm, TC-BOT-17, TC-BOT-18 |

## Còn thiếu ở mức API (cần `@SpringBootTest` hoặc Postman)

TC-CUS-08 (Customer gọi `/api/staff/custom-requests/{id}/status` phải 403) và TC-TRK-06 (không có endpoint nhận sự kiện) kiểm ở tầng URL và cần ngữ cảnh Spring; hiện dùng collection Postman `docs/postman/custom-requests.postman_collection.json`. Chuyển thành test tự động khi nhóm chốt dependency test (xem ghi chú bàn giao).
