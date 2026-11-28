# KeyMech – Web TMĐT bàn phím cơ

## Yêu cầu

- JDK 21+
- Docker & Docker Compose
- Maven trên PATH
- Node.js 22.12+ nếu chạy Vite riêng

## Cấu hình môi trường

Từ thư mục gốc, sao chép `.env.example` thành `.env` và điền thông tin kết nối. Compose và backend dùng cùng tài khoản và mật khẩu. Chạy lệnh sau để khởi động MariaDB và MailHog.

```shell
docker compose up -d
```

Nạp biến từ `.env` vào terminal sẽ chạy Maven và Java. Nếu chạy backend từ một thư mục khác, đặt `APP_UPLOAD_DIR` thành đường dẫn tuyệt đối đến thư mục lưu ảnh.

```bash
# Bash hoặc Git Bash, từ thư mục gốc
set -a
. ./.env
set +a
```

```powershell
# PowerShell, từ thư mục gốc
Get-Content -LiteralPath .env | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
```

## Khởi chạy khi phát triển

Backend dùng cổng 8080.

```shell
mvn -f backend/pom.xml spring-boot:run
```

Mở terminal khác, vào `frontend/` rồi chạy `npm ci` và `npm run dev`. Trên PowerShell có thể dùng `npm.cmd`. Vite ở cổng 5173 và chuyển tiếp `/api` cùng `/uploads` về backend.

## Đóng gói và chạy JAR

Chạy hai lệnh sau từ thư mục gốc trong terminal đã nạp biến môi trường. MariaDB phải sẵn sàng để Maven chạy các test tích hợp.

```shell
mvn -f backend/pom.xml clean package
java -jar backend/target/keymech-0.0.1-SNAPSHOT.jar
```

Maven tự tải Node và npm, chạy `npm ci`, `npm run build` và đóng gói frontend vào JAR. Lần build đầu cần Internet để tải phụ thuộc. Sau khi khởi động, mở `http://localhost:8080`.

## Điều chỉnh tồn kho

Staff và Admin đặt số lượng qua `PATCH /api/staff/products/{id}/stock` với JSON dạng `{"stockQuantity": 5}`. API `PUT /api/staff/products/{id}` dùng để lưu thông tin sản phẩm và giữ nguyên tồn kho hiện tại.

## Chatbot G-04

- Demo dùng `APP_CHATBOT_MODE=rule` mặc định. Để bật RAG, đặt `APP_CHATBOT_MODE=ai` và `GEMINI_API_KEY` trong môi trường của backend, rồi khởi động lại. Có thể đặt `GEMINI_CHAT_MODEL` theo quyền của khóa Gemini. RAG dùng MariaDB 11.8 và bảng `vector_store` trong `database/01-schema.sql`.
- Chỉ mục được khởi tạo bất đồng bộ khi backend sẵn sàng nếu chưa có tài liệu kiến thức. Các thao tác tạo, sửa, ẩn và hiện sản phẩm cập nhật chỉ mục sau khi giao dịch lưu sản phẩm thành công. Chatbot dùng câu trả lời `rule` khi RAG thiếu ngữ cảnh, gặp lỗi hoặc hết 8 giây.
- `POST /api/admin/chatbot/reindex` dựng lại chỉ mục sản phẩm công khai và tài liệu kiến thức sau khi nhập dữ liệu bằng SQL, đổi model embedding hoặc cập nhật danh mục trong chế độ `rule`. Gửi cookie phiên Admin, không cần body. Phản hồi `200` có dạng `{"indexedDocuments":35}`. Guest nhận `401`, Customer và Staff nhận `403`. Nếu có lượt cập nhật đang chạy, endpoint trả `409 CHATBOT_INDEX_BUSY`. Khi AI tắt, thiếu khóa hoặc không dựng được chỉ mục, endpoint trả `503` với `CHATBOT_AI_DISABLED` hoặc `CHATBOT_INDEX_FAILED`.
- Gemini tạo lời tư vấn chung từ từ vựng đã lọc và tài liệu kiến thức. Tìm kiếm vector sản phẩm dùng từ khóa công khai, lọc theo danh mục, layout và giá. Backend kiểm tra lại giá và tồn kho trong MariaDB trước khi trả thẻ. Nội dung gốc và lịch sử không được đưa vào yêu cầu Gemini.
- FAQ về mạch hàn, hot-swap, chân switch và keycap trả lời trực tiếp ở cả hai chế độ. Nếu chỉ mục AI đã tồn tại, Admin có thể gọi reindex để đưa bản cập nhật tài liệu linh kiện vào RAG.
- `POST /api/chat` nhận `{"message":"…","context":{…}}`. `context` là tùy chọn. Widget gửi lại `context` mà API trả ở lượt trước để cập nhật Custom. Context gồm `layout`, `budget` (VND), `quiet` và `switchName`. Phản hồi `context: null` kết thúc ngữ cảnh Custom.
- Custom giữ đúng tên switch, tính tổng theo số gói cần mua và nêu rõ dữ liệu tương thích còn thiếu. Khi tổng tạm tính vượt ngân sách, chatbot báo chênh lệch và gợi ý các bàn phím lắp sẵn cùng layout nếu có hàng phù hợp. `intent` có thể là `custom`, `customBuild`, `customAlternatives` hoặc `clarify`.
- Nút “Thêm tất cả vào giỏ hàng” thêm các món còn hàng theo `products[].quantity` trong kết quả chat. Nếu một món bị từ chối vì tồn kho thay đổi, có thể thử lại các món chưa thêm.
- Để chạy `ChatbotRagIntegrationTests`, đặt `RAG_TEST_DB_URL`, `RAG_TEST_DB_USERNAME` và `RAG_TEST_DB_PASSWORD` trỏ tới MariaDB thử nghiệm riêng đã nạp `01-schema.sql` và `02-data.sql`. Test chạy chuỗi tư vấn Custom, ghi sản phẩm và dựng lại `vector_store` bằng Gemini giả lập trên máy.
- Chạy `postman collection run docs/chatbot.postman_collection.json` từ thư mục gốc để kiểm tra lời chào và quyền Guest trên backend đang chạy ở cổng 8080.

## Cấu trúc

```
keymech/
├── backend/      Spring Boot (Maven)
├── frontend/     React + Vite
├── database/     SQL schema & seed data
├── uploads/      Ảnh sản phẩm
├── docs/         Tài liệu, hợp đồng API và Postman
└── docker-compose.yml
```

## Quy định

Xem `CODING_CONVENTIONS.md` và `KHUNG_DU_AN_SPRING.md`.
