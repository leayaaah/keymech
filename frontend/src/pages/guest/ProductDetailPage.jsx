import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
import Loading from '../../components/Loading';
import ErrorState from '../../components/ErrorState';
import { toast } from '../../utils/toast';
import { productApi } from '../../api/productApi';
import { cartApi } from '../../api/cartApi';

export function ProductDetailPage() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    let ignore = false;
    productApi
      .detail(parseInt(id, 10))
      .then((data) => {
        if (!ignore) {
          setProduct(data);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!ignore) {
          setError(err?.message || 'Không tìm thấy sản phẩm');
          setLoading(false);
        }
      });
  }, [id]);

  const handleAddToCart = async () => {
    if (!product) return;
    setAdding(true);
    try {
      await cartApi.addItem({ productId: product.id, quantity });
      toast.success(`Đã thêm ${quantity} sản phẩm vào giỏ hàng`);
    } catch (err) {
      toast.error(err?.message || 'Không thể thêm vào giỏ hàng');
    } finally {
      setAdding(false);
    }
  };

  return (
    <div>
      <Link to="/products" style={{ color: '#2563eb', textDecoration: 'none', fontSize: '0.9rem', display: 'inline-block', marginBottom: '1.5rem' }}>
        ← Quay lại danh sách sản phẩm
      </Link>

      {loading && <Loading message="Đang tải chi tiết sản phẩm..." />}
      {!loading && error && <ErrorState title="Không tìm thấy sản phẩm" message={error} onRetry={() => window.location.reload()} />}

      {!loading && !error && product && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '2rem' }}>
          {/* Image */}
          <div style={{ height: '360px', backgroundColor: '#f3f4f6', borderRadius: '8px', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            {product.imageUrl ? (
              <img src={product.imageUrl} alt={product.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            ) : (
              <span style={{ fontSize: '4rem' }}>⌨️</span>
            )}
          </div>

          {/* Info */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <span style={{ fontSize: '0.75rem', color: '#2563eb', fontWeight: 600, textTransform: 'uppercase' }}>{product.brand}</span>
              <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#111827', margin: '0.25rem 0' }}>{product.name}</h1>
            </div>

            <div style={{ fontSize: '1.5rem', fontWeight: 700, color: '#dc2626' }}>{formatVND(product.price)}</div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', fontSize: '0.9rem' }}>
              {product.layout && <InfoRow label="Layout" value={product.layout} />}
              {product.switchType && <InfoRow label="Switch" value={product.switchType} />}
              <InfoRow label="Kho" value={`${product.stockQuantity} còn`} highlight={product.stockQuantity > 0 ? false : true} />
            </div>

            {(product.description || product.specs) && (
              <div style={{ fontSize: '0.85rem', color: '#4b5563', background: '#f9fafb', padding: '0.75rem', borderRadius: '6px' }}>
                {product.description && <div style={{ marginBottom: '0.5rem' }}><strong>Mô tả:</strong> {product.description}</div>}
                {product.specs && <div><strong>Thông số:</strong> {product.specs}</div>}
              </div>
            )}

            <div style={{ display: 'flex', gap: '1rem', marginTop: 'auto' }}>
              <div style={{ display: 'flex', alignItems: 'center', border: '1px solid #d1d5db', borderRadius: '6px', overflow: 'hidden' }}>
                <button onClick={() => setQuantity(Math.max(1, quantity - 1))} style={{ padding: '0.5rem 0.75rem', border: 'none', background: '#f3f4f6', cursor: 'pointer', fontSize: '1rem' }}>−</button>
                <span style={{ padding: '0.5rem 1rem', borderLeft: '1px solid #d1d5db', borderRight: '1px solid #d1d5db', minWidth: '2rem', textAlign: 'center' }}>{quantity}</span>
                <button onClick={() => setQuantity(quantity + 1)} style={{ padding: '0.5rem 0.75rem', border: 'none', background: '#f3f4f6', cursor: 'pointer', fontSize: '1rem' }}>+</button>
              </div>
              <button
                onClick={handleAddToCart}
                disabled={adding || product.stockQuantity === 0}
                style={{
                  flex: 1,
                  padding: '0.65rem 1rem',
                  backgroundColor: product.stockQuantity > 0 ? '#2563eb' : '#9ca3af',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: product.stockQuantity > 0 ? 'pointer' : 'not-allowed',
                  fontWeight: 600,
                  fontSize: '1rem',
                }}
              >
                {adding ? 'Đang thêm...' : product.stockQuantity > 0 ? '🛒 Thêm vào giỏ' : 'Hết hàng'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function InfoRow({ label, value, highlight }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.4rem 0', borderBottom: '1px solid #f3f4f6' }}>
      <span style={{ color: '#6b7280' }}>{label}</span>
      <span style={{ fontWeight: 600, color: highlight ? '#dc2626' : '#111827' }}>{value}</span>
    </div>
  );
}

export default ProductDetailPage;