import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        Career<span>Pilot</span>
      </Link>
      <div className="navbar-user">
        <span>{user?.fullName}</span>
        <button type="button" className="btn btn-quiet btn-small" style={{ color: '#fff', borderColor: '#486581' }} onClick={handleLogout}>
          Log out
        </button>
      </div>
    </header>
  );
}
