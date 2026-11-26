import { useEffect, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import authApi from '../../api/authApi';
import userApi from '../../api/userApi';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import { toast } from '../../utils/toast';
import '../../styles/auth.css';
import '../../styles/customer.css';

/**
 * Trang Hồ sơ cá nhân Customer (Figma frame 3:562).
 * Tải thông tin từ GET /api/auth/me; lưu qua PUT /api/users/me rồi làm mới phiên.
 */
export function CustomerProfilePage() {
  const { refreshUser } = useAuth();

  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [email, setEmail] = useState('');
  const [savedProfile, setSavedProfile] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;
    authApi
      .getMe()
      .then((data) => {
        if (!active) return;
        setFullName(data?.fullName || '');
        setPhone(data?.phone || '');
        setAddress(data?.address || '');
        setEmail(data?.email || '');
        setSavedProfile({
          fullName: data?.fullName?.trim() || '',
          phone: data?.phone?.trim() || null,
          address: data?.address?.trim() || null,
        });
        setError('');
      })
      .catch((err) => {
        if (!active) return;
        setError(
          err?.response?.data?.message || 'Không thể tải thông tin hồ sơ. Vui lòng thử lại.'
        );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [reloadKey]);

  const profile = {
    fullName: fullName.trim(),
    phone: phone.trim() || null,
    address: address.trim() || null,
  };
  const hasChanges = savedProfile && Object.keys(profile).some((key) => profile[key] !== savedProfile[key]);

  const validate = () => {
    const next = {};
    const phoneRegex = /^0\d{9}$/;

    if (!fullName.trim() || fullName.trim().length < 2) {
      next.fullName = 'Họ tên phải có ít nhất 2 ký tự.';
    }
    if (phone.trim() && !phoneRegex.test(phone.trim())) {
      next.phone = 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0.';
    }

    setFieldErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (saving || !hasChanges || !validate()) return;

    setSaving(true);
    try {
      await userApi.updateProfile(profile);
      setSavedProfile(profile);
      const refreshed = await refreshUser();
      if (refreshed) {
        setFullName(refreshed.fullName || '');
        setPhone(refreshed.phone || '');
        setAddress(refreshed.address || '');
        setEmail(refreshed.email || '');
        setSavedProfile({
          fullName: refreshed.fullName?.trim() || '',
          phone: refreshed.phone?.trim() || null,
          address: refreshed.address?.trim() || null,
        });
      }
      toast.success('Thông tin tài khoản của bạn đã được lưu an toàn.');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không thể cập nhật hồ sơ.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <Loading message="Đang tải hồ sơ cá nhân..." />;
  }

  if (error) {
    return (
      <ErrorState
        title="Lỗi tải hồ sơ"
        message={error}
        onRetry={() => {
          setLoading(true);
          setError('');
          setReloadKey((k) => k + 1);
        }}
      />
    );
  }

  return (
    <>
      <div>
        <h1 className="km-page-title">Hồ sơ cá nhân</h1>
        <p className="km-auth-subtitle">Quản lý thông tin và địa chỉ giao hàng của bạn.</p>
      </div>

      <form className="km-account-card" onSubmit={handleSubmit} noValidate>
        <h2>Thông tin cơ bản</h2>

        <div className="km-form-row-2">
          <div className="km-field">
            <label htmlFor="profile-fullname" className="km-label">
              Họ tên <span className="km-required">*</span>
            </label>
            <input
              id="profile-fullname"
              type="text"
              className={`km-input ${fieldErrors.fullName ? 'km-input--error' : ''}`}
              value={fullName}
              onChange={(e) => {
                setFullName(e.target.value);
                if (fieldErrors.fullName) setFieldErrors((p) => ({ ...p, fullName: '' }));
              }}
              autoComplete="name"
            />
            {fieldErrors.fullName && <p className="km-field-error">{fieldErrors.fullName}</p>}
          </div>

          <div className="km-field">
            <label htmlFor="profile-email" className="km-label">
              Email
            </label>
            <input
              id="profile-email"
              type="email"
              readOnly
              aria-describedby="profile-email-hint"
              className="km-input km-input--readonly"
              value={email}
            />
            <p id="profile-email-hint" className="km-field-hint">Chỉ đọc. Email dùng để đăng nhập và nhận thông báo.</p>
          </div>
        </div>

        <div className="km-field">
          <label htmlFor="profile-phone" className="km-label">
            Số điện thoại
          </label>
          <input
            id="profile-phone"
            type="tel"
            className={`km-input ${fieldErrors.phone ? 'km-input--error' : ''}`}
            value={phone}
            onChange={(e) => {
              setPhone(e.target.value);
              if (fieldErrors.phone) setFieldErrors((p) => ({ ...p, phone: '' }));
            }}
            autoComplete="tel"
          />
          {fieldErrors.phone && <p className="km-field-error">{fieldErrors.phone}</p>}
        </div>

        <div className="km-field">
          <label htmlFor="profile-address" className="km-label">
            Địa chỉ giao hàng
          </label>
          <input
            id="profile-address"
            type="text"
            className="km-input"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            autoComplete="street-address"
          />
        </div>

        <div>
          <button type="submit" className="km-btn km-btn-primary" disabled={saving || !hasChanges}>
            {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>
      </form>

    </>
  );
}

export default CustomerProfilePage;
