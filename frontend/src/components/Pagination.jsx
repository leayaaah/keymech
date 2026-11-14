/** Điều khiển phân trang theo phản hồi {page, totalPages, totalElements}; page tính từ 0. */
export default function Pagination({ page, totalPages, totalElements, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  return (
    <nav className="cr-pager" aria-label="Phân trang">
      <button type="button" className="cr-btn" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        Trang trước
      </button>
      <span aria-live="polite">
        Trang {page + 1}/{totalPages} ({totalElements} yêu cầu)
      </span>
      <button type="button" className="cr-btn" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
        Trang sau
      </button>
    </nav>
  );
}
