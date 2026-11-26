import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import Loading from '../../components/Loading';
import EmptyState from '../../components/EmptyState';
import ErrorState from '../../components/ErrorState';
import ProductCard from '../../components/ProductCard';
import { productApi } from '../../api/productApi';
import '../../styles/shop.css';

const PAGE_SIZE = 12;

const CATEGORIES = [
  { id: '1', name: 'Bàn phím cơ' },
  { id: '2', name: 'Switch' },
  { id: '3', name: 'Keycap' },
  { id: '4', name: 'Dụng cụ và phụ kiện' },
  { id: '5', name: 'Kit bàn phím' },
];

const BRANDS = [
  'Keychron',
  'Akko',
  'Leopold',
  'Ducky',
  'Royal Kludge',
  'Varmilo',
  'KBDfans',
  'Gateron',
  'Filco',
];

const LAYOUTS = ['60%', '65%', '75%', 'TKL', 'Full-size'];

const SORT_OPTIONS = [
  { id: '', name: 'Sắp xếp: Mới nhất' },
  { id: 'price-asc', name: 'Giá tăng dần' },
  { id: 'price-desc', name: 'Giá giảm dần' },
  { id: 'name-asc', name: 'Tên A–Z' },
];

/**
 * Trang danh sách sản phẩm (Figma frame 3:132).
 */
export function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // Filter draft state (applied when clicking "Áp dụng")
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '');
  const [categoryId, setCategoryId] = useState(searchParams.get('categoryId') || '');
  const [brand, setBrand] = useState(searchParams.get('brand') || '');
  const [layout, setLayout] = useState(searchParams.get('layout') || '');
  const [minPrice, setMinPrice] = useState(searchParams.get('minPrice') || '');
  const [maxPrice, setMaxPrice] = useState(searchParams.get('maxPrice') || '');
  const [sort, setSort] = useState(searchParams.get('sort') || '');

  // Applied filter state
  const [applied, setApplied] = useState({
    keyword: searchParams.get('keyword') || '',
    categoryId: searchParams.get('categoryId') || '',
    brand: searchParams.get('brand') || '',
    layout: searchParams.get('layout') || '',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    sort: searchParams.get('sort') || '',
  });

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  useEffect(() => {
    let ignore = false;

    const params = { page, size: PAGE_SIZE };
    if (applied.keyword) params.keyword = applied.keyword;
    if (applied.categoryId) params.categoryId = applied.categoryId;
    if (applied.brand) params.brand = applied.brand;
    if (applied.layout) params.layout = applied.layout;
    if (applied.minPrice) params.minPrice = applied.minPrice;
    if (applied.maxPrice) params.maxPrice = applied.maxPrice;
    if (applied.sort) params.sort = applied.sort;

    productApi
      .list(params)
      .then((res) => {
        if (!ignore) {
          setProducts(res?.content ?? []);
          setTotalPages(res?.totalPages ?? 1);
          setTotalElements(res?.totalElements ?? 0);
          setError(null);
        }
      })
      .catch((err) => {
        if (!ignore) {
          setError(err?.response?.data?.message || 'Không thể tải danh sách sản phẩm');
        }
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [page, applied, reloadKey]);

  const applyFilters = (next = {}) => {
    const merged = {
      keyword,
      categoryId,
      brand,
      layout,
      minPrice,
      maxPrice,
      sort,
      ...next,
    };
    setLoading(true);
    setApplied(merged);
    setPage(0);
    const qs = {};
    Object.entries(merged).forEach(([k, v]) => {
      if (v) qs[k] = v;
    });
    setSearchParams(qs);
    setMobileFilterOpen(false);
  };

  const handleReset = () => {
    setKeyword('');
    setCategoryId('');
    setBrand('');
    setLayout('');
    setMinPrice('');
    setMaxPrice('');
    setSort('');
    setLoading(true);
    setApplied({
      keyword: '',
      categoryId: '',
      brand: '',
      layout: '',
      minPrice: '',
      maxPrice: '',
      sort: '',
    });
    setPage(0);
    setSearchParams({});
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    applyFilters();
  };

  const handleSortChange = (e) => {
    const nextSort = e.target.value;
    setSort(nextSort);
    applyFilters({ sort: nextSort });
  };

  const handlePageChange = (nextPage) => {
    setLoading(true);
    setPage(nextPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const totalPagesArr = Array.from({ length: totalPages }, (_, i) => i);
  const visiblePages = totalPagesArr.filter(
    (p) => p === 0 || p === totalPages - 1 || Math.abs(p - page) <= 2
  );

  return (
    <div className="km-catalog">
      <nav className="km-breadcrumb" aria-label="Breadcrumb">
        <Link to="/">Trang chủ</Link>
        <span>/</span>
        <span>Sản phẩm</span>
      </nav>

      <div className="km-catalog-header">
        <h1>Tìm chiếc phím dành cho bạn</h1>
        <p className="km-catalog-subtitle">
          {loading ? 'Đang tải danh sách sản phẩm...' : `Khám phá ${totalElements} sản phẩm từ các thương hiệu yêu thích.`}
        </p>
      </div>

      {/* Search & Sort Bar */}
      <form className="km-search-sort-bar" onSubmit={handleSearchSubmit}>
        <div className="km-search-input-wrap">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.3-4.3" strokeLinecap="round" />
          </svg>
          <input
            type="text"
            className="km-search-input"
            aria-label="Tìm tên sản phẩm"
            placeholder="Tìm tên sản phẩm..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
        </div>

        <select
          className="km-sort-select"
          aria-label="Sắp xếp sản phẩm"
          value={sort}
          onChange={handleSortChange}
        >
          {SORT_OPTIONS.map((opt) => (
            <option key={opt.id} value={opt.id}>
              {opt.name}
            </option>
          ))}
        </select>

        <button
          type="button"
          className="km-btn km-btn-soft km-mobile-filter-toggle"
          onClick={() => setMobileFilterOpen((o) => !o)}
        >
          {mobileFilterOpen ? 'Ẩn bộ lọc' : 'Bộ lọc'}
        </button>
      </form>

      <div className="km-catalog-body">
        {/* Filter Sidebar */}
        <aside
          className={`km-filter-sidebar ${mobileFilterOpen ? '' : 'km-filter-sidebar--hidden'}`}
          aria-label="Bộ lọc sản phẩm"
        >
          <h2 className="km-filter-title">Bộ lọc</h2>

          <div className="km-filter-group">
            <span className="km-filter-group-label">Danh mục</span>
            {CATEGORIES.map((c) => (
              <label key={c.id} className="km-filter-option">
                <input
                  type="radio"
                  name="categoryId"
                  checked={categoryId === c.id}
                  onChange={() => setCategoryId(categoryId === c.id ? '' : c.id)}
                />
                <span>{c.name}</span>
              </label>
            ))}
          </div>

          <div className="km-filter-group">
            <label htmlFor="filter-brand" className="km-filter-group-label">
              Thương hiệu
            </label>
            <select
              id="filter-brand"
              className="km-filter-input"
              value={brand}
              onChange={(e) => setBrand(e.target.value)}
            >
              <option value="">Tất cả thương hiệu</option>
              {BRANDS.map((b) => (
                <option key={b} value={b}>
                  {b}
                </option>
              ))}
            </select>
          </div>

          <div className="km-filter-group">
            <span className="km-filter-group-label">Layout</span>
            <div className="km-filter-layout-grid">
              {LAYOUTS.map((l) => (
                <label key={l} className="km-filter-option">
                  <input
                    type="radio"
                    name="layout"
                    checked={layout === l}
                    onChange={() => setLayout(layout === l ? '' : l)}
                  />
                  <span>{l}</span>
                </label>
              ))}
            </div>
          </div>

          <div className="km-filter-group">
            <span className="km-filter-group-label">Khoảng giá (₫)</span>
            <input
              type="number"
              min="0"
              className="km-filter-input"
              aria-label="Giá tối thiểu"
              placeholder="Từ 500.000"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
            />
            <input
              type="number"
              min="0"
              className="km-filter-input"
              aria-label="Giá tối đa"
              placeholder="Đến 4.000.000"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
            />
          </div>

          <div className="km-filter-actions">
            <button type="button" className="km-btn km-btn-primary km-btn-full" onClick={() => applyFilters()}>
              Áp dụng
            </button>
            <button type="button" className="km-btn km-btn-soft km-btn-full" onClick={handleReset}>
              Đặt lại
            </button>
          </div>
        </aside>

        {/* Product Grid */}
        <section>
          {loading && <Loading message="Đang tải sản phẩm..." />}
          {!loading && error && (
            <ErrorState
              title="Lỗi tải sản phẩm"
              message={error}
              onRetry={() => {
                setLoading(true);
                setError(null);
                setReloadKey((k) => k + 1);
              }}
            />
          )}
          {!loading && !error && products.length === 0 && (
            <EmptyState
              title="Không tìm thấy sản phẩm"
              description="Hãy thử thay đổi bộ lọc hoặc từ khóa tìm kiếm khác."
              actionText="Đặt lại bộ lọc"
              onAction={handleReset}
            />
          )}

          {!loading && !error && products.length > 0 && (
            <>
              <div className="km-product-grid-3">
                {products.map((p) => (
                  <ProductCard key={p.id} product={p} />
                ))}
              </div>

              {totalPages > 1 && (
                <div style={{ display: 'flex', justifyContent: 'center' }}>
                  <nav className="km-pagination" aria-label="Phân trang sản phẩm">
                    <button
                      type="button"
                      className="km-page-btn"
                      disabled={page === 0}
                      aria-label="Trang trước"
                      onClick={() => handlePageChange(page - 1)}
                    >
                      ‹
                    </button>
                    {visiblePages.map((p, idx, arr) => (
                      <span key={p} style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                        {idx > 0 && arr[idx - 1] !== p - 1 && <span style={{ padding: '0 4px', color: 'var(--color-text-muted)' }}>…</span>}
                        <button
                          type="button"
                          className={`km-page-btn ${p === page ? 'active' : ''}`}
                          onClick={() => handlePageChange(p)}
                        >
                          {p + 1}
                        </button>
                      </span>
                    ))}
                    <button
                      type="button"
                      className="km-page-btn"
                      disabled={page >= totalPages - 1}
                      aria-label="Trang sau"
                      onClick={() => handlePageChange(page + 1)}
                    >
                      ›
                    </button>
                  </nav>
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  );
}

export default ProductListPage;
