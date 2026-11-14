import { STATUS_LABEL, STATUS_TONE } from '../utils/customStatus';

export default function CustomStatusBadge({ status }) {
  return (
    <span className={`cr-badge cr-badge--${STATUS_TONE[status] || 'neutral'}`}>
      {STATUS_LABEL[status] || status}
    </span>
  );
}
