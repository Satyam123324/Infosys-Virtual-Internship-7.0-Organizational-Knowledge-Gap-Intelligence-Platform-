import { useEffect, useState } from 'react';
import { employeeApi } from '../../api/employeeApi';

export default function ManagerView({ user }) {
  const [myDeptId, setMyDeptId] = useState(null);
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

        if (departmentId) {
          setMyDeptId(departmentId);
        }

        // Fall back to org-wide view if manager has no department assigned yet
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
      <div className="card-grid">
        <div className="info-card">
          <div className="label">My Department</div>
          <div className="value">{deptName}</div>
        </div>
        <div className="info-card">
          <div className="label">Team Size</div>
          <div className="value">{loading ? '—' : teamProfiles.length}</div>
        </div>
        <div className="info-card">
          <div className="label">Total Skills Logged</div>
          <div className="value">{loading ? '—' : totalSkillsInTeam}</div>
        </div>
        <div className="info-card">
          <div className="label">Avg Skills / Person</div>
          <div className="value">{loading ? '—' : avgSkillsPerPerson}</div>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="section-title">Team Skill Snapshot</div>
      {loading ? (
        <div className="loading-text">Loading team data...</div>
      ) : teamProfiles.length === 0 ? (
        <div className="info-card" style={{ color: '#6b7280' }}>
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
