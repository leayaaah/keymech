import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import authApi from '../../api/authApi';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';

const CODE_TTL_SECONDS = 10 * 60;
const RESEND_COOLDOWN_SECONDS = 60;
const MAX_WRONG_ATTEMPTS = 5;

function formatTimer(sec) {
  const m = String(Math.floor(sec / 60)).padStart(2, '0');
  const s = String(sec % 60).padStart(2, '0');
  return `${m}:${s}`;
}

/**
 * Trang Đặt lại mật khẩu bằng mã xác thực 6 chữ số (Figma frame 3:264).
 * Mã có hiệu lực 10 phút, tối đa 5 lần nhập sai.
 */
export function ResetPasswordPage() {
  const location = useLocation();
  const navigate = useNavigate();

  const [email, setEmail] = useState(location.state?.email || '');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [wrongAttempts, setWrongAttempts] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  const [ttl, setTtl] = useState(CODE_TTL_SECONDS);
  const [resendCooldown, setResendCooldown] = useState(RESEND_COOLDOWN_SECONDS);
  const [resending, setResending] = useState(false);

  useEffect(() => {
    if (ttl <= 0) return;
    const id = setInterval(() => setTtl((t) => Math.max(0, t - 1)), 1000);
    return () => clearInterval(id);
  }, [ttl]);

  useEffect(() => {
    if (resendCooldown <= 0) return;
    const id = setInterval(() => setResendCooldown((c) => Math.max(0, c - 1)), 1000);
    return () => clearInterval(id);
  }, [resendCooldown]);

  const validate = () => {
    const next = {};
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!email.trim() || !emailRegex.test(email.trim())) {
      next.email = 'Vui lòng nhập địa chỉ email hợp lệ.';
    }
    if (!/^\d{6}$/.test(code.trim())) {
      next.code = 'Mã xác thực phải gồm đúng 6 chữ số.';
    }
    if (!newPassword || newPassword.length < 8) {
      next.newPassword = 'Mật khẩu mới phải có ít nhất 8 ký tự.';
    }
    if (confirmPassword !== newPassword) {
      next.confirmPassword = 'Mật khẩu xác nhận không khớp.';
    }

    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleResend = async () => {
    if (resendCooldown > 0 || resending || !email.trim()) return;
    setResending(true);
    try {
      await authApi.forgotPassword({ email: email.trim() });
    } catch {
      // Thông báo trung lập
    } finally {
      setResending(false);
      setTtl(CODE_TTL_SECONDS);
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
      setWrongAttempts(0);
      toast.info('Nếu email tồn tại, mã xác thực mới đã được gửi.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

    if (ttl <= 0) {
      setServerError('Mã xác thực đã hết hạn. Vui lòng yêu cầu mã mới.');
      return;
    }
    if (wrongAttempts >= MAX_WRONG_ATTEMPTS) {
      setServerError('Bạn đã nhập sai 5/5 lần. Vui lòng yêu cầu mã mới.');
      return;
    }
    if (!validate()) return;

    setSubmitting(true);
    try {
      await authApi.resetPassword({
        email: email.trim(),
        code: code.trim(),
        newPassword,
      });
      toast.success('Đặt lại mật khẩu thành công! Vui lòng đăng nhập lại.');
      navigate('/login', { replace: true });
    } catch (err) {
      const msg =
        err.response?.data?.message || 'Mã xác thực không đúng hoặc đã hết hạn.';
      setWrongAttempts((n) => Math.min(MAX_WRONG_ATTEMPTS, n + 1));
      setServerError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="km-auth-wrap">
      <div className="km-auth-card">
        <p className="km-auth-brand">KeyMech</p>
        <h1 className="km-auth-title">Đặt lại mật khẩu</h1>
        <p className="km-auth-subtitle">Mã xác thực có hiệu lực trong 10 phút.</p>

        <form className="km-form" onSubmit={handleSubmit} noValidate>
          <div className="km-field">
            <label htmlFor="reset-email" className="km-label">
              Email <span className="km-required">*</span>
            </label>
            <input
              id="reset-email"
              type="email"
              autoComplete="email"
              className={`km-input ${errors.email ? 'km-input--error' : ''}`}
              placeholder="minhanh@example.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (errors.email) setErrors((p) => ({ ...p, email: '' }));
              }}
            />
            {errors.email && <p className="km-field-error">{errors.email}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="reset-code" className="km-label">
              Mã xác thực 6 chữ số <span className="km-required">*</span>
            </label>
            <input
              id="reset-code"
              type="text"
              inputMode="numeric"
              maxLength={6}
              className={`km-input ${errors.code ? 'km-input--error' : ''}`}
              placeholder="123456"
              value={code}
              onChange={(e) => {
                setCode(e.target.value.replace(/\D/g, '').slice(0, 6));
                if (errors.code) setErrors((p) => ({ ...p, code: '' }));
              }}
            />
            {errors.code && <p className="km-field-error">{errors.code}</p>}
          </div>

          <p className="km-countdown">
            {ttl > 0 ? `Mã hết hạn sau ${formatTimer(ttl)}` : 'Mã xác thực đã hết hạn.'}
          </p>

          <button
            type="button"
            className="km-btn km-btn-soft"
            disabled={resendCooldown > 0 || resending || !email.trim()}
            onClick={handleResend}
          >
            {resendCooldown > 0
              ? `Gửi lại mã (${resendCooldown} giây)`
              : resending
                ? 'Đang gửi lại...'
                : 'Gửi lại mã'}
          </button>

          {wrongAttempts > 0 && (
            <p className="km-field-error">
              Bạn đã nhập sai {wrongAttempts}/{MAX_WRONG_ATTEMPTS} lần. Sau {MAX_WRONG_ATTEMPTS} lần sai, vui lòng yêu cầu mã mới.
            </p>
          )}

          {serverError && (
            <div role="alert" className="km-alert-error">
              {serverError}
            </div>
          )}

          <div className="km-field">
            <label htmlFor="reset-new-password" className="km-label">
              Mật khẩu mới <span className="km-required">*</span>
            </label>
            <input
              id="reset-new-password"
              type="password"
              autoComplete="new-password"
              className={`km-input ${errors.newPassword ? 'km-input--error' : ''}`}
              placeholder="••••••••"
              value={newPassword}
              onChange={(e) => {
                setNewPassword(e.target.value);
                if (errors.newPassword) setErrors((p) => ({ ...p, newPassword: '' }));
              }}
            />
            <p className="km-field-hint">Sử dụng ít nhất 8 ký tự.</p>
            {errors.newPassword && <p className="km-field-error">{errors.newPassword}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="reset-confirm-password" className="km-label">
              Xác nhận mật khẩu <span className="km-required">*</span>
            </label>
            <input
              id="reset-confirm-password"
              type="password"
              autoComplete="new-password"
              className={`km-input ${errors.confirmPassword ? 'km-input--error' : ''}`}
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                if (errors.confirmPassword) setErrors((p) => ({ ...p, confirmPassword: '' }));
              }}
            />
            {errors.confirmPassword && <p className="km-field-error">{errors.confirmPassword}</p>}
          </div>

          <button
            type="submit"
            className="km-btn km-btn-primary km-btn-full"
            disabled={submitting || wrongAttempts >= MAX_WRONG_ATTEMPTS || ttl <= 0}
          >
            {submitting ? 'Đang cập nhật...' : 'Đổi mật khẩu'}
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

export default ResetPasswordPage;
