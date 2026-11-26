import { useState } from 'react';
import { Link } from 'react-router-dom';
import formatVND from '../utils/formatVND';
import { useAuth } from '../hooks/useAuth';
import { cartApi } from '../api/cartApi';
import { toast } from '../utils/toast';

/**
 * Thẻ sản phẩm dùng chung (Trang chủ, Danh sách sản phẩm, Sản phẩm liên quan).
 * Ẩn nút "Thêm vào giỏ" đối với vai trò Staff / Admin.
 */
export function ProductCard({ product }) {
  const { isStaffOrAdmin } = useAuth();
  const [adding, setAdding] = useState(false);

  const outOfStock = (product.stockQuantity ?? 0) <= 0;
  const specParts = [
    outOfStock ? 'Hết hàng' : null,
    product.layout,
    product.switchType,
  ].filter(Boolean);

  const handleAddToCart = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (outOfStock || adding) return;
    setAdding(true);
    try {
      await cartApi.addItem({ productId: product.id, quantity: 1 });
      window.dispatchEvent(new CustomEvent('keymech:cart-updated'));
      toast.success(`Đã thêm "${product.name}" vào giỏ hàng`);
    } catch (err) {
      const msg = err?.response?.data?.message || 'Không thể thêm vào giỏ hàng';
      toast.error(msg);
    } finally {
      setAdding(false);
    }
  };

  return (
    <article className="km-product-card">
      <Link to={`/products/${product.id}`} className="km-product-card-link">
        <div className="km-product-card-img">
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} />
          ) : (
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" aria-hidden="true">
              <rect x="2" y="6" width="20" height="12" rx="2" />
              <path d="M6 10h.01M10 10h.01M14 10h.01M18 10h.01M8 14h8" strokeLinecap="round" />
            </svg>
          )}
        </div>
        {(product.categoryName || product.category) && (
          <span className="km-product-card-cat">{product.categoryName || product.category}</span>
        )}
        <h3 className="km-product-card-title">{product.name}</h3>
        {product.brand && <p className="km-product-card-brand">{product.brand}</p>}
        {specParts.length > 0 && (
          <p className={`km-product-card-specs ${outOfStock ? 'km-product-card-specs--out' : ''}`}>
            {specParts.join('  •  ')}
          </p>
        )}
        <div className="km-product-card-price">{formatVND(product.price)}</div>
      </Link>

      {!isStaffOrAdmin && (
        <button
          type="button"
          className="km-btn km-btn-primary km-btn-full"
          disabled={outOfStock || adding}
          onClick={handleAddToCart}
        >
          {adding ? 'Đang thêm...' : 'Thêm vào giỏ'}
        </button>
      )}
    </article>
  );
}

export default ProductCard;
