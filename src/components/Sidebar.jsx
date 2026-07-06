import { NavLink, useNavigate } from 'react-router-dom';
import { LayoutDashboard, ListChecks, ClipboardCheck, ShieldCheck, LogOut, Brain } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const isAdmin = user.roles?.includes('SYSTEM_ADMINISTRATOR');
  const initials = user.fullName
    .split(' ')
    .map((n) => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const linkClass = ({ isActive }) => 'nav-link' + (isActive ? ' active' : '');

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="mark"><Brain size={18} /></div>
        Knowledge Gap<br />Intelligence
      </div>

      <div className="nav-group-label">Workspace</div>
      <NavLink to="/dashboard" className={linkClass}>
        <LayoutDashboard size={17} /> Dashboard
      </NavLink>
      <NavLink to="/skills" className={linkClass}>
        <ListChecks size={17} /> My Skills
      </NavLink>
      <NavLink to="/assessment" className={linkClass}>
        <ClipboardCheck size={17} /> Assessment Test
      </NavLink>

      {isAdmin && (
        <>
          <div className="nav-group-label">Administration</div>
          <NavLink to="/admin" className={linkClass}>
            <ShieldCheck size={17} /> Admin Console
          </NavLink>
        </>
      )}

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <div className="avatar">{initials}</div>
          <div>
            <div className="name">{user.fullName}</div>
            <div className="role">{(user.roles?.[0] || '').replaceAll('_', ' ')}</div>
          </div>
        </div>
        <button className="logout-link" onClick={handleLogout}>
          <LogOut size={16} /> Logout
        </button>
      </div>
    </aside>
  );
}
