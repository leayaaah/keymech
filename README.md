# KeyMech – Web TMĐT bàn phím cơ

## Yêu cầu

- JDK 21+
- Docker & Docker Compose
- Node.js 20+ (frontend, làm sau)

## Khởi chạy

```bash
# 1. Khởi động MariaDB + MailHog
docker compose up -d

# 2. Tạo file .env từ mẫu và điền giá trị thật
cp .env.example .env

# 3. Chạy backend
cd backend
export $(grep -v '^#' ../.env | xargs)    # Git Bash / WSL
mvn spring-boot:run
```

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
├── frontend/     React + Vite (làm sau)
├── database/     SQL schema & seed data
├── uploads/      Ảnh sản phẩm
├── docs/         Tài liệu, Postman
└── docker-compose.yml
```

## Quy định

Xem `CODING_CONVENTIONS.md` và `KHUNG_DU_AN_SPRING.md`.
