import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { cartApi } from '../api/cartApi';
import chatApi from '../api/chatApi';
import formatVND from '../utils/formatVND';
import { toast } from '../utils/toast';

const MAX_MSG_LENGTH = 500;
const DEFAULT_SUGGESTIONS = [
  'Phím 75% dưới 2 triệu',
  'Switch linear',
  'Tư vấn Custom',
];

function ChatProductRecommendations({ message, addingMessage, onAdd, onOpenProduct }) {
  const { products } = message;
  const inStock = products.filter((product) => product.inStock);
  const allAdded = inStock.every((product) => message.cartAddedIds?.includes(product.id));
  let label = message.cartAddedIds?.length ? 'Thêm các món còn lại' : 'Thêm tất cả vào giỏ hàng';
  if (allAdded) label = 'Đã thêm vào giỏ hàng';
  if (message.cartNeedsReview) label = 'Kiểm tra giỏ hàng trước khi thêm lại';
  if (addingMessage === message) label = 'Đang thêm vào giỏ…';

  return (
    <div className="km-chat-products">
      {products.map((product) => (
        <Link
          key={product.id}
          to={product.url || `/products/${product.id}`}
          className="km-chat-product-card"
          onClick={onOpenProduct}
        >
          <div style={{ fontWeight: 600, fontSize: '13px', color: 'var(--color-text)' }}>
            {product.name}
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '12px' }}>
            <strong style={{ color: 'var(--color-primary)' }}>
              {formatVND(product.price)}{product.quantity > 1 ? ` × ${product.quantity}` : ''}
            </strong>
            <span style={{ color: product.inStock ? 'var(--color-success)' : 'var(--color-danger)' }}>
              {product.inStock ? 'Còn hàng' : 'Hết hàng'}
            </span>
          </div>
        </Link>
      ))}
      {inStock.length > 0 && message.intent !== 'customAlternatives' && (
        <>
          {inStock.length < products.length && <p className="km-field-hint">Chỉ thêm các món còn hàng.</p>}
          <button
            type="button"
            className="km-btn km-btn-primary km-btn-full"
            disabled={addingMessage !== null || allAdded || message.cartNeedsReview}
            onClick={() => onAdd(message)}
          >
            {label}
          </button>
        </>
      )}
    </div>
  );
}

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
  const [addingMessage, setAddingMessage] = useState(null);
  const addingToCart = useRef(false);
  const loadedHistory = useRef(false);
  const chatContext = useRef(null);
  const chatBodyRef = useRef(null);
  const lastMessageRef = useRef(null);
  const endRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    const openCompatibilityChat = () => {
      setOpen(true);
      setInput((current) => current || 'Làm sao kiểm tra tương thích linh kiện Custom?');
      inputRef.current?.focus();
    };
    window.addEventListener('keymech:chat-compatibility', openCompatibilityChat);
    return () => window.removeEventListener('keymech:chat-compatibility', openCompatibilityChat);
  }, []);

  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  useEffect(() => {
    if (!open || loadedHistory.current) return;
    loadedHistory.current = true;
    chatApi
      .getHistory()
      .then((history) => {
        if (Array.isArray(history) && history.length > 0) {
          setMessages((previous) => previous.length === 0 ? history : previous);
        }
      })
      .catch(() => {
        // Phiên mới hoặc chưa có lịch sử
      });
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const lastMessage = lastMessageRef.current;
    if (!sending && lastMessage && lastMessage.offsetHeight > (chatBodyRef.current?.clientHeight ?? 0)) {
      lastMessage.scrollIntoView({ behavior: 'smooth', block: 'start' });
      return;
    }
    endRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' });
  }, [messages, open, sending]);

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
      const res = await chatApi.sendMessage(text, chatContext.current);
      chatContext.current = res?.context ?? null;
      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          content: res?.answer || 'Xin lỗi, tôi chưa tìm thấy câu trả lời phù hợp.',
          intent: res?.intent,
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

  const addRecommendationsToCart = async (message) => {
    if (addingToCart.current || message.cartNeedsReview) return;
    const addedIds = new Set(message.cartAddedIds);
    const pending = message.products.filter((product) => product.inStock && !addedIds.has(product.id));
    if (pending.length === 0) return;

    addingToCart.current = true;
    setAddingMessage(message);
    const errors = [];
    let needsReview = false;

    try {
      for (const product of pending) {
        try {
          await cartApi.addItem({ productId: product.id, quantity: product.quantity ?? 1 });
          addedIds.add(product.id);
        } catch (err) {
          const status = err?.response?.status;
          if (!status || status >= 500) {
            needsReview = true;
            break;
          }
          errors.push(`${product.name}: ${err.response.data?.message || 'Không thể thêm vào giỏ hàng'}`);
          if (status === 401 || status === 403) break;
        }
      }

      const addedCount = addedIds.size - (message.cartAddedIds?.length ?? 0);
      if (addedCount > 0 || needsReview) window.dispatchEvent(new CustomEvent('keymech:cart-updated'));
      if (addedCount > 0) toast.success(`Đã thêm ${addedCount} sản phẩm vào giỏ hàng`);
      if (needsReview) toast.error('Chưa xác nhận được kết quả. Vui lòng kiểm tra giỏ hàng trước khi thêm lại.');
      if (errors.length > 0) toast.error(errors.join(' • '));

      setMessages((previous) => previous.map((item) => item === message
        ? { ...item, cartAddedIds: [...addedIds], cartNeedsReview: needsReview }
        : item));
    } finally {
      addingToCart.current = false;
      setAddingMessage(null);
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

          <div className="km-chat-body" ref={chatBodyRef}>
            {messages.length === 0 && (
              <div className="km-chat-bubble km-chat-bubble--bot">
                Chào bạn! Bạn đang tìm bàn phím layout nào, loại switch hay tầm giá bao nhiêu?
              </div>
            )}

            {messages.map((m, idx) => (
              <div
                key={idx}
                ref={idx === messages.length - 1 ? lastMessageRef : undefined}
                style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}
              >
                <div
                  className={`km-chat-bubble ${
                    m.role === 'user' ? 'km-chat-bubble--user' : 'km-chat-bubble--bot'
                  }`}
                >
                  {m.content}
                </div>

                {m.products?.length > 0 && (
                  <ChatProductRecommendations
                    message={m}
                    addingMessage={addingMessage}
                    onAdd={addRecommendationsToCart}
                    onOpenProduct={() => setOpen(false)}
                  />
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
              ref={inputRef}
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
