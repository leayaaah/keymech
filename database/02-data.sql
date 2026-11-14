-- =============================================================================
-- 02-data.sql  |  Dữ liệu mẫu  |  Chạy SAU 01-schema.sql
--
-- Nội dung: 1 tài khoản Admin, 5 danh mục, 47 sản phẩm (45 đang hiện, 2 đang ẩn).
-- Tên sản phẩm, giá, tồn kho và thông số là DỮ LIỆU MẪU tự đặt để học tập và demo;
-- không dùng làm thông tin thương mại.
--
-- Tài khoản Admin (ĐỔI mật khẩu trước khi nộp hoặc demo công khai):
--   email: admin@keyboardshop.vn   mật khẩu tạm: Admin@12345
--   Mật khẩu lưu dạng BCrypt (cost 10). Đăng nhập thử bằng mật khẩu tạm sau khi nạp.
--
-- Không có Staff và Customer ở đây: Staff do Admin tạo qua giao diện, Customer tự
-- đăng ký; tài khoản mẫu cho dev nằm ở database/dev-accounts.sql (ngoài thư mục Docker).
--
-- Thời gian dùng giá trị cố định vì schema không dùng DEFAULT dựa trên hàm.
-- Ảnh: image_url trỏ tới uploads/products/<slug>.jpg trong kho mã.
--
-- Nạp lại từ đầu: xóa dữ liệu rồi chạy lại file này, rồi gọi
-- POST /api/admin/chatbot/reindex để dựng lại chỉ mục vector (dữ liệu nạp bằng SQL
-- không đi qua Service nên không tự được nhúng; lần chạy đầu app sẽ tự nhúng nếu
-- vector_store rỗng).
-- =============================================================================

SET NAMES utf8mb4;

-- 1. Admin (id = 1)
INSERT INTO users (id, full_name, email, password, phone, address, role, enabled, created_at) VALUES
  (1, 'Quản trị viên', 'admin@keyboardshop.vn', '$2b$10$mVXcmNLm1tQZ5niAPDKASOgOpoQxbM/8S81brST2emM54c2L.mGjS', NULL, NULL, 'ADMIN', 1, '2026-11-09 09:00:00');

-- 2. Danh mục
INSERT INTO categories (id, name, slug, description, status) VALUES
  (1, 'Bàn phím cơ', 'ban-phim-co', 'Bàn phím cơ hoàn chỉnh, dùng được ngay', 'ACTIVE'),
  (2, 'Switch', 'switch', 'Switch rời cho bàn phím hot-swap hoặc tự hàn (gói 35 cái)', 'ACTIVE'),
  (3, 'Keycap', 'keycap', 'Bộ keycap và keycap đơn', 'ACTIVE'),
  (4, 'Dụng cụ và phụ kiện', 'dung-cu-phu-kien', 'Dụng cụ tháo lắp, lube, kê tay, cáp và phụ kiện', 'ACTIVE'),
  (5, 'Kit bàn phím', 'kit-ban-phim', 'Kit barebone để tự lắp switch và keycap', 'ACTIVE');

-- 3. Sản phẩm (status HIDDEN dùng để kiểm thử ẩn/hiện, TC-HID-02)
INSERT INTO products (id, category_id, name, brand, price, stock_quantity, layout, switch_type, status, description, specs, image_url, created_at) VALUES
  (1, 1, 'Keychron K2 75%', 'Keychron', 2200000, 25, '75%', 'Tactile', 'ACTIVE', 'Bàn phím 75% không dây, gọn nhưng vẫn giữ hàng phím mũi tên và phím chức năng. Dùng switch tactile có điểm nhấn rõ, gõ êm vừa phải, hợp làm việc văn phòng và mang theo.', '{"connectivity":"USB-C và Bluetooth","keys":84,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/keychron-k2.jpg', '2026-11-09 09:00:00'),
  (2, 1, 'Keychron K6 65%', 'Keychron', 1990000, 18, '65%', 'Linear', 'ACTIVE', 'Bàn phím 65% nhỏ gọn, có phím mũi tên. Switch linear mượt, ít tiếng lạch cạch, hợp người cần tiết kiệm diện tích bàn.', '{"connectivity":"USB-C và Bluetooth","keys":68,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/keychron-k6.jpg', '2026-11-09 09:00:00'),
  (3, 1, 'Keychron K8 TKL', 'Keychron', 2390000, 15, 'TKL', 'Tactile', 'ACTIVE', 'Bàn phím TKL bỏ cụm phím số, giữ đủ cụm điều hướng. Switch tactile cho cảm giác gõ rõ ràng, phù hợp lập trình và soạn văn bản.', '{"connectivity":"USB-C và Bluetooth","keys":87,"case":"Nhựa","hotSwap":true,"backlight":"Trắng"}', '/uploads/products/keychron-k8.jpg', '2026-11-09 09:00:00'),
  (4, 1, 'Keychron K10 Fullsize', 'Keychron', 2590000, 10, 'Fullsize', 'Linear', 'ACTIVE', 'Bàn phím fullsize đủ cụm phím số, phù hợp kế toán, nhập liệu nhiều. Switch linear gõ nhẹ và mượt.', '{"connectivity":"USB-C và Bluetooth","keys":104,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/keychron-k10.jpg', '2026-11-09 09:00:00'),
  (5, 1, 'Keychron Q1 75%', 'Keychron', 4990000, 8, '75%', 'Linear', 'ACTIVE', 'Bàn phím 75% vỏ nhôm nguyên khối, hỗ trợ tùy biến phím bằng phần mềm. Thân nặng, chắc, âm thanh dày; hợp người muốn nâng cấp lên tầm cao hơn.', '{"connectivity":"USB-C có dây","keys":84,"case":"Nhôm","hotSwap":true,"backlight":"RGB"}', '/uploads/products/keychron-q1.jpg', '2026-11-09 09:00:00'),
  (6, 1, 'Akko 3068B 65%', 'Akko', 1690000, 30, '65%', 'Tactile', 'ACTIVE', 'Bàn phím 65% đa kết nối, giá dễ tiếp cận. Switch tactile gõ êm, hợp học tập và làm việc.', '{"connectivity":"USB-C, Bluetooth và 2.4GHz","keys":68,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/akko-3068b.jpg', '2026-11-09 09:00:00'),
  (7, 1, 'Akko 3084 75%', 'Akko', 1590000, 28, '75%', 'Linear', 'ACTIVE', 'Bàn phím 75% giá tốt dưới 2 triệu, switch linear nhẹ tay, phù hợp chơi game và gõ phím hằng ngày.', '{"connectivity":"USB-C có dây","keys":84,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/akko-3084.jpg', '2026-11-09 09:00:00'),
  (8, 1, 'Akko 5075B Plus 75%', 'Akko', 1890000, 14, '75%', 'Linear', 'ACTIVE', 'Bàn phím 75% đa kết nối với núm xoay chỉnh âm lượng. Switch linear mượt, gõ êm, hợp cả công việc lẫn giải trí.', '{"connectivity":"USB-C, Bluetooth và 2.4GHz","keys":84,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/akko-5075b-plus.jpg', '2026-11-09 09:00:00'),
  (9, 1, 'Akko 3108 Fullsize', 'Akko', 1490000, 20, 'Fullsize', 'Clicky', 'ACTIVE', 'Bàn phím fullsize switch clicky, tiếng “click” rõ và phản hồi mạnh. Hợp người thích âm thanh gõ đặc trưng, không phù hợp nơi cần yên tĩnh.', '{"connectivity":"USB-C có dây","keys":104,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/akko-3108.jpg', '2026-11-09 09:00:00'),
  (10, 1, 'Leopold FC750R TKL', 'Leopold', 2790000, 9, 'TKL', 'Tactile', 'ACTIVE', 'Bàn phím TKL nổi tiếng về độ bền và chất lượng hoàn thiện. Switch tactile gõ chắc tay, không đèn nền nên gọn gàng cho môi trường văn phòng.', '{"connectivity":"USB-C có dây","keys":87,"case":"Nhựa","hotSwap":false,"backlight":"Không"}', '/uploads/products/leopold-fc750r.jpg', '2026-11-09 09:00:00'),
  (11, 1, 'Leopold FC660M 65%', 'Leopold', 2690000, 0, '65%', 'Tactile', 'ACTIVE', 'Bàn phím 65% bố cục gọn, switch tactile gõ êm và chắc. Hiện tạm hết hàng.', '{"connectivity":"USB-C có dây","keys":68,"case":"Nhựa","hotSwap":false,"backlight":"Không"}', '/uploads/products/leopold-fc660m.jpg', '2026-11-09 09:00:00'),
  (12, 1, 'Ducky One 3 Mini 60%', 'Ducky', 2490000, 12, '60%', 'Linear', 'ACTIVE', 'Bàn phím 60% siêu gọn, bỏ cụm mũi tên và hàng chức năng để tối ưu không gian. Switch linear nhẹ, hợp game thủ cần chuột có chỗ rộng.', '{"connectivity":"USB-C có dây","keys":61,"case":"Nhựa","hotSwap":false,"backlight":"RGB"}', '/uploads/products/ducky-one3-mini.jpg', '2026-11-09 09:00:00'),
  (13, 1, 'Royal Kludge RK61 60%', 'Royal Kludge', 990000, 35, '60%', 'Clicky', 'ACTIVE', 'Bàn phím 60% giá rẻ nhất cửa hàng, phù hợp người mới thử bàn phím cơ. Switch clicky tiếng lớn, phản hồi rõ.', '{"connectivity":"USB-C và Bluetooth","keys":61,"case":"Nhựa","hotSwap":true,"backlight":"RGB"}', '/uploads/products/royal-kludge-rk61.jpg', '2026-11-09 09:00:00'),
  (14, 1, 'Varmilo VA87M TKL', 'Varmilo', 3190000, 6, 'TKL', 'Tactile', 'ACTIVE', 'Bàn phím TKL keycap PBT dày, hoàn thiện đẹp. Switch tactile cho tiếng gõ êm và cảm giác chắc.', '{"connectivity":"USB-C có dây","keys":87,"case":"Nhựa","hotSwap":false,"backlight":"Trắng"}', '/uploads/products/varmilo-va87m.jpg', '2026-11-09 09:00:00'),
  (15, 1, 'Leopold FC900R Fullsize', 'Leopold', 3090000, 4, 'Fullsize', 'Tactile', 'HIDDEN', 'Bàn phím fullsize đã ngừng kinh doanh, được ẩn khỏi cửa hàng. Giữ lại trong dữ liệu để kiểm thử ẩn/hiện.', '{"connectivity":"USB-C có dây","keys":104,"case":"Nhựa","hotSwap":false,"backlight":"Không"}', '/uploads/products/leopold-fc900r.jpg', '2026-11-09 09:00:00'),
  (16, 5, 'KBDfans Tofu65 Barebone 65%', 'KBDfans', 3400000, 5, '65%', NULL, 'ACTIVE', 'Kit barebone 65% vỏ nhôm, tự lắp switch và keycap theo ý. Phù hợp người chơi custom muốn tự chọn từng thành phần.', '{"keys":68,"case":"Nhôm","hotSwap":true,"includes":"Case, PCB, plate, foam"}', '/uploads/products/kbdfans-tofu65.jpg', '2026-11-09 09:00:00'),
  (17, 5, 'Akko ACR68 Barebone 65%', 'Akko', 2600000, 7, '65%', NULL, 'ACTIVE', 'Kit 65% vỏ nhôm giá mềm, lắp được switch 3 chân và 5 chân, dễ làm quen với custom.', '{"keys":68,"case":"Nhôm","hotSwap":true,"includes":"Case, PCB, plate, foam"}', '/uploads/products/akko-acr68.jpg', '2026-11-09 09:00:00'),
  (18, 5, 'Keychron Q3 Barebone TKL', 'Keychron', 3900000, 4, 'TKL', NULL, 'ACTIVE', 'Kit TKL vỏ nhôm, hỗ trợ tùy biến phím bằng phần mềm. Thích hợp người cần cụm điều hướng đầy đủ.', '{"keys":87,"case":"Nhôm","hotSwap":true,"includes":"Case, PCB, plate, foam"}', '/uploads/products/keychron-q3-barebone.jpg', '2026-11-09 09:00:00'),
  (19, 5, 'KBDfans Tofu84 Barebone 75%', 'KBDfans', 3600000, 3, '75%', NULL, 'ACTIVE', 'Kit 75% vỏ nhôm, bố cục gọn nhưng đủ phím mũi tên và hàng chức năng. Dành cho người đã quen lắp bàn phím.', '{"keys":84,"case":"Nhôm","hotSwap":true,"includes":"Case, PCB, plate, foam"}', '/uploads/products/kbdfans-tofu84.jpg', '2026-11-09 09:00:00'),
  (20, 2, 'Gateron Yellow (gói 35)', 'Gateron', 245000, 80, NULL, 'Linear', 'ACTIVE', 'Switch linear nhẹ, hành trình mượt, giá tốt. Hợp người mới và game thủ thích gõ nhanh.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/gateron-yellow.jpg', '2026-11-09 09:00:00'),
  (21, 2, 'Gateron Oil King (gói 35)', 'Gateron', 420000, 0, NULL, 'Linear', 'ACTIVE', 'Switch linear được bôi sẵn, gõ rất mượt và âm thanh trầm. Hiện tạm hết hàng.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/gateron-oil-king.jpg', '2026-11-09 09:00:00'),
  (22, 2, 'Gateron Brown (gói 35)', 'Gateron', 245000, 70, NULL, 'Tactile', 'ACTIVE', 'Switch tactile có nấc nhẹ ở giữa hành trình, gõ êm, hợp vừa gõ văn bản vừa chơi game.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/gateron-brown.jpg', '2026-11-09 09:00:00'),
  (23, 2, 'Gateron Blue (gói 35)', 'Gateron', 245000, 60, NULL, 'Clicky', 'ACTIVE', 'Switch clicky có tiếng click rõ và phản hồi mạnh. Ồn, không hợp nơi làm việc chung.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/gateron-blue.jpg', '2026-11-09 09:00:00'),
  (24, 2, 'Cherry MX Red (gói 35)', 'Cherry', 480000, 50, NULL, 'Linear', 'ACTIVE', 'Switch linear chính hãng Cherry, hành trình êm và đều, bền bỉ.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/cherry-mx-red.jpg', '2026-11-09 09:00:00'),
  (25, 2, 'Cherry MX Brown (gói 35)', 'Cherry', 480000, 45, NULL, 'Tactile', 'ACTIVE', 'Switch tactile chính hãng Cherry, nấc nhẹ vừa phải, phù hợp làm việc văn phòng.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/cherry-mx-brown.jpg', '2026-11-09 09:00:00'),
  (26, 2, 'Cherry MX Blue (gói 35)', 'Cherry', 480000, 40, NULL, 'Clicky', 'ACTIVE', 'Switch clicky chính hãng Cherry, tiếng click đặc trưng và cảm giác phản hồi rõ.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/cherry-mx-blue.jpg', '2026-11-09 09:00:00'),
  (27, 2, 'Kailh Box White (gói 35)', 'Kailh', 330000, 55, NULL, 'Clicky', 'ACTIVE', 'Switch clicky dạng box chống bụi, tiếng click sắc và gọn.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/kailh-box-white.jpg', '2026-11-09 09:00:00'),
  (28, 2, 'Kailh Speed Silver (gói 35)', 'Kailh', 340000, 48, NULL, 'Linear', 'ACTIVE', 'Switch linear hành trình ngắn, kích hoạt nhanh, hợp game đòi hỏi phản xạ.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/kailh-speed-silver.jpg', '2026-11-09 09:00:00'),
  (29, 2, 'Durock T1 (gói 35)', 'Durock', 390000, 42, NULL, 'Tactile', 'ACTIVE', 'Switch tactile nấc rõ, gõ chắc tay và âm thanh dày.', '{"pack":35,"pin":"5 chân"}', '/uploads/products/durock-t1.jpg', '2026-11-09 09:00:00'),
  (30, 3, 'Akko Keycap Set World Tour Tokyo', 'Akko', 970000, 18, NULL, NULL, 'ACTIVE', 'Bộ keycap PBT in dye-sub, họa tiết lấy cảm hứng từ Tokyo, bền màu và không bóng theo thời gian.', '{"material":"PBT","profile":"Cherry"}', '/uploads/products/akko-world-tour-tokyo.jpg', '2026-11-09 09:00:00'),
  (31, 3, 'Keychron PBT Dye-Sub Keycap Set', 'Keychron', 690000, 25, NULL, NULL, 'ACTIVE', 'Bộ keycap PBT dye-sub, bề mặt nhám dễ chịu, hợp nhiều bố cục từ 60% đến fullsize.', '{"material":"PBT","profile":"OEM"}', '/uploads/products/keychron-pbt-dyesub.jpg', '2026-11-09 09:00:00'),
  (32, 3, 'Ducky PBT Double-shot Keycap Set', 'Ducky', 1150000, 10, NULL, NULL, 'ACTIVE', 'Bộ keycap PBT double-shot chữ không phai, dày tay, âm thanh gõ trầm hơn.', '{"material":"PBT","profile":"OEM"}', '/uploads/products/ducky-pbt-doubleshot.jpg', '2026-11-09 09:00:00'),
  (33, 3, 'Bộ keycap ABS Double-shot XDA 129 phím', 'Generic', 390000, 40, NULL, NULL, 'ACTIVE', 'Bộ keycap ABS double-shot giá rẻ, nhiều màu, phù hợp nâng cấp nhẹ cho bàn phím đầu tiên.', '{"material":"ABS","profile":"XDA"}', '/uploads/products/abs-xda-129.jpg', '2026-11-09 09:00:00'),
  (34, 3, 'Bộ keycap PBT Cherry Profile Backlit', 'Akko', 450000, 32, NULL, NULL, 'ACTIVE', 'Bộ keycap PBT in trong suốt ký tự, cho ánh sáng đèn nền xuyên qua rõ nét.', '{"material":"PBT","profile":"Cherry"}', '/uploads/products/pbt-cherry-backlit.jpg', '2026-11-09 09:00:00'),
  (35, 3, 'Keycap Artisan Resin ESC', 'Handmade', 350000, 15, NULL, NULL, 'ACTIVE', 'Keycap resin làm thủ công cho phím ESC, mỗi chiếc có chi tiết khác nhau. Dùng để trang trí.', '{"material":"Resin","profile":"Cherry"}', '/uploads/products/artisan-resin-esc.jpg', '2026-11-09 09:00:00'),
  (36, 3, 'Bộ keycap PBT Cherry 126 phím Retro Beige', 'Akko', 780000, 22, NULL, NULL, 'ACTIVE', 'Bộ keycap PBT tông be cổ điển, hợp phong cách retro và bàn làm việc tối giản.', '{"material":"PBT","profile":"Cherry"}', '/uploads/products/pbt-retro-beige.jpg', '2026-11-09 09:00:00'),
  (37, 3, 'Bộ keycap MDA Profile PBT Pastel', 'Epomaker', 820000, 14, NULL, NULL, 'ACTIVE', 'Bộ keycap PBT profile MDA thấp, màu pastel nhẹ nhàng, gõ êm và thoải mái.', '{"material":"PBT","profile":"MDA"}', '/uploads/products/mda-pastel.jpg', '2026-11-09 09:00:00'),
  (38, 3, 'Bộ keycap phiên bản giới hạn (ngừng bán)', 'Akko', 1250000, 2, NULL, NULL, 'HIDDEN', 'Bộ keycap phiên bản giới hạn đã ngừng bán, được ẩn khỏi cửa hàng để kiểm thử ẩn/hiện.', '{"material":"PBT","profile":"Cherry"}', '/uploads/products/limited-edition-set.jpg', '2026-11-09 09:00:00'),
  (39, 4, 'Kẹp tháo switch (switch puller)', 'Generic', 79000, 60, NULL, NULL, 'ACTIVE', 'Dụng cụ gắp switch ra khỏi bàn phím hot-swap mà không làm cong chân.', '{}', '/uploads/products/switch-puller.jpg', '2026-11-09 09:00:00'),
  (40, 4, 'Kẹp tháo keycap bằng thép', 'Generic', 49000, 80, NULL, NULL, 'ACTIVE', 'Dụng cụ tháo keycap dạng vòng thép, không làm xước keycap.', '{}', '/uploads/products/keycap-puller.jpg', '2026-11-09 09:00:00'),
  (41, 4, 'Lube Krytox 205g0 (3g)', 'Krytox', 250000, 30, NULL, NULL, 'ACTIVE', 'Mỡ bôi trơn phổ biến cho switch và stabilizer, giúp gõ mượt và giảm tiếng ma sát.', '{}', '/uploads/products/krytox-205g0.jpg', '2026-11-09 09:00:00'),
  (42, 4, 'Bộ 3 cọ bôi lube', 'Generic', 35000, 70, NULL, NULL, 'ACTIVE', 'Bộ cọ nhỏ dùng bôi lube lên lò xo và thân switch.', '{}', '/uploads/products/lube-brush.jpg', '2026-11-09 09:00:00'),
  (43, 4, 'Switch tester 9 loại', 'Generic', 189000, 25, NULL, NULL, 'ACTIVE', 'Bảng thử 9 loại switch gồm linear, tactile và clicky để bạn chọn loại hợp tay trước khi mua.', '{}', '/uploads/products/switch-tester.jpg', '2026-11-09 09:00:00'),
  (44, 4, 'Kê tay gỗ cho bàn phím TKL', 'Generic', 290000, 20, NULL, NULL, 'ACTIVE', 'Kê tay bằng gỗ, mặt phủ dầu, đệm cổ tay khi gõ lâu.', '{}', '/uploads/products/wrist-rest-wood-tkl.jpg', '2026-11-09 09:00:00'),
  (45, 4, 'Cáp USB-C xoắn 1m', 'Generic', 320000, 28, NULL, NULL, 'ACTIVE', 'Cáp USB-C dạng lò xo, đầu cắm hàng không, làm gọn bàn làm việc.', '{}', '/uploads/products/coiled-usbc.jpg', '2026-11-09 09:00:00'),
  (46, 4, 'Tấm lót tiêu âm cho case 65%', 'Generic', 120000, 26, NULL, NULL, 'ACTIVE', 'Tấm foam lót trong case giúp giảm tiếng vang và âm thanh rỗng.', '{}', '/uploads/products/case-foam-65.jpg', '2026-11-09 09:00:00'),
  (47, 4, 'Film cho switch (100 miếng)', 'Generic', 85000, 45, NULL, NULL, 'ACTIVE', 'Miếng film đặt trong switch giúp giảm lắc thân trên, tiếng gõ gọn hơn.', '{}', '/uploads/products/switch-film.jpg', '2026-11-09 09:00:00');
