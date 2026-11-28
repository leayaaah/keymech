# C-03 Chọn linh kiện Custom từ danh mục

Customer chọn một kit gồm vỏ, plate và PCB, một mẫu switch, một bộ keycap và một bộ stabilizer nếu kit chưa kèm. Khách có thể nhập ngân sách và ghi chú lắp ráp. Nhân viên vẫn báo giá và gửi cho khách qua luồng hiện có.

## API

- `GET /api/custom-requests/components` trả mảng kit đang bán có đủ lựa chọn tương thích. Mỗi phần tử có `kit`, `layout`, `hotSwap`, `stabilizersIncluded`, `switches`, `keycaps` và `stabilizers`. Mỗi lựa chọn có `productId`, `name`, `unitPrice` và `quantity`. `quantity` của switch là số gói đủ cho số phím của kit.
- `POST /api/custom-requests` nhận `kitProductId`, `switchProductId`, `keycapProductId`, `stabilizerProductId`, `budget` và `note`. Bỏ `stabilizerProductId` hoặc truyền `null` nếu đã đi kèm kit. Đơn giá, số lượng và thuộc tính cấu hình do backend xác định từ danh mục. Thành công trả `201`, trạng thái `PENDING` và `components` chứa bản chụp `slot`, `productId`, `name`, `quantity`, `unitPrice`.
- `GET /api/custom-requests/my/{id}` và chi tiết tương ứng của Staff trả bản chụp linh kiện. Yêu cầu cũ có `components: []` và vẫn hiển thị các trường cấu hình trước đây.
- Guest nhận `401`. Staff và Admin nhận `403` khi gọi nhóm API Customer. Thiếu lựa chọn trả `400 VALIDATION_ERROR`. Sai tương thích trả `400 CUSTOM_COMPONENT_INCOMPATIBLE`. Linh kiện bị ẩn hoặc không còn tồn tại trả `409 CUSTOM_COMPONENT_UNAVAILABLE`. Thiếu tồn kho trả `409 OUT_OF_STOCK`.

## Thông số bắt buộc trong `products.specs`

| Sản phẩm | Thông số dùng để kiểm tra |
| --- | --- |
| Kit, danh mục `kit-ban-phim` | `includes` liệt kê `Case`, `Plate`, `PCB` và `Stabilizer` nếu đi kèm. Cần `keys`, `hotSwap`, `switchFamily`, `supportedPins`, `stem`, `physicalLayout`, `spacebarWidth`, `rightShiftWidth`. Nếu kit chưa kèm stabilizer, cần thêm `platform`, `stabilizerMount`, `stabilizerSizes`. Layout lấy từ cột `products.layout`. |
| Switch, danh mục `switch` | `switchFamily`, `stem`, `pinCount`, `pack`. Loại Linear, Tactile hoặc Clicky lấy từ `products.switch_type`. |
| Keycap, danh mục `keycap` | `componentType=keycap-set`, `stem`, `physicalLayout`, `spacebarWidth`, `rightShiftWidth` khớp kit. Cột `products.layout` hoặc mảng `supportedLayouts` phải có layout của kit. |
| Stabilizer, danh mục `dung-cu-phu-kien` | `componentType=stabilizer`, `platform`, `mount`, `sizes` khớp thông số của kit. |

Sản phẩm thiếu dữ liệu để xác nhận tương thích sẽ không xuất hiện trong form. Việc gửi yêu cầu kiểm tra tồn hiện tại, không giữ hàng. Phần tạm tính linh kiện và báo giá của nhân viên được lưu riêng.

## CSDL và kiểm thử

Compose chờ MariaDB sẵn sàng rồi chạy dịch vụ `db-setup`. Dịch vụ chạy `01-schema.sql` khi thiếu bảng nền và nạp `02-data.sql` nếu các bảng người dùng, danh mục, sản phẩm đều trống. Sau đó nó áp dụng `04-custom-request-components.sql` để tạo bảng linh kiện C-03, giữ dữ liệu hiện có. Seed `03-custom-components-dev.sql` bổ sung dữ liệu demo cho 60%, 65%, 75%, TKL và Fullsize và có thể chạy lại.

### Kịch bản demo

| Chọn kit | Kết quả mong đợi |
| --- | --- |
| KM75 ANSI | Có lựa chọn Silent Linear và Silent Tactile. Chọn Silent Linear cần 3 gói, thêm bộ stabilizer KM75 và keycap PBT ANSI 75%. Tạm tính 1.870.000 ₫ theo giá seed. |
| KM60, KM65 | Đã kèm stabilizer. Với switch gói 35 chiếc, mỗi cấu hình cần 2 gói. Bộ keycap 60% và 65% được lọc theo kit. |
| KMTKL ISO | Chỉ hiện switch 3 chân, keycap ISO TKL và stabilizer KMTKL gắn vít. Chọn switch silent 5 chân hoặc keycap ANSI qua API trả `400 CUSTOM_COMPONENT_INCOMPATIBLE`. |
| KM104 ANSI | Đã kèm stabilizer. Cần 3 gói switch loại 35 chiếc và bộ keycap fullsize có cụm số. |

Chạy từ thư mục gốc và chờ `db-setup` thoát với mã `0` trước khi khởi động backend.

```sh
docker compose up -d
docker compose wait db-setup
```

Xem chi tiết bằng `docker compose logs db-setup`. Seed demo dùng cho local dev/test. Đặt `KEYMECH_SEED_DEMO=0` trong `.env` để bỏ qua lượt nạp demo.

Nếu volume đã có mật khẩu root riêng, đặt `MARIADB_ROOT_PASSWORD` trong `.env` bằng mật khẩu hiện có để Compose kết nối và chạy SQL. Nếu backend đang dùng `DB_USERNAME=root`, `DB_PASSWORD` cũng cần khớp mật khẩu root hiện có.

Postman collection dùng biến `baseUrl`, `customerEmail`, `customerPassword`. Chạy trên môi trường thử nghiệm vì collection tạo một yêu cầu `PENDING`.
