import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import { productApi } from '../../api/productApi';

const PAGE_SIZE = 12;

// Dropdown brands & layouts lấy từ dữ liệu mẫu
const BRANDS = [
  { id: '', name: 'Tất cả hãng' },
  { id: 'Keychron', name: 'Keychron' },
  { id: 'Gateron', name: 'Gateron' },
  { id: 'Filco', name: 'Filco' },
  { id: 'Ducky', name: 'Ducky' },
];

const LAYOUTS = [
  { id: '', name: 'Tất cả layout' },
  { id: 'Full-size', name: 'Full-size' },
  { id: 'TKL', name: 'TKL (87-key)' },
  { id: '75%', name: '75%' },
  { id: '65%', name: '65%' },
  { id: '60%', name: '60%' },
  { id: '40%', name: '40%' },
];

const SORT_OPTIONS = [
  { id: '', name: 'Mặc định' },
  { id: 'price-asc', name: 'Giá tăng' },
  { id: 'price-desc', name: 'Giá giảm' },
  { id: 'name-asc', name: 'Tên A-Z' },
];

// Categories from data.sql
const CATEGORIES = [
  { id: '', name: 'Tất cả danh mục' },
  { id: '1', name: 'Bàn phím cơ' },
  { id: '2', name: 'Switch' },
  { id: '3', name: 'Keycap' },
  { id: '4', name: 'Dụng cụ & phụ kiện' },
  { id: '5', name: 'Kit bàn phím' },
];

export function ProductListPage() {
  // pagination + filter state
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // filter state
  const [keyword, setKeyword] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [brand, setBrand] = useState('');
  const [layout, setLayout] = useState('');
  const [sort, setSort] = useState('');

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;

    const params = {};
    if (keyword) params.keyword = keyword;
    if (categoryId) params.categoryId = categoryId;
    if (brand) params.brand = brand;
    if (layout) params.layout = layout;
    if (sort) params.sort = sort;
    params.page = page;
    params.size = PAGE_SIZE;

    productApi
      .list(params)
      .then((res) => {
        if (!ignore) {
          setProducts(res.content ?? []);
          setTotalPages(res.totalPages ?? 1);
          setTotalElements(res.totalElements ?? 0);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!ignore) {
          setError(err?.message || 'Không thể tải danh sách sản phẩm');
          setLoading(false);
        }
      });
  }, [page, keyword, categoryId, brand, layout, sort]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
  };

  const totalPagesArr = Array.from({ length: totalPages }, (_, i) => i);
  const visiblePages = totalPagesArr.filter((p) =>
    p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 2
  );

  return (
    <div>
      {/* Header */}
      <div style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#111827', margin: '0 0 1rem 0' }}>
          Sản phẩm
        </h1>

        {/* Search form */}
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <input
            type="text"
            placeholder="Tìm tên sản phẩm..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ flex: 1, minWidth: '200px', padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid #d1d5db' }}
          />
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} style={{ padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid #d1d5db' }}>
            {CATEGORIES.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <select value={brand} onChange={(e) => setBrand(e.target.value)} style={{ padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid #d1d5db' }}>
            {BRANDS.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
          </select>
          <select value={layout} onChange={(e) => setLayout(e.target.value)} style={{ padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid #d1d5db' }}>
            {LAYOUTS.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
          <select value={sort} onChange={(e) => setSort(e.target.value)} style={{ padding: '0.5rem 0.75rem', borderRadius: '6px', border: '1px solid #d1d5db' }}>
            {SORT_OPTIONS.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
          </select>
          <button type="submit" style={{ padding: '0.5rem 1rem', backgroundColor: '#2563eb', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}>Tìm</button>
        </form>
        <div style={{ color: '#6b7280', fontSize: '0.85rem', marginTop: '0.5rem' }}>
          {loading ? '' : `${totalElements} sản phẩm`}
        </div>
      </div>

      {/* Body */}
      {loading && <Loading message="Đang tải sản phẩm..." />}
      {!loading && error && <ErrorState title="Lỗi tải sản phẩm" message={error} onRetry={() => setPage(page)} />}
      {!loading && !error && products.length === 0 && (
        <EmptyState
          title="Không tìm thấy sản phẩm"
          description="Hãy thử thay đổi bộ lọc hoặc tìm kiếm khác."
          actionText="Xóa bộ lọc"
          onAction={() => { setKeyword(''); setCategoryId(''); setBrand(''); setLayout(''); setSort(''); setPage(0); }}
        />
      )}

      {!loading && !error && products.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1.5rem' }}>
          {products.map((p) => (
            <div key={p.id} style={{ backgroundColor: '#fff', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column' }}>
              <div style={{ height: '180px', backgroundColor: '#f3f4f6', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2rem' }}>
                {p.imageUrl
                  ? <img src={p.imageUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  : '⌨️'}
              </div>
              <div style={{ padding: '1rem', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: 600 }}>{p.brand}</span>
                <h2 style={{ fontSize: '0.95rem', fontWeight: 600, margin: '0.3rem 0 0.5rem 0', color: '#1f2937', lineHeight: 1.3, minHeight: '2.6em' }}>
                  {p.name}
                </h2>
                {p.descriptionShort && (
                  <p style={{ fontSize: '0.8rem', color: '#6b7280', margin: '0 0 0.5rem', lineHeight: 1.4, display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                    {p.descriptionShort}
                  </p>
                )}
                <div style={{ marginTop: 'auto', paddingTop: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontSize: '1.1rem', fontWeight: 700, color: '#dc2626' }}>{formatVND(p.price)}</span>
                  <Link
                    to={`/products/${p.id}`}
                    style={{ padding: '0.4rem 0.8rem', backgroundColor: '#2563eb', color: '#fff', borderRadius: '6px', textDecoration: 'none', fontSize: '0.85rem' }}
                  >Chi tiết</Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && !error && totalPages > 1 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '2rem' }}>
          <button disabled={page === 0} onClick={() => setPage(page - 1)} style={btnStyle(page === 0)}>‹</button>
          {visiblePages.map((p, idx, arr) => (
            <span key={p}>
              {idx > 0 && arr[idx - 1] !== p - 1 && <span style={{ padding: '0 0.25rem', color: '#9ca3af' }}>…</span>}
              <button onClick={() => setPage(p)} style={btnStyle(p === page, '#2563eb')}>
                {p + 1}
              </button>
            </span>
          ))}
          <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} style={btnStyle(page >= totalPages - 1)}>›</button>
        </div>
      )}
    </div>
  );
}

function btnStyle(disabled, activeColor = null) {
  return {
    width: 32,
    height: 32,
    borderRadius: 6,
    border: '1px solid #d1d5db',
    backgroundColor: disabled ? '#f3f4f6' : activeColor || '#fff',
    color: disabled ? '#9ca3af' : activeColor ? '#fff' : '#374151',
    cursor: disabled ? 'not-allowed' : 'pointer',
    fontSize: '1rem',
    fontWeight: activeColor ? 700 : 400,
  };
}

export default ProductListPage;
