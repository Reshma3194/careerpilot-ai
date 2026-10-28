import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  addApplication,
  deleteJob,
  getJob,
  listApplications,
  updateApplication,
} from '../services/jobService.js';
import { analyzeResume } from '../services/aiService.js';
import { errorMessage } from '../services/api.js';
import { STATUSES } from '../utils/constants.js';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import MatchScoreCard from '../components/MatchScoreCard.jsx';
import ApplicationStatusBadge from '../components/ApplicationStatusBadge.jsx';

export default function JobDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [job, setJob] = useState(null);
  const [application, setApplication] = useState(null);
  const [notes, setNotes] = useState('');
  const [analysis, setAnalysis] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getJob(id), listApplications()])
      .then(([j, apps]) => {
        setJob(j);
        const app = apps.find((a) => a.job.id === j.id) ?? null;
        setApplication(app);
        setNotes(app?.notes ?? '');
      })
      .catch((err) => setError(errorMessage(err)));
  }, [id]);

  const save = async (changes, okMessage) => {
    setError('');
    setMessage('');
    try {
      const updated = application
        ? await updateApplication(application.id, changes)
        : await addApplication(job.id, changes.status);
      setApplication(updated);
      setMessage(okMessage);
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const handleAnalyze = async () => {
    setError('');
    setAnalyzing(true);
    try {
      const result = await analyzeResume({ jobId: job.id });
      setAnalysis(result);
      setApplication((a) => (a ? { ...a, matchScore: result.matchScore } : a));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setAnalyzing(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Remove this job and its tracking history?')) return;
    try {
      await deleteJob(job.id);
      navigate('/jobs');
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!job) return error ? <div className="alert alert-error" role="alert">{error}</div> : <LoadingSpinner />;

  return (
    <>
      <p><Link to="/jobs">Back to job tracker</Link></p>
      <div className="page-head">
        <h1>{job.title}</h1>
        <p>{[job.company, job.location].filter(Boolean).join(' · ')}</p>
      </div>

      {error && <div className="alert alert-error" role="alert">{error}</div>}
      {message && <div className="alert alert-ok" role="status">{message}</div>}

      <section className="panel">
        <div className="row-between">
          <h2>Your application</h2>
          {application && <ApplicationStatusBadge status={application.status} />}
        </div>
        <div className="field">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={application?.status ?? ''}
            onChange={(e) => save({ status: e.target.value }, 'Status updated.')}
          >
            {!application && <option value="" disabled>Not tracked yet</option>}
            {STATUSES.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
          </select>
          {application?.appliedDate && <div className="hint">Applied on {application.appliedDate}</div>}
        </div>
        {application && (
          <div className="field">
            <label htmlFor="notes">Notes</label>
            <textarea id="notes" value={notes} onChange={(e) => setNotes(e.target.value)} />
            <button type="button" className="btn btn-quiet btn-small" style={{ marginTop: '0.5rem' }} onClick={() => save({ notes }, 'Notes saved.')}>
              Save notes
            </button>
          </div>
        )}
        {job.jobUrl && <p><a href={job.jobUrl} target="_blank" rel="noreferrer">Open original posting</a></p>}
      </section>

      <section className="panel">
        <div className="row-between">
          <h2>Resume match</h2>
          <button type="button" className="btn" onClick={handleAnalyze} disabled={analyzing || !job.description}>
            {analyzing ? 'Analyzing…' : 'Analyze my resume'}
          </button>
        </div>
        {!job.description && <p className="hint">This job has no description. Add one to analyze your match.</p>}
        {analyzing && <LoadingSpinner label="Analyzing. This can take up to a minute." />}
        {analysis ? (
          <>
            <MatchScoreCard score={analysis.matchScore} summary={analysis.summary} />
            {analysis.gaps?.length > 0 && (
              <div className="list-block">
                <h3>What is missing</h3>
                <ul>{analysis.gaps.map((g, i) => <li key={i}>{g}</li>)}</ul>
              </div>
            )}
          </>
        ) : (
          application?.matchScore != null && <MatchScoreCard score={application.matchScore} />
        )}
        <p style={{ marginTop: '1rem' }}><Link to="/interview">Practice interview questions for this job</Link></p>
      </section>

      {job.description && (
        <section className="panel">
          <h2>Job description</h2>
          <p style={{ whiteSpace: 'pre-wrap' }}>{job.description}</p>
        </section>
      )}

      <button type="button" className="btn btn-danger" onClick={handleDelete}>Remove job</button>
    </>
  );
}
