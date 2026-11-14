# Bàn giao phần của Phương (Sprint 1 đến 3)

Theo `Phan_cong_Nhom4_deadline_30-11.md`. Phần của Phương: C-03, C-04 (Custom), C-06, A-07, tracking, `ChatWidget`, bảng ánh xạ, hợp đồng API Custom, câu hỏi giảng viên.

## 1. Đã làm

| Hạng mục | File chính |
| --- | --- |
| `ActivityTracker.record...` (chốt chữ ký) | `tracking/ActivityTracker.java`, `ActivityWriter.java`, `ActivityEvent.java`, `KeywordNormalizer.java` |
| Hồ sơ quan tâm (Could) | `tracking/ProfileService.java`, `InterestProfileBuilder.java`, `dto/InterestProfile.java`, `dto/ActivitySignal.java` |
| Custom phía Customer: C-03, C-04, C-06 | `controller/CustomRequestController.java`, `service/CustomRequestService.java` |
| Custom phía Staff: A-07 | `controller/StaffCustomRequestController.java` |
| Bảng chuyển trạng thái | `service/CustomStatusPolicy.java` |
| Repository | `CustomRequestRepository` (khóa dòng, EntityGraph), `CustomRequestHistoryRepository` (timeline), `UserActivityRepository` |
| DTO | `dto/*Custom*`, `QuoteRequest`, `AcceptQuoteRequest`, `DeclineRequest`, `ChangeCustomStatusRequest`, `CustomerSummary`, `PageResponse` |
| Frontend Custom | `frontend/src/pages/customer/*`, `pages/staff/*`, `components/{CustomStatusBadge,StatusTimeline,RequestSpecs,Pagination}.jsx`, `api/customRequestApi.js`, `utils/customStatus.js`, `routes/customRoutes.jsx` |
| `ChatWidget` | `frontend/src/components/ChatWidget.{jsx,css}`, `api/chatApi.js` |
| Test | `src/test/.../service/*`, `src/test/.../tracking/*` (JUnit 5, Mockito, không cần CSDL) |
| Tài liệu | `docs/api-contract-custom.md`, `docs/anh-xa-use-case-chuc-nang-test.md`, `docs/cau-hoi-giang-vien.md`, `docs/postman/custom-requests.postman_collection.json` |

## 2. Sửa vào file dùng chung (báo cả nhóm)

- `util/CodeGenerator`: thêm `customCode()` (mã `CUS-XXXXXXXXXX`).
- `exception/GlobalExceptionHandler`: thêm hai handler trả **400** cho body JSON sai/enum sai (`HttpMessageNotReadableException`) và tham số query sai kiểu (`MethodArgumentTypeMismatchException`). Trước đó hai trường hợp này rơi vào handler chung và trả 500.
- `dto/PageResponse`: mới. Thuận và Thùy cũng cần phân trang; nếu đã có lớp tương tự thì dùng chung một lớp, xóa bản trùng khi merge.
- API: thêm `GET /api/staff/custom-requests/{id}` so với bảng API ở kế hoạch (Staff cần xem lịch sử). Không đổi `SecurityConfig`, `01-schema.sql`.

## 3. Việc cần người khác làm để nối vào

| Ai | Việc |
| --- | --- |
| Trí | Gọi `tracker.linkVisitorToUser(session, userId)` khi **Customer** đăng nhập (sau `changeSessionId`, không gọi cho Staff và Admin). Đảm bảo principal trong `SecurityContext` là `AppUserDetails` (controller Custom dùng `@AuthenticationPrincipal AppUserDetails`). Gộp `customRoutes.jsx` vào cây route. Cung cấp `src/api/http.js`, `src/utils/format.js` (`formatVND`, `formatDateTime`) hoặc sửa import trong code của Phương |
| Thuận | Gọi `recordView`, `recordSearch`, `recordAddToCart` (bảng ở `docs/api-contract-custom.md` mục 5) |
| Thùy | Gọi `recordPurchase(session, productIds)` **sau khi** `placeOrder` commit; gọi `ProfileService.getProfile(session)` trong luồng Chatbot; xác nhận tên trường `/api/chat` (mục 6 của hợp đồng); cho `<ChatWidget user={user} />` vào layout cửa hàng |

## 4. Giả định frontend (khung của Trí chưa có trong kho)

- `src/api/http.js`: export default instance Axios, `baseURL=/api`, `withCredentials=true`.
- `src/utils/format.js`: export `formatVND`, `formatDateTime`.
- Dùng `react-router-dom` v6 trở lên. `ChatWidget` nhận prop `user` (có `role`, hoặc `null` nếu Guest) và tự ẩn với Staff, Admin.
- CSS dùng biến theme nếu có (`--color-primary`, `--color-text`...), không có thì dùng giá trị dự phòng.

## 5. Đã kiểm chứng và chưa kiểm chứng

**Đã chạy được:** logic thuần (bảng chuyển trạng thái đủ 49 cặp, hồ sơ quan tâm, chuẩn hóa từ khóa) bằng một harness Java riêng, 180 kiểm tra đều đạt. Giao diện (`ChatWidget`, 5 trang Custom) được bundle bằng esbuild và chạy trên Chromium headless với API giả: 50 kiểm tra đều đạt, gồm validate form, nội dung body gửi API, các trạng thái tải/rỗng/lỗi, ẩn widget với Staff và Admin, không tràn ngang ở 360px. Cú pháp mọi file Java đã được parse.

**Chưa chạy được** (môi trường soạn không có Maven, mạng và jar Spring): biên dịch backend đầy đủ và `./mvnw test`. Cần Phương hoặc nhóm chạy `./mvnw test` một lần và báo lỗi nếu có. Các điểm dễ lệch giữa phiên bản:

- Truy vấn JPQL `LEFT JOIN Product p ON p.id = a.productId` (entity join, Hibernate 6) trong `UserActivityRepository`.
- `@Lock(PESSIMISTIC_WRITE)` trong `findByIdForUpdate`, cần chạy trên MariaDB thật để thấy khóa hoạt động.
- Chưa có test ở tầng URL (TC-CUS-08, TC-TRK-06): Spring Boot 4 tách các lớp test web sang starter riêng (`spring-boot-starter-webmvc-test`), và thêm dependency cần cả nhóm đồng ý (quy ước mục 13). Tạm thời dùng collection Postman.

## 6. Lưu ý về code của người khác

`security/CartAccessManager.authorize(...)` đang `return null` và `check(...)` đã bị comment `@Override`, nên có khả năng `/api/cart/**` bị chặn với mọi người (kể cả Guest). Trí nên kiểm tra khi chạy thử TC-CART-07 và TC-SEC-15.

## 7. Còn lại của Phương

- Email đổi trạng thái Custom (Could, cần `EmailService` của Trí).
- Sprint 4: tổng hợp báo cáo, slide, sơ đồ, quay video demo; điền cột UC trong bảng ánh xạ từ SRS.
- Nên tách PR theo quy ước (dưới khoảng 400 dòng): (1) tracking + test, (2) backend Custom + test, (3) frontend Custom, (4) `ChatWidget`, (5) tài liệu.
