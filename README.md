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
./mvnw spring-boot:run
```

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
