import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import { useAuth } from '../../hooks/useAuth';
import ProductCard from '../../components/ProductCard';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import EmptyState from '../../components/EmptyState';
import heroImage from '../../assets/shop-hero.webp';
import keyboardsImage from '../../assets/shop-keyboards.webp';
import switchesImage from '../../assets/shop-switches.webp';
import keycapsImage from '../../assets/shop-keycaps.webp';
import toolsImage from '../../assets/shop-tools.webp';
import kitsImage from '../../assets/shop-kits.webp';
import '../../styles/shop.css';

const HOME_CATEGORIES = [
  { id: '1', name: 'Bàn phím cơ', image: keyboardsImage },
  { id: '2', name: 'Switch', image: switchesImage },
  { id: '3', name: 'Keycap', image: keycapsImage },
  { id: '4', name: 'Dụng cụ và phụ kiện', image: toolsImage },
  { id: '5', name: 'Kit bàn phím', image: kitsImage },
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
          setFeatured((res?.content ?? []).slice(0, 4));
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
          <h1 className="km-hero-title">
            Tìm cảm giác gõ của riêng bạn
          </h1>
          <p className="km-hero-subtitle">
            Bàn phím, switch và linh kiện tuyển chọn cho mọi góc làm việc.
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
        <div className="km-hero-visual">
          <img src={heroImage} alt="" width="1536" height="1024" fetchPriority="high" />
        </div>
      </section>

      {/* Khám phá danh mục */}
      <section className="km-home-categories" aria-labelledby="home-categories-title">
        <div className="km-section-header">
          <h2 className="km-section-title" id="home-categories-title">Danh mục sản phẩm</h2>
        </div>
        <div className="km-category-grid" role="group" aria-label="Danh mục sản phẩm" tabIndex={0}>
          {HOME_CATEGORIES.map((cat) => (
            <Link key={cat.id} to={`/products?categoryId=${cat.id}`} className="km-category-card">
              <div className="km-category-thumb" aria-hidden="true">
                <img src={cat.image} alt="" width="400" height="400" loading="lazy" />
              </div>
              <p className="km-category-name">{cat.name}</p>
            </Link>
          ))}
        </div>
      </section>

      {/* Custom Banner (ẩn với Staff/Admin) */}
      {!isStaffOrAdmin && (
        <section className="km-custom-banner">
          <div className="km-custom-banner-inner">
            <h2 className="km-section-title">Một chiếc phím. Chất riêng của bạn.</h2>
            <p>
              Tự chọn layout, switch, case và keycap. Đội ngũ KeyMech đồng hành từ ý tưởng đến lần chạm phím đầu tiên.
            </p>
            <Link to="/customer/custom-requests" className="km-btn km-btn-primary">
              Tạo yêu cầu Custom
            </Link>
          </div>
        </section>
      )}

      <ul className="km-shop-benefits" aria-label="Lợi ích khi mua sắm tại KeyMech">
        <li><span aria-hidden="true">✓</span> Hàng chính hãng</li>
        <li><span aria-hidden="true">✓</span> Hỗ trợ kỹ thuật tận tâm</li>
        <li><span aria-hidden="true">✓</span> Giao hàng toàn quốc</li>
      </ul>

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

    </div>
  );
}

export default HomePage;
