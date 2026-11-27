export default function PageNavigation({ page, totalPages, onChange, label }) {
  if (totalPages <= 1) return null;

  return (
    <nav className="km-action-row" aria-label={label}>
      <button type="button" className="km-btn km-btn-soft" disabled={page === 0}
        onClick={() => onChange(page - 1)}>
        Trang trước
      </button>
      <span>Trang {page + 1}/{totalPages}</span>
      <button type="button" className="km-btn km-btn-soft" disabled={page + 1 >= totalPages}
        onClick={() => onChange(page + 1)}>
        Trang sau
      </button>
    </nav>
  );
}
