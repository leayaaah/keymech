# Ánh xạ Use Case – chức năng – kiểm thử (Phương)

| UC | Vai trò | Chức năng/API | Kiểm thử đối chiếu |
|---|---|---|---|
| UC-CUS-03 | Customer | Tạo cấu hình Custom; `POST /api/custom-requests` | `PostUiApiIntegrationTests` – tạo yêu cầu, trạng thái ban đầu `PENDING` |
| UC-CUS-04 | Customer | Xem danh sách/chi tiết/lịch sử của mình; `GET .../my` | `PostUiApiIntegrationTests` – giới hạn quyền sở hữu, đọc lịch sử |
| UC-CUS-06 | Customer | Đồng ý giá hiện hành hoặc từ chối; `PUT .../accept`, `PUT .../decline` | `PostUiApiIntegrationTests` – `QUOTE_CHANGED`, chấp nhận đúng giá |
| UC-STAFF-07 | Staff/Admin | Xem yêu cầu, báo giá, cập nhật tiến độ; `/api/staff/custom-requests/**` | `PostUiApiIntegrationTests` – role matrix và luồng trạng thái đến `COMPLETED` |
| UC-TRK-01 | Guest/Customer | Ghi xem sản phẩm, tìm kiếm, thêm giỏ, mua hàng | `ActivityTrackerImplTest`, `CheckoutPurchaseIntegrationTests` |
| UC-CHAT-01 | Guest/Customer | ChatWidget gọi `/api/chat`; tracking cá nhân hóa là phần mở rộng | `ChatControllerTest`, `ChatRateLimiterTest` |

## Luồng Custom đã kiểm tra trong test tích hợp

`PENDING → QUOTED → ACCEPTED → IN_PROGRESS → COMPLETED`. Customer xác nhận giá; Staff thực hiện hai bước tiến độ cuối. Sau đó Customer tải lại chi tiết và thấy đủ bốn lần đổi trạng thái theo thứ tự mới nhất trước. Kiểm thử cũng xác nhận Customer khác không đọc được yêu cầu (`404`) và giá cũ bị từ chối (`409 QUOTE_CHANGED`).

## Kịch bản demo/ghi hình

1. Đăng nhập Customer, gửi cấu hình Custom và ghi lại mã yêu cầu cùng trạng thái `PENDING`.
2. Đăng nhập Staff, mở yêu cầu, báo giá rồi chuyển sang `QUOTED`.
3. Quay lại Customer, mở yêu cầu, đồng ý báo giá.
4. Đăng nhập Staff, lần lượt chuyển `IN_PROGRESS` rồi `COMPLETED`.
5. Quay lại Customer và cho thấy timeline có `QUOTED`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`.
6. Trên slide/báo cáo, chụp timeline và trích kết quả test. Không đưa mật khẩu hoặc token vào ảnh/video.

## Hồ sơ bàn giao Phương

- Hợp đồng endpoint, quyền, state machine và mã lỗi: [custom-request-api-contract.md](custom-request-api-contract.md).
- Bảng ánh xạ UC và kịch bản demo: tài liệu này.
- Kiểm thử luồng hoàn chỉnh: `backend/src/test/java/vn/edu/iuh/keymech/controller/PostUiApiIntegrationTests.java`.
- Hình cần chụp khi demo: form gửi yêu cầu, báo giá Staff, xác nhận Customer, timeline hoàn thành. Video cần ghi theo 6 bước trên môi trường nhóm.
