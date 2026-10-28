import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  addApplication,
  createJob,
  deleteJob,
  listApplications,
  updateApplication,
} from '../services/jobService.js';
import { errorMessage } from '../services/api.js';
import { STATUSES } from '../utils/constants.js';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import ApplicationStatusBadge from '../components/ApplicationStatusBadge.jsx';

const PIPELINE = STATUSES.filter((s) => ['SAVED', 'APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED'].includes(s.value));
const EMPTY_FORM = { title: '', company: '', location: '', jobUrl: '', description: '' };

export default function JobTracker() {
  const [applications, setApplications] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    listApplications()
      .then(setApplications)
      .catch((err) => setError(errorMessage(err)));
  }, []);

  useEffect(load, [load]);

  const setField = (name) => (e) => setForm((f) => ({ ...f, [name]: e.target.value }));

  const handleAdd = async (e) => {
    e.preventDefault();
    if (!form.title.trim() || !form.company.trim()) {
      return setError('Enter a job title and company.');
    }
    setError('');
    setBusy(true);
    try {
      const job = await createJob(form);
      await addApplication(job.id);
      setForm(EMPTY_FORM);
      setShowForm(false);
      load();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const handleStatus = async (application, status) => {
    try {
      const updated = await updateApplication(application.id, { status });
      setApplications((list) => list.map((a) => (a.id === updated.id ? updated : a)));
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const handleDelete = async (application) => {
    if (!window.confirm(`Remove ${application.job.title} at ${application.job.company}?`)) return;
    try {
      await deleteJob(application.job.id);
      setApplications((list) => list.filter((a) => a.id !== application.id));
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!applications) return error ? <div className="alert alert-error" role="alert">{error}</div> : <LoadingSpinner />;

  const count = (status) => applications.filter((a) => a.status === status).length;
  const visible = filter === 'ALL' ? applications : applications.filter((a) => a.status === filter);

  return (
    <>
      <div className="page-head row-between">
        <div>
          <h1>Job tracker</h1>
          <p>Move each job along as your search progresses.</p>
        </div>
        <button className="btn" type="button" onClick={() => setShowForm((v) => !v)}>
          {showForm ? 'Cancel' : 'Add job'}
        </button>
      </div>

      {error && <div className="alert alert-error" role="alert">{error}</div>}

      {showForm && (
        <form className="panel" onSubmit={handleAdd} noValidate>
          <div className="split">
            <div className="field">
              <label htmlFor="title">Job title</label>
              <input id="title" type="text" value={form.title} onChange={setField('title')} />
            </div>
            <div className="field">
              <label htmlFor="company">Company</label>
              <input id="company" type="text" value={form.company} onChange={setField('company')} />
            </div>
            <div className="field">
              <label htmlFor="location">Location</label>
              <input id="location" type="text" value={form.location} onChange={setField('location')} />
            </div>
            <div className="field">
              <label htmlFor="jobUrl">Job link</label>
              <input id="jobUrl" type="url" value={form.jobUrl} onChange={setField('jobUrl')} />
            </div>
          </div>
          <div className="field">
            <label htmlFor="description">Job description</label>
            <textarea id="description" value={form.description} onChange={setField('description')} />
            <div className="hint">Paste the full posting. It is used for resume matching and interview questions.</div>
          </div>
          <button className="btn" type="submit" disabled={busy}>{busy ? 'Saving…' : 'Save job'}</button>
        </form>
      )}

      <div className="pipeline" role="group" aria-label="Filter by status">
        <button type="button" aria-pressed={filter === 'ALL'} onClick={() => setFilter('ALL')}>
          <span className="count">{applications.length}</span>
          <span className="name">All</span>
        </button>
        {PIPELINE.map((s) => (
          <button key={s.value} type="button" aria-pressed={filter === s.value} onClick={() => setFilter(s.value)}>
            <span className="count">{count(s.value)}</span>
            <span className="name">{s.label}</span>
          </button>
        ))}
      </div>

      <section className="panel">
        {visible.length === 0 ? (
          <p className="empty">
            {applications.length === 0
              ? 'No jobs yet. Choose Add job to save your first one.'
              : 'No jobs with this status.'}
          </p>
        ) : (
          <ul className="app-list">
            {visible.map((a) => (
              <li key={a.id} className="app-item">
                <div>
                  <Link className="app-title" to={`/jobs/${a.job.id}`}>{a.job.title}</Link>{' '}
                  <ApplicationStatusBadge status={a.status} />
                  <div className="app-meta">
                    {[a.job.company, a.job.location].filter(Boolean).join(' · ')}
                    {a.matchScore != null && <> · <span className="score-chip">{a.matchScore}% match</span></>}
                  </div>
                </div>
                <div className="app-actions">
                  <select aria-label={`Status for ${a.job.title}`} value={a.status} onChange={(e) => handleStatus(a, e.target.value)}>
                    {STATUSES.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                  </select>
                  <button type="button" className="btn btn-danger btn-small" onClick={() => handleDelete(a)}>Remove</button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
