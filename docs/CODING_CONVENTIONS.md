# Quy định chung khi code – Web TMĐT bàn phím cơ (Nhóm 4)

Áp dụng cho mọi thành viên từ Sprint 1. Tài liệu này bám theo Kế hoạch dự án bản 3 và SRS; khi mâu thuẫn thì **SRS là chuẩn**, sau đó đến kế hoạch. Mục nào ghi *(đề xuất)* là lựa chọn mặc định, cả nhóm có thể đổi ở buổi họp M1 rồi sửa file này, không tự ý đổi riêng lẻ.

## 1. Nguyên tắc chung

1. Code chạy được trên máy sạch theo `README.md`. Không merge thứ làm hỏng build hoặc test.
2. Mỗi thay đổi nhỏ, một mục đích, có thể review trong 15 phút.
3. Không người nào là người kiểm thử duy nhất phần của mình; chạy thử chéo phần của nhau.
4. Đổi phạm vi hoặc quy tắc nghiệp vụ thì cập nhật **cả SRS và kế hoạch** trong cùng tuần (rủi ro R16).
5. Khi không chắc, hỏi nhóm trước khi code thay vì đoán.

## 2. Ngôn ngữ và định dạng

| Nội dung | Ngôn ngữ |
| --- | --- |
| Tên class, hàm, biến, bảng, cột, endpoint, mã lỗi | Tiếng Anh |
| Thông báo hiển thị cho người dùng (UI, `message` trong lỗi API) | Tiếng Việt có dấu |
| Comment, tài liệu, mô tả PR | Tiếng Việt (thuật ngữ kỹ thuật giữ nguyên tiếng Anh) |
| Commit message | Tiền tố tiếng Anh, mô tả tiếng Việt hoặc tiếng Anh đều được, nhưng thống nhất trong một commit |

- Mã hóa UTF-8, xuống dòng LF, thêm `.editorconfig` ở gốc kho: Java 4 dấu cách, JS/JSX/JSON/YAML/SQL 2 dấu cách, cuối file có một dòng trống.
- Java: Google Java Style nới độ dài dòng tới 120; React: ESLint và Prettier *(đề xuất)*. Chạy formatter trước khi commit; không commit file chỉ đổi định dạng lẫn với thay đổi logic.
- Xóa `import` thừa, code bị comment, `System.out.println`, `console.log` trước khi mở PR.
- Không để `TODO` mà không có tên người và mã chức năng, ví dụ `// TODO(Lan, A-05): kiểm tra đơn SHIPPING`.

## 3. Git

### 3.1 Nhánh

- `main` luôn build và chạy được; **không push trực tiếp**, chỉ merge qua Pull Request.
- Nhánh làm việc tách từ `main`, đặt tên: `feature/<mã chức năng>-<mô tả ngắn>`, ví dụ `feature/G-03-cart`, `feature/A-05-order-edit`. Sửa lỗi dùng `fix/`, tài liệu dùng `docs/`, việc kỹ thuật dùng `chore/`.
- Sống ngắn: một nhánh không quá vài ngày; rebase hoặc merge `main` vào nhánh ít nhất mỗi ngày làm việc.
- Gắn tag `m1`, `m2`, … `m6` trên `main` ở mỗi mốc.

### 3.2 Commit

Theo Conventional Commits: `<type>(<scope>): <mô tả>`.

- `type`: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`.
- `scope`: tên module như `cart`, `order`, `custom`, `auth`, `chatbot`, `tracking`, `db`, `ui`.
- Ví dụ: `feat(cart): cộng dồn số lượng khi thêm sản phẩm đã có`, `fix(order): hoàn tồn kho khi hủy đơn SHIPPING`.
- Một commit một ý; không dùng message kiểu “update”, “fix bug”, “abc”.

### 3.3 Pull Request

- Kích thước nên dưới khoảng 400 dòng thay đổi (không tính file sinh tự động và dữ liệu seed). Lớn hơn thì tách.
- Mô tả PR gồm: chức năng hoặc ID liên quan (ví dụ `C-02`, `TC-PAY-07`), làm gì, cách tự kiểm tra, ảnh chụp nếu có giao diện.
- Cần **ít nhất 1 người khác** duyệt và đã chạy thử trước khi merge; người mở PR không tự duyệt.
- Merge bằng squash để lịch sử `main` gọn. Xóa nhánh sau khi merge.
- PR đụng đến `01-schema.sql`, cấu hình bảo mật (`SecurityConfig`) hoặc hợp đồng API phải báo cả nhóm trước khi merge (mục 5, 6, 7).

### 3.4 Không đưa vào Git

`.env`, `application-local.yml`, khóa Gemini, App Password Gmail, `node_modules/`, `target/`, `dist/`, file log, ảnh upload mới phát sinh khi chạy thử (chỉ ảnh mẫu của seed nằm trong `uploads/`). Lỡ commit bí mật thì **đổi khóa ngay**, rồi mới xóa khỏi lịch sử (R10).

## 4. Cấu trúc kho mã

```
backend/    Spring Boot, Maven
frontend/   React, Vite
database/   01-schema.sql, 02-data.sql   (dev-accounts.sql nằm ngoài thư mục Docker nạp)
uploads/    ảnh mẫu của dữ liệu seed
docs/       báo cáo, sơ đồ, Postman, bảng ánh xạ use case
```

Không đổi tên hoặc di chuyển thư mục cấp một nếu chưa thống nhất với nhóm.

## 5. Quy ước CSDL (MariaDB)

- **Cấm** Function, Check constraint, Stored Procedure, Trigger do nhóm tự viết. Ràng buộc nằm ở Bean Validation, Service và `UNIQUE`/FK.
- Tên bảng và cột dùng `snake_case`, bảng ở dạng số nhiều (`orders`, `order_items`); khóa chính `id`; khóa ngoại `<bảng_số_ít>_id`; thời gian `created_at`, `updated_at`.
- Trạng thái là `VARCHAR`, ánh xạ bằng `enum` Java với `@Enumerated(EnumType.STRING)`; không dùng số ma thuật hay `ORDINAL`.
- Tiền: `DECIMAL(12,0)` trong CSDL, `BigDecimal` trong Java; **không dùng `double` hoặc `float`**.
- **Không xóa vật lý** dữ liệu nghiệp vụ. Ẩn/hiện bằng `status` (`ACTIVE`, `HIDDEN`) hoặc `enabled`. Không có `DELETE` repository dành cho `users`, `products`, `categories`, `orders`, `custom_requests`. Ngoại lệ duy nhất: dữ liệu dẫn xuất trong `vector_store`.
- Chỉ sửa schema ở `database/01-schema.sql` (không để Hibernate tự sinh bảng): `spring.jpa.hibernate.ddl-auto=validate` *(đề xuất)*. Sửa schema phải báo nhóm, mỗi người nạp lại CSDL local.
- Dữ liệu mẫu ở `02-data.sql` (chỉ có Admin, danh mục, sản phẩm); tài khoản Staff và Customer mẫu chỉ ở `dev-accounts.sql`.
- Truy vấn dùng Spring Data JPA hoặc `@Query` có tham số; **không nối chuỗi SQL** từ dữ liệu người dùng.
- Mọi truy vấn phía công khai phải lọc `status = 'ACTIVE'`; chỉ endpoint `/api/staff/**` và `/api/admin/**` được thấy mục đã ẩn.

## 6. Quy ước backend (Spring Boot)

### 6.1 Phân lớp

```
Controller  →  Service  →  Repository  →  CSDL
```

- **Controller:** nhận request, `@Valid`, gọi Service, trả DTO. Không chứa nghiệp vụ, không gọi Repository, không dùng `@Transactional`.
- **Service:** nơi duy nhất chứa nghiệp vụ và kiểm tra quyền sở hữu; đặt `@Transactional` ở đây.
- **Repository:** chỉ truy cập dữ liệu; chỉ Service được gọi.
- **Entity không ra khỏi Service.** Controller chỉ nhận và trả **DTO** (`record` của Java *(đề xuất)*), có hậu tố `Request` hoặc `Response`. DTO phản hồi **không bao giờ có `password`**.
- Package: `config`, `controller`, `service`, `repository`, `model`, `dto`, `security`, `exception`, `util`, cộng `ai` và `tracking`. Không tạo package mới ngoài danh sách này khi chưa bàn với nhóm.

### 6.2 Đặt tên

- Class `PascalCase`: `OrderService`, `ProductController`, `CreateStaffRequest`.
- Hàm và biến `camelCase`; hằng `UPPER_SNAKE_CASE`; package toàn chữ thường.
- Endpoint danh từ số nhiều, `kebab-case`: `/api/custom-requests`. Mã lỗi `UPPER_SNAKE_CASE`: `OUT_OF_STOCK`.
- Test: `tenHam_dieuKien_ketQua`, ví dụ `placeOrder_outOfStock_throwsBusinessException`.
- Lombok: chỉ dùng `@Getter`, `@Setter`, `@NoArgsConstructor` cho entity; **không dùng `@Data` trên entity** *(đề xuất)*.
- Dùng `jakarta.*`, không dùng `javax.*`.

### 6.3 Validate

- Chặn dữ liệu sai ngay ở Model/DTO bằng Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@DecimalMin("1")`, `@Min(0)`…) và **cũng** validate ở Client React; không chỉ làm một phía.
- Quy tắc nghiệp vụ (tồn kho, trạng thái, quyền) kiểm ở Service, không dựa vào frontend.
- Không tin dữ liệu từ client: giá luôn lấy lại từ CSDL; `role` không bao giờ nhận từ body (`RegisterRequest` và `CreateStaffRequest` không có trường `role`).

### 6.4 Ngoại lệ và mã lỗi

- Ném `BusinessException(code, message)` cho vi phạm nghiệp vụ, `ResourceNotFoundException` cho không tìm thấy, `AccessDeniedException` cho sai quyền.
- Mọi lỗi đi qua `GlobalExceptionHandler` và trả đúng dạng `{status, error, message, timestamp}`; không trả stack trace ra ngoài.
- `message` hiển thị cho người dùng nên bằng tiếng Việt; thông báo đăng nhập sai và quên mật khẩu **chung chung** để không lộ email nào tồn tại.
- Không bắt `Exception` rồi nuốt lỗi; bắt thì phải ghi log hoặc ném lại.

### 6.5 Giao dịch và việc bất đồng bộ

- Việc cần nhất quán (đặt hàng, hủy đơn, đổi trạng thái Custom + ghi lịch sử) nằm **trong một `@Transactional`**; lỗi thì rollback toàn bộ.
- **Trừ tồn kho chỉ bằng một câu `UPDATE ... WHERE stock_quantity >= :q AND status = 'ACTIVE'`** và kiểm tra số dòng bị ảnh hưởng. Cấm kiểu “đọc tồn rồi mới trừ”.
- Việc phụ phải chạy **sau khi commit**: xóa giỏ khỏi Session, gửi email, ghi `PURCHASE`, nhúng lại sản phẩm. Dùng `@TransactionalEventListener(phase = AFTER_COMMIT)` hoặc gọi sau khi `placeOrder` trả về.
- `@Async` cho email, `ActivityTracker`, `ProductIndexer`. Truyền **DTO**, không truyền entity sang luồng khác (tránh lỗi lazy loading). Lỗi ở các việc này chỉ ghi log, **không làm hỏng** luồng chính.

### 6.6 Session và giỏ hàng

- Giỏ chỉ lưu trong `HttpSession` dạng `Map<Long, Integer>` (productId → số lượng), **không lưu giá** và **không ghi CSDL**. Sửa xong phải `setAttribute` lại.
- Đăng nhập thủ công phải đổi session id và lưu `SecurityContext` bằng `SecurityContextRepository` (đúng luồng ở mục 7 của kế hoạch). Staff và Admin đăng nhập thì xóa `cart`.

### 6.7 Log

- Dùng SLF4J (`log.info`, `log.warn`, `log.error`); không `System.out`.
- **Cấm ghi log**: mật khẩu, mã quên mật khẩu, khóa API, toàn bộ nội dung email, số điện thoại và địa chỉ giao hàng. Prompt gửi tới Gemini chỉ ghi ở mức `DEBUG` và không chứa dữ liệu định danh.

## 7. Bảo mật và phân quyền

- Ba role độc lập `CUSTOMER`, `STAFF`, `ADMIN`; **không dùng `RoleHierarchy`**. Bảng quy tắc ở mục 7 của kế hoạch là chuẩn; đổi quy tắc truy cập phải sửa bảng đó và test.
- Matcher cụ thể đặt trước matcher chung. Staff và Admin **bị chặn** giỏ hàng, đăng ký, đặt hàng, Custom.
- Mật khẩu băm BCrypt; mật khẩu mới ≥ 8 ký tự. Không có chức năng Admin đặt lại mật khẩu người khác.
- Kiểm quyền sở hữu trong Service (ví dụ `getMyOrder(id, currentUserId)`), không chỉ dựa vào URL.
- Không ai sửa hoặc ẩn tài khoản `ADMIN`; Staff chỉ do Admin tạo và role gán cứng trong Service.
- **Không có endpoint `DELETE`** cho dữ liệu nghiệp vụ (trừ `DELETE` giỏ hàng, vì giỏ chỉ ở Session).
- Cookie `HttpOnly`, `SameSite=Lax`, hết hạn 30 phút không hoạt động. Không lưu token xác thực ở `localStorage`.
- Thêm endpoint mới thì **thêm test phân quyền** cho từng vai trò (Guest, Customer, Staff, Admin).

## 8. Quy ước API

- Tiền tố `/api`, JSON UTF-8, thuộc tính `camelCase`. Đổi hợp đồng API (đường dẫn, JSON, mã lỗi) phải sửa file hợp đồng API trong `docs/` và báo nhóm frontend **trước khi merge**.
- Phân trang: tham số `page` (từ 0) và `size`; phản hồi `content`, `page`, `size`, `totalElements`, `totalPages`.
- Mã trạng thái: `200` thành công, `201` tạo mới, `400` dữ liệu sai, `401` chưa đăng nhập, `403` không đủ quyền, `404` không tìm thấy hoặc đã ẩn (phía công khai), `409` vi phạm nghiệp vụ, `429` gọi quá nhanh.
- Danh sách có thể lớn thì **bắt buộc phân trang** phía server.
- Ảnh: CSDL chỉ lưu đường dẫn; file nằm trong thư mục `APP_UPLOAD_DIR`, phục vụ ở `/uploads/**`. Kiểm tra loại và dung lượng file khi upload; đổi tên file ngẫu nhiên, không dùng tên người dùng gửi.

## 9. Chatbot AI và tracking

- Gọi mô hình chỉ qua interface `ChatbotService`; không gọi `ChatClient` hay `EmbeddingModel` trực tiếp từ Controller hoặc nơi khác.
- **Giá, tồn kho, liên kết luôn lấy từ CSDL**, không lấy từ câu trả lời của mô hình hoặc từ `metadata` của vector store. Id sản phẩm mô hình trả về phải nằm trong tập đã truy xuất, nếu không thì bỏ.
- System prompt nằm trong file `chatbot/system-prompt.txt`, **không ghép câu hỏi người dùng vào system prompt**; câu hỏi chỉ vào vai trò `user`.
- **Không đưa dữ liệu định danh** (tên, email, địa chỉ, số điện thoại, mã đơn) vào prompt hay vào `vector_store`.
- Mọi lời gọi Gemini có timeout (8 giây) và phải có đường lui sang `RuleBasedChatbotService`.
- Khóa `GEMINI_API_KEY` chỉ ở biến môi trường; test **không gọi Gemini thật** (mock `ChatModel` và `EmbeddingModel`).
- Sửa, ẩn/hiện sản phẩm phải phát `ProductChangedEvent` để chỉ mục vector đồng bộ.
- Tracking chỉ ghi qua `ActivityTracker.record(...)`: bất đồng bộ, không ném lỗi ra ngoài, **bỏ qua Staff và Admin**, không có endpoint nhận sự kiện từ client, không thêm cột IP hay User-Agent.

## 10. Quy ước frontend (React, Vite)

- Cấu trúc thư mục: `api`, `components`, `pages` (guest, customer, staff, admin), `routes`, `context`, `hooks`, `utils`.
- Component hàm và hooks; tên file component `PascalCase.jsx`, hooks `useXxx.js`, tiện ích `camelCase.js`.
- **Mọi lời gọi API nằm trong `src/api/`** qua một instance Axios dùng chung; không `fetch` rải rác trong component. URL lấy từ `VITE_API_URL` (`/api`), không viết cứng `localhost`.
- Interceptor Axios xử lý chung: `401` về trang đăng nhập, `403` báo không đủ quyền, `409` và `429` hiển thị `message` từ server.
- Quyền xem trang đi qua `ProtectedRoute` theo vai trò; **ẩn nút trên giao diện không thay cho kiểm quyền ở backend**.
- Biểu tượng giỏ hàng và Chatbot chỉ hiện với Guest và Customer.
- Mỗi trang danh sách có đủ ba trạng thái: đang tải, rỗng (“Không tìm thấy sản phẩm”), lỗi.
- Tiền hiển thị bằng một hàm dùng chung `formatVND`; ngày giờ dùng một hàm định dạng chung.
- Validate form ở Client giống quy tắc ở server; thông báo lỗi tiếng Việt.
- Giao diện chạy được từ 360px; ảnh có `alt`; nút và ô nhập có nhãn.
- Không lưu mật khẩu, mã xác thực hay dữ liệu cá nhân ở `localStorage`/`sessionStorage`.

## 11. Kiểm thử

- Viết test **cùng PR** với code nghiệp vụ mới: JUnit 5 và Mockito cho Service, `MockMvc` cho API và phân quyền, Testcontainers (MariaDB 11.8) cho tích hợp.
- Đặt ID test case trong `@DisplayName`, ví dụ `@DisplayName("TC-PAY-07: hai request cùng mua món cuối")`, để khớp bảng test case trong báo cáo.
- Test độc lập và lặp lại được: không phụ thuộc thứ tự chạy, không phụ thuộc mạng, không gọi Gemini hay SMTP thật.
- Sửa lỗi thì thêm test tái hiện lỗi đó.
- Chạy `./mvnw test` và `npm run lint` trước khi mở PR.
- Postman: giữ collection trong `docs/`, cập nhật khi đổi API.

## 12. Cấu hình và biến môi trường

- Cấu hình theo profile `dev`, `test`, `prod`; giá trị nhạy cảm đọc từ biến môi trường: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_APP_PASSWORD`, `GEMINI_API_KEY`, `APP_CHATBOT_MODE`, `APP_UPLOAD_DIR`, `APP_CORS_ORIGIN`.
- Thêm biến mới thì cập nhật `.env.example` (giá trị giả) và bảng biến môi trường trong kế hoạch và README.
- Dev dùng MailHog hoặc Mailtrap, không dùng Gmail thật cho test hằng ngày.

## 13. Khi nào phải báo cả nhóm trước

Báo trên nhóm chat (hoặc họp ngắn) trước khi làm:

1. Sửa `01-schema.sql` (thêm, đổi bảng hoặc cột).
2. Đổi hợp đồng API đã chốt.
3. Đổi quy tắc truy cập ở `SecurityConfig`.
4. Thêm thư viện hoặc đổi phiên bản Spring Boot, Spring AI, JDK, React.
5. Thêm hoặc bỏ chức năng so với mục 3 của kế hoạch.

## 14. Definition of Done

Một chức năng chỉ được coi là xong khi đủ:

- [ ] Chạy đúng tiêu chí chấp nhận của chức năng (mục 3 kế hoạch) và đúng bảng phân quyền.
- [ ] Validate ở cả Client và Model/Service; thông báo lỗi tiếng Việt.
- [ ] Có test đơn vị hoặc API, đã đặt ID `TC-…`, test đều qua.
- [ ] Không có Function, Check, Stored Procedure, Trigger, không xóa vật lý; truy vấn công khai lọc `ACTIVE`.
- [ ] Không lộ `password`, khóa hoặc dữ liệu cá nhân trong response, log và Git.
- [ ] Đã được một người khác chạy thử và duyệt PR.
- [ ] Cập nhật tài liệu liên quan: hợp đồng API, Postman, README, bảng ánh xạ use case, chức năng, test case.

## 15. Mười điều không được làm

1. Push thẳng lên `main`.
2. Commit `.env`, khóa API hoặc mật khẩu.
3. Trả entity hoặc `password` ra API.
4. Viết `DELETE` xóa dữ liệu nghiệp vụ.
5. Tin giá, role hoặc id từ client mà không kiểm lại ở server.
6. Trừ tồn kho bằng cách đọc rồi ghi.
7. Xóa giỏ, gửi email hoặc ghi tracking **trước** khi transaction commit.
8. Lấy giá hoặc tồn kho từ câu trả lời của mô hình AI.
9. Đưa dữ liệu định danh vào prompt hoặc `vector_store`.
10. Đổi schema, API hoặc phân quyền mà không báo nhóm.
