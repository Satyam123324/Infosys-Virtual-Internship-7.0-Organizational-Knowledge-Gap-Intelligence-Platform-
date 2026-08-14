import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (!user) return null;

  const isAdmin = user.roles?.includes('SYSTEM_ADMINISTRATOR');

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        🧠 Knowledge Gap Intelligence Platform
      </div>
      <div className="navbar-links">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/skills">My Skills</Link>
        <Link to="/assessment">Assessment Test</Link>
        {isAdmin && <Link to="/admin">Admin Console</Link>}
        <span style={{ fontSize: 13, color: '#6b7280' }}>{user.fullName}</span>
        <button className="logout-btn" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
