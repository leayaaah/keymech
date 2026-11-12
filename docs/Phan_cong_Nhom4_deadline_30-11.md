# Phân công code – Web TMĐT bàn phím cơ (Nhóm 4)

Bản phân công theo hạn nộp **30/11/2026**, dựa trên Kế hoạch bản 3, `CODING_CONVENTIONS.md` và `KHUNG_DU_AN_SPRING.md`. Soạn ngày 11/11/2026.

Thành viên: **Trí, Thuận, Thùy, Phương**.

## 1. Tóm tắt và giả định

- Hạn nộp 30/11 (Thứ Hai) thay cho 18/01 của kế hoạch gốc. Còn 19 ngày, ít hơn một nửa thời gian dự kiến, nên **phải cắt phạm vi** (mục 3). Nếu 30/11 chỉ là hạn làm code còn hạn nộp muộn hơn thì báo mình để nới lại.
- Mỗi người sở hữu trọn một module (entity, repository, service, controller và trang React của module đó). Người khác chỉ gọi vào, không viết lại.
- Chatbot do **Thùy** làm; Custom (một phần việc của Trí) do **Phương** làm; tracking do Phương giữ.
- Báo cáo và slide viết dần từ Sprint 2, không dồn vào cuối.

## 2. Mốc thời gian

| Sprint | Thời gian | Mốc |
| --- | --- | --- |
| 1. Nền tảng | 11/11 đến 15/11 | M1 (15/11): khung chạy trên máy cả nhóm, đăng nhập thủ công, entity đủ, hợp đồng API v1, đã gửi câu hỏi giảng viên |
| 2. Chức năng lõi | 16/11 đến 22/11 | M2 (22/11): Guest xem, lọc, giỏ, đăng ký; đăng nhập; checkout chạy; Custom phía Customer chạy |
| 3. Staff, Admin, hoàn thiện | 23/11 đến 27/11 | M3 (27/11): **feature freeze**, mọi chức năng mức Must và Should chạy được |
| 4. Kiểm thử, đóng gói, nộp | 28/11 đến 30/11 | M4 (30/11): nộp báo cáo, slide, mã nguồn, video demo |

## 3. Mức ưu tiên (cắt từ dưới lên nếu trễ)

| Mức | Chức năng |
| --- | --- |
| **Must** (luồng demo chính) | C-01, G-01, G-02, G-03, G-05, C-02, C-04 (đơn hàng và Custom), A-01, A-02, A-03 (tạo Staff, ẩn/hiện), A-04, A-06, C-03, A-07 |
| **Should** | G-04 (Chatbot), C-05, C-06, A-05, A-09, A-08 (chỉ thẻ số liệu) |
| **Could** (cắt trước) | G-06 Quên mật khẩu, cá nhân hóa Chatbot (`ProfileService`), endpoint reindex, biểu đồ Dashboard, bật lại CSRF, email đổi trạng thái Custom, hồ sơ cá nhân |

Nếu cắt G-06 thì phải sửa SRS (UC-23) và báo cáo cho khớp (rủi ro R16).

**Chatbot làm hai giai đoạn:** giai đoạn A là `RuleBasedChatbotService` + `/api/chat` + `ChatWidget` (chắc chắn xong); giai đoạn B là RAG bằng Spring AI chỉ làm khi Thùy xong checkout trước 22/11. Nếu spike Sprint 1 không chạy sau 2 ngày thì dùng `SimpleVectorStore`, nếu vẫn lỗi thì giữ giai đoạn A.

**Kiểm thử rút gọn:** chỉ viết test tự động cho phần rủi ro cao: đăng nhập và quyền (TC-SEC-07, 19), trừ tồn kho (TC-PAY-07 đến 09), bảng chuyển trạng thái Custom. Các test case còn lại chạy tay theo checklist và ghi kết quả. Cần cả nhóm đồng ý nới mục 11 và 14 của `CODING_CONVENTIONS.md` cho đến 30/11.

## 4. Phân module

| Người | Chức năng | Giữ |
| --- | --- | --- |
| **Trí** | C-01, G-05, C-05, A-03, G-06 (Could) | `SecurityConfig`, `AuthService`, `EmailService` (hạ tầng), `AuthContext`, `ProtectedRoute`, khung frontend (Vite, Axios, routes, `formatVND`); giao diện Staff cho đơn hàng (A-04 đến A-06) theo API của Thùy |
| **Thuận** | G-01, G-02, G-03, A-01, A-02, A-09, A-08 | `ProductService`, `CartService`, `ProductChangedEvent`, layout cửa hàng, `DashboardRepository` |
| **Thùy** | C-02, C-04 (đơn hàng), A-04, A-05, A-06, G-04 | `OrderService`, trừ và hoàn tồn kho, package `ai` (`ChatbotService`, `RuleBasedChatbotService`, `AiChatbotService`, `ProductIndexer`) |
| **Phương** | C-03, C-04 (Custom), C-06, A-07 | `CustomRequestService`, package `tracking` (`ActivityTracker`, `ProfileService`), `ChatWidget` (React), hợp đồng API, bảng ánh xạ UC |

## 5. Việc theo sprint

### Sprint 1 (11/11 đến 15/11)

| Người | Việc |
| --- | --- |
| Trí | Đăng nhập thủ công (`changeSessionId`, lưu `SecurityContext`, xóa giỏ của Staff và Admin), logout, `/me`; merge trước 13/11. Khung frontend. Chốt phiên bản Boot, Spring AI, JDK cùng nhóm ngay trong tuần này |
| Thuận | Entity và repository `Product`, `Category`; merge trước 12/11 vì Order và Chatbot cần. Ảnh mẫu trong `uploads/`. Hợp đồng API catalog và giỏ |
| Thùy | Spike Chatbot tối đa 2 ngày (khóa Gemini, DDL `vector_store`, `SimpleVectorStore` dự phòng). Entity `Order`, `OrderItem`. Hợp đồng API đơn hàng |
| Phương | Entity `CustomRequest`, `CustomRequestHistory`, `UserActivity`. Chốt chữ ký `ActivityTracker.record(...)`, merge trước 13/11. Bảng ánh xạ UC, chức năng, test case. Gửi câu hỏi cho giảng viên (vai trò Staff, `VEC_DISTANCE` có vi phạm "không dùng Function" không, Internet khi demo). Hợp đồng API Custom |

### Sprint 2 (16/11 đến 22/11)

| Người | Việc |
| --- | --- |
| Trí | Hoàn thiện C-01, G-05 + email chào mừng (`EmailService`, MailHog), C-05; matcher `/api/staff/**`, `/api/admin/**`; trang đăng nhập, đăng ký, đổi mật khẩu; backend A-03 (tạo Staff, ẩn/hiện, không đụng Admin) |
| Thuận | G-01, G-02, G-03 (API và giao diện: danh sách, lọc, phân trang, chi tiết, giỏ), `CartContext`, header; gọi `ActivityTracker` cho `VIEW_PRODUCT`, `SEARCH`, `ADD_TO_CART`; tạo sẵn `ProductChangedEvent`. Giỏ API xong trước 19/11 |
| Thùy | `placeOrder` (UPDATE tồn có điều kiện, rollback, việc phụ sau commit) và `POST /api/orders/checkout`, nối giỏ thật sau 19/11; email xác nhận đơn; Chatbot giai đoạn A (`RuleBasedChatbotService`, `/api/chat`) |
| Phương | `ActivityTracker` (async, bỏ qua Staff và Admin, `visitor_id` trong Session); Custom phía Customer: C-03, C-04, C-06, bảng chuyển trạng thái, lịch sử, form và trang tiến độ |

### Sprint 3 (23/11 đến 27/11, feature freeze 27/11)

| Người | Việc |
| --- | --- |
| Trí | Giao diện Staff đơn hàng (tìm kiếm, sửa số lượng, đổi trạng thái, thanh toán) theo API Thùy chốt trước 20/11; giao diện Admin quản lý người dùng; G-06 nếu còn thời gian; rà phân quyền 4 vai trò |
| Thuận | A-01 Staff thêm, sửa sản phẩm (upload: kiểm tra loại và dung lượng, đổi tên ngẫu nhiên); A-02, A-09 (409 khi còn sản phẩm đang hiện); A-08 Dashboard dạng thẻ số liệu |
| Thùy | A-04, A-05, A-06 (API, hủy đơn hoàn tồn, `payment_status`); trang thanh toán, "Đặt hàng thành công", đơn của tôi; Chatbot giai đoạn B nếu kịp |
| Phương | A-07 (báo giá, chuyển trạng thái đúng luồng, ghi lịch sử) và trang Staff cho Custom; `ChatWidget`; ghi `PURCHASE` khi Thùy gọi; `ProfileService` nếu còn thời gian |

### Sprint 4 (28/11 đến 30/11)

Không thêm tính năng; mỗi người sửa lỗi module của mình.

| Người | Việc |
| --- | --- |
| Trí | Hồi quy phân quyền 4 vai trò; review chéo |
| Thuận | Gộp build vào `.jar`, README, chạy thử trên máy sạch |
| Thùy | Kết quả test PAY và Chatbot; viết phần báo cáo về đơn hàng và Chatbot |
| Phương | Tổng hợp báo cáo, slide, sơ đồ, quay video demo |

## 6. Quy ước tránh đụng file

| Điểm chung | Quy ước |
| --- | --- |
| `SecurityConfig` | Chỉ Trí sửa; ai thêm endpoint thì nhắn Trí |
| `ProductRepository` | Thuận giữ. Thùy đặt câu UPDATE tồn kho và truy vấn chatbot trong interface repository riêng |
| `UserRepository` | Trí giữ; ai cần thêm method thì thêm cuối file bằng PR nhỏ |
| `EmailService` | Trí dựng hạ tầng; mỗi người tự thêm template và method của mình (Thùy: `order.html`) |
| Giỏ hàng | Chỉ Thuận viết; Thùy chỉ đọc snapshot trong Session |
| `ActivityTracker` | Chỉ Phương viết; Thuận và Thùy chỉ gọi |
| Dashboard | Thuận tạo `DashboardRepository` riêng, không sửa repository của người khác |
| Routes React | Mỗi người tự thêm route của mình; `ProtectedRoute` do Trí giữ |
| Hợp đồng API | Đổi hợp đồng phải báo nhóm trước khi merge (mục 8 và 13 của quy ước) |

## 7. Thứ tự merge và hạn phụ thuộc

| Hạn | Việc | Người |
| --- | --- | --- |
| 12/11 | Entity và repository `Product`, `Category` | Thuận |
| 13/11 | Đăng nhập thủ công | Trí |
| 13/11 | Chữ ký `ActivityTracker.record(...)` | Phương |
| 15/11 | Khung frontend | Trí |
| 19/11 | Giỏ API | Thuận |
| 20/11 | Hợp đồng API đơn hàng Staff | Thùy |
| 22/11 | API Custom phía Customer | Phương |
| 27/11 | Feature freeze | Cả nhóm |

## 8. Tài liệu cần sửa theo hạn mới

- Kế hoạch: mục 1 (giả định thời gian), mục 12 (bảng sprint), rủi ro R1 và R15 (tăng mức; cách dự phòng chính là bảng mức ưu tiên ở mục 3).
- `CODING_CONVENTIONS.md`: mục 11 và 14 nới yêu cầu test cho đến 30/11.
- SRS: chỉ sửa nếu cắt chức năng Could (ví dụ UC-23 Quên mật khẩu).

## 9. Checklist demo tối thiểu (27/11)

- [ ] Guest xem, lọc, xem chi tiết sản phẩm; thêm giỏ; đăng ký; đăng nhập
- [ ] Customer đặt hàng đến email xác nhận; xem đơn của mình
- [ ] Customer gửi yêu cầu Custom; Staff báo giá; Customer đồng ý; Staff chuyển trạng thái đến hoàn thành
- [ ] Staff thêm, sửa sản phẩm; xử lý đơn (đổi trạng thái, thanh toán, hủy hoàn tồn)
- [ ] Admin tạo Staff, ẩn/hiện sản phẩm, danh mục, người dùng; xem Dashboard
- [ ] Chatbot trả lời được các nhóm câu hỏi đã định (chế độ `rule` là đủ; `ai` nếu kịp)
- [ ] Staff và Admin bị chặn giỏ hàng, đặt hàng, Custom (403)
- [ ] Chạy được trên máy sạch theo README
