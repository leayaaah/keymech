import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import chatApi from '../api/chatApi';
import formatVND from '../utils/formatVND';

const MAX_MSG_LENGTH = 500;
const DEFAULT_SUGGESTIONS = [
  'Phím 75% dưới 2 triệu',
  'Switch linear',
  'Tư vấn Custom',
];

/**
 * Widget Chatbot tư vấn KeyMech (Figma frames 3:334 desktop & 3:492 mobile).
 * Chỉ hiển thị cho Guest và Customer (không hiển thị cho Staff/Admin).
 */
export function ChatWidget() {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [suggestions, setSuggestions] = useState(DEFAULT_SUGGESTIONS);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [rateLimited, setRateLimited] = useState('');
  const loadedHistory = useRef(false);
  const endRef = useRef(null);

  useEffect(() => {
    if (!open || loadedHistory.current) return;
    loadedHistory.current = true;
    chatApi
      .getHistory()
      .then((history) => {
        if (Array.isArray(history) && history.length > 0) {
          setMessages(history);
        }
      })
      .catch(() => {
        // Phiên mới hoặc chưa có lịch sử
      });
  }, [open]);

  useEffect(() => {
    if (open) {
      endRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, open]);

  const sendText = async (rawText) => {
    const text = rawText.trim();
    if (!text || sending) return;
    if (text.length > MAX_MSG_LENGTH) {
      setRateLimited(`Câu hỏi tối đa ${MAX_MSG_LENGTH} ký tự.`);
      return;
    }

    setRateLimited('');
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', content: text }]);
    setSending(true);

    try {
      const res = await chatApi.sendMessage(text);
      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          content: res?.answer || 'Xin lỗi, tôi chưa tìm thấy câu trả lời phù hợp.',
          products: Array.isArray(res?.products) ? res.products : [],
        },
      ]);
      if (Array.isArray(res?.suggestions) && res.suggestions.length > 0) {
        setSuggestions(res.suggestions);
      }
    } catch (err) {
      const status = err?.response?.status;
      const msg =
        status === 429
          ? 'Bạn gửi câu hỏi quá nhanh, vui lòng thử lại sau'
          : err?.response?.data?.message || 'Không thể kết nối trợ lý tư vấn. Vui lòng thử lại.';
      setRateLimited(msg);
    } finally {
      setSending(false);
    }
  };

  return (
    <>
      {!open && (
        <button
          type="button"
          className="km-chat-fab"
          aria-label="Mở cửa sổ Chatbot tư vấn"
          onClick={() => setOpen(true)}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      )}

      {open && (
        <aside className="km-chat-panel" aria-label="Tư vấn KeyMech">
          <div className="km-chat-header">
            <div>
              <strong>Tư vấn KeyMech</strong>
              <div style={{ fontSize: '11px', opacity: 0.85 }}>Trợ lý tìm bàn phím & switch</div>
            </div>
            <button
              type="button"
              className="km-chat-close"
              aria-label="Đóng cửa sổ tư vấn"
              onClick={() => setOpen(false)}
            >
              ×
            </button>
          </div>

          <div className="km-chat-notice">
            Hệ thống có thể ghi nhận sản phẩm bạn xem trong phiên để gợi ý phù hợp hơn. Giá và tồn kho luôn lấy trực tiếp từ cửa hàng.
          </div>

          <div className="km-chat-body">
            {messages.length === 0 && (
              <div className="km-chat-bubble km-chat-bubble--bot">
                Chào bạn! Bạn đang tìm bàn phím layout nào, loại switch hay tầm giá bao nhiêu?
              </div>
            )}

            {messages.map((m, idx) => (
              <div key={idx} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div
                  className={`km-chat-bubble ${
                    m.role === 'user' ? 'km-chat-bubble--user' : 'km-chat-bubble--bot'
                  }`}
                >
                  {m.content}
                </div>

                {m.products && m.products.length > 0 && (
                  <div className="km-chat-products">
                    {m.products.map((p) => (
                      <Link
                        key={p.id}
                        to={p.url || `/products/${p.id}`}
                        className="km-chat-product-card"
                        onClick={() => setOpen(false)}
                      >
                        <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--color-text)' }}>
                          {p.name}
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px' }}>
                          <strong style={{ color: 'var(--color-primary)' }}>{formatVND(p.price)}</strong>
                          <span style={{ color: p.inStock ? 'var(--color-success)' : 'var(--color-danger)' }}>
                            {p.inStock ? 'Còn hàng' : 'Hết hàng'}
                          </span>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            ))}

            {sending && (
              <div className="km-chat-bubble km-chat-bubble--bot">Đang tìm gợi ý...</div>
            )}
            <div ref={endRef} />
          </div>

          {rateLimited && (
            <div role="alert" className="km-chat-error">
              {rateLimited}
            </div>
          )}

          <div className="km-chat-suggestions">
            {suggestions.map((s) => (
              <button
                key={s}
                type="button"
                className="km-chat-chip"
                disabled={sending}
                onClick={() => sendText(s)}
              >
                {s}
              </button>
            ))}
          </div>

          <form
            className="km-chat-form"
            onSubmit={(e) => {
              e.preventDefault();
              sendText(input);
            }}
          >
            <input
              type="text"
              className="km-chat-input"
              aria-label="Nhập câu hỏi tư vấn"
              placeholder="Nhập câu hỏi (tối đa 500 ký tự)..."
              maxLength={MAX_MSG_LENGTH}
              value={input}
              onChange={(e) => setInput(e.target.value)}
            />
            <button type="submit" className="km-btn km-btn-primary km-btn-sm" disabled={sending || !input.trim()}>
              Gửi
            </button>
          </form>
        </aside>
      )}
    </>
  );
}

export default ChatWidget;
