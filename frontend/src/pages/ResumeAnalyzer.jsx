import { useEffect, useState } from 'react';
import { listResumes } from '../services/resumeService.js';
import { listJobs } from '../services/jobService.js';
import { analyzeResume } from '../services/aiService.js';
import { errorMessage } from '../services/api.js';
import ResumeUploader from '../components/ResumeUploader.jsx';
import MatchScoreCard from '../components/MatchScoreCard.jsx';
import LoadingSpinner from '../components/LoadingSpinner.jsx';

function Items({ title, items }) {
  if (!items?.length) return null;
  return (
    <div className="list-block">
      <h3>{title}</h3>
      <ul>{items.map((item, i) => <li key={i}>{item}</li>)}</ul>
    </div>
  );
}

export default function ResumeAnalyzer() {
  const [resumes, setResumes] = useState(null);
  const [jobs, setJobs] = useState([]);
  const [resumeId, setResumeId] = useState('');
  const [source, setSource] = useState('tracked'); // 'tracked' | 'paste'
  const [jobId, setJobId] = useState('');
  const [jobTitle, setJobTitle] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([listResumes(), listJobs()])
      .then(([r, j]) => {
        setResumes(r);
        setJobs(j);
        if (r.length) setResumeId(String(r[0].id));
        if (j.length) setJobId(String(j[0].id));
        else setSource('paste');
      })
      .catch((err) => { setResumes([]); setError(errorMessage(err)); });
  }, []);

  const handleUploaded = (resume) => {
    setResumes((list) => [resume, ...(list ?? [])]);
    setResumeId(String(resume.id));
  };

  const handleAnalyze = async (e) => {
    e.preventDefault();
    if (!resumeId) return setError('Upload a resume first.');
    if (source === 'tracked' && !jobId) return setError('Choose a job to compare against.');
    if (source === 'paste' && !jobDescription.trim()) return setError('Paste a job description.');
    setError('');
    setResult(null);
    setBusy(true);
    try {
      const payload = { resumeId: Number(resumeId) };
      if (source === 'tracked') payload.jobId = Number(jobId);
      else { payload.jobTitle = jobTitle; payload.jobDescription = jobDescription; }
      setResult(await analyzeResume(payload));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  if (!resumes) return <LoadingSpinner />;

  const trackedJobsWithText = jobs.filter((j) => j.description);

  return (
    <>
      <div className="page-head">
        <h1>Resume analyzer</h1>
        <p>See how well your resume fits a job and what to improve.</p>
      </div>

      <section className="panel">
        <h2>1. Your resume</h2>
        <ResumeUploader onUploaded={handleUploaded} />
        {resumes.length > 0 && (
          <div className="field" style={{ marginTop: '1rem' }}>
            <label htmlFor="resume">Resume to analyze</label>
            <select id="resume" value={resumeId} onChange={(e) => setResumeId(e.target.value)}>
              {resumes.map((r) => <option key={r.id} value={r.id}>{r.fileName}</option>)}
            </select>
          </div>
        )}
      </section>

      <form className="panel" onSubmit={handleAnalyze}>
        <h2>2. The job</h2>
        <div className="row" role="radiogroup" aria-label="Job source" style={{ marginBottom: '1rem' }}>
          <label style={{ margin: 0 }}>
            <input type="radio" name="source" checked={source === 'tracked'} onChange={() => setSource('tracked')} disabled={jobs.length === 0} />{' '}
            A job I am tracking
          </label>
          <label style={{ margin: 0 }}>
            <input type="radio" name="source" checked={source === 'paste'} onChange={() => setSource('paste')} />{' '}
            Paste a description
          </label>
        </div>

        {source === 'tracked' ? (
          <div className="field">
            <label htmlFor="job">Job</label>
            <select id="job" value={jobId} onChange={(e) => setJobId(e.target.value)}>
              {jobs.map((j) => <option key={j.id} value={j.id}>{j.title} · {j.company}</option>)}
            </select>
            {trackedJobsWithText.length < jobs.length && (
              <div className="hint">Jobs without a saved description cannot be analyzed.</div>
            )}
          </div>
        ) : (
          <>
            <div className="field">
              <label htmlFor="jobTitle">Job title</label>
              <input id="jobTitle" type="text" value={jobTitle} onChange={(e) => setJobTitle(e.target.value)} />
            </div>
            <div className="field">
              <label htmlFor="jobDescription">Job description</label>
              <textarea id="jobDescription" value={jobDescription} onChange={(e) => setJobDescription(e.target.value)} />
            </div>
          </>
        )}

        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <button className="btn" type="submit" disabled={busy}>{busy ? 'Analyzing…' : 'Analyze match'}</button>
      </form>

      {busy && <LoadingSpinner label="Reading your resume against the job. This can take up to a minute." />}

      {result && (
        <section className="panel">
          <MatchScoreCard score={result.matchScore} summary={result.summary} />
          <Items title="What matches" items={result.strengths} />
          <Items title="What is missing" items={result.gaps} />
          {result.missingKeywords?.length > 0 && (
            <div className="list-block">
              <h3>Keywords to add</h3>
              <div className="tags">{result.missingKeywords.map((k, i) => <span key={i} className="tag">{k}</span>)}</div>
            </div>
          )}
          <Items title="How to improve" items={result.suggestions} />
        </section>
      )}
    </>
  );
}
