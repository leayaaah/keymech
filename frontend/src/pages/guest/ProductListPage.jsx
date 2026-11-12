import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';

/**
 * Trang danh sách sản phẩm, hỗ trợ đủ 3 trạng thái: đang tải, rỗng và lỗi.
 */
export function ProductListPage() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [category, setCategory] = useState('ALL');

  useEffect(() => {
    let ignore = false;
    const timer = setTimeout(() => {
      if (!ignore) {
        setProducts([
          {
            id: 1,
            name: 'Bàn phím cơ Keychron Q1 Pro Wireless',
            price: 4390000,
            category: 'KEYBOARD',
            categoryName: 'Bàn phím cơ',
          },
          {
            id: 2,
            name: 'Switch Gateron Oil King Tuyển chọn (Pack 35)',
            price: 450000,
            category: 'SWITCH',
            categoryName: 'Switch',
          },
          {
            id: 3,
            name: 'Bộ Keycap PBT Dyesub Cherry Profile Retro',
            price: 850000,
            category: 'KEYCAP',
            categoryName: 'Keycap',
          },
          {
            id: 4,
            name: 'Bộ Kit custom MonsGeek M1 V3 nhôm CNC',
            price: 2190000,
            category: 'KIT',
            categoryName: 'Kit bàn phím',
          },
        ]);
        setLoading(false);
      }
    }, 400);

    return () => {
      ignore = true;
      clearTimeout(timer);
    };
  }, []);

  const handleRetry = () => {
    setLoading(true);
    setError(null);
    setTimeout(() => {
      setProducts([
        {
          id: 1,
          name: 'Bàn phím cơ Keychron Q1 Pro Wireless',
          price: 4390000,
          category: 'KEYBOARD',
          categoryName: 'Bàn phím cơ',
        },
        {
          id: 2,
          name: 'Switch Gateron Oil King Tuyển chọn (Pack 35)',
          price: 450000,
          category: 'SWITCH',
          categoryName: 'Switch',
        },
        {
          id: 3,
          name: 'Bộ Keycap PBT Dyesub Cherry Profile Retro',
          price: 850000,
          category: 'KEYCAP',
          categoryName: 'Keycap',
        },
        {
          id: 4,
          name: 'Bộ Kit custom MonsGeek M1 V3 nhôm CNC',
          price: 2190000,
          category: 'KIT',
          categoryName: 'Kit bàn phím',
        },
      ]);
      setLoading(false);
    }, 400);
  };

  const filteredProducts =
    category === 'ALL'
      ? products
      : products.filter((p) => p.category === category);

  return (
    <div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '1rem',
          marginBottom: '1.5rem',
        }}
      >
        <h1
          style={{
            fontSize: '1.75rem',
            fontWeight: 700,
            color: '#111827',
            margin: 0,
          }}
        >
          Danh mục sản phẩm
        </h1>

        {/* Filter categories */}
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          {[
            { id: 'ALL', name: 'Tất cả' },
            { id: 'KEYBOARD', name: 'Bàn phím' },
            { id: 'SWITCH', name: 'Switch' },
            { id: 'KEYCAP', name: 'Keycap' },
            { id: 'KIT', name: 'Kit Custom' },
          ].map((cat) => (
            <button
              key={cat.id}
              type="button"
              onClick={() => setCategory(cat.id)}
              style={{
                padding: '0.4rem 0.85rem',
                borderRadius: '20px',
                border: '1px solid #d1d5db',
                backgroundColor: category === cat.id ? '#2563eb' : '#ffffff',
                color: category === cat.id ? '#ffffff' : '#374151',
                fontSize: '0.875rem',
                cursor: 'pointer',
                fontWeight: category === cat.id ? 600 : 400,
              }}
            >
              {cat.name}
            </button>
          ))}
        </div>
      </div>

      {/* 3 trạng thái: Đang tải, Lỗi, và Rỗng */}
      {loading && <Loading message="Đang tải danh sách sản phẩm..." />}

      {!loading && error && (
        <ErrorState
          title="Lỗi tải sản phẩm"
          message={error}
          onRetry={handleRetry}
        />
      )}

      {!loading && !error && filteredProducts.length === 0 && (
        <EmptyState
          title="Không tìm thấy sản phẩm"
          description="Không có sản phẩm nào thuộc danh mục này."
          actionText="Xem tất cả sản phẩm"
          onAction={() => setCategory('ALL')}
        />
      )}

      {!loading && !error && filteredProducts.length > 0 && (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
            gap: '1.5rem',
          }}
        >
          {filteredProducts.map((p) => (
            <div
              key={p.id}
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '8px',
                overflow: 'hidden',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                display: 'flex',
                flexDirection: 'column',
              }}
            >
              <div
                style={{
                  height: '180px',
                  backgroundColor: '#e5e7eb',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '2rem',
                }}
              >
                ⌨️
              </div>
              <div style={{ padding: '1rem', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <span
                  style={{
                    fontSize: '0.75rem',
                    color: '#6b7280',
                    textTransform: 'uppercase',
                    fontWeight: 600,
                  }}
                >
                  {p.categoryName}
                </span>
                <h2
                  style={{
                    fontSize: '1rem',
                    fontWeight: 600,
                    margin: '0.35rem 0 0.5rem 0',
                    color: '#1f2937',
                  }}
                >
                  {p.name}
                </h2>
                <div
                  style={{
                    marginTop: 'auto',
                    paddingTop: '0.5rem',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <span
                    style={{
                      fontSize: '1.1rem',
                      fontWeight: 700,
                      color: '#dc2626',
                    }}
                  >
                    {formatVND(p.price)}
                  </span>
                  <Link
                    to={`/products/${p.id}`}
                    style={{
                      padding: '0.4rem 0.8rem',
                      backgroundColor: '#2563eb',
                      color: '#ffffff',
                      borderRadius: '6px',
                      textDecoration: 'none',
                      fontSize: '0.85rem',
                    }}
                  >
                    Chi tiết
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default ProductListPage;
