import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Layout from '../components/Layout';
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

const ROLE_PRIORITY = [
  'SYSTEM_ADMINISTRATOR',
  'HR_SPECIALIST',
  'LEARNING_DEVELOPMENT_ADMIN',
  'DEPARTMENT_HEAD',
  'TEAM_LEAD_MANAGER',
  'EMPLOYEE',
];

// Which "view family" each role maps to — several admin-ish roles share AdminHRView,
// several manager-ish roles share ManagerView, so the switcher only needs to offer
// one option per distinct view, not one per role.
const VIEW_FAMILY = {
  SYSTEM_ADMINISTRATOR: 'ADMIN',
  HR_SPECIALIST: 'ADMIN',
  LEARNING_DEVELOPMENT_ADMIN: 'ADMIN',
  DEPARTMENT_HEAD: 'MANAGER',
  TEAM_LEAD_MANAGER: 'MANAGER',
  EMPLOYEE: 'EMPLOYEE',
};

const FAMILY_LABEL = { ADMIN: 'Admin View', MANAGER: 'Manager View', EMPLOYEE: 'Employee View' };

export default function Dashboard() {
  const { user } = useAuth();
  const [viewOverride, setViewOverride] = useState(null);
  if (!user) return null;

  const primaryRole = ROLE_PRIORITY.find((r) => user.roles?.includes(r)) || 'EMPLOYEE';

  // Every distinct view family this account can access, in priority order.
  const availableFamilies = [...new Set(
    ROLE_PRIORITY.filter((r) => user.roles?.includes(r)).map((r) => VIEW_FAMILY[r])
  )];

  const activeFamily = viewOverride && availableFamilies.includes(viewOverride)
    ? viewOverride
    : VIEW_FAMILY[primaryRole];

  const renderView = () => {
    switch (activeFamily) {
      case 'ADMIN':
        return <AdminHRView />;
      case 'MANAGER':
        return <ManagerView user={user} />;
      default:
        return <EmployeeView user={user} />;
    }
  };

  return (
    <Layout title={DASHBOARD_LABELS[primaryRole]} subtitle={`Welcome back, ${user.fullName.split(' ')[0]} — here's your view of the platform`}>
      {availableFamilies.length > 1 && (
        <div style={{ display: 'flex', gap: 6, marginBottom: 18 }}>
          {availableFamilies.map((family) => (
            <button
              key={family}
              onClick={() => setViewOverride(family)}
              className="status-pill"
              style={{
                border: '1px solid ' + (activeFamily === family ? '#0d9488' : '#e2e8f0'),
                background: activeFamily === family ? '#0d9488' : 'white',
                color: activeFamily === family ? 'white' : '#475569',
                cursor: 'pointer', fontSize: 12.5,
              }}
            >
              {FAMILY_LABEL[family]}
            </button>
          ))}
        </div>
      )}
      {renderView()}
    </Layout>
  );
}
