import { useParams, Link } from 'react-router-dom';
import formatVND from '../../utils/formatVND';

/**
 * Trang chi tiết sản phẩm.
 */
export function ProductDetailPage() {
  const { id } = useParams();

  return (
    <div
      style={{
        backgroundColor: '#ffffff',
        borderRadius: '8px',
        padding: '2rem',
        boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
      }}
    >
      <Link
        to="/products"
        style={{
          color: '#2563eb',
          textDecoration: 'none',
          fontSize: '0.9rem',
          display: 'inline-block',
          marginBottom: '1.5rem',
        }}
      >
        ← Quay lại danh sách sản phẩm
      </Link>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
          gap: '2rem',
        }}
      >
        <div
          style={{
            height: '320px',
            backgroundColor: '#f3f4f6',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '4rem',
          }}
        >
          ⌨️
        </div>

        <div>
          <span
            style={{
              fontSize: '0.8rem',
              color: '#2563eb',
              fontWeight: 600,
              textTransform: 'uppercase',
            }}
          >
            Mã sản phẩm: #{id}
          </span>
          <h1
            style={{
              fontSize: '1.75rem',
              fontWeight: 700,
              color: '#111827',
              margin: '0.5rem 0 1rem 0',
            }}
          >
            Bàn phím cơ KeyMech Custom Edition #{id}
          </h1>

          <div
            style={{
              fontSize: '1.5rem',
              fontWeight: 700,
              color: '#dc2626',
              marginBottom: '1rem',
            }}
          >
            {formatVND(2500000)}
          </div>

          <p
            style={{
              color: '#4b5563',
              lineHeight: 1.6,
              marginBottom: '1.5rem',
            }}
          >
            Trang chi tiết sản phẩm chuẩn bị tích hợp API `GET /api/products/{id}`.
            Sản phẩm được bảo hành chính hãng 12 tháng, hỗ trợ mod âm và lube switch miễn phí theo yêu cầu.
          </p>

          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
            <button
              type="button"
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#2563eb',
                color: '#ffffff',
                border: 'none',
                borderRadius: '6px',
                fontWeight: 600,
                cursor: 'pointer',
              }}
              onClick={() => alert('Chức năng thêm vào giỏ hàng sẽ được gắn ở Sprint sau.')}
            >
              Thêm vào giỏ hàng
            </button>
            <Link
              to="/cart"
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#f3f4f6',
                color: '#1f2937',
                border: '1px solid #d1d5db',
                borderRadius: '6px',
                textDecoration: 'none',
                fontWeight: 600,
              }}
            >
              Xem giỏ hàng
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default ProductDetailPage;
