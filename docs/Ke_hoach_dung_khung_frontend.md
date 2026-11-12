# Kế hoạch dựng khung frontend – Web TMĐT bàn phím cơ (Nhóm 4)

Người giữ khung: **Trí**. Mốc bàn giao: **15/11/2026 (M1)**.

Căn cứ: `CODING_CONVENTIONS.md` (mục 10), `Phan_cong_Nhom4_deadline_30-11.md`, Kế hoạch bản 3 (danh sách API, bảng quyền).

---

## Bước 1. Khởi tạo dự án

```bash
npm create vite@latest frontend -- --template react
cd frontend
npm i axios react-router-dom
npm i -D eslint prettier eslint-config-prettier
```

- [ ] Thêm `.editorconfig` ở gốc kho (JS/JSX 2 dấu cách, LF, UTF-8, cuối file có một dòng trống).
- [ ] Cài ESLint và Prettier (quy ước ghi là đề xuất, nên báo nhóm trước khi chốt).
- [ ] Thêm script `lint` vào `package.json`; cả nhóm chạy `npm run lint` trước khi mở PR.

## Bước 2. Cấu hình môi trường và proxy

- [ ] Tạo `.env` với `VITE_API_URL=/api` và file mẫu `.env.example`.
- [ ] Trong `vite.config.js`, đặt proxy `/api` và `/uploads` sang `http://localhost:8080`.
- [ ] Không viết cứng `localhost` trong code.

Lý do: dev dùng chung origin nên tránh phần lớn lỗi CORS và cookie (rủi ro R3). Khi nộp, bản build được gộp vào `static` của Spring.

## Bước 3. Tạo cấu trúc thư mục

```
src/
  api/        axiosClient.js, authApi.js
  components/ Loading, EmptyState, ErrorState, Toast
  context/    AuthContext.jsx
  hooks/      useAuth.js
  pages/      guest/  customer/  staff/  admin/
  routes/     AppRoutes.jsx, ProtectedRoute.jsx
  utils/      formatVND.js, formatDate.js
```

Quy ước đặt tên: component `PascalCase.jsx`, hook `useXxx.js`, tiện ích `camelCase.js`. Dùng component hàm và hooks.

## Bước 4. Dựng instance Axios dùng chung

File: `src/api/axiosClient.js`

- [ ] `baseURL` lấy từ `VITE_API_URL`.
- [ ] Bật `withCredentials: true` để gửi cookie Session.
- [ ] Interceptor phản hồi:

| Mã | Xử lý |
| --- | --- |
| `401` | Về trang đăng nhập (trừ lời gọi `/auth/me` và `/auth/login`) |
| `403` | Báo "Bạn không đủ quyền" |
| `409`, `429` | Hiện `message` từ server |

- Lỗi server có dạng `{status, error, message, timestamp}`.
- Mọi lời gọi API nằm trong `src/api/`, không dùng `fetch` rải rác trong component.

> **Cần hỏi Trí:** `GET /api/auth/me` mở cho mọi người, nên với Guest có thể trả 401. Nếu interceptor chuyển hướng ở đây thì Guest bị đẩy về trang đăng nhập ngay khi vào web. Cần chốt backend trả gì cho Guest.

## Bước 5. Viết tiện ích dùng chung

- [ ] `formatVND`: `Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' })`.
- [ ] Một hàm định dạng ngày giờ dùng chung.

Toàn nhóm chỉ dùng hai hàm này để hiển thị tiền và ngày giờ.

## Bước 6. Dựng AuthContext

File: `src/context/AuthContext.jsx` (kèm `useAuth.js`)

- [ ] State: `user` và `loading`.
- [ ] Khi tải trang, gọi `GET /api/auth/me` để khôi phục phiên.
- [ ] `login(email, password)` gọi `POST /api/auth/login`, rồi cập nhật `user`.
- [ ] `logout()` gọi `POST /api/auth/logout`, rồi xóa `user`.
- [ ] Vai trò lấy từ `user.role`: `CUSTOMER`, `STAFF`, `ADMIN`. Guest là `user = null`.
- [ ] Không lưu mật khẩu, mã xác thực hay dữ liệu cá nhân ở `localStorage` hoặc `sessionStorage`.

Backend đăng nhập của Trí merge trước 13/11. Nếu chưa xong thì mock tạm, không chờ.

## Bước 7. Dựng ProtectedRoute

File: `src/routes/ProtectedRoute.jsx`, nhận prop `roles`.

- [ ] Đang `loading`: hiện spinner.
- [ ] Chưa đăng nhập: về `/login`.
- [ ] Sai vai trò: hiện trang "Không đủ quyền".

Phân vùng theo bảng quyền:

| Khu vực | Ai vào được |
| --- | --- |
| Giỏ hàng | Guest và Customer |
| Đơn hàng, Custom, hồ sơ cá nhân | Chỉ Customer |
| `/staff/**` | Staff và Admin |
| `/admin/**` | Chỉ Admin |

Lưu ý: ẩn nút trên giao diện không thay cho kiểm quyền ở backend. Việc Admin vào khu Staff vẫn cần xác nhận lại với SRS.

## Bước 8. Dựng routes và layout

File: `src/routes/AppRoutes.jsx`

- [ ] 4 layout: cửa hàng (Guest), Customer, Staff, Admin.
- [ ] Mỗi module một trang trống để mọi người gắn vào.
- [ ] Biểu tượng giỏ hàng và Chatbot chỉ hiện ở layout cửa hàng, chỉ với Guest và Customer.

Quy ước: mỗi người tự thêm route của mình; `ProtectedRoute` chỉ Trí sửa.

## Bước 9. Dựng component dùng chung

- [ ] Ba trạng thái cho trang danh sách: đang tải, rỗng ("Không tìm thấy sản phẩm"), lỗi.
- [ ] Thành phần hiển thị thông báo (Toast) cho lỗi `409` và `429`.

Dựng một lần ở `components/` để Thuận, Thùy và Phương dùng lại.

## Bước 10. Dựng trang xác thực

| Trang | API | Ghi chú |
| --- | --- | --- |
| Đăng ký | `POST /api/auth/register` | Mật khẩu ≥ 8 ký tự; email trùng báo lỗi rõ; đã đăng nhập thì 403 |
| Đăng nhập | `POST /api/auth/login` | Sai thông tin hoặc tài khoản bị ẩn: báo lỗi chung |
| Đổi mật khẩu | `PUT /api/users/me/password` | Gồm `oldPassword`, `newPassword` |
| Quên mật khẩu (Could) | `POST /api/auth/forgot-password`, `POST /api/auth/reset-password` | Làm sau cùng, nếu còn thời gian |

- [ ] Validate ở client giống quy tắc ở server.
- [ ] Thông báo lỗi bằng tiếng Việt.
- [ ] Sau khi đăng nhập với Customer, gọi `GET /api/cart` để đồng bộ giỏ.

## Bước 11. Kiểm tra toàn bộ

- [ ] `npm install && npm run dev` chạy trên máy cả nhóm.
- [ ] Gọi thử `/api/products` qua proxy, không lỗi CORS.
- [ ] Đăng nhập bằng 3 vai trò: Customer thấy giỏ hàng, Staff và Admin thì không.
- [ ] Vào `/admin` bằng tài khoản Customer: bị chặn.
- [ ] Giao diện chạy được từ 360px; ảnh có `alt`; nút và ô nhập có nhãn.
- [ ] `npm run lint` không lỗi.

## Bước 12. Bàn giao cho nhóm (M1)

- [ ] Viết README: cách chạy frontend (Node.js 20 trở lên, cổng 5173).
- [ ] Nhắn nhóm 4 điều:
  1. Thêm route ở `AppRoutes.jsx`.
  2. Thêm API vào `src/api/`.
  3. Mọi lời gọi đi qua Axios chung, không dùng `fetch`.
  4. `ProtectedRoute` chỉ Trí sửa.
- [ ] Báo xong khung để Thuận, Thùy, Phương gắn trang của mình.

---

## Rủi ro cần canh

| Rủi ro | Cách xử lý |
| --- | --- |
| Lỗi CORS và cookie khi tách frontend, backend | Dùng proxy Vite khi dev; gộp bản build vào Spring khi nộp; tắt CSRF ở dev và ghi rõ trong báo cáo |
| Khung trễ hạn 15/11, chặn các bạn khác | Các bạn có thể code trang trên khung Vite trống rồi ghép route sau |
| Bật lại CSRF (Axios gửi `X-XSRF-TOKEN`) | Mức Could, chỉ thử từ Sprint 4 |
