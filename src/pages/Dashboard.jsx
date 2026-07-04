import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';

const ROLE_LABELS = {
  EMPLOYEE: 'Employee',
  TEAM_LEAD_MANAGER: 'Team Lead / Manager',
  HR_SPECIALIST: 'HR Specialist',
  DEPARTMENT_HEAD: 'Department Head',
  LEARNING_DEVELOPMENT_ADMIN: 'Learning & Development Admin',
  SYSTEM_ADMINISTRATOR: 'System Administrator',
};

export default function Dashboard() {
  const { user } = useAuth();

  if (!user) return null;

  return (
    <div className="app-shell">
      <Navbar />
      <div className="page-content">
        <div className="page-header">
          <h1>Welcome, {user.fullName.split(' ')[0]} 👋</h1>
          <p>Here's your profile overview on the Knowledge Gap Intelligence Platform</p>
        </div>

        <div className="card-grid">
          <div className="info-card">
            <div className="label">Full Name</div>
            <div className="value">{user.fullName}</div>
          </div>
          <div className="info-card">
            <div className="label">Email</div>
            <div className="value">{user.email}</div>
          </div>
          <div className="info-card">
            <div className="label">Department</div>
            <div className="value">{user.department || '—'}</div>
          </div>
          <div className="info-card">
            <div className="label">Designation</div>
            <div className="value">{user.designation || '—'}</div>
          </div>
        </div>

        <div className="section-title">Assigned Roles</div>
        <div>
          {user.roles?.map((role) => (
            <span key={role} className="role-badge">{ROLE_LABELS[role] || role}</span>
          ))}
        </div>

        <div className="section-title">What's Next</div>
        <div className="info-card" style={{ color: '#6b7280', fontSize: 14, lineHeight: 1.6 }}>
          This is Module 1 — Authentication & Role-Based Access. Upcoming modules will add
          your Skill Inventory, Knowledge Gap Analysis, Training Recommendations, and
          Analytics Dashboards right here.
        </div>
      </div>
    </div>
  );
}
