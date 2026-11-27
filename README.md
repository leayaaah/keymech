# KeyMech – Web TMĐT bàn phím cơ

## Yêu cầu

- JDK 21+
- Docker & Docker Compose
- Node.js 20+ (frontend, làm sau)

## Khởi chạy

```bash
# 1. Khởi động MariaDB + MailHog
docker compose up -d

# 2. Tạo file .env từ mẫu (PowerShell)
Copy-Item .env.example .env

# 3. Chạy backend (PowerShell)
./backend/run-dev.ps1

# Chạy test (Docker DB cần đang chạy); có thể chọn một lớp test
./backend/test-dev.ps1 -TestName PostUiApiIntegrationTests

# 4. Chạy frontend trong một terminal khác
cd frontend
npm ci
npm run dev
```

Mặc định MariaDB dùng cổng host `3311`, tránh xung đột với MySQL đang dùng `3306`;
MailHog nhận thư tại `http://localhost:8025`. Nếu đổi cổng DB, cập nhật đồng thời
`DB_HOST_PORT` và cổng trong `DB_URL` ở `.env`.

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
