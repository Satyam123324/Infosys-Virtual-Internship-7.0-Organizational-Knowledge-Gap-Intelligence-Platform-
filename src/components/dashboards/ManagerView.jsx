import { useEffect, useState } from 'react';
import { Users2, Building2, ListChecks, BarChart3, AlertOctagon } from 'lucide-react';
import { employeeApi } from '../../api/employeeApi';
import { gapAnalysisApi } from '../../api/gapAnalysisApi';

const SEVERITY_COLOR = {
  NONE: '#0d9488',
  MINOR: '#2563eb',
  MODERATE: '#f59e0b',
  CRITICAL: '#e11d48',
};

export default function ManagerView({ user }) {
  const [deptName, setDeptName] = useState('');
  const [teamProfiles, setTeamProfiles] = useState([]);
  const [gapReports, setGapReports] = useState([]);
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

        if (departmentId) {
          const gapRes = await gapAnalysisApi.getReportsForDepartment(departmentId).catch(() => null);
          if (gapRes) setGapReports(gapRes.data.data);
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

  const withFramework = gapReports.filter((r) => r.frameworkFound);
  const atRisk = withFramework.filter((r) => r.overallReadinessPercent < 40).length;
  const needsAttention = withFramework.filter((r) => r.overallReadinessPercent >= 40 && r.overallReadinessPercent < 70).length;
  const onTrack = withFramework.filter((r) => r.overallReadinessPercent >= 70).length;

  const skillColumns = [];
  const seenSkills = new Set();
  withFramework.forEach((r) => {
    r.gaps.forEach((g) => {
      if (!seenSkills.has(g.skillId)) {
        seenSkills.add(g.skillId);
        skillColumns.push({ id: g.skillId, name: g.skillName });
      }
    });
  });

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

      {withFramework.length > 0 && (
        <>
          <div className="section-title">Risk Flags</div>
          <div className="card-grid">
            <div className="stat-card">
              <div className="stat-icon danger"><AlertOctagon size={17} /></div>
              <div className="stat-label">At Risk</div>
              <div className="stat-value" style={{ color: '#e11d48' }}>{atRisk}</div>
              <div className="stat-sub">Readiness below 40%</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon amber"><AlertOctagon size={17} /></div>
              <div className="stat-label">Needs Attention</div>
              <div className="stat-value" style={{ color: '#f59e0b' }}>{needsAttention}</div>
              <div className="stat-sub">Readiness 40–69%</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon teal"><AlertOctagon size={17} /></div>
              <div className="stat-label">On Track</div>
              <div className="stat-value" style={{ color: '#0d9488' }}>{onTrack}</div>
              <div className="stat-sub">Readiness 70%+</div>
            </div>
          </div>

          <div className="section-title">Gap Heatmap</div>
          <div className="chart-card" style={{ overflowX: 'auto', marginBottom: 24 }}>
            <table style={{ minWidth: 600 }}>
              <thead>
                <tr>
                  <th>Employee</th>
                  {skillColumns.map((s) => <th key={s.id} style={{ textAlign: 'center' }}>{s.name}</th>)}
                  <th style={{ textAlign: 'center' }}>Readiness</th>
                </tr>
              </thead>
              <tbody>
                {withFramework.map((r) => {
                  const gapBySkillId = {};
                  r.gaps.forEach((g) => { gapBySkillId[g.skillId] = g; });
                  return (
                    <tr key={r.userId}>
                      <td>{r.fullName}</td>
                      {skillColumns.map((s) => {
                        const gap = gapBySkillId[s.id];
                        if (!gap) return <td key={s.id} style={{ textAlign: 'center', color: '#cbd5e1' }}>—</td>;
                        return (
                          <td key={s.id} style={{ textAlign: 'center' }}>
                            <span style={{
                              display: 'inline-block', width: 14, height: 14, borderRadius: 4,
                              background: SEVERITY_COLOR[gap.severity],
                            }} title={`${gap.severity}: ${gap.currentLevel || 'Not assessed'} vs ${gap.requiredLevel}`} />
                          </td>
                        );
                      })}
                      <td style={{ textAlign: 'center', fontWeight: 600 }}>
                        <span style={{
                          color: r.overallReadinessPercent >= 70 ? '#0d9488' : r.overallReadinessPercent >= 40 ? '#f59e0b' : '#e11d48',
                        }}>
                          {r.overallReadinessPercent}%
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
            <div style={{ display: 'flex', gap: 16, marginTop: 12, fontSize: 12, color: '#64748b' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <span style={{ width: 10, height: 10, borderRadius: 3, background: SEVERITY_COLOR.NONE, display: 'inline-block' }} /> Meets requirement
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <span style={{ width: 10, height: 10, borderRadius: 3, background: SEVERITY_COLOR.MINOR, display: 'inline-block' }} /> Minor gap
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <span style={{ width: 10, height: 10, borderRadius: 3, background: SEVERITY_COLOR.MODERATE, display: 'inline-block' }} /> Moderate gap
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <span style={{ width: 10, height: 10, borderRadius: 3, background: SEVERITY_COLOR.CRITICAL, display: 'inline-block' }} /> Critical gap
              </span>
            </div>
          </div>
        </>
      )}

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
