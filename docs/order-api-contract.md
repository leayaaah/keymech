# Hợp đồng API đơn hàng v1 — C-02, C-04, A-04 đến A-06

Ngày soạn: 13/11/2026. Đây là hợp đồng để frontend dựng giao diện và dữ liệu mock; các endpoint chưa được triển khai. Khi thay đổi đường dẫn, JSON hoặc mã lỗi sau khi nhóm chốt, phải cập nhật file này và báo nhóm frontend trước khi merge.

## Quy ước chung

- URL bắt đầu bằng `/api`; request và response là JSON UTF-8 với tên thuộc tính `camelCase`.
- Xác thực bằng Session cookie. `/api/orders/**` chỉ dành cho Customer; `/api/staff/orders/**` dành cho Staff và Admin. Guest nhận 401, vai trò không phù hợp nhận 403. Customer chỉ xem đơn của chính mình; xem đơn của người khác nhận 403 theo kế hoạch.
- Tiền là số nguyên VND trong JSON, ánh xạ `BigDecimal` ở Java. Ngày giờ là chuỗi ISO 8601 dạng `yyyy-MM-ddTHH:mm:ss` theo giờ ứng dụng.
- `status`: `NEW`, `CONFIRMED`, `SHIPPING`, `COMPLETED`, `CANCELLED`; `paymentMethod`: `COD`, `BANK_TRANSFER`; `paymentStatus`: `UNPAID`, `PAID`.
- API danh sách nhận `page` từ 0 và `size` dương; response luôn có `content`, `page`, `size`, `totalElements`, `totalPages`. Giá trị mặc định và giới hạn `size` cần nhóm chốt trước khi triển khai.
- Mã lỗi dùng `{status, error, message, timestamp}`. `message` tiếng Việt; client xử lý theo `status` và `error`, không phân tích chuỗi `message`.
- Giá, trạng thái hiển thị và tồn kho của sản phẩm luôn được đọc lại từ CSDL khi checkout. `unitPrice` trong đơn là giá chốt khi mua, không đổi khi giá sản phẩm về sau thay đổi.

### Mẫu lỗi

```json
{
  "status": 409,
  "error": "OUT_OF_STOCK",
  "message": "Sản phẩm không còn đủ hàng",
  "timestamp": "2026-11-13T10:30:00"
}
```

| HTTP | `error` | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Body, tham số hoặc khoảng ngày không hợp lệ |
| 400 | `EMPTY_CART` *(đề xuất)* | Giỏ trong Session rỗng khi checkout |
| 401 | `UNAUTHORIZED` | Chưa đăng nhập |
| 403 | `FORBIDDEN` | Sai vai trò hoặc Customer xem đơn người khác |
| 404 | `NOT_FOUND` | Không tồn tại đơn hoặc dòng hàng |
| 409 | `OUT_OF_STOCK` | Thiếu hàng hoặc sản phẩm đã ẩn; checkout rollback và giữ giỏ |
| 409 | `INVALID_TRANSITION` *(đề xuất)* | Trạng thái mới không theo luồng hợp lệ |
| 409 | `ORDER_NOT_EDITABLE` *(đề xuất)* | Sửa số lượng ngoài trạng thái `NEW` hoặc `CONFIRMED` |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống; không trả stack trace |

Các mã đánh dấu *đề xuất* chưa có code nghiệp vụ tương ứng và cần nhóm xác nhận trước khi frontend phụ thuộc vào chúng. Không tự retry checkout sau lỗi không rõ kết quả; cần tải lại giỏ và lịch sử đơn trước.

## Customer

### `POST /api/orders/checkout`

Đặt hàng từ giỏ `Map<productId, quantity>` trong Session. Không nhận danh sách sản phẩm, đơn giá hoặc tổng tiền từ client. Body bắt buộc:

```json
{
  "receiverName": "Nguyễn An",
  "receiverPhone": "0901234567",
  "shippingAddress": "12 Nguyễn Trãi, Quận 1, TP.HCM",
  "paymentMethod": "BANK_TRANSFER"
}
```

Thành công: `201 Created`. Đơn khởi tạo `NEW`, `UNPAID`; giỏ chỉ xóa sau khi transaction tạo đơn và trừ tồn đã commit.

```json
{
  "id": 101,
  "orderCode": "ORD-A1B2C3D4E5",
  "status": "NEW",
  "paymentStatus": "UNPAID",
  "totalAmount": 4190000
}
```

Lỗi: 400 `VALIDATION_ERROR` hoặc `EMPTY_CART`; 401/403; 409 `OUT_OF_STOCK` (rollback toàn bộ, giữ giỏ); 500 `INTERNAL_ERROR`. Email xác nhận và tracking chạy sau commit, lỗi các việc phụ không hủy đơn.

### `GET /api/orders/my?page=0&size=10`

Danh sách đơn của Customer hiện tại, phân trang theo `orderDate` mới nhất trước *(đề xuất)*. Thành công: `200 OK`.

```json
{
  "content": [
    {
      "id": 101,
      "orderCode": "ORD-A1B2C3D4E5",
      "orderDate": "2026-11-13T10:30:00",
      "status": "NEW",
      "paymentMethod": "BANK_TRANSFER",
      "paymentStatus": "UNPAID",
      "totalAmount": 4190000
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

Lỗi: 400 `VALIDATION_ERROR` nếu phân trang sai; 401/403.

### `GET /api/orders/my/{id}`

Chi tiết đơn của Customer hiện tại. Thành công: `200 OK`.

```json
{
  "id": 101,
  "orderCode": "ORD-A1B2C3D4E5",
  "orderDate": "2026-11-13T10:30:00",
  "receiverName": "Nguyễn An",
  "receiverPhone": "0901234567",
  "shippingAddress": "12 Nguyễn Trãi, Quận 1, TP.HCM",
  "paymentMethod": "BANK_TRANSFER",
  "paymentStatus": "UNPAID",
  "status": "NEW",
  "totalAmount": 4190000,
  "items": [
    {
      "id": 501,
      "productId": 1,
      "productName": "Keychron K2 75%",
      "quantity": 1,
      "unitPrice": 2200000,
      "lineTotal": 2200000
    },
    {
      "id": 502,
      "productId": 2,
      "productName": "Keychron K6 65%",
      "quantity": 1,
      "unitPrice": 1990000,
      "lineTotal": 1990000
    }
  ]
}
```

`productName` đọc từ sản phẩm hiện tại; `order_items` chỉ lưu snapshot đơn giá. Sản phẩm đã ẩn vẫn xuất hiện trong đơn cũ. Lỗi: 401/403; 404 `NOT_FOUND` nếu ID không tồn tại.

## Staff và Admin

### `GET /api/staff/orders?keyword=an&fromDate=2026-11-01&toDate=2026-11-30&status=NEW&page=0&size=10`

Tất cả bộ lọc đều tùy chọn. `keyword` tìm theo mã đơn, tên hoặc email Customer; `fromDate` và `toDate` dạng `yyyy-MM-dd`, gồm cả ngày đầu và ngày cuối; `status` dùng enum ở trên. Sắp xếp `orderDate` mới nhất trước *(đề xuất)*. Thành công: `200 OK`.

```json
{
  "content": [
    {
      "id": 101,
      "orderCode": "ORD-A1B2C3D4E5",
      "customerId": 12,
      "customerName": "Nguyễn An",
      "customerEmail": "an@example.com",
      "orderDate": "2026-11-13T10:30:00",
      "status": "NEW",
      "paymentStatus": "UNPAID",
      "totalAmount": 4190000
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

Lỗi: 400 `VALIDATION_ERROR` nếu `fromDate > toDate`, enum hoặc phân trang sai; 401/403.

### `GET /api/staff/orders/{id}`

Chi tiết mọi đơn, kể cả sản phẩm đã ẩn. Thành công: `200 OK`; cùng cấu trúc chi tiết của `GET /api/orders/my/{id}`, thêm `customerId`, `customerName`, `customerEmail`. Lỗi: 401/403; 404 `NOT_FOUND`.

### `PUT /api/staff/orders/{id}/items/{itemId}`

Sửa số lượng của dòng hàng thuộc đúng đơn; chỉ khi đơn đang `NEW` hoặc `CONFIRMED`. Không đổi `unitPrice` đã chốt. Body:

```json
{
  "quantity": 2
}
```

`quantity` là số nguyên từ 1. Tăng số lượng dùng `UPDATE` trừ tồn có điều kiện và kiểm tra số dòng ảnh hưởng; giảm số lượng hoàn tồn phần chênh lệch. Sản phẩm đã ẩn chỉ được giảm. Cập nhật `totalAmount` cùng transaction. Thành công: `200 OK`, trả chi tiết đơn theo mẫu trên với số lượng và tổng tiền mới. Lỗi: 400 `VALIDATION_ERROR`; 401/403; 404 `NOT_FOUND`; 409 `OUT_OF_STOCK` hoặc `ORDER_NOT_EDITABLE`.

### `PUT /api/staff/orders/{id}/status`

Body:

```json
{
  "status": "CONFIRMED"
}
```

Luồng hợp lệ: `NEW → CONFIRMED → SHIPPING → COMPLETED`; chỉ `NEW` và `CONFIRMED` có thể chuyển sang `CANCELLED`. Khi hủy, hoàn tồn kho từng dòng trong cùng transaction. `COMPLETED` và `CANCELLED` là trạng thái cuối. Thành công: `200 OK`, trả chi tiết đơn với `status` mới. Lỗi: 400 `VALIDATION_ERROR`; 401/403; 404 `NOT_FOUND`; 409 `INVALID_TRANSITION`.

### `PUT /api/staff/orders/{id}/payment`

Cập nhật `paymentStatus` do Staff hoặc Admin xác nhận, ví dụ chuyển khoản đã nhận được tiền:

```json
{
  "paymentStatus": "PAID"
}
```

Thành công: `200 OK`, trả chi tiết đơn với `paymentStatus` mới. Lỗi: 400 `VALIDATION_ERROR`; 401/403; 404 `NOT_FOUND`. Quy tắc cho phép chuyển ngược `PAID → UNPAID` và xử lý thanh toán của đơn `CANCELLED` chưa được kế hoạch xác định; nhóm cần chốt trước khi code endpoint này.

## Điểm cần chốt với nhóm trước khi triển khai

1. Mặc định và giới hạn tối đa của `size`, thứ tự sắp xếp danh sách.
2. Các mã đề xuất `EMPTY_CART`, `INVALID_TRANSITION`, `ORDER_NOT_EDITABLE`.
3. Quy tắc cập nhật `paymentStatus` khi đã `PAID` hoặc đơn đã hủy.
4. Tên sản phẩm trong chi tiết đơn lấy từ sản phẩm hiện tại vì schema chưa có cột snapshot tên; nếu cần giữ tên lúc mua, phải đổi schema và báo nhóm.
