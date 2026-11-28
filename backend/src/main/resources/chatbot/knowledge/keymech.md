# Kiến thức tư vấn KeyMech

## Layout bàn phím
Fullsize thường có cụm phím số. TKL bỏ cụm phím số nhưng giữ hàng phím chức năng và cụm điều hướng riêng. Layout 75% thường giữ hàng chức năng và phím mũi tên trong thân gọn hơn TKL. Layout 65% thường giữ phím mũi tên, còn 60% thường dùng tổ hợp phím cho chức năng thiếu. Bố trí cụ thể phụ thuộc từng mẫu.

## Switch
Linear di chuyển đều, không có khấc phản hồi rõ rệt. Tactile có khấc để cảm nhận điểm nhấn. Clicky tạo tiếng click rõ, phù hợp khi tiếng gõ không ảnh hưởng người khác. Khi cần gõ êm, cân nhắc switch silent và xem thêm vật liệu, cấu trúc của từng bàn phím.

## Switch theo tên gọi và độ êm
Red thường là linear, nhấn đều, không khấc và không có tiếng click. Brown thường là tactile, có khấc nhẹ. Blue thường là clicky, có khấc và tiếng click rõ. Red thường êm hơn Blue, nhưng switch Red thông thường vẫn phát ra tiếng khi gõ. Tên màu là quy ước tùy hãng. Khi xem mẫu cụ thể, dựa vào `switchType` và cờ `silent` trong thông số sản phẩm (`specs`). Nếu dữ liệu thiếu cờ `silent`, cần hỏi thêm để xác nhận. Khi cần giảm tiếng gõ, ưu tiên mẫu được công bố là silent.

## Keycap và khả năng tương thích
Keycap gắn trên stem của switch. Kiểm tra kiểu stem, bố trí ANSI/ISO và độ dài các phím đặc biệt như Space, Shift và Enter. Chỉ biết tỷ lệ layout là chưa đủ. ABS và PBT là hai vật liệu phổ biến. Profile OEM và Cherry có độ cao và góc khác nhau.

## PCB hot-swap, mạch hàn và chân switch
PCB hot-swap có socket để tháo và lắp switch tương thích mà không cần hàn. Với PCB hàn, vẫn có thể thay switch bằng cách tháo mối hàn cũ và hàn switch mới. Switch cơ kiểu MX 5 chân có thêm hai chân nhựa định vị, cần hai lỗ tương ứng trên PCB. Cần biết model và phiên bản bàn phím hoặc PCB cùng mẫu switch để đối chiếu chuẩn switch và số chân được hỗ trợ.

## Dịch vụ Custom
Khách hàng đăng nhập, gửi yêu cầu về layout, switch, màu sắc và ngân sách trong trang Custom. Nhân viên xem yêu cầu và gửi báo giá. Khách hàng xem báo giá trước khi quyết định đồng ý hoặc từ chối.

## Giao hàng, bảo hành và đổi trả
Hỏi bộ phận hỗ trợ để xác nhận chính sách phù hợp với sản phẩm và đơn hàng. Chatbot tư vấn thông tin chung và không kiểm tra trạng thái giao hàng hay bảo hành của từng đơn.
