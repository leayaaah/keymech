import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';

/**
 * Trang Đăng nhập hệ thống KeyMech (Figma frame 3:198).
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

  if (user) {
    return (
      <div className="km-auth-wrap">
        <div className="km-auth-card" style={{ textAlign: 'center', alignItems: 'center' }}>
          <p className="km-auth-brand">KeyMech</p>
          <h1 className="km-auth-title">Bạn đã đăng nhập</h1>
          <p className="km-auth-subtitle">
            Tài khoản hiện tại: <strong>{user.fullName || user.email}</strong> ({user.role})
          </p>
          <Link
            to={user.role === 'ADMIN' ? '/admin' : user.role === 'STAFF' ? '/staff' : '/'}
            className="km-btn km-btn-primary"
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
      window.dispatchEvent(new CustomEvent('keymech:cart-updated'));
      toast.success('Đăng nhập thành công!');

      const redirect = new URLSearchParams(location.search).get('redirect');
      const isOrderPath =
        redirect === '/checkout' ||
        /^\/customer\/orders(?:\/\d+(?:\/success)?)?$/.test(redirect ?? '');
      const from =
        location.state?.from?.pathname ||
        (loggedUser.role === 'CUSTOMER' && isOrderPath ? redirect : null);

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
        err.response?.data?.message || 'Email hoặc mật khẩu không đúng';
      setServerError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="km-auth-wrap">
      <div className="km-auth-card">
        <p className="km-auth-brand">KeyMech</p>
        <h1 className="km-auth-title">Chào mừng trở lại</h1>
        <p className="km-auth-subtitle">Đăng nhập để tiếp tục hành trình đam mê.</p>

        <form className="km-form" onSubmit={handleSubmit} noValidate>
          <div className="km-field">
            <label htmlFor="login-email" className="km-label">
              Email <span className="km-required">*</span>
            </label>
            <input
              id="login-email"
              type="email"
              autoComplete="email"
              className={`km-input ${errors.email ? 'km-input--error' : ''}`}
              placeholder="minhanh@example.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (errors.email) setErrors((prev) => ({ ...prev, email: '' }));
              }}
            />
            {errors.email && <p className="km-field-error">{errors.email}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="login-password" className="km-label">
              Mật khẩu <span className="km-required">*</span>
            </label>
            <div className="km-password-wrap">
              <input
                id="login-password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                className={`km-input ${errors.password ? 'km-input--error' : ''}`}
                placeholder="••••••••"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (errors.password) setErrors((prev) => ({ ...prev, password: '' }));
                }}
              />
              <button
                type="button"
                className="km-password-toggle"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiển thị mật khẩu'}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                  {showPassword ? (
                    <>
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                      <path d="M1 1l22 22" />
                    </>
                  ) : (
                    <>
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </>
                  )}
                </svg>
              </button>
            </div>
            {errors.password && <p className="km-field-error">{errors.password}</p>}
          </div>

          {serverError && (
            <div role="alert" className="km-alert-error">
              {serverError}
            </div>
          )}

          <Link to="/forgot-password" className="km-auth-link km-auth-link--sm">
            Quên mật khẩu?
          </Link>

          <button type="submit" className="km-btn km-btn-primary km-btn-full" disabled={submitting}>
            {submitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <Link to="/register" className="km-auth-link">
          Chưa có tài khoản? Đăng ký
        </Link>

        <p className="km-auth-footer-note">
          Giỏ hàng của bạn được giữ nguyên sau khi đăng nhập.
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
