import api, { unwrap } from './api.js';

export const listJobs = () => unwrap(api.get('/jobs'));
export const getJob = (id) => unwrap(api.get(`/jobs/${id}`));
export const createJob = (job) => unwrap(api.post('/jobs', job));
export const updateJob = (id, job) => unwrap(api.put(`/jobs/${id}`, job));
export const deleteJob = (id) => unwrap(api.delete(`/jobs/${id}`));

export const listApplications = (status) =>
  unwrap(api.get('/applications', { params: status ? { status } : {} }));
export const addApplication = (jobId, status = 'SAVED') =>
  unwrap(api.post('/applications', { jobId, status }));
export const updateApplication = (id, changes) => unwrap(api.patch(`/applications/${id}`, changes));
export const deleteApplication = (id) => unwrap(api.delete(`/applications/${id}`));
export const applicationStats = () => unwrap(api.get('/applications/stats'));
