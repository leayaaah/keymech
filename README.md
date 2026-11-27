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
- Gemini nhận từ vựng tư vấn đã lọc cùng dữ liệu danh mục công khai. Lịch sử trò chuyện vẫn lưu trong phiên và không được gửi tới Gemini. Cá nhân hóa chưa thuộc nhánh này.
- Để chạy `ChatbotRagIntegrationTests`, đặt `RAG_TEST_DB_URL`, `RAG_TEST_DB_USERNAME` và `RAG_TEST_DB_PASSWORD` trỏ tới MariaDB thử nghiệm riêng đã nạp `01-schema.sql`. Test ghi sản phẩm và dựng lại `vector_store` bằng Gemini giả lập trên máy.
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
