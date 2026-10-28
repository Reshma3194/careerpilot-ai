import { MAX_RESUME_MB, RESUME_EXTENSIONS } from './constants.js';

export function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim()) ? '' : 'Enter a valid email address.';
}

export function validatePassword(password) {
  if (password.length < 8) return 'Use at least 8 characters.';
  if (password.length > 72) return 'Use 72 characters or fewer.';
  return '';
}

export function validateResumeFile(file) {
  if (!file) return 'Choose a resume file.';
  const name = file.name.toLowerCase();
  if (!RESUME_EXTENSIONS.some((ext) => name.endsWith(ext))) {
    return 'Upload a PDF, DOCX or TXT file.';
  }
  if (file.size > MAX_RESUME_MB * 1024 * 1024) {
    return `File is larger than ${MAX_RESUME_MB} MB.`;
  }
  return '';
}
