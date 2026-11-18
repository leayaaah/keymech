# Hợp đồng API đơn hàng v1 — C-02, C-04, A-04 đến A-06

Soạn ngày 13/11/2026, bổ sung hợp đồng Staff ngày 18/11/2026. `POST /api/orders/checkout` đã có trên `main`. Phần Staff bên dưới là hợp đồng triển khai Sprint 3 để backend và frontend làm song song. Các endpoint danh sách và chi tiết đơn của Customer cũng đang chờ triển khai. Khi thay đổi đường dẫn, JSON hoặc mã lỗi sau khi nhóm chốt, phải cập nhật file này và báo nhóm frontend trước khi merge.

## Quy ước chung

- URL bắt đầu bằng `/api`; request và response là JSON UTF-8 với tên thuộc tính `camelCase`.
- Xác thực bằng Session cookie. `/api/orders/**` chỉ dành cho Customer; `/api/staff/orders/**` dành cho Staff và Admin. Guest nhận 401, vai trò không phù hợp nhận 403. Customer chỉ xem đơn của chính mình; xem đơn của người khác nhận 403 theo kế hoạch.
- Tiền là số nguyên VND trong JSON, ánh xạ `BigDecimal` ở Java. Ngày giờ là chuỗi ISO 8601 dạng `yyyy-MM-ddTHH:mm:ss` theo giờ ứng dụng.
- `status`: `NEW`, `CONFIRMED`, `SHIPPING`, `COMPLETED`, `CANCELLED`; `paymentMethod`: `COD`, `BANK_TRANSFER`; `paymentStatus`: `UNPAID`, `PAID`.
- API danh sách mặc định `page=0`, `size=10`; `page` phải từ 0, `size` từ 1 đến 50. Giá trị ngoài khoảng trả 400 `VALIDATION_ERROR`, không tự sửa. Sắp xếp `orderDate DESC, id DESC` để thứ tự ổn định. Response luôn có `content`, `page`, `size`, `totalElements`, `totalPages`.
- Mã lỗi dùng `{status, error, message, timestamp}`. `message` tiếng Việt; client xử lý theo `status` và `error`, không phân tích chuỗi `message`.
- Giá, trạng thái hiển thị và tồn kho của sản phẩm luôn được đọc lại từ CSDL khi checkout. `unitPrice` trong đơn là giá chốt khi mua, không đổi khi giá sản phẩm về sau thay đổi. `productName` trong chi tiết đơn lấy từ sản phẩm hiện tại nên có thể đổi khi Staff sửa tên; schema hiện chưa lưu tên lúc mua.

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
| 400 | `EMPTY_CART` | Giỏ trong Session rỗng khi checkout |
| 401 | `UNAUTHORIZED` | Chưa đăng nhập |
| 403 | `FORBIDDEN` | Sai vai trò hoặc Customer xem đơn người khác |
| 404 | `NOT_FOUND` | Không tồn tại đơn hoặc dòng hàng |
| 409 | `OUT_OF_STOCK` | Thiếu hàng hoặc sản phẩm đã ẩn; checkout rollback và giữ giỏ |
| 409 | `INVALID_TRANSITION` | Trạng thái đơn mới không theo luồng hợp lệ |
| 409 | `ORDER_NOT_EDITABLE` | Sửa số lượng ngoài trạng thái `NEW` hoặc `CONFIRMED` |
| 409 | `PAYMENT_NOT_EDITABLE` | Đánh dấu đã trả cho đơn đã hủy, đổi `PAID` về `UNPAID`, đánh dấu `PAID` lần nữa hoặc hủy đơn đã trả |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống; không trả stack trace |

Các mã lỗi này là hợp đồng cho phần nghiệp vụ; chỉ các mã của endpoint đã triển khai có code tương ứng. Không tự retry checkout sau lỗi không rõ kết quả; cần tải lại giỏ và lịch sử đơn trước.

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

Thành công: `201 Created`. Đơn khởi tạo `NEW`, `UNPAID`. Giá được đọc dưới khóa dòng sản phẩm trước khi trừ tồn. Giỏ chỉ xóa sau khi transaction đã commit. Checkout thứ hai trên cùng phiên sau khi giỏ được xóa trả `400 EMPTY_CART`.

```json
{
  "id": 101,
  "orderCode": "ORD-A1B2C3D4E5",
  "status": "NEW",
  "paymentStatus": "UNPAID",
  "totalAmount": 4190000
}
```

Lỗi: 400 `VALIDATION_ERROR` hoặc `EMPTY_CART`; 401/403; 409 `OUT_OF_STOCK` (rollback toàn bộ, giữ giỏ); 500 `INTERNAL_ERROR`. Email xác nhận được lên lịch sau commit; lỗi email không hủy đơn. Ghi `PURCHASE` sẽ nối với `ActivityTracker` do Phương phụ trách khi module tracking được merge.

### `GET /api/orders/my?page=0&size=10`

Danh sách đơn của Customer hiện tại, phân trang và sắp xếp theo quy ước chung. Thành công: `200 OK`.

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

`productName` đọc từ sản phẩm hiện tại, kể cả sản phẩm đã ẩn; tên có thể đổi sau khi Staff sửa sản phẩm. `order_items` chỉ lưu snapshot đơn giá. Nếu sau này cần cố định tên lúc mua, phải đổi schema trong một PR riêng và báo nhóm. Lỗi: 401/403; 404 `NOT_FOUND` nếu ID không tồn tại.

## Staff và Admin

Thùy phụ trách backend, Trí phụ trách giao diện. Theo [bản phân công hạn 30/11](Phan_cong_Nhom4_deadline_30-11.md), A-04 và A-06 là Must, A-05 là Should. Mốc bàn giao hợp đồng là 20/11. Dùng cookie Session khi gọi API. Staff và Admin có cùng quyền trên nhóm endpoint này. Guest nhận `401 UNAUTHORIZED`, Customer nhận `403 FORBIDDEN`.

`id` trên URL là ID số nguyên dương của đơn hàng. `itemId` là ID số nguyên dương của dòng hàng trong đơn đó. Sai định dạng trả `400 VALIDATION_ERROR`. ID hợp lệ nhưng đơn hoặc dòng hàng tương ứng không tồn tại trả `404 NOT_FOUND`.

### A-04 · Danh sách và chi tiết đơn

#### `GET /api/staff/orders`

Tất cả tham số đều tùy chọn. Các bộ lọc được áp dụng đồng thời.

| Tham số | Quy tắc |
| --- | --- |
| `keyword` | Tìm chuỗi con trong mã đơn, họ tên hoặc email Customer. Bỏ khoảng trắng ở hai đầu, so khớp không phân biệt chữ hoa/thường. Chuỗi rỗng tương đương bỏ bộ lọc. |
| `fromDate`, `toDate` | Dạng `yyyy-MM-dd`, tính theo ngày nghiệp vụ `Asia/Ho_Chi_Minh`. Có thể dùng riêng từng cận. `fromDate` tính từ đầu ngày, `toDate` bao gồm cả ngày cuối. |
| `status` | Một giá trị `OrderStatus` theo quy ước chung. |
| `page`, `size` | Số nguyên, mặc định `0` và `10`. `page ≥ 0` và `1 ≤ size ≤ 50`. |

Ví dụ `GET /api/staff/orders?keyword=an&fromDate=2026-11-01&toDate=2026-11-30&status=NEW&page=0&size=10`. Trả `200 OK`, sắp xếp `orderDate DESC, id DESC`.

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

Kết quả không có dòng trả `200 OK` với `content: []`. Nếu không có đơn khớp, `totalElements` và `totalPages` bằng 0. Nếu trang vượt kết quả, giữ nguyên `page` yêu cầu và trả tổng theo tập lọc. Ngày sai định dạng, `fromDate > toDate`, enum hoặc phân trang sai trả `400 VALIDATION_ERROR`.

#### `GET /api/staff/orders/{id}`

Trả `200 OK` với chi tiết đầy đủ của đơn, kể cả các dòng có sản phẩm đã ẩn. Đây cũng là cấu trúc response cho cả ba thao tác cập nhật phía dưới. `items[].id` là `itemId` dùng để sửa số lượng.

```json
{
  "id": 101,
  "orderCode": "ORD-A1B2C3D4E5",
  "customerId": 12,
  "customerName": "Nguyễn An",
  "customerEmail": "an@example.com",
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

### A-05 · Sửa số lượng

#### `PUT /api/staff/orders/{id}/items/{itemId}`

Sửa số lượng của dòng hàng thuộc đúng đơn khi đơn đang `NEW` hoặc `CONFIRMED`. Body JSON bắt buộc:

```json
{
  "quantity": 2
}
```

`quantity` là số nguyên từ 1. Backend dùng `unitPrice` đã chốt để tính `lineTotal` và `totalAmount`. Khi tăng số lượng, trừ phần chênh lệch bằng `UPDATE` có điều kiện và kiểm tra số dòng ảnh hưởng. Khi giảm, hoàn tồn phần chênh lệch. Với sản phẩm đã ẩn, thao tác thay đổi chỉ được giảm số lượng. Tồn kho, dòng hàng và tổng tiền được cập nhật cùng transaction.

Trả `200 OK` với chi tiết đơn sau cập nhật. Ví dụ đổi dòng 501 từ 1 thành 2 trong mẫu trên thì `lineTotal` của dòng là `4400000` và `totalAmount` là `6390000`. Số lượng không hợp lệ trả `400 VALIDATION_ERROR`. Dòng hàng thuộc đơn khác trả `404 NOT_FOUND`. Thiếu tồn hoặc tăng số lượng sản phẩm đã ẩn trả `409 OUT_OF_STOCK`. Đơn ngoài `NEW` và `CONFIRMED` trả `409 ORDER_NOT_EDITABLE`.

### A-06 · Trạng thái đơn và thanh toán

#### `PUT /api/staff/orders/{id}/status`

Body JSON bắt buộc, `status` là một giá trị `OrderStatus`:

```json
{
  "status": "CONFIRMED"
}
```

| Trạng thái hiện tại | Trạng thái đích hợp lệ |
| --- | --- |
| `NEW` | `CONFIRMED`, hoặc `CANCELLED` nếu `UNPAID` |
| `CONFIRMED` | `SHIPPING`, hoặc `CANCELLED` nếu `UNPAID` |
| `SHIPPING` | `COMPLETED` |
| `COMPLETED`, `CANCELLED` | Trạng thái cuối |

Khi hủy, hoàn tồn từng dòng, kể cả sản phẩm đã ẩn, trong cùng transaction với chuyển trạng thái. Đơn `NEW` hoặc `CONFIRMED` đã `PAID` trả `409 PAYMENT_NOT_EDITABLE` khi yêu cầu hủy. Các chuyển đổi khác ngoài bảng trả `409 INVALID_TRANSITION`, kể cả gửi lại cùng trạng thái. Giá trị enum hoặc body sai trả `400 VALIDATION_ERROR`. Thành công trả `200 OK` với chi tiết đơn và `status` mới.

Ví dụ response khi yêu cầu hủy đơn `CONFIRMED` đã thanh toán:

```json
{
  "status": 409,
  "error": "PAYMENT_NOT_EDITABLE",
  "message": "Không thể hủy đơn đã thanh toán",
  "timestamp": "2026-11-18T09:30:00"
}
```

#### `PUT /api/staff/orders/{id}/payment`

Staff hoặc Admin xác nhận đã nhận tiền cho đơn COD hoặc chuyển khoản. Body JSON bắt buộc, `paymentStatus` là một giá trị `PaymentStatus`:

```json
{
  "paymentStatus": "PAID"
}
```

Chỉ cho chuyển `UNPAID → PAID` một lần khi đơn chưa `CANCELLED`. Trả `200 OK` với chi tiết đơn và `paymentStatus` mới. Cập nhật thanh toán chỉ tác động đến `paymentStatus`. Enum hoặc body sai trả `400 VALIDATION_ERROR`. Đơn đã hủy, yêu cầu chuyển về `UNPAID` hoặc xác nhận `PAID` lần nữa đều trả `409 PAYMENT_NOT_EDITABLE`.

### Nối giao diện và nghiệm thu

- Giao diện dùng response chi tiết mới từ mỗi thao tác thành công. Khi nhận `409` hoặc mất kết nối sau khi gửi yêu cầu, tải lại chi tiết đơn trước khi người dùng thao tác tiếp.
- Backend kiểm tra trạng thái hiện tại khi ghi. Yêu cầu đồng thời trên cùng đơn phải bảo đảm chỉ hoàn tồn một lần khi hủy và đơn đã hủy không chuyển được sang `PAID`.

| Kiểm tra | Kết quả mong đợi |
| --- | --- |
| Quyền truy cập | Guest 401, Customer 403. Staff và Admin gọi được cả nhóm API khi dữ liệu hợp lệ. |
| A-04 | Tìm theo mã/tên/email, lọc đủ hai ngày biên, phân trang ổn định và mở được đơn có sản phẩm ẩn. |
| A-05 | Tăng/giảm đúng phần tồn chênh lệch và tính lại tổng tiền. Nếu thiếu tồn hoặc sai trạng thái, dữ liệu đơn và tồn kho giữ nguyên. |
| A-06 · TC-PAY-09 | Hủy đơn `NEW` còn `UNPAID`, hoàn đúng tồn kho. Yêu cầu hủy lặp không hoàn tồn thêm. |
| A-06 · TC-PAY-10 | Xác nhận chuyển khoản thành `PAID`. Xác nhận lại hoặc hủy đơn đã trả đều bị chặn theo quy tắc tương ứng. |
