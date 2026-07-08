import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { AlertTriangle, CheckCircle2, ExternalLink, Sparkles } from 'lucide-react';
import { employeeApi } from '../api/employeeApi';
import { gapAnalysisApi } from '../api/gapAnalysisApi';

const SEVERITY_STYLE = {
  CRITICAL: { bg: '#fff1f2', color: '#e11d48', label: 'Critical' },
  MODERATE: { bg: '#fffbeb', color: '#f59e0b', label: 'Moderate' },
  MINOR: { bg: '#eff6ff', color: '#2563eb', label: 'Minor' },
  NONE: { bg: '#f0fdfa', color: '#0d9488', label: 'On Track' },
};

const SEEDED_ROLES = [
  'Software Developer', 'Senior Software Developer', 'Frontend Developer',
  'DevOps Engineer', 'Data Analyst', 'HR Specialist', 'Team Lead',
];

export default function GapAnalysis() {
  const [profile, setProfile] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(false);
  const [roleTitle, setRoleTitle] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [saving, setSaving] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    setError('');
    try {
      const [profileRes, deptRes] = await Promise.all([
        employeeApi.getMyProfile(),
        employeeApi.getAllDepartments(),
      ]);
      setProfile(profileRes.data.data);
      setDepartments(deptRes.data.data);
      setRoleTitle(profileRes.data.data.currentRoleTitle || '');
      setDepartmentId(profileRes.data.data.departmentId || '');

      const reportRes = await gapAnalysisApi.getMyReport();
      setReport(reportRes.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load gap analysis');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const saveRoleAndDept = async () => {
    setSaving(true);
    try {
      await employeeApi.updateMyProfile({
        currentRoleTitle: roleTitle,
        departmentId: departmentId ? Number(departmentId) : null,
      });
      setEditing(false);
      loadAll();
    } catch (err) {
      alert('Failed to update profile: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Layout title="My Gap Analysis" subtitle="See how your skills compare to your role's requirements, with AI-generated recommendations to close each gap">
      {loading ? (
        <div className="loading-text">Analyzing your skill gaps...</div>
      ) : (
        <>
          <div className="info-card" style={{ marginBottom: 20 }}>
            {!editing ? (
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div className="label">Current Role</div>
                  <div className="value">{profile?.currentRoleTitle || 'Not set'}</div>
                  <div style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
                    Department: {profile?.departmentName || 'Not set'}
                  </div>
                </div>
                <button className="btn-sm" onClick={() => setEditing(true)}>Edit Role / Department</button>
              </div>
            ) : (
              <div>
                <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'flex-end' }}>
                  <div className="form-group" style={{ marginBottom: 0, minWidth: 220 }}>
                    <label>Current Role Title</label>
                    <select value={roleTitle} onChange={(e) => setRoleTitle(e.target.value)}>
                      <option value="">Select a role...</option>
                      {SEEDED_ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                    </select>
                  </div>
                  <div className="form-group" style={{ marginBottom: 0, minWidth: 200 }}>
                    <label>Department</label>
                    <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
                      <option value="">Select a department...</option>
                      {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                    </select>
                  </div>
                  <button className="btn-primary" style={{ width: 'auto', padding: '10px 20px' }} onClick={saveRoleAndDept} disabled={saving}>
                    {saving ? 'Saving...' : 'Save'}
                  </button>
                  <button className="btn-sm" onClick={() => setEditing(false)}>Cancel</button>
                </div>
                <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 8 }}>
                  Tip: pick one of these seeded role titles to see a full gap report — HR/Admin can define more under Competency Frameworks.
                </div>
              </div>
            )}
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          {report && !report.frameworkFound && (
            <div className="alert alert-error">
              No competency framework has been defined yet for the role "{report.roleTitle || '(not set)'}".
              Set your role above to one of the seeded titles, or ask HR/Admin to define a framework for it.
            </div>
          )}

          {report && report.frameworkFound && (
            <>
              <div className="card-grid">
                <div className="stat-card">
                  <div className="stat-icon teal"><CheckCircle2 size={17} /></div>
                  <div className="stat-label">Overall Readiness</div>
                  <div className="stat-value">{report.overallReadinessPercent}%</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon slate"><Sparkles size={17} /></div>
                  <div className="stat-label">Required Skills</div>
                  <div className="stat-value">{report.totalRequiredSkills}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon teal"><CheckCircle2 size={17} /></div>
                  <div className="stat-label">Meeting Requirement</div>
                  <div className="stat-value">{report.skillsMeetingRequirement}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-icon danger"><AlertTriangle size={17} /></div>
                  <div className="stat-label">Skills With Gaps</div>
                  <div className="stat-value">{report.skillsWithGap}</div>
                </div>
              </div>

              <div className="section-title">Skill-by-Skill Breakdown</div>
              {report.gaps.map((gap) => {
                const style = SEVERITY_STYLE[gap.severity];
                return (
                  <div key={gap.skillId} className="info-card" style={{ marginBottom: 14 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: gap.gapSize > 0 ? 10 : 0 }}>
                      <div>
                        <span style={{ fontWeight: 600, fontSize: 15 }}>{gap.skillName}</span>
                        {gap.mandatory && <span style={{ fontSize: 11, color: '#94a3b8', marginLeft: 8 }}>(mandatory)</span>}
                      </div>
                      <span className="status-pill" style={{ background: style.bg, color: style.color }}>
                        {style.label}
                      </span>
                    </div>

                    <div style={{ fontSize: 13, color: '#64748b', marginBottom: gap.gapSize > 0 ? 10 : 0 }}>
                      Current: <strong>{gap.currentLevel || 'Not assessed'}</strong> &nbsp;→&nbsp; Required: <strong>{gap.requiredLevel}</strong>
                    </div>

                    {gap.gapSize > 0 && (
                      <div style={{ background: '#f8fafc', borderRadius: 8, padding: 14 }}>
                        <div style={{ display: 'flex', gap: 8, alignItems: 'flex-start', marginBottom: 10 }}>
                          <Sparkles size={16} style={{ color: '#0d9488', flexShrink: 0, marginTop: 2 }} />
                          <div style={{ fontSize: 13.5, color: '#334155', lineHeight: 1.5 }}>
                            {gap.recommendationText}
                          </div>
                        </div>
                        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                          {gap.suggestedResources?.map((r) => (
                            <a
                              key={r.provider}
                              href={r.url}
                              target="_blank"
                              rel="noreferrer"
                              style={{
                                fontSize: 12, color: '#0d9488', textDecoration: 'none',
                                display: 'flex', alignItems: 'center', gap: 4,
                                background: '#f0fdfa', padding: '5px 10px', borderRadius: 6,
                              }}
                            >
                              {r.provider} <ExternalLink size={12} />
                            </a>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </>
          )}
        </>
      )}
    </Layout>
  );
}
