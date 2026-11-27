# KeyMech – Web TMĐT bàn phím cơ

## Yêu cầu

- JDK 21+
- Docker & Docker Compose
- Maven (trên PATH) — repo **không** có Maven wrapper (`./mvnw`)

## Khởi chạy (dev)

```bash
# 1. Khởi động MariaDB + MailHog
docker compose up -d

# 2. Tạo file .env từ mẫu và điền giá trị thật (DB_URL, ...), hoặc set env riêng
cp .env.example .env

# 3. Chạy backend
cd backend
mvn spring-boot:run
```

Frontend dev (React + Vite) ở port 5173, proxy `/api` và `/uploads` về backend:

```bash
cd frontend
npm ci
npm run dev
```

## Đóng gói JAR (có sẵn frontend bên trong)

```bash
cd backend
mvn clean package
# JAR: target/keymech-0.0.1-SNAPSHOT.jar — chứa cả index.html và assets frontend
java -jar target/keymech-0.0.1-SNAPSHOT.jar
```

> Bước `package` tự chạy `npm ci` + `npm run build` (qua `frontend-maven-plugin`, tự tải Node + npm)
> rồi copy `frontend/dist/` vào `static/` trong JAR. Máy sạch chỉ cần JDK + Maven + Internet
> để tải Node/npm lần đầu.

## Cấu trúc

```
keymech/
├── backend/      Spring Boot (Maven)
├── frontend/     React + Vite
├── database/     SQL schema & seed data
├── uploads/      Ảnh sản phẩm
├── docs/         Tài liệu, hợp đồng API
└── docker-compose.yml
```

## Quy định

Xem `CODING_CONVENTIONS.md` và `KHUNG_DU_AN_SPRING.md`.
