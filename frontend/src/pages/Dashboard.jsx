import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { applicationStats, listApplications } from '../services/jobService.js';
import { listResumes } from '../services/resumeService.js';
import { errorMessage } from '../services/api.js';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import ApplicationStatusBadge from '../components/ApplicationStatusBadge.jsx';

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recent, setRecent] = useState([]);
  const [resumeCount, setResumeCount] = useState(0);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([applicationStats(), listApplications(), listResumes()])
      .then(([s, apps, resumes]) => {
        setStats(s);
        setRecent(apps.slice(0, 5));
        setResumeCount(resumes.length);
      })
      .catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <div className="alert alert-error" role="alert">{error}</div>;
  if (!stats) return <LoadingSpinner />;

  const firstName = user?.fullName?.split(' ')[0];
  const active = stats.APPLIED + stats.INTERVIEWING;

  return (
    <>
      <div className="page-head">
        <h1>Welcome back, {firstName}</h1>
        <p>Here is where your job search stands.</p>
      </div>

      <section className="panel">
        <div className="stat-line">
          <div className="stat"><span className="num">{stats.TOTAL}</span><span className="lbl">Jobs tracked</span></div>
          <div className="stat"><span className="num">{active}</span><span className="lbl">In progress</span></div>
          <div className="stat"><span className="num">{stats.INTERVIEWING}</span><span className="lbl">Interviewing</span></div>
          <div className="stat"><span className="num">{stats.OFFER}</span><span className="lbl">Offers</span></div>
        </div>
      </section>

      {resumeCount === 0 && (
        <section className="panel">
          <h2>Upload your resume</h2>
          <p>Add a resume to see how well it matches the jobs you are tracking.</p>
          <Link className="btn" to="/resume">Upload resume</Link>
        </section>
      )}

      <section className="panel">
        <div className="row-between">
          <h2>Recent activity</h2>
          <Link to="/jobs">View all jobs</Link>
        </div>
        {recent.length === 0 ? (
          <p className="empty">No jobs yet. <Link to="/jobs">Add your first job</Link> to start tracking.</p>
        ) : (
          <ul className="app-list">
            {recent.map((a) => (
              <li key={a.id} className="app-item">
                <div>
                  <Link className="app-title" to={`/jobs/${a.job.id}`}>{a.job.title}</Link>
                  <div className="app-meta">{a.job.company}</div>
                </div>
                <div className="app-actions">
                  {a.matchScore != null && <span className="score-chip">{a.matchScore}% match</span>}
                  <ApplicationStatusBadge status={a.status} />
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
