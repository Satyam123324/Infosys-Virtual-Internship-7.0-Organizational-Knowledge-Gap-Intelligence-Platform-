import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
} from 'recharts';
import {
  Users, Building2, Target, AlertOctagon, UserCheck, Layers, ShieldCheck,
  FileSpreadsheet, ArrowRight, Activity, TrendingDown,
} from 'lucide-react';
import Layout from '../components/Layout';
import { adminApi } from '../api/adminApi';
import { gapAnalysisApi } from '../api/gapAnalysisApi';
import { employeeApi } from '../api/employeeApi';
import { reportsApi } from '../api/reportsApi';

const ALL_ROLES = [
  'EMPLOYEE', 'TEAM_LEAD_MANAGER', 'HR_SPECIALIST',
  'DEPARTMENT_HEAD', 'LEARNING_DEVELOPMENT_ADMIN', 'SYSTEM_ADMINISTRATOR',
];

const readinessColor = (v) => (v >= 70 ? '#0d9488' : v >= 40 ? '#f59e0b' : '#e11d48');

export default function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [summaries, setSummaries] = useState([]);
  const [allReports, setAllReports] = useState([]);
  const [profiles, setProfiles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [downloading, setDownloading] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [usersRes, summaryRes, reportsRes, profilesRes, deptRes] = await Promise.all([
          adminApi.getAllUsers(),
          gapAnalysisApi.getDepartmentSummaries(),
          gapAnalysisApi.getAllReports(),
          employeeApi.getAllProfiles(),
          employeeApi.getAllDepartments(),
        ]);
        setUsers(usersRes.data.data || []);
        setSummaries(summaryRes.data.data || []);
        setAllReports(reportsRes.data.data || []);
        setProfiles(profilesRes.data.data || []);
        setDepartments(deptRes.data.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load admin dashboard data');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const runDownload = async (key, fn) => {
    setDownloading(key);
    try {
      await fn();
    } catch (err) {
      alert('Could not generate report: ' + (err.response?.data?.message || err.message || 'Unknown error'));
    } finally {
      setDownloading('');
    }
  };

  // ---- derived metrics ----
  const activeUsers = users.filter((u) => u.enabled).length;
  const disabledUsers = users.length - activeUsers;
  const withFramework = allReports.filter((r) => r.frameworkFound);
  const totalCriticalGaps = summaries.reduce((s, d) => s + (d.criticalGaps || 0), 0);
  const totalGaps = summaries.reduce((s, d) => s + (d.totalGaps || 0), 0);
  const orgAvgReadiness = withFramework.length
    ? (withFramework.reduce((s, r) => s + r.overallReadinessPercent, 0) / withFramework.length).toFixed(1)
    : 0;

  const roleCounts = ALL_ROLES.map((role) => ({
    role,
    count: users.filter((u) => (u.roles || []).includes(role)).length,
  })).filter((r) => r.count > 0);

  const readinessChartData = summaries
    .filter((s) => s.employeeCount > 0)
    .map((s) => ({ name: s.departmentName, readiness: Math.round(s.avgReadinessPercent) }));

  const recentUsers = [...users]
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    .slice(0, 6);

  return (
    <Layout
      title="Admin Dashboard"
      subtitle="Organization-wide gap intelligence, workforce inventory, and system monitoring"
    >
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading organization intelligence...</div>
      ) : (
        <>
          <div className="hero-card">
            <div className="hero-eyebrow">Organization Intelligence</div>
            <div className="hero-title">{orgAvgReadiness}% average role readiness</div>
            <div className="hero-sub">
              {users.length} user{users.length === 1 ? '' : 's'} · {departments.length} departments ·
              {' '}{withFramework.length} of {allReports.length} profiles benchmarked against a role framework
            </div>
          </div>

          {/* KPI row */}
          <div className="card-grid">
            <div className="stat-card">
              <div className="stat-icon teal"><Users size={17} /></div>
              <div className="stat-label">Total Users</div>
              <div className="stat-value">{users.length}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon slate"><Building2 size={17} /></div>
              <div className="stat-label">Departments</div>
              <div className="stat-value">{departments.length}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon amber"><Target size={17} /></div>
              <div className="stat-label">Org Avg Readiness</div>
              <div className="stat-value">{orgAvgReadiness}%</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon danger"><AlertOctagon size={17} /></div>
              <div className="stat-label">Critical Gaps</div>
              <div className="stat-value">{totalCriticalGaps}</div>
            </div>
          </div>

          {/* Organization-wide gap intelligence */}
          <div className="chart-card" style={{ marginBottom: 24 }}>
            <div className="chart-title">Average Readiness by Department</div>
            <div className="chart-sub">How much of each department's required skills are being met</div>
            {readinessChartData.length === 0 ? (
              <div className="loading-text">No benchmarked employees yet.</div>
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={readinessChartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748b' }} />
                  <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748b' }} />
                  <Tooltip formatter={(v) => `${v}%`} />
                  <Bar dataKey="readiness" radius={[6, 6, 0, 0]}>
                    {readinessChartData.map((entry, i) => (
                      <Cell key={i} fill={readinessColor(entry.readiness)} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* Workforce skill inventory overview */}
          <div className="section-title">Workforce Inventory</div>
          <div className="card-grid">
            <div className="stat-card">
              <div className="stat-icon teal"><Layers size={17} /></div>
              <div className="stat-label">Employee Profiles</div>
              <div className="stat-value">{profiles.length}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon slate"><UserCheck size={17} /></div>
              <div className="stat-label">Active Accounts</div>
              <div className="stat-value">{activeUsers}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon amber"><TrendingDown size={17} /></div>
              <div className="stat-label">Total Skill Gaps</div>
              <div className="stat-value">{totalGaps}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon danger"><Activity size={17} /></div>
              <div className="stat-label">Disabled Accounts</div>
              <div className="stat-value">{disabledUsers}</div>
            </div>
          </div>

          {/* System monitoring & user management */}
          <div className="section-title">System Monitoring &amp; User Management</div>
          <div className="chart-card" style={{ marginBottom: 24 }}>
            <div className="chart-title">Users by Role</div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginTop: 12 }}>
              {roleCounts.map((r) => (
                <span key={r.role} className="status-pill" style={{ background: '#f1f5f9', color: '#334155' }}>
                  {r.role.replaceAll('_', ' ')}: <strong style={{ marginLeft: 4 }}>{r.count}</strong>
                </span>
              ))}
            </div>
          </div>

          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Roles</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {recentUsers.map((u) => (
                <tr key={u.id}>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td>{u.department || '—'}</td>
                  <td>{(u.roles || []).map((r) => r.replaceAll('_', ' ')).join(', ')}</td>
                  <td>
                    <span className={`status-pill ${u.enabled ? 'status-active' : 'status-disabled'}`}>
                      {u.enabled ? 'Active' : 'Disabled'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div style={{ marginTop: 12 }}>
            <Link to="/admin" style={{ color: '#0d9488', fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              Manage all users in the Admin Console <ArrowRight size={15} />
            </Link>
          </div>

          {/* Reports management */}
          <div className="section-title" style={{ marginTop: 28 }}>Reports Management</div>
          <div className="chart-card" style={{ marginBottom: 24 }}>
            <div className="chart-sub" style={{ marginBottom: 14 }}>
              Export live gap-analysis data for offline review and reporting.
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12 }}>
              <button
                onClick={() => runDownload('workforce', reportsApi.workforceExcel)}
                disabled={downloading === 'workforce'}
                style={reportBtnStyle}
              >
                <FileSpreadsheet size={16} />
                {downloading === 'workforce' ? 'Generating…' : 'Workforce Gap Report (Excel)'}
              </button>
              <button
                onClick={() => runDownload('dept', reportsApi.departmentSummaryExcel)}
                disabled={downloading === 'dept'}
                style={reportBtnStyle}
              >
                <FileSpreadsheet size={16} />
                {downloading === 'dept' ? 'Generating…' : 'Department Summary (Excel)'}
              </button>
            </div>
          </div>

          {/* Quick links to detailed admin pages */}
          <div className="section-title">Detailed Views</div>
          <div className="card-grid">
            <Link to="/admin/gap-dashboard" style={quickLinkStyle}>
              <ShieldCheck size={18} /> Organization Gap Analysis
            </Link>
            <Link to="/admin/employee-profiles" style={quickLinkStyle}>
              <Users size={18} /> Employee Profiles
            </Link>
            <Link to="/admin/competency-frameworks" style={quickLinkStyle}>
              <Target size={18} /> Competency Frameworks
            </Link>
            <Link to="/admin" style={quickLinkStyle}>
              <UserCheck size={18} /> User Management Console
            </Link>
          </div>
        </>
      )}
    </Layout>
  );
}

const reportBtnStyle = {
  display: 'inline-flex', alignItems: 'center', gap: 8, padding: '10px 16px',
  border: '1px solid #0d9488', background: '#f0fdfa', color: '#0d9488',
  borderRadius: 8, fontWeight: 600, cursor: 'pointer', fontSize: 14,
};

const quickLinkStyle = {
  display: 'flex', alignItems: 'center', gap: 10, padding: '18px 20px',
  background: '#fff', border: '1px solid #e2e8f0', borderRadius: 12,
  color: '#0f172a', fontWeight: 600, textDecoration: 'none',
};
