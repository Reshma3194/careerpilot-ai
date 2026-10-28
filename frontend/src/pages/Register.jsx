import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { errorMessage } from '../services/api.js';
import { validateEmail, validatePassword } from '../utils/validators.js';

export default function Register() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (isAuthenticated) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    const problem =
      (fullName.trim() ? '' : 'Enter your name.') || validateEmail(email) || validatePassword(password);
    if (problem) return setError(problem);
    setError('');
    setBusy(true);
    try {
      await register(fullName.trim(), email, password);
      navigate('/', { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <span className="brand">CareerPilot</span>
        <h1>Create your account</h1>
        {error && <div className="alert alert-error" role="alert">{error}</div>}
        <div className="field">
          <label htmlFor="name">Full name</label>
          <input id="name" type="text" autoComplete="name" value={fullName} onChange={(e) => setFullName(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="email">Email</label>
          <input id="email" type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)} />
          <div className="hint">At least 8 characters.</div>
        </div>
        <button className="btn" type="submit" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
        <p className="auth-switch">
          Already registered? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  );
}
