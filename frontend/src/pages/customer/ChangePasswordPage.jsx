import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { toast } from '../../utils/toast';

/**
 * Trang Đổi mật khẩu tài khoản (dành cho Customer đã đăng nhập).
 * Kết nối API PUT /api/users/me/password.
 */
export function ChangePasswordPage() {
  const { changePassword } = useAuth();

  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = () => {
    const newErrors = {};

    if (!oldPassword) {
      newErrors.oldPassword = 'Vui lòng nhập mật khẩu hiện tại.';
    }

    if (!newPassword) {
      newErrors.newPassword = 'Vui lòng nhập mật khẩu mới.';
    } else if (newPassword.length < 8) {
      newErrors.newPassword = 'Mật khẩu mới phải có ít nhất 8 ký tự.';
    } else if (newPassword === oldPassword) {
      newErrors.newPassword = 'Mật khẩu mới không được trùng với mật khẩu hiện tại.';
    }

    if (!confirmPassword) {
      newErrors.confirmPassword = 'Vui lòng xác nhận lại mật khẩu mới.';
    } else if (confirmPassword !== newPassword) {
      newErrors.confirmPassword = 'Mật khẩu xác nhận không trùng khớp.';
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
      await changePassword({ oldPassword, newPassword });
      toast.success('Đổi mật khẩu thành công!');
      setOldPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setErrors({});
    } catch (err) {
      const message =
        err.response?.data?.message ||
        'Không thể đổi mật khẩu. Vui lòng kiểm tra lại mật khẩu hiện tại.';
      setServerError(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: '480px' }}>
      <h1
        style={{
          fontSize: '1.4rem',
          fontWeight: 700,
          color: '#111827',
          marginBottom: '0.5rem',
        }}
      >
        Đổi mật khẩu
      </h1>
      <p style={{ color: '#4b5563', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
        Để bảo vệ tài khoản của bạn, vui lòng không chia sẻ mật khẩu cho người khác. Mật khẩu mới cần từ 8 ký tự trở lên.
      </p>

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
        {/* Old password */}
        <div style={{ marginBottom: '1.25rem' }}>
          <label
            htmlFor="change-old-password"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Mật khẩu hiện tại <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <input
            id="change-old-password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            placeholder="Nhập mật khẩu đang dùng"
            value={oldPassword}
            onChange={(e) => {
              setOldPassword(e.target.value);
              if (errors.oldPassword)
                setErrors((prev) => ({ ...prev, oldPassword: '' }));
            }}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: `1px solid ${errors.oldPassword ? '#dc2626' : '#d1d5db'}`,
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
          {errors.oldPassword && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.oldPassword}
            </p>
          )}
        </div>

        {/* New password */}
        <div style={{ marginBottom: '1.25rem' }}>
          <label
            htmlFor="change-new-password"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Mật khẩu mới (≥ 8 ký tự) <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <input
            id="change-new-password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Tối thiểu 8 ký tự"
            value={newPassword}
            onChange={(e) => {
              setNewPassword(e.target.value);
              if (errors.newPassword)
                setErrors((prev) => ({ ...prev, newPassword: '' }));
            }}
            style={{
              width: '100%',
              padding: '0.65rem 0.85rem',
              borderRadius: '6px',
              border: `1px solid ${errors.newPassword ? '#dc2626' : '#d1d5db'}`,
              outline: 'none',
              fontSize: '0.95rem',
            }}
          />
          {errors.newPassword && (
            <p style={{ color: '#dc2626', fontSize: '0.8rem', margin: '0.25rem 0 0 0' }}>
              {errors.newPassword}
            </p>
          )}
        </div>

        {/* Confirm new password */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label
            htmlFor="change-confirm-password"
            style={{
              display: 'block',
              fontSize: '0.875rem',
              fontWeight: 600,
              color: '#374151',
              marginBottom: '0.35rem',
            }}
          >
            Xác nhận mật khẩu mới <span style={{ color: '#dc2626' }}>*</span>
          </label>
          <input
            id="change-confirm-password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="new-password"
            placeholder="Nhập lại mật khẩu mới"
            value={confirmPassword}
            onChange={(e) => {
              setConfirmPassword(e.target.value);
              if (errors.confirmPassword)
                setErrors((prev) => ({ ...prev, confirmPassword: '' }));
            }}
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

        {/* Show password checkbox */}
        <div style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <input
            id="toggle-change-password-visibility"
            type="checkbox"
            checked={showPassword}
            onChange={(e) => setShowPassword(e.target.checked)}
            style={{ cursor: 'pointer' }}
          />
          <label
            htmlFor="toggle-change-password-visibility"
            style={{ fontSize: '0.875rem', color: '#4b5563', cursor: 'pointer' }}
          >
            Hiển thị mật khẩu
          </label>
        </div>

        {/* Submit button */}
        <button
          type="submit"
          disabled={submitting}
          style={{
            padding: '0.7rem 1.5rem',
            backgroundColor: submitting ? '#93c5fd' : '#2563eb',
            color: '#ffffff',
            border: 'none',
            borderRadius: '6px',
            fontSize: '0.95rem',
            fontWeight: 600,
            cursor: submitting ? 'not-allowed' : 'pointer',
          }}
        >
          {submitting ? 'Đang cập nhật...' : 'Lưu mật khẩu mới'}
        </button>
      </form>
    </div>
  );
}

export default ChangePasswordPage;
