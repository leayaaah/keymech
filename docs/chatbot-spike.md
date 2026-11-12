# Spike Chatbot G-04 — 13/11/2026

## Môi trường và phạm vi

- Phiên bản đã được Trí chốt: Spring Boot 4.0.0, Spring AI 2.0.0, JDK 21.
- MariaDB `mariadb:11.8` trong container riêng, cơ sở dữ liệu `keymech_spike`; không dùng dữ liệu MariaDB phát triển.
- Code thử đặt trong `backend/target/chatbot-spike/` (Git bỏ qua); không đưa vào nhánh chính.
- Khóa `GEMINI_API_KEY` đọc từ `.env` vào biến môi trường của tiến trình, không ghi vào source, log hay commit.
- Năm `Document` mô phỏng sản phẩm seed ID 1–5; nội dung chỉ gồm đặc điểm sản phẩm, không có giá, tồn kho hoặc dữ liệu định danh.

## Kết quả

| Bước | Kết quả |
| --- | --- |
| Biên dịch harness với ba starter Spring AI 2.0.0 | Đạt |
| Khởi tạo `MariaDBVectorStore` với `initialize-schema=true`, `COSINE`, 768 chiều | Đạt |
| Dùng `01-schema.sql` hiện tại với `initialize-schema=false` trên container mới | Khởi động, lưu 5 document và tìm kiếm `topK=3` thành công; bảng không bị đổi |
| Gemini `gemini-embedding-001` | Trả vector 768 chiều |
| Lưu 5 document vào `vector_store` | 5 dòng |
| `similaritySearch` với `topK=3` | Trả productId 2, 5, 3 cho câu hỏi bàn phím nhỏ gọn dùng switch linear |
| `VEC_DISTANCE_COSINE(embedding, embedding)` | 0 trên 5 dòng; xác nhận vector có thể truy vấn |
| `ChatClient` với `gemini-2.5-flash` hoặc `gemini-2.5-flash-lite` | API trả 404: hai model không cấp cho khóa này |
| Lượt đầu với `gemini-3.8-flash` và `gemini-3.5-flash-lite` | Không trả kết quả sau hơn 50 giây mỗi lần; tiến trình thử được dừng thủ công, không có HTTP error để xác định nguyên nhân |
| `ChatClient` tách khỏi vector store, dùng `gemini-3.5-flash-lite` | Thành công: câu ngắn 2,3 giây; câu tư vấn có ngữ cảnh 4,6 và 14,7 giây |
| Chuỗi đủ 5 document → tìm kiếm → `ChatClient`, dùng `gemini-3.5-flash-lite` | Thành công; riêng lời gọi chat mất 11,3 giây |
| `ChatClient` với `thinking-level=MINIMAL`, `max-output-tokens=128` | Câu tư vấn tách riêng thành công sau 2,4 và 6,5 giây; chuỗi đầy đủ thành công, riêng chat mất 2,4 giây |
| Gọi REST `generateContent` trực tiếp với `gemini-3.5-flash-lite` | Thành công: một lượt `v1beta` khoảng 19 giây, một lượt `v1` khoảng 5,8 giây |

**Nguyên nhân đã xác nhận:** model `gemini-2.5-flash` trong cấu hình dự thảo trả 404 với khóa hiện tại; `gemini-2.5-flash-lite` cũng vậy. `ChatClient` và MariaDB Vector không bị lỗi kết nối cố định vì các lượt thử lại đã chạy trọn vẹn. Độ trễ của `gemini-3.5-flash-lite` thay đổi theo lượt; log của các lượt bị dừng không đủ để quy nguyên nhân cụ thể cho retry, mạng hay dịch vụ Gemini. Hai lượt với `thinking-level=MINIMAL` dưới 8 giây chỉ là mẫu nhỏ, chưa bảo đảm ngưỡng này khi demo.

**Kết quả phục vụ M1:** Gemini embedding, MariaDB Vector và `ChatClient` đều chạy được với `01-schema.sql` hiện tại. Chưa cần chuyển sang `SimpleVectorStore` vì vector store không lỗi. Khi triển khai G-04, cần chọn model chat mà khóa được phép dùng, timeout 8 giây và đường lui sang `RuleBasedChatbotService`; đo thêm trên máy thứ hai trước khi chốt AI cho demo. Nếu quy định không cho dùng `VEC_DISTANCE` dựng sẵn, phương án dự phòng là `SimpleVectorStore` theo kế hoạch.

**Quyết định cấu hình ngày 13/11:** dùng `gemini-3.5-flash-lite` làm model chat mặc định trong `application.yml` và các ví dụ tài liệu. Các dòng thử model 2.5 ở trên được giữ để giải thích lỗi 404. Đây chưa phải bằng chứng bảo đảm mục tiêu 8 giây; timeout và bản dự phòng vẫn bắt buộc.

## DDL thực tế của `vector_store`

Kết quả `SHOW CREATE TABLE vector_store` sau khi Spring AI 2.0.0 khởi tạo trên MariaDB 11.8:

```sql
CREATE TABLE `vector_store` (
  `id` uuid NOT NULL DEFAULT uuid(),
  `content` text DEFAULT NULL,
  `metadata` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metadata`)),
  `embedding` vector(768) NOT NULL,
  PRIMARY KEY (`id`),
  VECTOR KEY `vector_store_embedding_idx` (`embedding`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
```

So với bảng tạo từ `database/01-schema.sql`, DDL do thư viện sinh có thêm `DEFAULT uuid()`, tên vector index khác, không ghi tường minh `DISTANCE='cosine'` và dùng collation khác. Cả hai bảng đều được MariaDB biểu diễn cột `JSON` thành `longtext` với `CHECK (json_valid(...))`; đây không phải khác biệt riêng của DDL thư viện. Quy định nhóm không tự viết Function/Check, nên cần hỏi nhóm và giảng viên cách đánh giá các cấu trúc do MariaDB hoặc thư viện sinh ra. **Chưa sửa `01-schema.sql`** và ứng dụng vẫn dùng `initialize-schema=false`. Schema hiện tại đã qua thử nghiệm lưu/tìm vector trên CSDL mới; chỉ sửa schema nếu nhóm quyết định cần đồng bộ DDL, và phải báo cả nhóm trước khi merge.

## Lưu ý cấu hình Spring AI 2.0.0

- Embedding auto-configuration cần `spring.ai.google.genai.embedding.api-key=${GEMINI_API_KEY}` riêng; chỉ đặt `spring.ai.google.genai.api-key` khiến khởi động báo thiếu `project-id`.
- Harness dùng `spring.ai.google.genai.embedding.text.model=gemini-embedding-001` và `spring.ai.google.genai.embedding.text.dimensions=768`. Cấu trúc `embedding.text.options` trong `application.yml` hiện tại cần rà lại khi bật starter.
- `gemini-2.5-flash` trong cấu hình cũ không dùng được với khóa thử này. `gemini-3.5-flash-lite` đã chạy qua `ChatClient` với Spring AI 2.0.0; cấu hình `thinking-level=MINIMAL`, `max-output-tokens=128` cải thiện các lượt đo nhỏ nhưng chưa được đặt làm mặc định vì cần xác nhận chất lượng câu trả lời và đo lặp.

Tham khảo: [MariaDB Vector Store](https://docs.spring.io/spring-ai/reference/api/vectordbs/mariadb.html), [Google GenAI Embeddings](https://docs.spring.io/spring-ai/reference/api/embeddings/google-genai-embeddings-text.html), [Google GenAI Chat](https://docs.spring.io/spring-ai/reference/api/chat/google-genai-chat.html).
