export const TOKEN_KEY = 'careerpilot.token';
export const USER_KEY = 'careerpilot.user';

// Order matters: this is the pipeline a job moves through.
export const STATUSES = [
  { value: 'SAVED', label: 'Saved' },
  { value: 'APPLIED', label: 'Applied' },
  { value: 'INTERVIEWING', label: 'Interviewing' },
  { value: 'OFFER', label: 'Offer' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'WITHDRAWN', label: 'Withdrawn' },
];

export const statusLabel = (value) => STATUSES.find((s) => s.value === value)?.label ?? value;

export const MAX_RESUME_MB = 5;
export const RESUME_EXTENSIONS = ['.pdf', '.docx', '.txt'];
