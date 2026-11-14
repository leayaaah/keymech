# Câu hỏi gửi giảng viên (Nhóm 4): bản nháp

Soạn bởi Phương, 13/11/2026. Cần trả lời trước mốc M1 (15/11). Mỗi câu nêu rõ nhóm đang làm gì để thầy/cô chỉ cần xác nhận hoặc sửa.

**Kính gửi thầy/cô,** nhóm 4 xin hỏi ba điểm để chốt thiết kế đồ án web bán bàn phím cơ (React, Spring Boot, MariaDB).

1. **Vai trò Staff.** Đề gốc có 3 vai trò (Guest, Customer, Admin). Theo SRS và Use Case, nhóm tách thêm Staff để xử lý sản phẩm, đơn hàng và yêu cầu Custom; Admin tạo tài khoản Staff, Staff không mua hàng. Nhóm có được dùng 4 vai trò này không? Nếu không, nhóm sẽ gộp Staff vào Admin.

2. **Chatbot dùng vector search.** Nhóm không tự viết Function, Stored Procedure, Check hay Trigger. Tuy vậy MariaDB Vector Store (thư viện Spring AI) gọi hàm có sẵn `VEC_DISTANCE` khi tìm kiếm ngữ nghĩa. Việc này có vi phạm quy định "không dùng Function" không? Nếu có, nhóm dùng `SimpleVectorStore` lưu trong bộ nhớ, bỏ bảng `vector_store`.

3. **Internet khi demo.** Chatbot gọi Gemini API bên ngoài nên cần Internet và API key. Nơi demo có Internet ổn định không? Nếu không, nhóm demo chế độ dự phòng `rule` (trả lời theo từ khóa, truy vấn trực tiếp CSDL, không gọi Gemini).

Nhóm cũng xin xác nhận thêm (nếu tiện): ngày nộp chính thức, định dạng báo cáo và slide, có bắt buộc sơ đồ UML không.

Trân trọng cảm ơn thầy/cô.
