-- Danh mục minh họa cho form và chatbot Custom. Chạy sau 01-schema.sql và 02-data.sql.
-- Chỉ dùng trên CSDL dev/test. Chạy lại không tạo bản trùng hay đổi giá và tồn kho đã lưu.
SET NAMES utf8mb4;

INSERT INTO products (category_id, name, brand, price, stock_quantity, layout, switch_type, status, description, specs, created_at)
SELECT c.id, seed.name, 'KeyMech Demo', seed.price, seed.stock_quantity, seed.layout, seed.switch_type,
       'ACTIVE', seed.description, seed.specs, '2026-11-28 00:00:00'
FROM (
  SELECT 'kit-ban-phim' AS category_slug, 'KeyMech Demo KM75 Barebone 75%' AS name,
         850000 AS price, 16 AS stock_quantity, '75%' AS layout, NULL AS switch_type,
         'Kit thử nghiệm gồm vỏ, plate và PCB hot-swap. Stabilizer, switch và keycap bán riêng.' AS description,
         '{"platform":"KM75","keys":84,"includes":"Case, Plate, PCB","hotSwap":true,"switchFamily":"MX","supportedPins":[3,5],"stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"1.75u","stabilizerMount":"plate-mount","stabilizerSizes":["6.25u","2u","2u","2u"]}' AS specs
  UNION ALL
  SELECT 'dung-cu-phu-kien', 'KeyMech Demo PCB KM75 Hot-swap 75%', 490000, 12, '75%', NULL,
         'PCB thay thế cho kit thử nghiệm KM75, hỗ trợ switch cơ MX 3 và 5 chân.',
         '{"componentType":"pcb","platform":"KM75","keys":84,"hotSwap":true,"switchFamily":"MX","supportedPins":[3,5],"stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"1.75u"}'
  UNION ALL
  SELECT 'switch', 'KeyMech Demo Silent Linear MX (gói 35)', 205000, 36, NULL, 'Linear',
         'Switch minh họa loại silent linear, chuẩn cơ MX 5 chân. Một gói gồm 35 switch.',
         '{"silent":true,"switchFamily":"MX","pinCount":5,"pack":35,"stem":"MX"}'
  UNION ALL
  SELECT 'switch', 'KeyMech Demo Silent Tactile MX (gói 35)', 215000, 24, NULL, 'Tactile',
         'Switch minh họa loại silent tactile, chuẩn cơ MX 5 chân. Một gói gồm 35 switch.',
         '{"silent":true,"switchFamily":"MX","pinCount":5,"pack":35,"stem":"MX"}'
  UNION ALL
  SELECT 'dung-cu-phu-kien', 'KeyMech Demo Stabilizer KM75', 140000, 18, '75%', NULL,
         'Bộ stabilizer minh họa cho KM75. Một thanh 6.25u và ba thanh 2u.',
         '{"componentType":"stabilizer","platform":"KM75","mount":"plate-mount","sizes":["6.25u","2u","2u","2u"]}'
  UNION ALL
  SELECT 'keycap', 'KeyMech Demo Bộ keycap PBT ANSI 75%', 265000, 19, '75%', NULL,
         'Bộ keycap minh họa cho stem MX, bố trí ANSI, Space 6.25u và Shift phải 1.75u.',
         '{"componentType":"keycap-set","material":"PBT","stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"1.75u"}'
  UNION ALL
  SELECT 'kit-ban-phim', 'KeyMech Demo KM60 Barebone 60%', 690000, 14, '60%', NULL,
         'Kit ANSI 61 phím gồm vỏ, plate, PCB hot-swap và stabilizer. Hỗ trợ switch cơ MX 3 và 5 chân.',
         '{"platform":"KM60","keys":61,"includes":"Case, Plate, PCB, Stabilizer","case":"Nhựa","hotSwap":true,"switchFamily":"MX","supportedPins":[3,5],"stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"2.75u"}'
  UNION ALL
  SELECT 'kit-ban-phim', 'KeyMech Demo KM65 Barebone 65%', 760000, 14, '65%', NULL,
         'Kit ANSI 68 phím gồm vỏ, plate, PCB hot-swap và stabilizer. Dùng switch cơ MX 3 hoặc 5 chân.',
         '{"platform":"KM65","keys":68,"includes":"Case, Plate, PCB, Stabilizer","case":"Nhựa","hotSwap":true,"switchFamily":"MX","supportedPins":[3,5],"stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"1.75u"}'
  UNION ALL
  SELECT 'kit-ban-phim', 'KeyMech Demo KMTKL Barebone ISO', 895000, 12, 'TKL', NULL,
         'Kit ISO 88 phím gồm vỏ, plate và PCB hàn cho switch cơ MX 3 chân. Cần thêm stabilizer gắn vít đúng bộ KMTKL.',
         '{"platform":"KMTKL","keys":88,"includes":"Case, Plate, PCB","case":"Nhựa","hotSwap":false,"switchFamily":"MX","supportedPins":[3],"stem":"MX","physicalLayout":"ISO","spacebarWidth":"6.25u","rightShiftWidth":"2.75u","stabilizerMount":"pcb-screw-in","stabilizerSizes":["6.25u","2u","2u","2u"]}'
  UNION ALL
  SELECT 'kit-ban-phim', 'KeyMech Demo KM104 Barebone Fullsize', 980000, 10, 'Fullsize', NULL,
         'Kit ANSI 104 phím có cụm số, gồm vỏ, plate, PCB hot-swap và stabilizer. Hỗ trợ switch cơ MX 3 và 5 chân.',
         '{"platform":"KM104","keys":104,"includes":"Case, Plate, PCB, Stabilizer","case":"Nhựa","hotSwap":true,"switchFamily":"MX","supportedPins":[3,5],"stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"2.75u"}'
  UNION ALL
  SELECT 'switch', 'KeyMech Demo Linear MX 3 chân (gói 35)', 185000, 30, NULL, 'Linear',
         'Switch cơ MX 3 chân, hành trình linear. Một gói gồm 35 switch.',
         '{"silent":false,"switchFamily":"MX","pinCount":3,"pack":35,"stem":"MX"}'
  UNION ALL
  SELECT 'switch', 'KeyMech Demo Clicky MX 3 chân (gói 35)', 195000, 24, NULL, 'Clicky',
         'Switch cơ MX 3 chân có tiếng click rõ. Một gói gồm 35 switch.',
         '{"silent":false,"switchFamily":"MX","pinCount":3,"pack":35,"stem":"MX"}'
  UNION ALL
  SELECT 'dung-cu-phu-kien', 'KeyMech Demo Stabilizer KMTKL gắn vít', 155000, 15, 'TKL', NULL,
         'Bộ stabilizer gắn vít vào PCB KMTKL, gồm một thanh 6.25u và ba thanh 2u.',
         '{"componentType":"stabilizer","platform":"KMTKL","mount":"pcb-screw-in","sizes":["6.25u","2u","2u","2u"]}'
  UNION ALL
  SELECT 'keycap', 'KeyMech Demo Bộ keycap PBT ANSI 60%', 230000, 16, '60%', NULL,
         'Bộ keycap stem MX cho bố trí ANSI 60%, Space 6.25u và Shift phải 2.75u.',
         '{"componentType":"keycap-set","material":"PBT","stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"2.75u"}'
  UNION ALL
  SELECT 'keycap', 'KeyMech Demo Bộ keycap PBT ANSI 65%', 245000, 16, '65%', NULL,
         'Bộ keycap stem MX cho bố trí ANSI 65%, gồm cụm mũi tên, Space 6.25u và Shift phải 1.75u.',
         '{"componentType":"keycap-set","material":"PBT","stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"1.75u"}'
  UNION ALL
  SELECT 'keycap', 'KeyMech Demo Bộ keycap PBT ISO TKL', 275000, 14, 'TKL', NULL,
         'Bộ keycap stem MX cho bố trí ISO TKL, có Enter cao, Shift trái ngắn, Space 6.25u và Shift phải 2.75u.',
         '{"componentType":"keycap-set","material":"PBT","stem":"MX","physicalLayout":"ISO","spacebarWidth":"6.25u","rightShiftWidth":"2.75u"}'
  UNION ALL
  SELECT 'keycap', 'KeyMech Demo Bộ keycap PBT ANSI Fullsize', 315000, 13, 'Fullsize', NULL,
         'Bộ keycap stem MX cho bố trí ANSI fullsize, gồm cụm số, Space 6.25u và Shift phải 2.75u.',
         '{"componentType":"keycap-set","material":"PBT","stem":"MX","physicalLayout":"ANSI","spacebarWidth":"6.25u","rightShiftWidth":"2.75u"}'
) seed
JOIN categories c ON c.slug = seed.category_slug
WHERE NOT EXISTS (SELECT 1 FROM products p WHERE p.name = seed.name);

-- Bổ sung các khóa còn thiếu cho phiên bản seed cũ, giữ các giá trị đã khai báo.
UPDATE products
SET specs = JSON_INSERT(specs, '$.stabilizerMount', 'plate-mount',
    '$.stabilizerSizes', JSON_ARRAY('6.25u', '2u', '2u', '2u'))
WHERE brand = 'KeyMech Demo' AND name = 'KeyMech Demo KM75 Barebone 75%' AND JSON_VALID(specs)
  AND JSON_CONTAINS_PATH(specs, 'all', '$.stabilizerMount', '$.stabilizerSizes') = 0;

UPDATE products
SET specs = JSON_INSERT(specs, '$.stem', 'MX')
WHERE brand = 'KeyMech Demo' AND name IN ('KeyMech Demo Silent Linear MX (gói 35)',
    'KeyMech Demo Silent Tactile MX (gói 35)') AND JSON_VALID(specs)
    AND JSON_CONTAINS_PATH(specs, 'one', '$.stem') = 0;
