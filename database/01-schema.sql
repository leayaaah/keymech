-- =============================================================================
-- 01-schema.sql  |  Web TMĐT bàn phím cơ (Nhóm 4)  |  MariaDB 11.7+ (dùng 11.8)
--
-- Quy ước (theo kế hoạch bản 3 và CODING_CONVENTIONS.md):
--   * Không Function, Check constraint, Stored Procedure, Trigger do nhóm viết.
--   * Không DEFAULT dựa trên hàm (CURRENT_TIMESTAMP, uuid()): các cột thời gian
--     và id UUID do ứng dụng đặt giá trị (@PrePersist / Spring AI).
--   * Không xóa vật lý: ẩn/hiện bằng cột status hoặc enabled; mọi FK là RESTRICT.
--   * Trạng thái là VARCHAR, ánh xạ enum Java bằng @Enumerated(EnumType.STRING).
--   * Tiền là DECIMAL(n,0) (VND, không có phần lẻ).
--   * Docker nạp file theo thứ tự chữ cái: 01-schema.sql trước 02-data.sql.
--
-- Cơ sở dữ liệu được tạo bởi biến môi trường MARIADB_DATABASE của Docker.
-- Chạy thủ công thì bỏ comment 3 dòng dưới:
--   CREATE DATABASE IF NOT EXISTS keyboard_shop
--     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   USE keyboard_shop;
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Dựng lại từ đầu khi dev (bỏ comment nếu cần). Thứ tự: bảng con trước, bảng cha sau.
-- DROP TABLE IF EXISTS vector_store, user_activity, custom_request_history,
--   custom_requests, order_items, orders, products, categories, users;

-- -----------------------------------------------------------------------------
-- 1. users
--    role: CUSTOMER | STAFF | ADMIN
--    enabled = 0 nghĩa là bị ẩn, không đăng nhập được.
--    email UNIQUE; collation utf8mb4_unicode_ci nên không phân biệt hoa thường.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  full_name   VARCHAR(100) NOT NULL,
  email       VARCHAR(150) NOT NULL,
  password    VARCHAR(100) NOT NULL COMMENT 'Hash BCrypt',
  phone       VARCHAR(15)  NULL,
  address     VARCHAR(255) NULL,
  role        VARCHAR(20)  NOT NULL,
  enabled     TINYINT(1)   NOT NULL DEFAULT 1,
  created_at  DATETIME     NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. categories
--    status: ACTIVE | HIDDEN
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  slug        VARCHAR(120) NOT NULL,
  description VARCHAR(500) NULL,
  status      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name),
  UNIQUE KEY uk_categories_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. products
--    status: ACTIVE | HIDDEN
--    layout: 60% | 65% | 75% | TKL | Fullsize (hoặc NULL với phụ kiện)
--    switch_type: Linear | Tactile | Clicky (hoặc NULL)
--    specs: thông tin phụ dạng văn bản hoặc JSON
--    image_url: đường dẫn tương đối, ví dụ /uploads/keychron-k2.jpg
--    Giá > 0 và tồn kho >= 0 do Bean Validation và câu UPDATE có điều kiện đảm bảo.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  category_id    BIGINT        NOT NULL,
  name           VARCHAR(200)  NOT NULL,
  brand          VARCHAR(100)  NULL,
  price          DECIMAL(12,0) NOT NULL,
  stock_quantity INT           NOT NULL DEFAULT 0,
  layout         VARCHAR(20)   NULL,
  switch_type    VARCHAR(30)   NULL,
  status         VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
  description    TEXT          NULL,
  specs          TEXT          NULL,
  image_url      VARCHAR(255)  NULL,
  created_at     DATETIME      NOT NULL,
  PRIMARY KEY (id),
  KEY idx_products_category (category_id),
  KEY idx_products_name (name),
  KEY idx_products_layout (layout),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id)
    REFERENCES categories (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. orders
--    status: NEW | CONFIRMED | SHIPPING | COMPLETED | CANCELLED
--    payment_method: COD | BANK_TRANSFER
--    payment_status: UNPAID | PAID
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
  id               BIGINT        NOT NULL AUTO_INCREMENT,
  order_code       VARCHAR(30)   NOT NULL,
  user_id          BIGINT        NOT NULL,
  receiver_name    VARCHAR(100)  NOT NULL,
  receiver_phone   VARCHAR(15)   NOT NULL,
  shipping_address VARCHAR(255)  NOT NULL,
  payment_method   VARCHAR(20)   NOT NULL,
  payment_status   VARCHAR(10)   NOT NULL DEFAULT 'UNPAID',
  status           VARCHAR(15)   NOT NULL DEFAULT 'NEW',
  total_amount     DECIMAL(14,0) NOT NULL,
  order_date       DATETIME      NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_orders_order_code (order_code),
  KEY idx_orders_user (user_id),
  KEY idx_orders_order_date (order_date),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. order_items
--    unit_price là giá chốt lúc mua.
--    UNIQUE (order_id, product_id): mỗi sản phẩm chỉ một dòng trong một đơn
--    (giỏ hàng là Map productId -> số lượng); khóa này cũng làm chỉ mục cho order_id.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  order_id    BIGINT        NOT NULL,
  product_id  BIGINT        NOT NULL,
  quantity    INT           NOT NULL,
  unit_price  DECIMAL(12,0) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_items_order_product (order_id, product_id),
  KEY idx_order_items_product (product_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
    REFERENCES orders (id) ON DELETE RESTRICT,
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
    REFERENCES products (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. custom_requests
--    status: PENDING | QUOTED | ACCEPTED | IN_PROGRESS | COMPLETED | REJECTED | CANCELLED
--    layout: 60% | 65% | 75% | TKL | Fullsize
--    switch_type: Linear/Tactile/Clicky kèm tên switch (tối đa 80 ký tự)
--    case_material: Nhựa | Nhôm | Gỗ | Acrylic
--    rgb_option: Không | RGB | Đơn sắc
--    quoted_price: NULL cho tới khi Staff báo giá
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS custom_requests (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  request_code  VARCHAR(30)   NOT NULL,
  user_id       BIGINT        NOT NULL,
  layout        VARCHAR(20)   NOT NULL,
  case_material VARCHAR(20)   NULL,
  switch_type   VARCHAR(80)   NOT NULL,
  keycap_set    VARCHAR(120)  NULL,
  lubing        TINYINT(1)    NOT NULL DEFAULT 0,
  hot_swap      TINYINT(1)    NOT NULL DEFAULT 1,
  rgb_option    VARCHAR(20)   NULL,
  budget        DECIMAL(12,0) NULL,
  note          VARCHAR(1000) NULL,
  quoted_price  DECIMAL(12,0) NULL,
  status        VARCHAR(15)   NOT NULL DEFAULT 'PENDING',
  created_at    DATETIME      NOT NULL,
  updated_at    DATETIME      NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_custom_requests_code (request_code),
  KEY idx_custom_requests_user (user_id),
  KEY idx_custom_requests_status (status),
  CONSTRAINT fk_custom_requests_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. custom_request_history
--    old_status NULL ở dòng khởi tạo (new_status = PENDING).
--    changed_by là Customer, Staff hoặc Admin.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS custom_request_history (
  id                 BIGINT       NOT NULL AUTO_INCREMENT,
  custom_request_id  BIGINT       NOT NULL,
  old_status         VARCHAR(15)  NULL,
  new_status         VARCHAR(15)  NOT NULL,
  note               VARCHAR(500) NULL,
  changed_by         BIGINT       NOT NULL,
  changed_at         DATETIME     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_crh_request (custom_request_id, changed_at),
  KEY idx_crh_changed_by (changed_by),
  CONSTRAINT fk_crh_request FOREIGN KEY (custom_request_id)
    REFERENCES custom_requests (id) ON DELETE RESTRICT,
  CONSTRAINT fk_crh_changed_by FOREIGN KEY (changed_by)
    REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. user_activity  (user tracking cho Chatbot)
--    event_type: VIEW_PRODUCT | SEARCH | ADD_TO_CART | PURCHASE
--    visitor_id: UUID lưu trong HttpSession; user_id NULL khi là Guest.
--    Chỉ ghi Guest và Customer. Không có cột IP hay User-Agent.
--    Chỉ lưu sự kiện, không lưu nội dung giỏ hàng.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_activity (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  visitor_id   VARCHAR(36)  NOT NULL,
  user_id      BIGINT       NULL,
  event_type   VARCHAR(20)  NOT NULL,
  product_id   BIGINT       NULL,
  category_id  BIGINT       NULL,
  keyword      VARCHAR(100) NULL,
  created_at   DATETIME     NOT NULL,
  PRIMARY KEY (id),
  KEY idx_activity_user (user_id, created_at),
  KEY idx_activity_visitor (visitor_id, created_at),
  KEY idx_activity_product (product_id),
  KEY idx_activity_category (category_id),
  CONSTRAINT fk_activity_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_activity_product FOREIGN KEY (product_id)
    REFERENCES products (id) ON DELETE RESTRICT,
  CONSTRAINT fk_activity_category FOREIGN KEY (category_id)
    REFERENCES categories (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. vector_store  (Spring AI MariaDBVectorStore)
--    Dữ liệu dẫn xuất: có thể xóa và dựng lại bằng POST /api/admin/chatbot/reindex.
--    Cấu hình đi kèm trong application.yml:
--      spring.ai.vectorstore.mariadb.initialize-schema: false
--      spring.ai.vectorstore.mariadb.dimensions: 768
--      spring.ai.vectorstore.mariadb.distance-type: COSINE
--    Số chiều VECTOR(768) phải khớp dimensions của embedding (gemini-embedding-001),
--    và DISTANCE=cosine phải khớp distance-type.
--    metadata (JSON): type (PRODUCT | KNOWLEDGE), productId, status, layout,
--                     switchType, brand, categoryId, price.
--
--    LƯU Ý: đây là DDL dựng theo tài liệu Spring AI, chưa đối chiếu với bản bạn dùng.
--    Ở bước spike (Sprint 1) hãy chạy một lần với initialize-schema=true trên CSDL
--    trống, xem lệnh CREATE TABLE do thư viện sinh ra (SHOW CREATE TABLE vector_store)
--    và sửa khối dưới cho khớp. Nếu thư viện không tự đặt id khi INSERT thì cần
--    thêm DEFAULT uuid() và báo giảng viên (câu hỏi 6).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vector_store (
  id         UUID         NOT NULL,
  content    TEXT         NULL,
  metadata   JSON         NULL,
  embedding  VECTOR(768)  NOT NULL,
  PRIMARY KEY (id),
  VECTOR INDEX idx_vector_store_embedding (embedding) DISTANCE=cosine
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
