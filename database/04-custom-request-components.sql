-- Áp dụng cho CSDL đã có 01-schema.sql. Yêu cầu Custom cũ giữ nguyên dữ liệu.
CREATE TABLE IF NOT EXISTS custom_request_components (
  custom_request_id BIGINT        NOT NULL,
  component_slot    VARCHAR(16)   NOT NULL,
  product_id        BIGINT        NOT NULL,
  product_name      VARCHAR(200)  NOT NULL,
  quantity          INT           NOT NULL,
  unit_price        DECIMAL(12,0) NOT NULL,
  PRIMARY KEY (custom_request_id, component_slot),
  KEY idx_crc_product (product_id),
  CONSTRAINT fk_crc_request FOREIGN KEY (custom_request_id) REFERENCES custom_requests (id) ON DELETE RESTRICT,
  CONSTRAINT fk_crc_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
