import { useEffect, useState } from 'react';
import { fetchMe } from '../services/authService.js';
import { deleteResume, listResumes } from '../services/resumeService.js';
import { errorMessage } from '../services/api.js';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import ResumeUploader from '../components/ResumeUploader.jsx';

export default function Profile() {
  const [me, setMe] = useState(null);
  const [resumes, setResumes] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([fetchMe(), listResumes()])
      .then(([m, r]) => { setMe(m); setResumes(r); })
      .catch((err) => setError(errorMessage(err)));
  }, []);

  const handleDelete = async (resume) => {
    if (!window.confirm(`Delete ${resume.fileName}?`)) return;
    try {
      await deleteResume(resume.id);
      setResumes((list) => list.filter((r) => r.id !== resume.id));
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  if (!me) return error ? <div className="alert alert-error" role="alert">{error}</div> : <LoadingSpinner />;

  return (
    <>
      <div className="page-head"><h1>Profile</h1></div>
      {error && <div className="alert alert-error" role="alert">{error}</div>}

      <section className="panel">
        <dl className="facts">
          <dt>Name</dt><dd>{me.fullName}</dd>
          <dt>Email</dt><dd>{me.email}</dd>
          <dt>Member since</dt><dd>{new Date(me.createdAt).toLocaleDateString()}</dd>
        </dl>
      </section>

      <section className="panel">
        <h2>Your resumes</h2>
        {resumes.length === 0 ? (
          <p className="empty">No resumes uploaded yet.</p>
        ) : (
          <ul className="app-list">
            {resumes.map((r) => (
              <li key={r.id} className="app-item">
                <div>
                  <span className="app-title">{r.fileName}</span>
                  <div className="app-meta">Uploaded {new Date(r.uploadedAt).toLocaleDateString()} · {r.characterCount.toLocaleString()} characters read</div>
                </div>
                <button type="button" className="btn btn-danger btn-small" onClick={() => handleDelete(r)}>Delete</button>
              </li>
            ))}
          </ul>
        )}
        <div style={{ marginTop: '1rem' }}>
          <ResumeUploader onUploaded={(r) => setResumes((list) => [r, ...list])} />
        </div>
      </section>
    </>
  );
}
