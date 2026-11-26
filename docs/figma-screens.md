# Danh sách màn hình Figma → code (KeyMech)

File Figma: https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech (fileKey `xRB4rxkcc98AsLmf1Gil8d`). Mỗi dòng là một frame; `node-id` trong link là id để gọi Figma MCP (`get_design_context`, `get_screenshot`). Trang "Design System" (node `0:1`) chứa màu, font, component: đọc bằng `get_variable_defs` trước khi code.

## Cách dùng

1. Làm lần lượt theo lô, mỗi lần 3–5 màn. Với mỗi màn: đọc frame bằng Figma MCP, sửa file trong cột "File frontend", nối API ở cột "API", chạy `npm run lint` và `npm run build`.
2. Frontend đã có sẵn nhiều trang trong `frontend/src/pages/`. Hãy **sửa trang có sẵn cho khớp Figma**, không tạo trang trùng. Tên file có chữ (MỚI) là trang chưa có.
3. Xong màn nào thì đổi `[ ]` thành `[x]`.
4. Khi Figma mâu thuẫn với tài liệu yêu cầu trong `docs/`, **theo tài liệu** và ghi chú lại cho mình.

## Lỗi thiết kế đã biết (theo tài liệu yêu cầu thì làm khác Figma)

- "Ngân sách" của form Custom **không bắt buộc** (chỉ ≥ 500.000 ₫ nếu có nhập); Figma đang ghi dấu *.
- Khách từ chối báo giá → trạng thái `CANCELLED` ("Đã hủy", ghi chú "Khách từ chối báo giá"), không phải `REJECTED` ("Từ chối"). Bỏ dòng "Đã xác nhận • KeyMech sẽ sớm bắt đầu lắp ráp" ở frame từ chối.
- Nhãn trạng thái ở các frame Custom (Customer và Staff) đang sai/lặp "Đã báo giá": lấy trạng thái thật từ dữ liệu API.
- Staff cần nút "Từ chối" (`PENDING → REJECTED`) kèm ghi chú; Figma chưa có.
- Hủy đơn đã thanh toán trả 409 `PAYMENT_NOT_EDITABLE`: hiện thông báo "Đơn hàng đã thanh toán, không thể hủy hoặc sửa số lượng."
- Số điện thoại khi đăng ký là tùy chọn.
- Không có phí vận chuyển và thuế: bỏ hai dòng "Phí vận chuyển" và "Đã bao gồm thuế". Tổng = tạm tính.
- Staff và Admin không có giỏ hàng, thanh toán, Custom, đổi mật khẩu (dùng Quên mật khẩu).

## Lô 1 — Cửa hàng cốt lõi (Guest)

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Trang chủ • Máy tính (Shop (Guest/Customer), 1440px) | [3:110](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-110) | `/` | `pages/guest/HomePage.jsx` | GET /api/products |
| [x] | Danh sách sản phẩm • Máy tính (Shop (Guest/Customer), 1440px) | [3:132](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-132) | `/products` | `pages/guest/ProductListPage.jsx` | GET /api/products (page, size=12, category, brand, layout, minPrice, maxPrice, keyword) |
| [x] | Chi tiết sản phẩm • Máy tính (Shop (Guest/Customer), 1440px) | [3:154](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-154) | `/products/:id` | `pages/guest/ProductDetailPage.jsx` | GET /api/products/{id}; POST /api/cart/items |
| [x] | Giỏ hàng • Máy tính (Shop (Guest/Customer), 1440px) | [3:176](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-176) | `/cart` | `pages/guest/CartPage.jsx` | GET /api/cart; PUT/DELETE /api/cart/items/{productId}; DELETE /api/cart |
| [x] | Chi tiết • Nhân viên • Máy tính (Shop (Guest/Customer), 1440px) | [3:382](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-382) | `/products/:id (vai trò Staff/Admin)` | `pages/guest/ProductDetailPage.jsx` | Ẩn nút giỏ hàng cho Staff/Admin |

## Lô 2 — Đăng nhập, đăng ký, quên mật khẩu, thanh toán

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Đăng nhập • Máy tính (Shop (Guest/Customer), 1440px) | [3:198](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-198) | `/login` | `pages/guest/LoginPage.jsx` | POST /api/auth/login |
| [x] | Đăng ký • Máy tính (Shop (Guest/Customer), 1440px) | [3:220](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-220) | `/register` | `pages/guest/RegisterPage.jsx` | POST /api/auth/register |
| [x] | Quên mật khẩu • Máy tính (Shop (Guest/Customer), 1440px) | [3:242](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-242) | `/forgot-password` | `pages/guest/ForgotPasswordPage.jsx (MỚI)` | POST /api/auth/forgot-password |
| [x] | Đặt lại mật khẩu • Máy tính (Shop (Guest/Customer), 1440px) | [3:264](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-264) | `/reset-password` | `pages/guest/ResetPasswordPage.jsx (MỚI)` | POST /api/auth/reset-password |
| [x] | Thanh toán • Máy tính (Shop (Guest/Customer), 1440px) | [3:286](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-286) | `/checkout` | `pages/customer/CheckoutPage.jsx` | POST /api/orders/checkout |
| [x] | Đặt hàng thành công • Máy tính (Shop (Guest/Customer), 1440px) | [3:310](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-310) | `/customer/orders/:id/success` | `pages/customer/CustomerOrderDetailPage.jsx (prop success)` | GET /api/orders/my/{id} |
| [x] | Đơn hàng sau mua • Máy tính (Shop (Guest/Customer), 1440px) | [3:358](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-358) | `/customer/orders` | `pages/customer/CustomerOrdersPage.jsx` | GET /api/orders/my — kiểm tra lại với Figma xem frame này là màn nào |

## Lô 3 — Khu Customer (đơn hàng, hồ sơ, đổi mật khẩu)

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Đơn hàng của tôi / Desktop (Customer Account, 1440px) | [3:530](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-530) | `/customer/orders` | `pages/customer/CustomerOrdersPage.jsx` | GET /api/orders/my |
| [x] | Đơn hàng • Trống / Desktop (Customer Account, 1440px) | [3:534](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-534) | `/customer/orders (trạng thái trống)` | `pages/customer/CustomerOrdersPage.jsx + components/EmptyState.jsx` | GET /api/orders/my |
| [x] | Chi tiết đơn hàng / Desktop (Customer Account, 1440px) | [3:538](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-538) | `/customer/orders/:id` | `pages/customer/CustomerOrderDetailPage.jsx` | GET /api/orders/my/{id} |
| [x] | Chi tiết đơn • Đã hủy / Desktop (Customer Account, 1440px) | [3:542](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-542) | `/customer/orders/:id (trạng thái Đã hủy)` | `pages/customer/CustomerOrderDetailPage.jsx` | GET /api/orders/my/{id} |
| [x] | Hồ sơ cá nhân / Desktop (Customer Account, 1440px) | [3:562](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-562) | `/customer/profile` | `pages/customer/CustomerProfilePage.jsx` | GET/PUT /api/users/me |
| [x] | Đổi mật khẩu / Desktop (Customer Account, 1440px) | [3:566](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-566) | `/customer/change-password` | `pages/customer/ChangePasswordPage.jsx` | PUT /api/users/me/password |
| [x] | Cập nhật thành công (Customer Account, 520px) | [3:4540](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4540) | `Modal/Toast thành công` | `components/Toast.jsx` | — |

## Lô 4 — Custom phía Customer

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Custom phím • Gửi yêu cầu / Desktop (Customer Account, 1440px) | [3:546](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-546) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Custom phím • Yêu cầu của tôi / Desktop (Customer Account, 1440px) | [3:550](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-550) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Custom phím • Báo giá / Desktop (Customer Account, 1440px) | [3:554](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-554) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Custom phím • Giá thay đổi / Desktop (Customer Account, 1440px) | [3:558](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-558) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Custom phím • Khách đã đồng ý / Desktop (Customer Account, 1440px) | [3:4299](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4299) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Custom phím • Chờ tiếp nhận / Desktop (Customer Account, 1440px) | [3:4369](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4369) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Custom phím • Đã hủy / Desktop (Customer Account, 1440px) | [3:4443](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4443) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |
| [x] | Đồng ý báo giá? (Customer Account, 520px) | [3:4507](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4507) | `Modal (Custom)` | Modal trong trang Custom | Accept: kèm quotedPrice, lệch giá → 409 QUOTE_CHANGED |
| [x] | Từ chối báo giá? (Customer Account, 520px) | [3:4518](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4518) | `Modal (Custom)` | Modal trong trang Custom | Decline → CANCELLED, ghi chú "Khách từ chối báo giá" |
| [x] | Hủy yêu cầu Custom? (Customer Account, 520px) | [3:4529](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4529) | `Modal (Custom)` | Modal trong trang Custom | Chỉ khi PENDING/QUOTED |
| [x] | Custom phím • Từ chối báo giá / Desktop (Customer Account, 1440px) | [3:4548](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-4548) | `/customer/custom-requests` | `pages/customer/CustomerCustomRequestsPage.jsx (+ trang chi tiết MỚI)` | Đã nối /api/custom-requests/** |

## Lô 5 — Khu Staff

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Tổng quan nhân viên (Staff Portal, 1440px) | [3:102](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-102) | `/staff` | `pages/staff/StaffDashboardPage.jsx` | Ngoài 21 chức năng (extra) |
| [x] | Quản lý đơn hàng (Staff Portal, 1440px) | [3:103](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-103) | `/staff/orders` | `pages/staff/StaffOrdersPage.jsx` | GET /api/staff/orders (keyword, from, to, status, page) |
| [x] | Chi tiết đơn hàng (Staff Portal, 1440px) | [3:104](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-104) | `/staff/orders/:id` | `pages/staff/StaffOrderDetailPage.jsx` | GET /api/staff/orders/{id}; PUT .../items/{itemId}; PUT .../status; PUT .../payment |
| [x] | Quản lý sản phẩm (Staff Portal, 1440px) | [3:105](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-105) | `/staff/products` | `pages/staff/StaffProductsPage.jsx` | GET /api/staff/products |
| [x] | Thêm và chỉnh sửa sản phẩm (Staff Portal, 1440px) | [3:106](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-106) | `/staff/products/new và /staff/products/:id/edit` | `pages/staff/StaffProductFormPage.jsx (MỚI)` | Upload ảnh vào uploads/; giá > 0, tồn kho ≥ 0 |
| [x] | Yêu cầu Custom (Staff Portal, 1440px) | [3:107](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-107) | `/staff/custom-requests` | `pages/staff/StaffCustomRequestsPage.jsx` | GET /api/staff/custom-requests |
| [x] | Chi tiết yêu cầu Custom (Staff Portal, 1440px) | [3:108](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-108) | `/staff/custom-requests/:id` | `pages/staff/StaffCustomRequestDetailPage.jsx (MỚI)` | GET /api/staff/custom-requests/{id}; PUT .../quote; PUT .../status |
| [x] | Xác nhận hủy đơn (Staff Portal, 1440px) | [3:109](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-109) | Modal trong chi tiết đơn | `pages/staff/StaffOrderDetailPage.jsx` | PUT /api/staff/orders/{id}/status (CANCELLED); 409 PAYMENT_NOT_EDITABLE nếu đã thanh toán |
| [x] | Custom · Chờ tiếp nhận (Staff Portal, 1440px) | [3:5903](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-5903) | `/staff/custom-requests/:id (trạng thái tương ứng)` | `pages/staff/StaffCustomRequestDetailPage.jsx` | Chuyển trạng thái theo bảng mục 9 kế hoạch |
| [x] | Custom · Khách đã đồng ý (Staff Portal, 1440px) | [3:5989](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-5989) | `/staff/custom-requests/:id (trạng thái tương ứng)` | `pages/staff/StaffCustomRequestDetailPage.jsx` | Chuyển trạng thái theo bảng mục 9 kế hoạch |
| [x] | Custom · Đang lắp ráp (Staff Portal, 1440px) | [3:6075](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-6075) | `/staff/custom-requests/:id (trạng thái tương ứng)` | `pages/staff/StaffCustomRequestDetailPage.jsx` | Chuyển trạng thái theo bảng mục 9 kế hoạch |

## Lô 6 — Khu Admin, Chatbot, danh mục

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Tư vấn KeyMech • Máy tính (Shop (Guest/Customer), 1440px) | [3:334](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-334) | `Widget trên mọi trang cửa hàng` | `components/ChatWidget.jsx (MỚI)` | POST /api/chat; GET /api/chat/history |
| [x] | Dashboard quản trị (Admin Portal, 1440px) | [3:3825](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3825) | `/admin` | `pages/admin/AdminDashboardPage.jsx` | GET /api/admin/dashboard |
| [x] | Quản lý người dùng (Admin Portal, 1440px) | [3:3826](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3826) | `/admin/staff` | `pages/admin/AdminStaffPage.jsx` | GET /api/admin/users; PUT /api/admin/users/{id}/visibility |
| [x] | Tạo tài khoản nhân viên (Admin Portal, 1440px) | [3:3827](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3827) | `/admin/staff/new (hoặc modal)` | `pages/admin/AdminStaffPage.jsx` | POST /api/admin/users (role cố định STAFF) |
| [x] | Quản lý danh mục (Admin Portal, 1440px) | [3:3828](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3828) | `/admin/categories` | `pages/admin/AdminCategoriesPage.jsx (MỚI)` | GET /api/admin/categories |
| [x] | Thêm danh mục (Admin Portal, 1440px) | [3:3829](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3829) | `/admin/categories/new (hoặc modal)` | `pages/admin/AdminCategoriesPage.jsx` | Tên và slug không trùng |
| [x] | Không thể ẩn danh mục (Admin Portal, 1440px) | [3:3830](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3830) | `Modal lỗi 409` | `pages/admin/AdminCategoriesPage.jsx` | 409 khi còn sản phẩm đang hiện |
| [x] | Quản lý sản phẩm · Quản trị viên (Admin Portal, 1440px) | [3:3832](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3832) | `/staff/products (góc nhìn Admin: có Ẩn/Hiện)` | `pages/staff/StaffProductsPage.jsx` | GET /api/staff/products; PUT /api/admin/products/{id}/visibility |
| [x] | Người dùng · Sau khi ẩn (Admin Portal, 1440px) | [3:6220](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-6220) | `/admin/staff (trạng thái sau khi ẩn)` | `pages/admin/AdminStaffPage.jsx` | PUT /api/admin/users/{id}/visibility |
| [x] | Sản phẩm · Sau khi ẩn (Admin Portal, 1440px) | [3:6352](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-6352) | `/staff/products (trạng thái sau khi ẩn)` | `pages/staff/StaffProductsPage.jsx` | PUT .../{id}/visibility |

## Lô 7 — Trạng thái, lỗi, toast, di động (360px)

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [x] | Trang chủ • Di động (Shop (Guest/Customer), 360px) | [3:397](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-397) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Danh sách sản phẩm • Di động (Shop (Guest/Customer), 360px) | [3:416](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-416) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Chi tiết sản phẩm • Di động (Shop (Guest/Customer), 360px) | [3:435](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-435) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Giỏ hàng • Di động (Shop (Guest/Customer), 360px) | [3:454](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-454) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Thanh toán • Di động (Shop (Guest/Customer), 360px) | [3:473](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-473) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Tư vấn KeyMech • Di động (Shop (Guest/Customer), 360px) | [3:492](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-492) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Bộ lọc sản phẩm • Di động (Shop (Guest/Customer), 360px) | [3:511](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-511) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Đăng nhập • Di động (Shop (Guest/Customer), 360px) | [3:5579](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-5579) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Đặt hàng thành công • Di động (Shop (Guest/Customer), 360px) | [3:5636](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-5636) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Đơn hàng của tôi • Di động (Shop (Guest/Customer), 360px) | [3:5690](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-5690) | `Cùng route bản desktop, breakpoint 360px` | Cùng file bản desktop (CSS responsive) | Giống bản desktop |
| [x] | Menu tài khoản (Shop (Guest/Customer), 240px) | [3:6588](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-6588) | `Menu tài khoản (di động)` | `layouts/CustomerLayout.jsx` | — |

## Ngoài phạm vi (làm sau hoặc bỏ)

| Xong | Frame (trang Figma) | Link | Route | File frontend | API / ghi chú |
| --- | --- | --- | --- | --- | --- |
| [ ] | Báo cáo doanh thu (Admin Portal, 1440px) | [3:3831](https://www.figma.com/design/xRB4rxkcc98AsLmf1Gil8d/KeyMech?node-id=3-3831) | `/admin/reports` | `pages/admin/AdminReportsPage.jsx` | NGOÀI PHẠM VI 21 chức năng — làm sau cùng hoặc bỏ |

## Tích hợp API sau PR UI

Custom, sản phẩm Staff, danh mục và Dashboard Admin dùng dữ liệu từ API. Màn hình Custom phía Staff hiển thị mã khách hàng. Ảnh và tên sản phẩm trong danh sách đơn, bộ lọc trạng thái và nút **Mua lại** tiếp tục ở dạng placeholder theo phạm vi đã chốt.
