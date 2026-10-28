import api, { unwrap } from './api.js';

export const listResumes = () => unwrap(api.get('/resumes'));

export const uploadResume = (file) => {
  const form = new FormData();
  form.append('file', file);
  return unwrap(api.post('/resumes', form));
};

export const deleteResume = (id) => unwrap(api.delete(`/resumes/${id}`));
