import { useAuth } from '../context/AuthContext';
import Navbar from '../components/Navbar';
import EmployeeView from '../components/dashboards/EmployeeView';
import ManagerView from '../components/dashboards/ManagerView';
import AdminHRView from '../components/dashboards/AdminHRView';

const DASHBOARD_LABELS = {
  SYSTEM_ADMINISTRATOR: 'System Administrator Dashboard',
  HR_SPECIALIST: 'HR Analytics Dashboard',
  LEARNING_DEVELOPMENT_ADMIN: 'Learning & Development Dashboard',
  DEPARTMENT_HEAD: 'Department Head Dashboard',
  TEAM_LEAD_MANAGER: 'Team Manager Dashboard',
  EMPLOYEE: 'My Dashboard',
};

// Priority order — highest-privilege role determines which dashboard view renders
const ROLE_PRIORITY = [
  'SYSTEM_ADMINISTRATOR',
  'HR_SPECIALIST',
  'LEARNING_DEVELOPMENT_ADMIN',
  'DEPARTMENT_HEAD',
  'TEAM_LEAD_MANAGER',
  'EMPLOYEE',
];

export default function Dashboard() {
  const { user } = useAuth();
  if (!user) return null;

  const primaryRole = ROLE_PRIORITY.find((r) => user.roles?.includes(r)) || 'EMPLOYEE';

  const renderView = () => {
    switch (primaryRole) {
      case 'SYSTEM_ADMINISTRATOR':
      case 'HR_SPECIALIST':
      case 'LEARNING_DEVELOPMENT_ADMIN':
        return <AdminHRView />;
      case 'DEPARTMENT_HEAD':
      case 'TEAM_LEAD_MANAGER':
        return <ManagerView user={user} />;
      default:
        return <EmployeeView user={user} />;
    }
  };

  return (
    <div className="app-shell">
      <Navbar />
      <div className="page-content">
        <div className="page-header">
          <h1>{DASHBOARD_LABELS[primaryRole]}</h1>
          <p>Welcome back, {user.fullName.split(' ')[0]} — here's your view of the platform</p>
        </div>
        {renderView()}
      </div>
    </div>
  );
}
