# Frontend KeyMech - Nền tảng TMĐT & Custom Bàn Phím Cơ (Nhóm 4)

Khung giao diện người dùng xây dựng bằng React 19, Vite, React Router 7 và Axios.

## Yêu cầu môi trường
- **Node.js**: Phiên bản 20 trở lên (khuyên dùng Node 20 LTS hoặc mới hơn).
- **npm**: Phiên bản 10 trở lên.
- **Backend Spring Boot**: Khởi chạy tại cổng `8080`.

## Hướng dẫn cài đặt và khởi chạy

```bash
cd frontend
npm install
npm run dev
```

Ứng dụng khởi chạy tại địa chỉ: `http://localhost:5173`.
Các yêu cầu tới `/api/*` và `/uploads/*` sẽ tự động được Vite proxy sang backend `http://localhost:8080` (tránh lỗi CORS và đồng bộ cookie Session).

## Lệnh kiểm tra code (Quy ước bắt buộc)

Trước khi mở Pull Request, chạy lệnh kiểm tra lint:

```bash
npm run lint
npm run build
```

---

## 4 Lưu ý quan trọng cho các thành viên (Thuận, Thùy, Phương, Trí)

1. **Thêm route**: Mọi người khai báo và gắn trang của mình tại `src/routes/AppRoutes.jsx`.
2. **Thêm API**: Mọi module API mới đặt trong `src/api/` (ví dụ `productApi.js`, `orderApi.js`, `cartApi.js`).
3. **Instance Axios dùng chung**: Tất cả các lời gọi API phải import và sử dụng qua `axiosClient` từ `src/api/axiosClient.js`; **tuyệt đối không dùng `fetch` rải rác** trong component.
4. **Bảo vệ tuyến đường**: `src/routes/ProtectedRoute.jsx` do người giữ khung (Trí) quản lý và cập nhật theo phân quyền SRS.

---

## Cấu trúc thư mục

```
frontend/src/
  api/          # axiosClient.js, authApi.js (quản lý kết nối backend)
  components/   # Component dùng chung: Loading, EmptyState, ErrorState, Toast
  context/      # AuthContext.jsx quản lý phiên người dùng
  hooks/        # useAuth.js
  layouts/      # GuestLayout, CustomerLayout, StaffLayout, AdminLayout
  pages/        # Phân trang theo vai trò: guest/, customer/, staff/, admin/
  routes/       # AppRoutes.jsx, ProtectedRoute.jsx
  utils/        # formatVND.js, formatDate.js, toast.js
```
