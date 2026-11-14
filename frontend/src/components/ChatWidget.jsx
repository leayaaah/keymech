import { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { getChatHistory, sendChatMessage } from '../api/chatApi';
import { formatVND } from '../utils/format';
import './ChatWidget.css';

const MAX_LENGTH = 500;
const STARTERS = ['Phím 75% dưới 2 triệu', 'Switch nào gõ êm?', 'Custom gồm những gì?', 'Chính sách đổi trả'];
const HIDDEN_ROLES = ['STAFF', 'ADMIN'];

const errorText = (err) =>
  err?.response?.status === 429
    ? err.response.data?.message || 'Bạn gửi câu hỏi quá nhanh, vui lòng thử lại sau'
    : 'Chưa gửi được câu hỏi. Vui lòng thử lại.';

// Lịch sử từ server có thể là mảng hoặc {messages: []}; role là user/assistant.
const normalizeHistory = (raw) => {
  const list = Array.isArray(raw) ? raw : raw?.messages || [];
  return list
    .filter((m) => m && (m.content || m.text))
    .map((m, i) => ({
      id: `h${i}`,
      role: m.role === 'user' ? 'user' : 'assistant',
      text: m.content ?? m.text,
      products: m.products || [],
    }));
};

/**
 * Hộp chat tư vấn (G-04), chỉ hiện với Guest và Customer. Truyền `user` là người dùng hiện tại
 * (null nếu chưa đăng nhập); Staff và Admin không thấy hộp chat (quy ước mục 10).
 * Giá, tồn kho, liên kết trong thẻ sản phẩm do server lấy từ CSDL; widget chỉ hiển thị.
 */
export default function ChatWidget({ user = null }) {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [suggestions, setSuggestions] = useState(STARTERS);
  const [mode, setMode] = useState(null);
  const [draft, setDraft] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);
  const loadedRef = useRef(false);
  const inputRef = useRef(null);
  const listRef = useRef(null);
  const seq = useRef(0);

  const nextId = () => `m${seq.current++}`;

  // Khôi phục lịch sử (tối đa 20 tin trong Session) lần đầu mở hộp chat
  useEffect(() => {
    if (!open || loadedRef.current) return;
    loadedRef.current = true;
    getChatHistory()
      .then((raw) => setMessages(normalizeHistory(raw)))
      .catch(() => {});   // không có lịch sử thì bắt đầu cuộc trò chuyện mới
  }, [open]);

  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  useEffect(() => {
    const el = listRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages, sending, open]);

  const send = useCallback(
    async (raw) => {
      const text = raw.trim();
      if (!text || sending) return;
      setError(null);
      setDraft('');
      setSending(true);
      setMessages((m) => [...m, { id: nextId(), role: 'user', text, products: [] }]);
      try {
        const res = await sendChatMessage(text);
        setMessages((m) => [...m, { id: nextId(), role: 'assistant', text: res.answer, products: res.products || [] }]);
        setSuggestions(res.suggestions?.length ? res.suggestions : []);
        setMode(res.mode || null);
      } catch (err) {
        setError(errorText(err));
      } finally {
        setSending(false);
        inputRef.current?.focus();
      }
    },
    [sending],
  );

  if (user && HIDDEN_ROLES.includes(user.role)) return null;

  const onKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send(draft);
    } else if (e.key === 'Escape') {
      setOpen(false);
    }
  };

  return (
    <div className="chat">
      {open && (
        <section className="chat__panel" role="dialog" aria-label="Tư vấn bàn phím cơ" onKeyDown={(e) => e.key === 'Escape' && setOpen(false)}>
          <header className="chat__header">
            <div>
              <strong>Tư vấn bàn phím cơ</strong>
              {mode === 'rule' && <div className="chat__mode">Đang trả lời ở chế độ cơ bản</div>}
            </div>
            <button type="button" className="chat__close" onClick={() => setOpen(false)} aria-label="Đóng hộp chat">×</button>
          </header>

          <div className="chat__list" ref={listRef} role="log" aria-live="polite" aria-relevant="additions">
            <p className="chat__notice">Gợi ý dựa trên các sản phẩm bạn đã xem trong phiên này.</p>

            {messages.length === 0 && (
              <p className="chat__bubble chat__bubble--bot">
                Xin chào! Bạn cần tìm bàn phím, switch, keycap hay muốn hỏi về dịch vụ Custom?
              </p>
            )}

            {messages.map((m) => (
              <div key={m.id} className={`chat__row chat__row--${m.role}`}>
                <p className={`chat__bubble chat__bubble--${m.role === 'user' ? 'user' : 'bot'}`}>{m.text}</p>
                {m.products.length > 0 && (
                  <ul className="chat__products">
                    {m.products.map((p) => (
                      <li key={p.id}>
                        <Link to={p.url || `/products/${p.id}`} className="chat__product">
                          {(p.imageUrl || p.image) && <img src={p.imageUrl || p.image} alt={p.name} loading="lazy" />}
                          <span className="chat__product-info">
                            <span className="chat__product-name">{p.name}</span>
                            <span>{formatVND(p.price)}</span>
                            <span className={p.inStock === false ? 'chat__stock is-out' : 'chat__stock'}>
                              {p.inStock === false ? 'Hết hàng' : 'Còn hàng'}
                            </span>
                          </span>
                        </Link>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            ))}

            {sending && <p className="chat__bubble chat__bubble--bot chat__typing" role="status">Đang trả lời…</p>}
            {error && <p className="chat__error" role="alert">{error}</p>}
          </div>

          {suggestions.length > 0 && !sending && (
            <div className="chat__chips">
              {suggestions.map((s) => (
                <button key={s} type="button" className="chat__chip" onClick={() => send(s)}>{s}</button>
              ))}
            </div>
          )}

          <form
            className="chat__form"
            onSubmit={(e) => {
              e.preventDefault();
              send(draft);
            }}
          >
            <label htmlFor="chat-input" className="chat__sr">Câu hỏi của bạn</label>
            <textarea
              id="chat-input"
              ref={inputRef}
              rows={2}
              maxLength={MAX_LENGTH}
              value={draft}
              placeholder="Nhập câu hỏi…"
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={onKeyDown}
            />
            <div className="chat__form-foot">
              <span className="chat__count">{draft.length}/{MAX_LENGTH}</span>
              <button type="submit" className="chat__send" disabled={sending || !draft.trim()}>Gửi</button>
            </div>
          </form>
        </section>
      )}

      <button type="button" className="chat__toggle" onClick={() => setOpen((o) => !o)} aria-expanded={open}>
        {open ? 'Thu gọn' : 'Hỏi tư vấn'}
      </button>
    </div>
  );
}
