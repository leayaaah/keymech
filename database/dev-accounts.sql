-- CHỈ dùng cho dev/test; mật khẩu thô của cả 3 tài khoản: Dev@12345.
-- Không mount file này vào Docker.
-- Chạy sau database/01-schema.sql và database/02-data.sql.
SET NAMES utf8mb4;

INSERT INTO users (id, full_name, email, password, phone, address, role, enabled, created_at) VALUES
  (2, 'Nhân viên thử nghiệm', 'staff@keyboardshop.vn', '$2b$10$kUtJ4QRW3jACLin/D23uGu0I4JIVvDwR145xvs7FYrNMEM6IuRSlu', NULL, NULL, 'STAFF', 1, '2026-11-14 09:00:00'),
  (3, 'Khách hàng thử nghiệm', 'customer@keyboardshop.vn', '$2b$10$kUtJ4QRW3jACLin/D23uGu0I4JIVvDwR145xvs7FYrNMEM6IuRSlu', NULL, NULL, 'CUSTOMER', 1, '2026-11-14 09:00:00'),
  (4, 'Khách hàng bị ẩn', 'hidden.customer@keyboardshop.vn', '$2b$10$kUtJ4QRW3jACLin/D23uGu0I4JIVvDwR145xvs7FYrNMEM6IuRSlu', NULL, NULL, 'CUSTOMER', 0, '2026-11-14 09:00:00');
