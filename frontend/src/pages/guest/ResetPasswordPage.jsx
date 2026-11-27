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
 * Server là nguồn kiểm tra chính xác cho hạn 10 phút và giới hạn 5 lần nhập sai.
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
  const [needsNewCode, setNeedsNewCode] = useState(false);
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
      setNeedsNewCode(false);
      setServerError('');
      toast.info('Nếu email tồn tại, mã xác thực mới đã được gửi.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');

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
      const status = err?.response?.status;
      const errorCode = err?.response?.data?.error;
      const serverMsg = err?.response?.data?.message;

      if (status === 400 && errorCode === 'INVALID_CODE') {
        const msg = serverMsg || 'Mã xác thực không đúng hoặc đã hết hạn.';
        setServerError(msg);
        if (/hết hạn|quá nhiều lần|yêu cầu mã mới/i.test(msg)) {
          setNeedsNewCode(true);
          setResendCooldown(0);
        } else {
          setWrongAttempts((n) => Math.min(MAX_WRONG_ATTEMPTS, n + 1));
        }
      } else {
        setServerError('Có lỗi xảy ra, vui lòng thử lại');
      }
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
            {ttl > 0
              ? `Thời gian tham khảo: còn ${formatTimer(ttl)}`
              : 'Mã có thể đã hết hạn, bạn có thể gửi lại mã mới.'}
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

          {wrongAttempts > 0 && !needsNewCode && (
            <p className="km-field-error">
              Bạn đã nhập sai mã {wrongAttempts}/{MAX_WRONG_ATTEMPTS} lần. Sau {MAX_WRONG_ATTEMPTS} lần sai, vui lòng yêu cầu mã mới.
            </p>
          )}

          {serverError && (
            <div role="alert" className="km-alert-error">
              <p style={{ margin: 0 }}>{serverError}</p>
              {needsNewCode && (
                <button
                  type="button"
                  className="km-btn km-btn-soft km-btn-sm"
                  style={{ marginTop: '8px' }}
                  disabled={resending || !email.trim()}
                  onClick={handleResend}
                >
                  Gửi lại mã
                </button>
              )}
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

          <button type="submit" className="km-btn km-btn-primary km-btn-full" disabled={submitting}>
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
