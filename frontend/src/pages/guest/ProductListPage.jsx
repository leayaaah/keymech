import { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import formatVND from '../../utils/formatVND';
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

const LAYOUTS = ['60%', '65%', '75%', 'TKL', 'Fullsize'];
const FILTER_KEYS = ['keyword', 'categoryId', 'brand', 'layout', 'minPrice', 'maxPrice', 'sort'];
const supportsLayout = (categoryId) => !categoryId || categoryId === '1' || categoryId === '5';
const validPrice = (value) => value === '' || (Number.isFinite(Number(value)) && Number(value) >= 0);

function readFilters(searchParams) {
  const filters = Object.fromEntries(FILTER_KEYS.map((key) => [key, searchParams.get(key) || '']));
  if (!supportsLayout(filters.categoryId)) filters.layout = '';
  if (filters.layout === 'Full-size') filters.layout = 'Fullsize';
  return filters;
}

const SORT_OPTIONS = [
  { id: '', name: 'Sắp xếp: Mặc định' },
  { id: 'price-asc', name: 'Giá tăng dần' },
  { id: 'price-desc', name: 'Giá giảm dần' },
  { id: 'name-asc', name: 'Tên A–Z' },
];

/**
 * Trang danh sách sản phẩm (Figma frame 3:132).
 */
export function ProductListPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const query = searchParams.toString();
  const pageParam = Number(searchParams.get('page'));
  const page = Number.isSafeInteger(pageParam) && pageParam >= 0 ? pageParam : 0;
  const applied = readFilters(searchParams);

  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [draft, setDraft] = useState(null);
  const { keyword, minPrice, maxPrice } = draft?.query === query ? draft : applied;
  const { categoryId, brand, layout, sort } = applied;
  const [priceError, setPriceError] = useState(null);

  function updateDraft(key, value) {
    setDraft({ query, keyword, minPrice, maxPrice, [key]: value });
    setPriceError(null);
  }

  const [products, setProducts] = useState([]);
  const [loadedQuery, setLoadedQuery] = useState(null);
  const [error, setError] = useState(null);
  const [reloadKey, setReloadKey] = useState(0);
  const [mobileFilterOpen, setMobileFilterOpen] = useState(false);

  useEffect(() => {
    let ignore = false;

    const params = { page, size: PAGE_SIZE };
    for (const [key, value] of Object.entries(readFilters(new URLSearchParams(query)))) {
      if (value) params[key] = value;
    }

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
        if (!ignore) setLoadedQuery({ query, reloadKey });
      });

    return () => {
      ignore = true;
    };
  }, [page, query, reloadKey]);

  const loading = loadedQuery?.query !== query || loadedQuery?.reloadKey !== reloadKey;

  function navigateFilters(filters, nextPage = 0, draftOverrides = null) {
    const next = new URLSearchParams();
    for (const key of FILTER_KEYS) {
      if (filters[key]) next.set(key, filters[key]);
    }
    if (!supportsLayout(filters.categoryId)) next.delete('layout');
    if (nextPage > 0) next.set('page', String(nextPage));
    setDraft(draftOverrides ? { query: next.toString(), keyword, minPrice, maxPrice, ...draftOverrides } : null);
    setPriceError(null);
    setSearchParams(next);
  }

  function handlePriceSubmit(event) {
    event.preventDefault();
    if (event.currentTarget.querySelector(':invalid') || !validPrice(minPrice) || !validPrice(maxPrice) ||
        (minPrice !== '' && maxPrice !== '' && Number(minPrice) > Number(maxPrice))) {
      setPriceError(query);
      return;
    }
    navigateFilters({ ...applied, minPrice, maxPrice }, 0, {});
  }

  function handleReset() {
    navigateFilters({});
  }

  function handleSearchSubmit(event) {
    event.preventDefault();
    navigateFilters({ ...applied, keyword: keyword.trim() }, 0, { keyword: keyword.trim() });
  }

  function handlePageChange(nextPage) {
    navigateFilters(applied, nextPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  const chips = [
    ['keyword', applied.keyword],
    ['categoryId', CATEGORIES.find(({ id }) => id === categoryId)?.name],
    ['brand', brand],
    ['layout', layout === 'Fullsize' ? 'Full-size' : layout],
    ['minPrice', applied.minPrice && `Từ ${formatVND(Number(applied.minPrice))}`],
    ['maxPrice', applied.maxPrice && `Đến ${formatVND(Number(applied.maxPrice))}`],
  ].filter(([, label]) => label);

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
        <h1 className="km-page-title">Tìm chiếc phím dành cho bạn</h1>
        <p className="km-catalog-subtitle" aria-live="polite">
          {loading ? 'Đang tải danh sách sản phẩm...' : error ? '' : `${totalElements} sản phẩm`}
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
            onChange={(e) => updateDraft('keyword', e.target.value)}
          />
        </div>

        <select
          className="km-sort-select"
          aria-label="Sắp xếp sản phẩm"
          value={sort}
          onChange={(e) => navigateFilters({ ...applied, sort: e.target.value }, 0, {})}
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
          aria-expanded={mobileFilterOpen}
          aria-controls="catalog-filters"
          onClick={() => setMobileFilterOpen((o) => !o)}
        >
          {mobileFilterOpen ? 'Ẩn bộ lọc' : 'Bộ lọc'}
        </button>
      </form>

      <div className="km-catalog-body">
        {/* Filter Sidebar */}
        <aside
          id="catalog-filters"
          className={`km-filter-sidebar ${mobileFilterOpen ? '' : 'km-filter-sidebar--hidden'}`}
          aria-label="Bộ lọc sản phẩm"
        >
          <h2 className="km-filter-title">Bộ lọc</h2>

          <div className="km-filter-group">
            <span className="km-filter-group-label">Danh mục</span>
            {[{ id: '', name: 'Tất cả' }, ...CATEGORIES].map((c) => (
              <label key={c.id} className="km-filter-option">
                <input
                  type="radio"
                  name="categoryId"
                  checked={categoryId === c.id}
                  onChange={() => navigateFilters({ ...applied, categoryId: c.id }, 0, {})}
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
              onChange={(e) => navigateFilters({ ...applied, brand: e.target.value }, 0, {})}
            >
              <option value="">Tất cả thương hiệu</option>
              {BRANDS.map((b) => (
                <option key={b} value={b}>
                  {b}
                </option>
              ))}
            </select>
          </div>

          {supportsLayout(categoryId) && (
            <div className="km-filter-group">
              <span className="km-filter-group-label">Layout</span>
              <div className="km-filter-layout-grid">
                {['', ...LAYOUTS].map((l) => (
                  <label key={l} className="km-filter-option">
                    <input
                      type="radio"
                      name="layout"
                      checked={layout === l}
                      onChange={() => navigateFilters({ ...applied, layout: l }, 0, {})}
                    />
                    <span>{l === 'Fullsize' ? 'Full-size' : l || 'Tất cả'}</span>
                  </label>
                ))}
              </div>
            </div>
          )}

          <form className="km-filter-group" onSubmit={handlePriceSubmit} noValidate>
            <span className="km-filter-group-label">Khoảng giá</span>
            {[
              ['minPrice', 'Giá tối thiểu', '500000', minPrice],
              ['maxPrice', 'Giá tối đa', '4000000', maxPrice],
            ].map(([key, label, placeholder, value]) => (
              <label className="km-price-field" key={key}>
                <input
                  type="number"
                  min="0"
                  step="any"
                  className="km-filter-input"
                  aria-label={label}
                  aria-invalid={priceError === query}
                  aria-describedby={priceError === query ? 'catalog-price-error' : undefined}
                  placeholder={placeholder}
                  value={value}
                  onChange={(event) => updateDraft(key, event.target.value)}
                />
                <span aria-hidden="true">₫</span>
              </label>
            ))}
            {priceError === query && (
              <p id="catalog-price-error" className="km-field-error" role="alert">
                Giá phải từ 0 ₫ và giá tối thiểu không được lớn hơn giá tối đa.
              </p>
            )}
            <button type="submit" className="km-btn km-btn-primary km-btn-full">Áp dụng giá</button>
          </form>
          <button type="button" className="km-btn km-btn-soft km-btn-full" onClick={handleReset}>
            Đặt lại
          </button>
        </aside>

        {/* Product Grid */}
        <section>
          {chips.length > 0 && (
            <ul className="km-filter-chips" aria-label="Bộ lọc đang áp dụng">
              {chips.map(([key, label]) => (
                <li key={key}>
                  <button type="button" aria-label={'Xóa bộ lọc ' + label} onClick={() => navigateFilters({ ...applied, [key]: '' })}>
                    {label} <span aria-hidden="true">×</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
          {loading && <Loading message="Đang tải sản phẩm..." />}
          {!loading && error && (
            <ErrorState
              title="Lỗi tải sản phẩm"
              message={error}
              onRetry={() => {
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
