import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';

/**
 * Trang Đăng ký tài khoản Khách hàng (Figma frame 3:220).
 * Quy tắc: Mật khẩu ≥ 8 ký tự; Số điện thoại là tùy chọn.
 */
export function RegisterPage() {
  const { register, user } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    address: '',
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  if (user) {
    return (
      <div className="km-auth-wrap">
        <div className="km-auth-card" style={{ textAlign: 'center', alignItems: 'center' }}>
          <p className="km-auth-brand">KeyMech</p>
          <h1 className="km-auth-title">Bạn đã đăng nhập</h1>
          <p className="km-auth-subtitle">
            Tài khoản hiện tại: <strong>{user.email}</strong>. Vui lòng đăng xuất nếu muốn đăng ký mới.
          </p>
          <Link to="/" className="km-btn km-btn-primary">
            Về trang chủ
          </Link>
        </div>
      </div>
    );
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^0\d{9}$/;

    if (!formData.fullName.trim()) {
      newErrors.fullName = 'Vui lòng nhập họ và tên.';
    } else if (formData.fullName.trim().length < 2) {
      newErrors.fullName = 'Họ và tên phải có ít nhất 2 ký tự.';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Vui lòng nhập địa chỉ email.';
    } else if (!emailRegex.test(formData.email.trim())) {
      newErrors.email = 'Địa chỉ email không đúng định dạng.';
    }

    if (formData.phone.trim() && !phoneRegex.test(formData.phone.trim())) {
      newErrors.phone = 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.';
    }

    if (!formData.password) {
      newErrors.password = 'Vui lòng nhập mật khẩu.';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Mật khẩu phải có ít nhất 8 ký tự.';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu.';
    } else if (formData.confirmPassword !== formData.password) {
      newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

    if (!validate()) return;

    setSubmitting(true);
    try {
      await register({
        fullName: formData.fullName.trim(),
        email: formData.email.trim(),
        password: formData.password,
        phone: formData.phone.trim() || undefined,
        address: formData.address.trim() || undefined,
      });
      toast.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.');
      navigate('/login');
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.';
      setServerError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="km-auth-wrap">
      <div className="km-auth-card">
        <p className="km-auth-brand">KeyMech</p>
        <h1 className="km-auth-title">Tạo tài khoản mới</h1>
        <p className="km-auth-subtitle">Tham gia cộng đồng yêu phím cơ KeyMech.</p>

        {serverError && (
          <div role="alert" className="km-alert-error">
            {serverError}
          </div>
        )}

        <form className="km-form" onSubmit={handleSubmit} noValidate>
          <div className="km-field">
            <label htmlFor="register-fullname" className="km-label">
              Họ và tên <span className="km-required">*</span>
            </label>
            <input
              id="register-fullname"
              name="fullName"
              type="text"
              className={`km-input ${errors.fullName ? 'km-input--error' : ''}`}
              placeholder="Nguyễn Minh Anh"
              value={formData.fullName}
              onChange={handleChange}
            />
            {errors.fullName && <p className="km-field-error">{errors.fullName}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="register-email" className="km-label">
              Email <span className="km-required">*</span>
            </label>
            <input
              id="register-email"
              name="email"
              type="email"
              autoComplete="email"
              className={`km-input ${errors.email ? 'km-input--error' : ''}`}
              placeholder="minhanh@example.com"
              value={formData.email}
              onChange={handleChange}
            />
            {errors.email && <p className="km-field-error">{errors.email}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="register-phone" className="km-label">
              Số điện thoại
            </label>
            <input
              id="register-phone"
              name="phone"
              type="tel"
              autoComplete="tel"
              className={`km-input ${errors.phone ? 'km-input--error' : ''}`}
              placeholder="0901234567"
              value={formData.phone}
              onChange={handleChange}
            />
            {errors.phone && <p className="km-field-error">{errors.phone}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="register-password" className="km-label">
              Mật khẩu <span className="km-required">*</span>
            </label>
            <input
              id="register-password"
              name="password"
              type="password"
              autoComplete="new-password"
              className={`km-input ${errors.password ? 'km-input--error' : ''}`}
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
            />
            <p className="km-field-hint">Sử dụng ít nhất 8 ký tự.</p>
            {errors.password && <p className="km-field-error">{errors.password}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="register-confirm" className="km-label">
              Xác nhận mật khẩu <span className="km-required">*</span>
            </label>
            <input
              id="register-confirm"
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              className={`km-input ${errors.confirmPassword ? 'km-input--error' : ''}`}
              placeholder="••••••••"
              value={formData.confirmPassword}
              onChange={handleChange}
            />
            {errors.confirmPassword && <p className="km-field-error">{errors.confirmPassword}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="register-address" className="km-label">
              Địa chỉ giao hàng
            </label>
            <textarea
              id="register-address"
              name="address"
              rows={2}
              className="km-textarea"
              placeholder="Số nhà, đường, quận/huyện, tỉnh/thành..."
              value={formData.address}
              onChange={handleChange}
            />
          </div>

          <button type="submit" className="km-btn km-btn-primary km-btn-full" disabled={submitting}>
            {submitting ? 'Đang tạo tài khoản...' : 'Đăng ký'}
          </button>
        </form>

        <Link to="/login" className="km-auth-link">
          Đã có tài khoản? Đăng nhập
        </Link>

        <p className="km-auth-footer-note">
          Giỏ hàng của bạn được giữ nguyên sau khi đăng nhập.
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;
