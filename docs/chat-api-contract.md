# Hợp đồng API Chatbot giai đoạn A — G-04

Giai đoạn A dùng quy tắc nội bộ, không gọi Gemini. API công khai cho mọi vai trò; giao diện chỉ hiển thị nút Chatbot cho Guest và Customer. Hội thoại nằm trong Session, tối đa 20 tin.

## POST /api/chat

Body JSON:

    { "message": "Phím 75% dưới 2 triệu" }

`message` bắt buộc, tối đa 500 ký tự. Thành công trả `200`:

    {
      "answer": "Đây là các sản phẩm đang hiển thị phù hợp với thông tin bạn cung cấp. Giá và tình trạng hàng được lấy từ cửa hàng.",
      "products": [
        { "id": 5, "name": "Bàn phím 75%", "price": 1800000,
          "imageUrl": "/uploads/keyboard.jpg", "inStock": true, "url": "/products/5" }
      ],
      "suggestions": ["Phím 75% dưới 2 triệu", "Switch linear", "Tư vấn Custom"],
      "mode": "rule"
    }

Chỉ trả sản phẩm và danh mục đang `ACTIVE`. Giá, tồn kho và liên kết lấy từ CSDL. Câu ngoài phạm vi trả lời xin lỗi, `products` rỗng. Sai body trả `400 VALIDATION_ERROR`; quá 10 câu trong một phút mỗi Session hoặc 200 câu mỗi giờ toàn ứng dụng trả `429 RATE_LIMITED`.

## GET /api/chat/history

Trả `200` với mảng tin nhắn trong Session, ví dụ:

    [{ "role": "user", "content": "Phím 75% dưới 2 triệu" },
     { "role": "assistant", "content": "Đây là các sản phẩm đang hiển thị phù hợp..." }]

Phiên mới trả `[]`. Lịch sử chỉ lưu vai trò và nội dung, không lưu trong CSDL.
