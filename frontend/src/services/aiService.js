import api, { unwrap } from './api.js';

// AI calls can take a while; give them more time than the default.
const SLOW = { timeout: 90000 };

export const analyzeResume = (payload) => unwrap(api.post('/ai/resume-analysis', payload, SLOW));

export const generateQuestions = (jobId, count = 8) =>
  unwrap(api.post('/ai/interview-questions', { jobId, count }, SLOW));

export const evaluateAnswer = (payload) => unwrap(api.post('/ai/evaluate-answer', payload, SLOW));
