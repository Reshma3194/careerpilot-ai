import { statusLabel } from '../utils/constants.js';

export default function ApplicationStatusBadge({ status }) {
  return <span className={`badge badge-${status}`}>{statusLabel(status)}</span>;
}
