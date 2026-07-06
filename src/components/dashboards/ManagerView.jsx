import { useEffect, useState } from 'react';
import { Users2, Building2, ListChecks, BarChart3 } from 'lucide-react';
import { employeeApi } from '../../api/employeeApi';

export default function ManagerView({ user }) {
  const [deptName, setDeptName] = useState('');
  const [teamProfiles, setTeamProfiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const meRes = await employeeApi.getMyProfile();
        const departmentId = meRes.data.data.departmentId;
        setDeptName(meRes.data.data.departmentName || 'Unassigned');

        const allRes = await employeeApi.getAllProfiles().catch(() => null);
        if (allRes) {
          const filtered = departmentId
            ? allRes.data.data.filter((p) => p.departmentId === departmentId)
            : allRes.data.data;
          setTeamProfiles(filtered);
        }
      } catch (err) {
        setError('Could not load team data — you may need Department Head or HR access for full team visibility.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const totalSkillsInTeam = teamProfiles.reduce((sum, p) => sum + (p.skills?.length || 0), 0);
  const avgSkillsPerPerson = teamProfiles.length ? (totalSkillsInTeam / teamProfiles.length).toFixed(1) : 0;

  return (
    <>
      <div className="hero-card">
        <div className="hero-eyebrow">Team Overview</div>
        <div className="hero-title">{deptName}</div>
        <div className="hero-sub">{loading ? 'Loading team data...' : `${teamProfiles.length} team members tracked`}</div>
      </div>

      <div className="card-grid">
        <div className="stat-card">
          <div className="stat-icon slate"><Building2 size={17} /></div>
          <div className="stat-label">My Department</div>
          <div className="stat-value" style={{ fontSize: 18 }}>{deptName}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon teal"><Users2 size={17} /></div>
          <div className="stat-label">Team Size</div>
          <div className="stat-value">{loading ? '—' : teamProfiles.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon amber"><ListChecks size={17} /></div>
          <div className="stat-label">Total Skills Logged</div>
          <div className="stat-value">{loading ? '—' : totalSkillsInTeam}</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon teal"><BarChart3 size={17} /></div>
          <div className="stat-label">Avg Skills / Person</div>
          <div className="stat-value">{loading ? '—' : avgSkillsPerPerson}</div>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="section-title">Team Skill Snapshot</div>
      {loading ? (
        <div className="loading-text">Loading team data...</div>
      ) : teamProfiles.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>
          No team members found in your department yet.
        </div>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Role Title</th>
              <th>Skills Logged</th>
              <th>Certifications</th>
            </tr>
          </thead>
          <tbody>
            {teamProfiles.map((p) => (
              <tr key={p.id}>
                <td>{p.fullName}</td>
                <td>{p.currentRoleTitle || '—'}</td>
                <td>{p.skills?.length || 0}</td>
                <td>{p.certifications?.length || 0}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  );
}
