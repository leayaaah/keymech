import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authApi from '../../api/authApi';
import '../../styles/auth.css';

const NEUTRAL_MESSAGE =
  'Nếu email tồn tại trong hệ thống, bạn sẽ nhận được mã xác thực. Vui lòng kiểm tra hộp thư.';

/**
 * Trang Quên mật khẩu (Figma frame 3:242).
 * Luôn hiển thị thông báo trung lập dù email có tồn tại hay không.
 */
export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const trimmed = email.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!trimmed) {
      setError('Vui lòng nhập địa chỉ email.');
      return;
    }
    if (!emailRegex.test(trimmed)) {
      setError('Địa chỉ email không đúng định dạng.');
      return;
    }

    setSubmitting(true);
    try {
      await authApi.forgotPassword({ email: trimmed });
    } catch {
      // Quy tắc bảo mật: luôn hiện thông báo trung lập, không lộ email tồn tại hay không
    } finally {
      setSubmitting(false);
      setSubmitted(true);
    }
  };

  return (
    <div className="km-auth-wrap">
      <div className="km-auth-card">
        <p className="km-auth-brand">KeyMech</p>
        <h1 className="km-auth-title">Quên mật khẩu?</h1>
        <p className="km-auth-subtitle">Nhập email để nhận mã xác thực.</p>

        <form className="km-form" onSubmit={handleSubmit} noValidate>
          <div className="km-field">
            <label htmlFor="forgot-email" className="km-label">
              Email <span className="km-required">*</span>
            </label>
            <input
              id="forgot-email"
              type="email"
              autoComplete="email"
              className={`km-input ${error ? 'km-input--error' : ''}`}
              placeholder="minhanh@example.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (error) setError('');
              }}
            />
            {error && <p className="km-field-error">{error}</p>}
          </div>

          {submitted && (
            <div role="status" className="km-alert-info">
              {NEUTRAL_MESSAGE}
            </div>
          )}

          <button type="submit" className="km-btn km-btn-primary km-btn-full" disabled={submitting}>
            {submitting ? 'Đang gửi...' : 'Gửi mã xác thực'}
          </button>

          {submitted && (
            <button
              type="button"
              className="km-btn km-btn-soft km-btn-full"
              onClick={() => navigate('/reset-password', { state: { email: email.trim() } })}
            >
              Nhập mã xác thực & Đặt lại mật khẩu
            </button>
          )}
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

export default ForgotPasswordPage;
