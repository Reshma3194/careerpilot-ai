import { NavLink } from 'react-router-dom';

const links = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/resume', label: 'Resume analyzer' },
  { to: '/jobs', label: 'Job tracker' },
  { to: '/interview', label: 'Interview prep' },
  { to: '/profile', label: 'Profile' },
];

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <nav aria-label="Main">
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} end={link.end}>
            {link.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
