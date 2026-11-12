import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';

/**
 * Trang Đăng nhập hệ thống KeyMech.
 * Kết nối API POST /api/auth/login.
 */
export function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  // Nếu người dùng đã đăng nhập, hiển thị thông báo chuyển tiếp
  if (user) {
    return (
      <div
        style={{
          maxWidth: '440px',
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
          Tài khoản hiện tại: <strong>{user.fullName || user.email}</strong> ({user.role})
        </p>
        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
          <Link
            to={user.role === 'ADMIN' ? '/admin' : user.role === 'STAFF' ? '/staff' : '/'}
            style={{
              padding: '0.6rem 1.25rem',
              backgroundColor: '#2563eb',
              color: '#ffffff',
              borderRadius: '6px',
              textDecoration: 'none',
              fontWeight: 500,
            }}
          >
            Đi đến trang chính
          </Link>
        </div>
      </div>
    );
  }

  const validate = () => {
    const newErrors = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email.trim()) {
      newErrors.email = 'Vui lòng nhập địa chỉ email.';
    } else if (!emailRegex.test(email.trim())) {
      newErrors.email = 'Địa chỉ email không đúng định dạng.';
    }

    if (!password) {
      newErrors.password = 'Vui lòng nhập mật khẩu.';
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
      const loggedUser = await login(email.trim(), password);
      toast.success('Đăng nhập thành công!');

      // Điều hướng theo trạng thái chuyển trang trước đó hoặc theo vai trò
      const from = location.state?.from?.pathname;
      if (from && from !== '/login') {
        navigate(from, { replace: true });
      } else if (loggedUser.role === 'ADMIN') {
        navigate('/admin', { replace: true });
      } else if (loggedUser.role === 'STAFF') {
        navigate('/staff', { replace: true });
      } else {
        navigate('/', { replace: true });
      }
    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại.';
      setServerError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        maxWidth: '440px',
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
          Đăng nhập KeyMech
        </h1>
        <p style={{ color: '#6b7280', fontSize: '0.9rem', margin: 0 }}>
          Truy cập tài khoản để mua sắm và theo dõi đơn hàng
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
        {/* Email field */}
        <div style={{ marginBottom: '1.25rem' }}>
          <label
            htmlFor="login-email"
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
            id="login-email"
            type="email"
            autoComplete="email"
            placeholder="nhapemail@example.com"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              if (errors.email) setErrors((prev) => ({ ...prev, email: '' }));
            }}
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
            <p style={{ color: '#dc2626', fontSize: '0.8rem', marginTop: '0.25rem', margin: '0.25rem 0 0 0' }}>
              {errors.email}
            </p>
          )}
        </div>

        {/* Password field */}
        <div style={{ marginBottom: '1.5rem' }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '0.35rem',
            }}
          >
            <label
              htmlFor="login-password"
              style={{
                fontSize: '0.875rem',
                fontWeight: 600,
                color: '#374151',
              }}
            >
              Mật khẩu <span style={{ color: '#dc2626' }}>*</span>
            </label>
          </div>
          <div style={{ position: 'relative' }}>
            <input
              id="login-password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => {
                setPassword(e.target.value);
                if (errors.password)
                  setErrors((prev) => ({ ...prev, password: '' }));
              }}
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

        {/* Submit button */}
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
          {submitting ? 'Đang xác thực...' : 'Đăng nhập'}
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
        Chưa có tài khoản?{' '}
        <Link
          to="/register"
          style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}
        >
          Đăng ký ngay
        </Link>
      </div>
    </div>
  );
}

export default LoginPage;
