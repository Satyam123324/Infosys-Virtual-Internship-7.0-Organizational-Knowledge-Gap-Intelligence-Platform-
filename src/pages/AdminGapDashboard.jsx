import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { AlertOctagon, Users, TrendingDown, Target } from 'lucide-react';
import Layout from '../components/Layout';
import { gapAnalysisApi } from '../api/gapAnalysisApi';

const SEVERITY_COLOR = { CRITICAL: '#e11d48', MODERATE: '#f59e0b', MINOR: '#2563eb' };

export default function AdminGapDashboard() {
  const [summaries, setSummaries] = useState([]);
  const [allReports, setAllReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [summaryRes, reportsRes] = await Promise.all([
          gapAnalysisApi.getDepartmentSummaries(),
          gapAnalysisApi.getAllReports(),
        ]);
        setSummaries(summaryRes.data.data);
        setAllReports(reportsRes.data.data);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load organization gap analysis');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const withFramework = allReports.filter((r) => r.frameworkFound);
  const totalCriticalGaps = summaries.reduce((sum, s) => sum + s.criticalGaps, 0);
  const totalGaps = summaries.reduce((sum, s) => sum + s.totalGaps, 0);
  const orgAvgReadiness = withFramework.length
    ? (withFramework.reduce((sum, r) => sum + r.overallReadinessPercent, 0) / withFramework.length).toFixed(1)
    : 0;

  const readinessChartData = summaries
    .filter((s) => s.employeeCount > 0)
    .map((s) => ({ name: s.departmentName, readiness: s.avgReadinessPercent }));

  return (
    <Layout title="Organization Gap Analysis" subtitle="Live comparison of employee skills against role requirements, org-wide">
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Running gap analysis across the organization...</div>
      ) : (
        <>
          <div className="hero-card">
            <div className="hero-eyebrow">Gap Analysis Engine</div>
            <div className="hero-title">{orgAvgReadiness}% average role readiness</div>
            <div className="hero-sub">
              Based on {withFramework.length} employee{withFramework.length === 1 ? '' : 's'} with a defined role framework, out of {allReports.length} total profiles.
            </div>
          </div>

          <div className="card-grid">
            <div className="stat-card">
              <div className="stat-icon teal"><Users size={17} /></div>
              <div className="stat-label">Employees Analyzed</div>
              <div className="stat-value">{withFramework.length}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon amber"><TrendingDown size={17} /></div>
              <div className="stat-label">Total Skill Gaps</div>
              <div className="stat-value">{totalGaps}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon danger"><AlertOctagon size={17} /></div>
              <div className="stat-label">Critical Gaps</div>
              <div className="stat-value">{totalCriticalGaps}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon slate"><Target size={17} /></div>
              <div className="stat-label">Org Avg Readiness</div>
              <div className="stat-value">{orgAvgReadiness}%</div>
            </div>
          </div>

          <div className="chart-card" style={{ marginBottom: 24 }}>
            <div className="chart-title">Average Readiness by Department</div>
            <div className="chart-sub">Percentage of required skills each department is meeting</div>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={readinessChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748b' }} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748b' }} />
                <Tooltip formatter={(v) => `${v}%`} />
                <Bar dataKey="readiness" radius={[6, 6, 0, 0]}>
                  {readinessChartData.map((entry, i) => (
                    <Cell key={i} fill={entry.readiness >= 70 ? '#0d9488' : entry.readiness >= 40 ? '#f59e0b' : '#e11d48'} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>

          <div className="section-title">Employee Gap Details</div>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Role</th>
                <th>Department</th>
                <th>Readiness</th>
                <th>Gaps</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {allReports.map((r) => (
                <tr key={r.userId}>
                  <td>{r.fullName}</td>
                  <td>{r.roleTitle || '—'}</td>
                  <td>{r.departmentName || '—'}</td>
                  <td>{r.frameworkFound ? `${r.overallReadinessPercent}%` : '—'}</td>
                  <td>{r.frameworkFound ? r.skillsWithGap : '—'}</td>
                  <td>
                    {r.frameworkFound ? (
                      <span
                        className="status-pill"
                        style={{
                          background: r.overallReadinessPercent >= 70 ? '#f0fdfa' : r.overallReadinessPercent >= 40 ? '#fffbeb' : '#fff1f2',
                          color: r.overallReadinessPercent >= 70 ? '#0d9488' : r.overallReadinessPercent >= 40 ? '#f59e0b' : '#e11d48',
                        }}
                      >
                        {r.overallReadinessPercent >= 70 ? 'On Track' : r.overallReadinessPercent >= 40 ? 'Needs Attention' : 'At Risk'}
                      </span>
                    ) : (
                      <span className="status-pill" style={{ background: '#f1f5f9', color: '#64748b' }}>No Framework</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </Layout>
  );
}
