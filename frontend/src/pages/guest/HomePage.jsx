import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import { useAuth } from '../../hooks/useAuth';
import ProductCard from '../../components/ProductCard';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import { toast } from '../../utils/toast';
import '../../styles/shop.css';

const HOME_CATEGORIES = [
  { id: '1', name: 'Bàn phím cơ' },
  { id: '2', name: 'Switch' },
  { id: '3', name: 'Keycap' },
  { id: '4', name: 'Dụng cụ và phụ kiện' },
  { id: '5', name: 'Kit bàn phím' },
];

/**
 * Trang chủ cửa hàng KeyMech (Figma frame 3:110).
 */
export function HomePage() {
  const { isStaffOrAdmin } = useAuth();
  const [featured, setFeatured] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;
    productApi
      .list({ page: 0, size: 4 })
      .then((res) => {
        if (active) {
          setFeatured(res?.content ?? []);
          setError(null);
        }
      })
      .catch((err) => {
        if (active) {
          setError(err?.response?.data?.message || 'Không thể tải sản phẩm nổi bật.');
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [reloadKey]);

  const handleRetry = () => {
    setLoading(true);
    setError(null);
    setReloadKey((k) => k + 1);
  };

  return (
    <div className="km-home">
      {/* Hero Banner */}
      <section className="km-hero">
        <div className="km-hero-content">
          <p className="km-hero-eyebrow">DÀNH CHO NGƯỜI YÊU PHÍM CƠ</p>
          <h1 className="km-hero-title">
            KeyMech – Đam Mê
            <br />
            Phím Cơ Đích Thực
          </h1>
          <p className="km-hero-subtitle">
            Tìm cảm giác gõ của riêng bạn. Bàn phím, switch và linh kiện được tuyển chọn cho mọi góc làm việc.
          </p>
          <div className="km-hero-actions">
            <Link to="/products" className="km-btn km-btn-primary">
              Khám phá sản phẩm
            </Link>
            {!isStaffOrAdmin && (
              <Link to="/customer/custom-requests" className="km-btn km-btn-soft">
                Đặt Custom phím
              </Link>
            )}
          </div>
        </div>
        <div className="km-hero-visual" aria-hidden="true">
          <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2">
            <rect x="2" y="6" width="20" height="12" rx="2" />
            <path d="M6 10h.01M10 10h.01M14 10h.01M18 10h.01M7 14h10" strokeLinecap="round" />
          </svg>
        </div>
      </section>

      {/* Khám phá danh mục */}
      <section>
        <div className="km-section-header">
          <h2 className="km-section-title">Bắt đầu từ đam mê của bạn</h2>
        </div>
        <div className="km-category-grid">
          {HOME_CATEGORIES.map((cat) => (
            <Link key={cat.id} to={`/products?categoryId=${cat.id}`} className="km-category-card">
              <div className="km-category-thumb" aria-hidden="true">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
                  <rect x="3" y="6" width="18" height="12" rx="2" />
                  <path d="M7 10h.01M11 10h.01M15 10h.01M8 14h8" strokeLinecap="round" />
                </svg>
              </div>
              <p className="km-category-name">{cat.name}</p>
            </Link>
          ))}
        </div>
      </section>

      {/* Sản phẩm nổi bật */}
      <section>
        <div className="km-section-header">
          <h2 className="km-section-title">Sản phẩm nổi bật</h2>
          <Link to="/products" className="km-section-link">
            Xem tất cả →
          </Link>
        </div>

        {loading && <Loading message="Đang tải sản phẩm nổi bật..." />}
        {!loading && error && (
          <ErrorState title="Lỗi tải sản phẩm" message={error} onRetry={handleRetry} />
        )}
        {!loading && !error && featured.length === 0 && (
          <EmptyState
            title="Không tìm thấy sản phẩm"
            description="Hiện chưa có sản phẩm nổi bật nào."
          />
        )}
        {!loading && !error && featured.length > 0 && (
          <div className="km-product-grid-4">
            {featured.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </section>

      {/* Custom Banner (ẩn với Staff/Admin) */}
      {!isStaffOrAdmin && (
        <section className="km-custom-banner">
          <div className="km-custom-banner-inner">
            <h2>Một chiếc phím. Chất riêng của bạn.</h2>
            <p>
              Tự chọn layout, switch, case và keycap. Đội ngũ KeyMech đồng hành từ ý tưởng đến lần chạm phím đầu tiên.
            </p>
            <Link to="/customer/custom-requests" className="km-btn km-btn-primary">
              Tạo yêu cầu Custom
            </Link>
          </div>
        </section>
      )}

      {/* Trợ lý mua sắm AI (ẩn với Staff/Admin) */}
      {!isStaffOrAdmin && (
        <section className="km-assistant-banner">
          <h2>Chưa biết chọn gì? Hỏi KeyMech.</h2>
          <p>AI tư vấn giúp bạn tìm bàn phím và switch phù hợp với nhu cầu, ngân sách.</p>
          <button
            type="button"
            className="km-btn km-btn-primary"
            onClick={() => toast.info('Tính năng Chatbot tư vấn sẽ được mở ở Lô 6.')}
          >
            Tư vấn ngay
          </button>
        </section>
      )}
    </div>
  );
}

export default HomePage;
