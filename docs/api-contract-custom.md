# Hợp đồng API: Custom, Tracking, Chatbot (phần của Phương)

Tiền tố `/api`, JSON UTF-8, thuộc tính `camelCase`. Đổi hợp đồng này phải báo cả nhóm trước khi merge (CODING_CONVENTIONS mục 8 và 13). Phiên bản v1, soạn 13/11/2026.

Lỗi trả về luôn dạng `{ "status", "error", "message", "timestamp" }`. Phân trang dạng `{ "content", "page", "size", "totalElements", "totalPages" }` (`page` từ 0, `size` tối đa 50, mặc định 10, mới nhất trước).

## 1. Customer: `/api/custom-requests/**` (chỉ `CUSTOMER`)

Guest nhận 401; Staff và Admin nhận 403.

| Phương thức | Đường dẫn | Chức năng | Thành công |
| --- | --- | --- | --- |
| POST | `/api/custom-requests` | C-03 gửi yêu cầu | 201 `CustomRequestResponse` |
| GET | `/api/custom-requests/my?page&size` | C-04 danh sách của tôi | 200 phân trang |
| GET | `/api/custom-requests/my/{id}` | C-04 chi tiết và tiến độ | 200 `CustomRequestDetailResponse` |
| PUT | `/api/custom-requests/my/{id}/accept` | C-06 đồng ý báo giá | 200 `CustomRequestResponse` |
| PUT | `/api/custom-requests/my/{id}/decline` | C-06 hủy yêu cầu hoặc từ chối báo giá | 200 `CustomRequestResponse` |

### POST `/api/custom-requests`

Họ tên, email, số điện thoại lấy từ tài khoản. Trạng thái luôn `PENDING`, client không gửi. Trường tùy chọn không nhập thì gửi `null` (không gửi chuỗi rỗng).

```json
{
  "layout": "75%",
  "switchType": "Linear - Gateron Yellow",
  "caseMaterial": "Nhôm",
  "keycapSet": "PBT cherry",
  "lubing": true,
  "hotSwap": true,
  "rgbOption": "RGB",
  "budget": 3000000,
  "note": "Muốn gõ êm"
}
```

| Trường | Bắt buộc | Quy tắc (lỗi 400 `VALIDATION_ERROR`) |
| --- | --- | --- |
| `layout` | có | Một trong `60%`, `65%`, `75%`, `TKL`, `Fullsize` |
| `switchType` | có | Không rỗng, tối đa 80 ký tự (giao diện ghép loại và tên: `Linear - Gateron Yellow`) |
| `caseMaterial` | không | `Nhựa`, `Nhôm`, `Gỗ`, `Acrylic` |
| `keycapSet` | không | Tối đa 120 ký tự |
| `lubing` | không | Mặc định `false` |
| `hotSwap` | không | Mặc định `true` |
| `rgbOption` | không | `Không`, `RGB`, `Đơn sắc` |
| `budget` | không | Số nguyên đồng, từ 500.000 trở lên nếu có |
| `note` | không | Tối đa 1000 ký tự |

### Đối tượng trả về

```jsonc
// CustomRequestResponse (phía Customer không có khối "customer")
{
  "id": 1, "requestCode": "CUS-7K2M9QX4AB", "layout": "75%", "caseMaterial": "Nhôm",
  "switchType": "Linear - Gateron Yellow", "keycapSet": "PBT cherry",
  "lubing": true, "hotSwap": true, "rgbOption": "RGB", "budget": 3000000, "note": "Muốn gõ êm",
  "quotedPrice": null, "status": "PENDING",
  "createdAt": "2026-11-20T09:15:00", "updatedAt": "2026-11-20T09:15:00"
}

// CustomRequestDetailResponse
{
  "request": { /* CustomRequestResponse */ },
  "history": [
    { "id": 1, "newStatus": "PENDING", "actor": "Bạn", "changedAt": "2026-11-20T09:15:00" },
    { "id": 2, "oldStatus": "PENDING", "newStatus": "QUOTED", "note": "Đã tính linh kiện",
      "actor": "Cửa hàng", "changedAt": "2026-11-20T14:02:00" }
  ],
  "allowedNext": ["ACCEPTED", "CANCELLED"]
}
```

- `history[].actor`: phía Customer chỉ là `Bạn` hoặc `Cửa hàng` (không lộ tên Staff). `oldStatus` vắng ở dòng khởi tạo.
- `allowedNext`: các trạng thái người xem được phép chuyển tới, để giao diện không tự lặp lại bảng chuyển trạng thái.

### PUT `.../accept`

Body `{ "quotedPrice": 2500000 }` là giá khách đang xem. Chỉ khi yêu cầu đang `QUOTED`.

### PUT `.../decline`

Body `{ "note": "..." }` (có thể bỏ trống, tối đa 500 ký tự). Chỉ khi `PENDING` (hủy yêu cầu) hoặc `QUOTED` (từ chối báo giá, ghi chú mặc định "Khách từ chối báo giá"). Kết quả đều là `CANCELLED`.

## 2. Staff và Admin: `/api/staff/custom-requests/**` (`STAFF`, `ADMIN`)

Customer nhận 403, Guest nhận 401. Phía này `CustomRequestResponse` có thêm khối `customer: { id, fullName, email, phone }` và `history[].actor` là `Họ tên (ROLE)`.

| Phương thức | Đường dẫn | Chức năng | Thành công |
| --- | --- | --- | --- |
| GET | `/api/staff/custom-requests?status&page&size` | A-07 danh sách, lọc trạng thái | 200 phân trang |
| GET | `/api/staff/custom-requests/{id}` | A-07 chi tiết và lịch sử | 200 `CustomRequestDetailResponse` |
| PUT | `/api/staff/custom-requests/{id}/quote` | A-07 nhập hoặc thay báo giá | 200 `CustomRequestResponse` |
| PUT | `/api/staff/custom-requests/{id}/status` | A-07 chuyển trạng thái | 200 `CustomRequestResponse` |

> **Thay đổi so với bảng API ở kế hoạch bản 3:** thêm `GET /api/staff/custom-requests/{id}` (kế hoạch chưa có, nhưng trang Staff cần xem lịch sử và thông số). Không cần sửa `SecurityConfig` vì đã nằm trong `/api/staff/**`.

- `quote`: body `{ "quotedPrice": 2800000 }`, giá là số nguyên đồng và > 0. Chỉ khi `PENDING` hoặc `QUOTED`; **không tự đổi trạng thái**; báo lại giá khi đang `QUOTED` thì khách phải đồng ý đúng giá mới.
- `status`: body `{ "status": "QUOTED", "note": "..." }`. `status` sai giá trị enum thì 400.

## 3. Bảng chuyển trạng thái và quyền

| Từ | Sang | Ai làm | Lỗi nếu vi phạm |
| --- | --- | --- | --- |
| `PENDING` | `QUOTED` | Staff, Admin (phải có `quotedPrice`) | 409 `QUOTE_REQUIRED` |
| `PENDING` | `REJECTED` | Staff, Admin | |
| `QUOTED` | `ACCEPTED` | Customer chủ yêu cầu (qua `accept`) | Staff làm thì 403; giá lệch thì 409 `QUOTE_CHANGED` |
| `PENDING`, `QUOTED` | `CANCELLED` | Customer chủ yêu cầu (qua `decline`), Staff, Admin | |
| `ACCEPTED` | `IN_PROGRESS` | Staff, Admin | |
| `IN_PROGRESS` | `COMPLETED` | Staff, Admin | |
| `ACCEPTED`, `IN_PROGRESS` | `CANCELLED` | Staff, Admin | Customer làm thì 409 `INVALID_TRANSITION` |

Thứ tự kiểm tra: không tồn tại (404), không phải chủ yêu cầu (403), sai sơ đồ (409 `INVALID_TRANSITION`), sai người thực hiện (403), thiếu giá (409 `QUOTE_REQUIRED`). `COMPLETED`, `REJECTED`, `CANCELLED` là trạng thái cuối. Mỗi lần chuyển ghi một dòng `custom_request_history` trong cùng transaction; yêu cầu mới có dòng đầu `PENDING` (không có `oldStatus`). Dòng yêu cầu bị khóa (`SELECT ... FOR UPDATE`) khi đổi trạng thái hoặc báo giá để hai request đồng thời không ghi đè nhau.

## 4. Mã lỗi

| HTTP | `error` | Khi nào |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Sai dữ liệu form, body JSON sai, enum sai, tham số query sai kiểu |
| 401 | `UNAUTHORIZED` | Chưa đăng nhập |
| 403 | `FORBIDDEN` | Sai vai trò, hoặc xem, sửa yêu cầu của người khác |
| 404 | `NOT_FOUND` | Không có yêu cầu với id đó |
| 409 | `INVALID_TRANSITION`, `QUOTE_REQUIRED`, `QUOTE_CHANGED` | Vi phạm nghiệp vụ như bảng trên |

## 5. Tracking: `ActivityTracker` (gọi trong code, không có endpoint)

Không có endpoint nhận sự kiện từ client (TC-TRK-06). Gọi từ luồng của request, truyền `HttpSession` hiện tại:

| Ai gọi | Khi nào | Lời gọi |
| --- | --- | --- |
| Thuận | `GET /api/products/{id}` thành công | `tracker.recordView(session, productId)` |
| Thuận | `GET /api/products` có `keyword` | `tracker.recordSearch(session, keyword, categoryId)` |
| Thuận | `POST /api/cart/items` thành công | `tracker.recordAddToCart(session, productId)` |
| Thùy | **Sau khi** `placeOrder` commit | `tracker.recordPurchase(session, productIds)` |
| Trí | Customer đăng nhập thành công, sau `changeSessionId()` | `tracker.linkVisitorToUser(session, userId)` |

Quy tắc: bất đồng bộ, lỗi chỉ ghi log; bỏ qua Staff và Admin; `visitor_id` (UUID) giữ trong Session; `VIEW_PRODUCT` cùng sản phẩm trong 60 giây của một phiên chỉ ghi một lần; từ khóa được chuẩn hóa và cắt còn 100 ký tự; `category_id` tự điền từ sản phẩm nếu không truyền.

`ProfileService.getProfile(session)` (Thùy gọi trong luồng Chatbot) trả `InterestProfile`: `topCategoryIds` (tối đa 2), `topLayout`, `topSwitchType`, `topBrand`, `averagePrice`, `recentProducts` (tối đa 3), `recentKeywords` (tối đa 5). Rỗng với Staff, Admin, Guest chưa có hành vi, hoặc khi `app.chatbot.personalization=false`. Không chứa dữ liệu định danh nên đưa vào prompt được.

## 6. Chatbot widget (G-04): giả định cần Thùy xác nhận

Kế hoạch mục 6 chỉ nêu nội dung phản hồi, chưa chốt tên trường. `ChatWidget` đang dựa trên:

- `POST /api/chat` body `{ "message": "..." }` (tối đa 500 ký tự).
- Phản hồi `{ "answer": "...", "products": [{ "id", "name", "price", "imageUrl", "inStock", "url" }], "suggestions": ["..."], "mode": "ai" | "rule" }`.
- `GET /api/chat/history` trả mảng `[{ "role": "user" | "assistant", "content": "..." }]` (hoặc `{ "messages": [...] }`).
- `429 RATE_LIMITED` thì hiển thị `message` của server.

Nếu tên trường khác, chỉ cần sửa `frontend/src/api/chatApi.js` và hàm `normalizeHistory` trong `ChatWidget.jsx`.
