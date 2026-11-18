# Bàn giao Sprint 2 của Thùy

Cập nhật ngày 18/11/2026. Phạm vi là C-02 checkout, email xác nhận đơn và G-04 chatbot rule theo [phân công hạn 30/11](Phan_cong_Nhom4_deadline_30-11.md#sprint-2-1611-đến-2211).

## Tích hợp với giỏ của Thuận

Nhánh checkout đã gộp `main` tại `9b2f233` và nhánh catalog/giỏ tại `e1f3702`. Khi xử lý xung đột `GlobalExceptionHandler`, giữ một handler JSON sai, handler tham số sai của Trí và mã HTTP do `BusinessException` cung cấp. Các API giỏ dùng cùng khóa Session với checkout và kiểm tra DTO trước khi cập nhật số lượng.

Checkout chỉ phụ thuộc giỏ Session. Catalog và giỏ vẫn gọi tracker bất đồng bộ của nhánh Thuận. Hợp đồng tracker chính thức, liên kết Guest với Customer và ghi `PURCHASE` sau commit tiếp tục ở phần tracking.

Kiểm thử lại trên bản tích hợp với MariaDB 11.8 và MailHog tách riêng đã đạt 76/76 test Maven. Frontend lint và build đạt. Chrome kiểm tra trang sản phẩm, thêm/sửa giỏ, giữ giỏ qua đăng nhập, cộng dồn cùng phiên và giỏ trống sau khi gọi API checkout. HTTP xác nhận TC-PAY-07/08, rollback giỏ nhiều món và email theo từng đơn đã commit. Postman xác nhận số lượng 0 trả `400 VALIDATION_ERROR`.

## Mốc kiểm thử ban đầu

| Ref | Mốc kiểm thử |
| --- | --- |
| `feature/C-02-checkout-G-04-rule` | `82baf32` cùng bản vá checkout, email và chatbot mô tả bên dưới |
| `origin/main` | `59414aa`, cung cấp auth và phân quyền |
| `origin/feature/G-01-products-list-g02-g03-cart` | `e1f3702`, cung cấp giỏ Session và catalog |

Worktree nằm tại `C:/Users/pc/.codex/worktrees/sprint2-thuy-checkout-rule/keymech` trên máy này. Chạy `git status --short --branch` và `git log --oneline -5` trong worktree trước khi làm tiếp. Kết quả kiểm thử dưới đây thuộc bản tích hợp tạm của ba ref trong bảng, có chép bản vá từ worktree vào thời điểm kiểm thử.

## Code và hành vi cần giữ

| Hạng mục | Điểm vào | Hành vi |
| --- | --- | --- |
| Checkout | [OrderController](../backend/src/main/java/vn/edu/iuh/keymech/controller/OrderController.java), [OrderService](../backend/src/main/java/vn/edu/iuh/keymech/service/OrderService.java), [OrderStockRepository](../backend/src/main/java/vn/edu/iuh/keymech/repository/OrderStockRepository.java) | `POST /api/orders/checkout` đọc snapshot `Map<Long, Integer>` từ `SessionKeys.CART`. Khóa theo phiên để tránh tạo hai đơn cho cùng giỏ. Khóa dòng sản phẩm theo thứ tự ID, đọc giá dưới khóa và trừ tồn có điều kiện. Dùng `READ_COMMITTED` cho checkout vì MariaDB 11.8 từng trả lỗi snapshot khi hai phiên tranh món cuối. Chỉ xóa giỏ sau commit. |
| Email | [OrderEmailService](../backend/src/main/java/vn/edu/iuh/keymech/service/OrderEmailService.java), [template xác nhận](../backend/src/main/resources/templates/mail/order-confirmation.html) | Gửi HTML bất đồng bộ bằng DTO sau commit. Lỗi lên lịch hoặc gửi email không hủy đơn. Log lỗi chỉ ghi mã đơn và loại ngoại lệ. |
| Chatbot rule | [ChatController](../backend/src/main/java/vn/edu/iuh/keymech/controller/ChatController.java), [RuleBasedChatbotService](../backend/src/main/java/vn/edu/iuh/keymech/ai/RuleBasedChatbotService.java), [ChatProductRepository](../backend/src/main/java/vn/edu/iuh/keymech/repository/ChatProductRepository.java) | Chuẩn hóa Fullsize và các biến thể, nhận ngân sách có hoặc không có đơn vị, đối chiếu loại switch và chỉ gợi ý sản phẩm/danh mục ACTIVE. Giới hạn tần suất và lưu tối đa 20 tin nhắn trong Session. |

Chi tiết request, response và lỗi nằm trong [hợp đồng đơn hàng](order-api-contract.md) và [hợp đồng chatbot](chat-api-contract.md).

## Kết quả đã chạy

- `mvn clean package` với JDK 21, Maven 3.9.6 và MariaDB 11.8 tách riêng đạt **75 test, 0 lỗi**. MariaDB mới nạp `database/01-schema.sql` và `database/02-data.sql`.
- HTTP xác nhận Guest đặt hàng nhận 401, Staff và Admin gọi giỏ hoặc checkout nhận 403. Giỏ Guest được giữ khi đăng nhập Customer.
- **TC-PAY-07**. Hai phiên tranh món cuối nhận 201 và 409. Tồn cuối bằng 0, phiên thua giữ giỏ và đơn thành công dùng giá mới trong CSDL.
- **TC-PAY-08**. Hai yêu cầu đồng thời cùng phiên nhận 201 và 400 `EMPTY_CART`; chỉ tạo một đơn. Checkout giỏ nhiều món có món thứ hai bị ẩn trả 409, rollback phần tồn đã trừ của món đầu và giữ giỏ.
- MailHog nhận đúng một email HTML cho mỗi đơn đã commit khi cấu hình host, port và địa chỉ gửi cục bộ tường minh. Lượt đầu có một `MailSendException`. Chưa kiểm tra SMTP triển khai.
- Postman xác nhận câu hỏi về Full-size dưới 2.000.000đ trả sản phẩm đúng ngân sách. HTTP xác nhận lịch sử phiên, 400 với câu dài hơn 500 ký tự và 429 với câu thứ 11 trong một phút.

Khi chạy lại, dùng MariaDB và MailHog tách riêng với dữ liệu mẫu. Cấp `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` và cấu hình MailHog qua biến môi trường. Repo chưa có Maven wrapper. Trên máy thiếu Maven trong PATH, dùng Maven 3.9.6 đã cài trong cache wrapper. Kiểm tra bằng HTTP trên bản tích hợp có cả auth và giỏ để bao phủ việc truyền Session.

## Việc tiếp theo và điểm phối hợp

1. **Thuận và Thùy** cùng review thay đổi khóa Session và validation của API giỏ. Dùng cùng cookie phiên khi nghiệm thu từ thêm giỏ đến checkout. UI thanh toán thuộc Sprint 3.
2. **Phương và Thuận** thống nhất hợp đồng `ActivityTracker` và nguồn `visitor_id`. Trên bản thử, một lần `ADD_TO_CART` của Customer lỗi vì `visitor_id` null, dù giỏ và checkout vẫn hoạt động. Phần Custom/tracking ở `b142f92` đã bị revert trên `main`; khi đối chiếu với nhánh catalog, hai bản khai báo `ActivityTracker` và `PageResponse` xung đột.
3. **Thùy và Phương** nối `PURCHASE` cho từng mặt hàng sau commit khi chữ ký tracker ổn định. Xác nhận checkout rollback không ghi sự kiện và lỗi tracking không đổi kết quả đặt hàng.
4. **Sprint 3** tiếp tục UI checkout, trang đặt hàng thành công và đơn của tôi, API Staff A-04 đến A-06, cùng `ChatWidget` do Phương phụ trách. Chatbot RAG còn phụ thuộc mốc hoàn tất checkout theo bản phân công.
