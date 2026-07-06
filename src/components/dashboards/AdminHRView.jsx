import { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar,
} from 'recharts';
import { Users, Building2, BookMarked, TrendingUp, AlertTriangle } from 'lucide-react';
import { employeeApi } from '../../api/employeeApi';
import { adminApi } from '../../api/adminApi';

const LEVEL_SCORE = { UNAWARE: 0, BEGINNER: 1, INTERMEDIATE: 2, ADVANCED: 3, EXPERT: 4 };

export default function AdminHRView() {
  const [profiles, setProfiles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [skills, setSkills] = useState([]);
  const [totalUsers, setTotalUsers] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [profilesRes, deptRes, skillsRes, usersRes] = await Promise.all([
          employeeApi.getAllProfiles(),
          employeeApi.getAllDepartments(),
          employeeApi.getAllSkills(),
          adminApi.getAllUsers().catch(() => ({ data: { data: [] } })),
        ]);
        setProfiles(profilesRes.data.data);
        setDepartments(deptRes.data.data);
        setSkills(skillsRes.data.data);
        setTotalUsers(usersRes.data.data.length);
      } catch (err) {
        setError('Failed to load organization-wide analytics. HR Specialist or Admin access required.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const deptHeadcount = departments.map((d) => ({
    name: d.name,
    employees: profiles.filter((p) => p.departmentId === d.id).length,
  }));

  const categoryTotals = {};
  profiles.forEach((p) => {
    (p.skills || []).forEach((s) => {
      const cat = s.categoryName || 'Other';
      if (!categoryTotals[cat]) categoryTotals[cat] = { sum: 0, count: 0 };
      categoryTotals[cat].sum += LEVEL_SCORE[s.proficiencyLevel] ?? 0;
      categoryTotals[cat].count += 1;
    });
  });
  const radarData = Object.entries(categoryTotals).map(([category, { sum, count }]) => ({
    category,
    avgLevel: count ? Number((sum / count).toFixed(2)) : 0,
  }));

  const gapAlerts = [...radarData].sort((a, b) => a.avgLevel - b.avgLevel).slice(0, 3);
  const totalSkillsLogged = profiles.reduce((sum, p) => sum + (p.skills?.length || 0), 0);

  if (error) {
    return <div className="alert alert-error">{error}</div>;
  }

  return (
    <>
      <div className="hero-card">
        <div className="hero-eyebrow">Workforce Intelligence</div>
        <div className="hero-title">Organization-wide skill coverage at a glance</div>
        <div className="hero-sub">
          {loading ? 'Loading live data...' : `Tracking ${totalSkillsLogged} logged skills across ${profiles.length} employee profiles in ${departments.length} departments.`}
        </div>
      </div>

      <div className="card-grid">
        <div className="stat-card">
          <div className="stat-icon teal"><Users size={17} /></div>
          <div className="stat-label">Total Users</div>
          <div className="stat-value">{loading ? '—' : totalUsers}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon slate"><Building2 size={17} /></div>
          <div className="stat-label">Departments</div>
          <div className="stat-value">{loading ? '—' : departments.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon amber"><BookMarked size={17} /></div>
          <div className="stat-label">Skills in Catalog</div>
          <div className="stat-value">{loading ? '—' : skills.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon teal"><TrendingUp size={17} /></div>
          <div className="stat-label">Skills Logged (org-wide)</div>
          <div className="stat-value">{loading ? '—' : totalSkillsLogged}</div>
        </div>
      </div>

      {loading ? (
        <div className="loading-text">Loading analytics...</div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 8 }}>
          <div className="chart-card">
            <div className="chart-title">Org Skill Radar</div>
            <div className="chart-sub">Average proficiency (0–4 scale) by skill category, across every employee</div>
            <ResponsiveContainer width="100%" height={280}>
              <RadarChart data={radarData}>
                <PolarGrid stroke="#e2e8f0" />
                <PolarAngleAxis dataKey="category" tick={{ fontSize: 11, fill: '#64748b' }} />
                <PolarRadiusAxis domain={[0, 4]} tick={{ fontSize: 10, fill: '#94a3b8' }} />
                <Radar dataKey="avgLevel" stroke="#0d9488" fill="#14b8a6" fillOpacity={0.35} />
                <Tooltip />
              </RadarChart>
            </ResponsiveContainer>
          </div>

          <div className="chart-card">
            <div className="chart-title">Headcount by Department</div>
            <div className="chart-sub">Where the workforce sits across the org</div>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={deptHeadcount}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="name" tick={{ fontSize: 11, fill: '#64748b' }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} />
                <Tooltip />
                <Bar dataKey="employees" fill="#0d9488" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {!loading && gapAlerts.length > 0 && (
        <>
          <div className="section-title">Early Gap Signals</div>
          <div className="card-grid">
            {gapAlerts.map((g) => (
              <div key={g.category} className="stat-card">
                <div className="stat-icon danger"><AlertTriangle size={17} /></div>
                <div className="stat-label">{g.category}</div>
                <div className="stat-value">{g.avgLevel.toFixed(1)} / 4</div>
                <div className="stat-sub">Lowest average proficiency org-wide</div>
              </div>
            ))}
          </div>
        </>
      )}

      <div className="section-title">All Employee Profiles</div>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Department</th>
            <th>Role Title</th>
            <th>Skills</th>
            <th>Certifications</th>
          </tr>
        </thead>
        <tbody>
          {profiles.map((p) => (
            <tr key={p.id}>
              <td>{p.fullName}</td>
              <td>{p.departmentName || '—'}</td>
              <td>{p.currentRoleTitle || '—'}</td>
              <td>{p.skills?.length || 0}</td>
              <td>{p.certifications?.length || 0}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
