# AGENTS.md — KeyMech

Hướng dẫn này áp dụng cho repo KeyMech. Tài liệu chi tiết nằm trong docs/; chỉ đọc các mục liên quan đến nhiệm vụ. Kiểm tra nhánh, working tree và code hiện tại trước khi kết luận hoặc sửa.

## Quy trình cho một nhiệm vụ

1. Xác định chức năng và phạm vi từ yêu cầu, kế hoạch và code. Hỏi khi thiếu thông tin làm thay đổi quyết định quan trọng; không dừng chỉ vì người dùng không nêu mã chức năng.
2. Đọc phần liên quan trong bảng tài liệu bên dưới. Đối chiếu với code và trạng thái Git vì tài liệu mô tả ý định, còn code cho biết trạng thái hiện tại.
3. Với thay đổi nhiều file, nêu ngắn gọn hướng làm. Hoàn thành phần việc người dùng đã giao và kiểm tra phù hợp.
4. Nếu phát hiện tài liệu và code lệch nhau, nêu chỗ lệch. Sửa trong phạm vi đã được giao khi hướng xử lý rõ; hỏi khi cần chọn giữa các yêu cầu mâu thuẫn hoặc thay đổi quyết định của nhóm.
5. Báo cáo file đã đổi, kết quả kiểm tra thực tế, giới hạn còn lại và việc cần phối hợp trước khi merge.

## Bản đồ tài liệu

- Phạm vi và tiêu chí chấp nhận theo mã G/C/A: docs/Ke_hoach_du_an_Web_TMDT_ban_phim_co_Nhom_4_v3.md, mục 3. Đọc thêm mục 1–2 khi cần quyết định nền.
- Ưu tiên, người phụ trách, sprint và thứ tự tích hợp theo hạn 30/11: docs/Phan_cong_Nhom4_deadline_30-11.md, mục 3–7. Tài liệu này thay mốc thời gian cũ của kế hoạch bản 3.
- Quy tắc code, test, Git, API và Definition of Done: docs/CODING_CONVENTIONS.md, mục đúng với phần đang làm; trước PR xem mục 3, 13–14.
- Backend và cấu hình nền: docs/KHUNG_DU_AN_SPRING.md, bước liên quan. Ví dụ trong tài liệu có thể cũ; package thật trong code là vn.edu.iuh.keymech.
- Frontend, Axios, auth, route và component chung: docs/Ke_hoach_dung_khung_frontend.md, bước liên quan.
- Đơn hàng C-02, C-04, A-04 đến A-06: docs/order-api-contract.md cho request, response và mã lỗi; kế hoạch mục 8 cho giỏ, giao dịch và tồn kho.
- Schema: database/01-schema.sql và kế hoạch mục 5.
- Endpoint và quyền: kế hoạch mục 6–7.
- Custom: kế hoạch mục 9. Chatbot, email và tracking: kế hoạch mục 10; xem thêm docs/chatbot-spike.md khi chọn model hoặc vector store.
- Test case, đóng gói và triển khai: kế hoạch mục 11; cách chạy hiện tại: README.md.

Nếu có SRS của nhóm, dùng SRS làm chuẩn nghiệp vụ, rồi đến kế hoạch bản 3. Hợp đồng API là nguồn chi tiết cho endpoint đã chốt. Khi các nguồn mâu thuẫn, nêu rõ mâu thuẫn và chỉ hỏi nếu không thể giải quyết bằng yêu cầu hiện tại.

## Thực tế của repo và lệnh kiểm tra

- Backend dùng Spring Boot, JDK 21 và package vn.edu.iuh.keymech; kiểm tra backend/pom.xml trước khi dựa vào phiên bản Spring AI hay thư viện.
- Backend hiện chưa có Maven wrapper. Dùng Maven đã cài: trong backend/ chạy mvn test hoặc mvn spring-boot:run. Nếu Maven không có trên PATH, báo cách chạy thay thế đã xác minh; không ghi rằng đã test khi chưa chạy.
- Frontend: trong frontend/ chạy npm ci, npm run lint và npm run build khi phần thay đổi liên quan.
- Test tích hợp cần MariaDB; kiểm tra môi trường trước khi chạy. Nêu rõ lỗi do code hay do thiếu môi trường.

## Ranh giới kỹ thuật cần giữ

- Vai trò CUSTOMER, STAFF, ADMIN độc lập; Guest chưa đăng nhập. Staff/Admin không dùng giỏ, mua hàng hay Custom. Kiểm quyền ở backend, không chỉ ẩn nút.
- Giỏ là Map<productId, quantity> trong HttpSession; không lưu giá và không ghi CSDL. Giá và tồn kho luôn đọc lại từ CSDL.
- Không xóa vật lý dữ liệu nghiệp vụ. Truy vấn công khai chỉ trả sản phẩm/danh mục ACTIVE. Dùng BigDecimal cho tiền.
- Đặt hàng và điều chỉnh tồn kho phải có transaction; trừ tồn bằng UPDATE có điều kiện rồi kiểm tra số dòng. Xóa giỏ, gửi email và ghi PURCHASE sau commit.
- API trả DTO, lỗi theo dạng status/error/message/timestamp, thông báo tiếng Việt. Không đưa mật khẩu, khóa, mã xác thực hay dữ liệu cá nhân vào log, prompt, response hoặc Git.
- Chatbot lấy giá và tồn kho từ CSDL, có timeout và bản dự phòng theo kế hoạch. Test không gọi Gemini hay SMTP thật.

## Phối hợp và hoàn thành

- Xem bảng sở hữu module ở docs/Phan_cong_Nhom4_deadline_30-11.md mục 6 để tránh đụng việc. Nếu nhiệm vụ người dùng đã giao cần sửa file dùng chung hoặc module của người khác, thực hiện phần cần thiết và nêu rõ để nhóm review; chỉ hỏi trước khi có quyết định chưa được giao hoặc nguy cơ thay đổi ngoài phạm vi.
- Thay đổi schema, hợp đồng API, phân quyền, thư viện/phiên bản hoặc phạm vi chức năng phải được báo nhóm theo docs/CODING_CONVENTIONS.md mục 13. Nếu người dùng đã giao rõ thay đổi, chuẩn bị code, test và tài liệu để review; không tự coi việc báo nhóm là lý do bỏ dở.
- Viết test có giá trị cho code nghiệp vụ và lỗi sửa; ưu tiên test phân quyền, tồn kho và chuyển trạng thái. Đối chiếu mức nới test đến 30/11 trong tài liệu phân công; chưa chạy được thì báo rõ, không đánh dấu hoàn thành.
- Không push trực tiếp lên main. PR cần người khác duyệt và chạy thử, merge bằng squash theo quy ước nhóm. Không commit, push hoặc merge khi yêu cầu hiện tại chỉ là kiểm tra hay tư vấn.
- Không commit .env, khóa API, mật khẩu, node_modules/, target/, dist/, log hoặc file upload phát sinh. Đã lộ khóa thì báo ngay để đổi khóa.
