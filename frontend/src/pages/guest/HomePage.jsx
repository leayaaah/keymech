import { Link } from 'react-router-dom';
import formatVND from '../../utils/formatVND';

/**
 * Trang chủ giới thiệu sản phẩm và dịch vụ Custom bàn phím KeyMech.
 */
export function HomePage() {
  const sampleProducts = [
    {
      id: 1,
      name: 'Bàn phím cơ Keychron Q1 Pro Wireless',
      price: 4390000,
      image: '/uploads/sample1.jpg',
      category: 'Bàn phím cơ',
    },
    {
      id: 2,
      name: 'Switch Gateron Oil King Tuyển chọn (Pack 35)',
      price: 450000,
      image: '/uploads/sample2.jpg',
      category: 'Switch',
    },
    {
      id: 3,
      name: 'Bộ Keycap PBT Dyesub Cherry Profile Retro',
      price: 850000,
      image: '/uploads/sample3.jpg',
      category: 'Keycap',
    },
  ];

  return (
    <div>
      {/* Hero Banner */}
      <section
        style={{
          background: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)',
          color: '#ffffff',
          borderRadius: '12px',
          padding: '3rem 2rem',
          textAlign: 'center',
          marginBottom: '2.5rem',
        }}
      >
        <h1
          style={{
            fontSize: '2.25rem',
            fontWeight: 800,
            marginBottom: '1rem',
          }}
        >
          KeyMech – Đam Mê Phím Cơ Đích Thực
        </h1>
        <p
          style={{
            fontSize: '1.1rem',
            maxWidth: '600px',
            margin: '0 auto 1.5rem auto',
            opacity: 0.9,
          }}
        >
          Cung cấp linh kiện phím cơ chính hãng, kit bàn phím cao cấp và dịch vụ
          custom phím độc bản theo ý thích.
        </p>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem' }}>
          <Link
            to="/products"
            style={{
              backgroundColor: '#ffffff',
              color: '#1e3a8a',
              padding: '0.75rem 1.5rem',
              borderRadius: '8px',
              textDecoration: 'none',
              fontWeight: 600,
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
            }}
          >
            Khám phá sản phẩm
          </Link>
          <Link
            to="/customer/custom-requests"
            style={{
              backgroundColor: 'rgba(255, 255, 255, 0.2)',
              color: '#ffffff',
              border: '1px solid #ffffff',
              padding: '0.75rem 1.5rem',
              borderRadius: '8px',
              textDecoration: 'none',
              fontWeight: 600,
            }}
          >
            Đặt Custom phím
          </Link>
        </div>
      </section>

      {/* Featured Products */}
      <section>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '1.5rem',
          }}
        >
          <h2
            style={{
              fontSize: '1.5rem',
              fontWeight: 700,
              color: '#111827',
              margin: 0,
            }}
          >
            Sản phẩm nổi bật
          </h2>
          <Link
            to="/products"
            style={{
              color: '#2563eb',
              textDecoration: 'none',
              fontSize: '0.95rem',
              fontWeight: 500,
            }}
          >
            Xem tất cả →
          </Link>
        </div>

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
            gap: '1.5rem',
          }}
        >
          {sampleProducts.map((p) => (
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
                  color: '#9ca3af',
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
                  {p.category}
                </span>
                <h3
                  style={{
                    fontSize: '1rem',
                    fontWeight: 600,
                    margin: '0.35rem 0 0.5rem 0',
                    color: '#1f2937',
                  }}
                >
                  {p.name}
                </h3>
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
                    Xem chi tiết
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

export default HomePage;
