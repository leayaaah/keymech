import { formatDateTime } from '../utils/format';
import { STATUS_LABEL } from '../utils/customStatus';

/** Tiến độ yêu cầu Custom: mỗi dòng là một lần chuyển trạng thái, cũ nhất ở trên. */
export default function StatusTimeline({ history }) {
  if (!history || history.length === 0) {
    return <p className="cr-muted">Chưa có cập nhật nào.</p>;
  }
  return (
    <ol className="cr-timeline">
      {history.map((h, i) => (
        <li key={h.id} className={i === history.length - 1 ? 'cr-timeline__item is-current' : 'cr-timeline__item'}>
          <div className="cr-timeline__head">
            <strong>{STATUS_LABEL[h.newStatus] || h.newStatus}</strong>
            <time dateTime={h.changedAt}>{formatDateTime(h.changedAt)}</time>
          </div>
          <div className="cr-muted">
            {h.oldStatus ? `Từ "${STATUS_LABEL[h.oldStatus] || h.oldStatus}" · ` : 'Gửi yêu cầu · '}
            {h.actor}
          </div>
          {h.note && <p className="cr-timeline__note">{h.note}</p>}
        </li>
      ))}
    </ol>
  );
}
