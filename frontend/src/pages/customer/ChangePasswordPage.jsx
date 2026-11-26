import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';

/**
 * Trang Đổi mật khẩu (chỉ Customer, Figma frame 3:566).
 * Kết nối PUT /api/users/me/password.
 */
export function ChangePasswordPage() {
  const { changePassword } = useAuth();

  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = () => {
    const next = {};

    if (!oldPassword) {
      next.oldPassword = 'Vui lòng nhập mật khẩu hiện tại.';
    }
    if (!newPassword) {
      next.newPassword = 'Vui lòng nhập mật khẩu mới.';
    } else if (newPassword.length < 8) {
      next.newPassword = 'Mật khẩu mới phải có ít nhất 8 ký tự.';
    } else if (newPassword === oldPassword) {
      next.newPassword = 'Mật khẩu mới không được trùng mật khẩu hiện tại.';
    }
    if (!confirmPassword) {
      next.confirmPassword = 'Vui lòng xác nhận mật khẩu mới.';
    } else if (confirmPassword !== newPassword) {
      next.confirmPassword = 'Mật khẩu xác nhận không trùng khớp.';
    }

    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!validate()) return;

    setSubmitting(true);
    try {
      await changePassword({ oldPassword, newPassword });
      toast.success('Cập nhật mật khẩu thành công!');
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setErrors({});
    } catch (err) {
      setServerError(
        err.response?.data?.message ||
          'Không thể đổi mật khẩu. Vui lòng kiểm tra lại mật khẩu hiện tại.'
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div>
        <h1 className="km-account-heading">Đổi mật khẩu</h1>
        <p className="km-auth-subtitle">Giữ tài khoản an toàn với mật khẩu mạnh.</p>
      </div>

      <form
        className="km-account-card"
        style={{ maxWidth: '650px' }}
        onSubmit={handleSubmit}
        noValidate
      >
        {serverError && (
          <div role="alert" className="km-alert-error">
            {serverError}
          </div>
        )}

        <div className="km-field">
          <label htmlFor="change-old-password" className="km-label">
            Mật khẩu hiện tại
          </label>
          <input
            id="change-old-password"
            type="password"
            autoComplete="current-password"
            className={`km-input ${errors.oldPassword ? 'km-input--error' : ''}`}
            placeholder="••••••••"
            value={oldPassword}
            onChange={(e) => {
              setOldPassword(e.target.value);
              if (errors.oldPassword) setErrors((p) => ({ ...p, oldPassword: '' }));
            }}
          />
          {errors.oldPassword && <p className="km-field-error">{errors.oldPassword}</p>}
        </div>

        <div className="km-field">
          <label htmlFor="change-new-password" className="km-label">
            Mật khẩu mới
          </label>
          <input
            id="change-new-password"
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
          <p className="km-field-hint">
            Ít nhất 8 ký tự. Nên kết hợp chữ, số và ký tự đặc biệt.
          </p>
          {errors.newPassword && <p className="km-field-error">{errors.newPassword}</p>}
        </div>

        <div className="km-field">
          <label htmlFor="change-confirm-password" className="km-label">
            Xác nhận mật khẩu mới
          </label>
          <input
            id="change-confirm-password"
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

        <div>
          <button type="submit" className="km-btn km-btn-primary" disabled={submitting}>
            {submitting ? 'Đang cập nhật...' : 'Cập nhật mật khẩu'}
          </button>
        </div>

        <p className="km-field-hint" style={{ fontSize: 'var(--text-sm)' }}>
          Sau khi đổi mật khẩu, hãy sử dụng mật khẩu mới cho lần đăng nhập tiếp theo.
        </p>
      </form>
    </>
  );
}

export default ChangePasswordPage;
