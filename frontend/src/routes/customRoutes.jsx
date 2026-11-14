// Các route của module Custom, để Trí gộp vào cây route chính (src/routes).
// Mỗi người tự thêm route của mình (mục 6 phân công); ProtectedRoute do Trí giữ.
//
// Cách dùng (ví dụ trong AppRoutes.jsx):
//   import { customerCustomRoutes, staffCustomRoutes } from './customRoutes';
//   <Route element={<ProtectedRoute roles={['CUSTOMER']} />}>{customerCustomRoutes}</Route>
//   <Route element={<ProtectedRoute roles={['STAFF', 'ADMIN']} />}>{staffCustomRoutes}</Route>
import { Route } from 'react-router-dom';
import CustomRequestDetailPage from '../pages/customer/CustomRequestDetailPage';
import CustomRequestFormPage from '../pages/customer/CustomRequestFormPage';
import MyCustomRequestsPage from '../pages/customer/MyCustomRequestsPage';
import StaffCustomRequestDetailPage from '../pages/staff/StaffCustomRequestDetailPage';
import StaffCustomRequestsPage from '../pages/staff/StaffCustomRequestsPage';

export const customerCustomRoutes = [
  <Route key="cr-new" path="/custom-requests/new" element={<CustomRequestFormPage />} />,
  <Route key="cr-list" path="/custom-requests" element={<MyCustomRequestsPage />} />,
  <Route key="cr-detail" path="/custom-requests/:id" element={<CustomRequestDetailPage />} />,
];

export const staffCustomRoutes = [
  <Route key="scr-list" path="/staff/custom-requests" element={<StaffCustomRequestsPage />} />,
  <Route key="scr-detail" path="/staff/custom-requests/:id" element={<StaffCustomRequestDetailPage />} />,
];
