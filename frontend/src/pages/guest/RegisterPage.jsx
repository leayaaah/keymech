import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';

/**
 * Trang Đăng ký tài khoản Khách hàng (Customer).
 * Kết nối API POST /api/auth/register.
 */
export function RegisterPage() {
  const { register, user } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phone: '',
    address: '',
  });

  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  // Nếu đã đăng nhập thì không cần đăng ký
  if (user) {
    return (
      <div
        style={{
          maxWidth: '460px',
          margin: '3rem auto',
          padding: '2rem',
          backgroundColor: '#ffffff',
          borderRadius: '8px',
          textAlign: 'center',
          boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
        }}
      >
        <h2 style={{ fontSize: '1.25rem', marginBottom: '1rem', color: '#111827' }}>
          Bạn đã đăng nhập!
        </h2>
        <p style={{ color: '#4b5563', marginBottom: '1.5rem', fontSize: '0.925rem' }}>
          Tài khoản hiện tại của bạn là <strong>{user.email}</strong>.
          Vui lòng đăng xuất nếu muốn đăng ký tài khoản mới.
        </p>
        <Link
          to="/"
          style={{
            padding: '0.6rem 1.25rem',
            backgroundColor: '#2563eb',
            color: '#ffffff',
            borderRadius: '6px',
            textDecoration: 'none',
            fontWeight: 500,
          }}
        >
          Về trang chủ
        </Link>
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
      newErrors.fullName = 'Vui lòng nhập họ và tên của bạn.';
    } else if (formData.fullName.trim().length < 2) {
      newErrors.fullName = 'Họ và tên phải có ít nhất 2 ký tự.';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'Vui lòng nhập địa chỉ email.';
    } else if (!emailRegex.test(formData.email.trim())) {
      newErrors.email = 'Địa chỉ email không đúng định dạng.';
    }

    if (!formData.password) {
      newErrors.password = 'Vui lòng nhập mật khẩu.';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Mật khẩu phải có độ dài từ 8 ký tự trở lên.';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = 'Vui lòng xác nhận mật khẩu.';
    } else if (formData.confirmPassword !== formData.password) {
      newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp.';
    }

    if (formData.phone.trim() && !phoneRegex.test(formData.phone.trim())) {
      newErrors.phone = 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.';
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
      // Body gửi lên server chỉ chứa thông tin cá nhân, không có trường role
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
      const message =
        err.response?.data?.message ||
        'Đăng ký không thành công. Vui lòng kiểm tra lại thông tin.';
      setServerError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        maxWidth: '480px',
        margin: '2rem auto',
        padding: '2rem',
        backgroundColor: '#ffffff',
        borderRadius: '8px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      }}
    >
      <div style={{ textAlign: 'center', marginBottom: '1.75rem' }}>
        <h1
          style={{
            fontSize: '1.65rem',
            fontWeight: 800,
            color: '#111827',
            margin: '0 0 0.5rem 0',
          }}
        >
          Tạo tài khoản KeyMech
        </h1>
        <p style={{ color: '#6b7280', fontSize: '0.9rem', margin: 0 }}>
          Trở thành thành viên để mua sắm và nhận ưu đãi riêng
        </p>
      </div>

      {serverError && (
        <div
          role="alert"
          style={{
            padding: '0.75rem 1rem',
            backgroundColor: '#fef2f2',
            border: '1px solid #fecaca',
            color: '#991b1b',
            borderRadius: '6px',
            fontSize: '0.875rem',
            marginBottom: '1.25rem',
          }}
        >
          {serverError}
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        {/* Full Name */}
        <div style={{ marginBottom: '1.15rem' }}>
          <label
            htmlFor="register-fullname"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Họ và tên <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <input
            id="register-fullname"
            name="fullName"
            type="text"
            placeholder="Ví dụ: Nguyễn Văn An"
            value={formData.fullName}
            onChange={handleChange}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: `1px solid ${errors.fullName ? '#dc2626' : '#d1d5db'}`,
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
          {errors.fullName && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.fullName}
            </p>
          )}
        </div>

        {/* Email */}
        <div style={{ marginBottom: '1.15rem' }}>
          <label
            htmlFor="register-email"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Địa chỉ Email <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <input
            id="register-email"
            name="email"
            type="email"
            autoComplete="email"
            placeholder="nhapemail@example.com"
            value={formData.email}
            onChange={handleChange}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: `1px solid ${errors.email ? '#dc2626' : '#d1d5db'}`,
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
          {errors.email && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.email}
            </p>
          )}
        </div>

        {/* Phone */}
        <div style={{ marginBottom: '1.15rem' }}>
          <label
            htmlFor="register-phone"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Số điện thoại
          </label>
          <input
            id="register-phone"
            name="phone"
            type="tel"
            autoComplete="tel"
            placeholder="0912345678"
            value={formData.phone}
            onChange={handleChange}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: `1px solid ${errors.phone ? '#dc2626' : '#d1d5db'}`,
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
          {errors.phone && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.phone}
            </p>
          )}
        </div>

        {/* Password */}
        <div style={{ marginBottom: '1.15rem' }}>
          <label
            htmlFor="register-password"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Mật khẩu (≥ 8 ký tự) <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <div style={{ position: 'relative' }}>
            <input
              id="register-password"
              name="password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              placeholder="Tối thiểu 8 ký tự"
              value={formData.password}
              onChange={handleChange}
              style={{
                width: '100%',
                padding: '0.65rem 2.5rem 0.65rem 0.85rem',
                borderRadius: '6px',
                border: `1px solid ${errors.password ? '#dc2626' : '#d1d5db'}`,
                outline: 'none',
                fontSize: '0.95rem',
              }}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiển thị mật khẩu'}
              style={{
                position: 'absolute',
                right: '10px',
                top: '50%',
                transform: 'translateY(-50%)',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#6b7280',
                fontSize: '1rem',
                padding: '4px',
              }}
            >
              {showPassword ? '👁️' : '🔒'}
            </button>
          </div>
          {errors.password && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.password}
            </p>
          )}
        </div>

        {/* Confirm Password */}
        <div style={{ marginBottom: '1.15rem' }}>
          <label
            htmlFor="register-confirm-password"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Xác nhận mật khẩu <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <input
            id="register-confirm-password"
            name="confirmPassword"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Nhập lại mật khẩu"
            value={formData.confirmPassword}
            onChange={handleChange}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: `1px solid ${errors.confirmPassword ? '#dc2626' : '#d1d5db'}`,
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
          {errors.confirmPassword && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.confirmPassword}
            </p>
          )}
        </div>

        {/* Address */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label
            htmlFor="register-address"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Địa chỉ giao hàng
          </label>
          <textarea
            id="register-address"
            name="address"
            rows={2}
            placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành..."
            value={formData.address}
            onChange={handleChange}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: '1px solid #d1d5db',
              outline: 'none',
              fontSize: '0.95rem',
              resize: 'vertical',
            }}
          />
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={submitting}
          style={{
            width: '100%',
            padding: '0.75rem',
            backgroundColor: submitting ? '#93c5fd' : '#2563eb',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            fontSize: '1rem',
            fontWeight: 600,
            cursor: submitting ? 'not-allowed' : 'pointer',
            transition: 'background-color 0.2s',
          }}
        >
          {submitting ? 'Đang tạo tài khoản...' : 'Đăng ký tài khoản'}
        </button>
      </form>

      <div
        style={{
          marginTop: '1.5rem',
          textAlign: 'center',
          fontSize: '0.875rem',
          color: '#4b5563',
        }}
      >
        Đã có tài khoản?{' '}
        <Link
          to="/login"
          style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}
        >
          Đăng nhập
        </Link>
      </div>
    </div>
  );
}

export default RegisterPage;
