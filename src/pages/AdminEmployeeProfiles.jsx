import { useEffect, useState } from 'react';
import { ChevronDown, ChevronUp, Search, Pencil, Check, X, Trash2, Power } from 'lucide-react';
import Layout from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import { employeeApi } from '../api/employeeApi';
import { adminApi } from '../api/adminApi';
import { gapAnalysisApi } from '../api/gapAnalysisApi';

const LEVEL_COLORS = {
  UNAWARE: '#e11d48',
  BEGINNER: '#f59e0b',
  INTERMEDIATE: '#2563eb',
  ADVANCED: '#7c3aed',
  EXPERT: '#0d9488',
};

const ROLE_OPTIONS = [
  'Software Developer', 'Senior Software Developer', 'Frontend Developer',
  'DevOps Engineer', 'Data Analyst', 'HR Specialist', 'Team Lead',
];

const ACCESS_LEVELS = [
  'EMPLOYEE',
  'TEAM_LEAD_MANAGER',
  'HR_SPECIALIST',
  'DEPARTMENT_HEAD',
  'LEARNING_DEVELOPMENT_ADMIN',
  'SYSTEM_ADMINISTRATOR',
];

export default function AdminEmployeeProfiles() {
  const { user: currentUser } = useAuth();
  const canEditAccessLevel = currentUser?.roles?.includes('SYSTEM_ADMINISTRATOR');
  const [profiles, setProfiles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [usersByEmail, setUsersByEmail] = useState({});
  const [gapByUserId, setGapByUserId] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [expandedId, setExpandedId] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editDeptId, setEditDeptId] = useState('');
  const [editRole, setEditRole] = useState('');
  const [editAccessLevels, setEditAccessLevels] = useState([]);
  const [saving, setSaving] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [profilesRes, deptRes, usersRes, gapRes] = await Promise.all([
        employeeApi.getAllProfiles(),
        employeeApi.getAllDepartments(),
        adminApi.getAllUsers().catch(() => ({ data: { data: [] } })),
        gapAnalysisApi.getAllReports().catch(() => ({ data: { data: [] } })),
      ]);
      setProfiles(profilesRes.data.data);
      setDepartments(deptRes.data.data);

      const emailMap = {};
      usersRes.data.data.forEach((u) => { emailMap[u.email] = u; });
      setUsersByEmail(emailMap);

      const gapMap = {};
      gapRes.data.data.forEach((g) => { gapMap[g.userId] = g; });
      setGapByUserId(gapMap);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load employee profiles');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const filtered = profiles.filter((p) => {
    const q = search.toLowerCase();
    return (
      p.fullName?.toLowerCase().includes(q) ||
      p.email?.toLowerCase().includes(q) ||
      p.departmentName?.toLowerCase().includes(q) ||
      p.currentRoleTitle?.toLowerCase().includes(q)
    );
  });

  const toggleExpand = (id) => setExpandedId(expandedId === id ? null : id);

  const startEdit = (p) => {
    const userInfo = usersByEmail[p.email];
    setEditingId(p.id);
    setEditDeptId(p.departmentId || '');
    setEditRole(p.currentRoleTitle || '');
    setEditAccessLevels(userInfo ? [...userInfo.roles] : ['EMPLOYEE']);
    setExpandedId(p.id);
  };

  const cancelEdit = () => setEditingId(null);

  const toggleAccessLevel = (role) => {
    setEditAccessLevels((prev) =>
      prev.includes(role) ? prev.filter((r) => r !== role) : [...prev, role]
    );
  };

  const saveEdit = async (userId) => {
    if (editAccessLevels.length === 0) {
      alert('At least one access level must be selected.');
      return;
    }
    setSaving(true);
    try {
      const updates = [
        employeeApi.updateProfileAsAdmin(userId, {
          departmentId: editDeptId ? Number(editDeptId) : null,
          currentRoleTitle: editRole,
        }),
      ];
      if (canEditAccessLevel) {
        updates.push(adminApi.updateUserRoles(userId, editAccessLevels));
      }
      await Promise.all(updates);
      setEditingId(null);
      loadAll();
    } catch (err) {
      alert('Failed to update: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setSaving(false);
    }
  };

  const handleToggleEnabled = async (userId, currentlyEnabled) => {
    try {
      await adminApi.toggleUser(userId, !currentlyEnabled);
      loadAll();
    } catch (err) {
      alert('Failed to update account status');
    }
  };

  const handleDelete = async (userId, name) => {
    if (!confirm(`Permanently delete ${name}'s account? This cannot be undone.`)) return;
    try {
      await adminApi.deleteUser(userId);
      loadAll();
    } catch (err) {
      alert('Failed to delete user: ' + (err.response?.data?.message || 'Unknown error'));
    }
  };

  return (
    <Layout title="Admin Dashboard — Employee Management" subtitle="Review every employee's profile, skills, and access level — and edit department, role, or access rights directly">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="info-card" style={{ marginBottom: 20, display: 'flex', alignItems: 'center', gap: 10 }}>
        <Search size={16} style={{ color: '#94a3b8' }} />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name, email, department, or role..."
          style={{ border: 'none', outline: 'none', flex: 1, fontSize: 14 }}
        />
      </div>

      {loading ? (
        <div className="loading-text">Loading employee profiles...</div>
      ) : filtered.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>No employees match your search.</div>
      ) : (
        filtered.map((p) => {
          const isOpen = expandedId === p.id;
          const isEditing = editingId === p.id;
          const userInfo = usersByEmail[p.email];
          const gap = gapByUserId[p.userId];

          return (
            <div key={p.id} className="info-card" style={{ marginBottom: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
                <div
                  style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer', flex: 1, minWidth: 200 }}
                  onClick={() => toggleExpand(p.id)}
                >
                  <div style={{
                    width: 36, height: 36, borderRadius: '50%', background: '#f0fdfa', color: '#0d9488',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 13,
                    flexShrink: 0,
                  }}>
                    {p.fullName?.split(' ').map((n) => n[0]).slice(0, 2).join('').toUpperCase()}
                  </div>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{p.fullName}</div>
                    <div style={{ fontSize: 12, color: '#64748b' }}>{p.email}</div>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
                  <span className="role-badge">{p.departmentName || 'No department'}</span>
                  <span className="role-badge">{p.currentRoleTitle || 'No role set'}</span>
                  {userInfo && (
                    <span
                      className="status-pill"
                      style={{ background: userInfo.enabled ? '#f0fdfa' : '#fff1f2', color: userInfo.enabled ? '#0d9488' : '#e11d48' }}
                    >
                      {userInfo.enabled ? 'Active' : 'Disabled'}
                    </span>
                  )}
                  {gap && gap.frameworkFound && (
                    <span
                      className="status-pill"
                      style={{
                        background: gap.overallReadinessPercent >= 70 ? '#f0fdfa' : gap.overallReadinessPercent >= 40 ? '#fffbeb' : '#fff1f2',
                        color: gap.overallReadinessPercent >= 70 ? '#0d9488' : gap.overallReadinessPercent >= 40 ? '#f59e0b' : '#e11d48',
                      }}
                    >
                      {gap.overallReadinessPercent}% ready
                    </span>
                  )}
                  <button className="btn-sm" onClick={() => startEdit(p)} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                    <Pencil size={12} /> Edit
                  </button>
                  {canEditAccessLevel && userInfo && (
                    <button
                      className="btn-sm"
                      onClick={() => handleToggleEnabled(userInfo.id, userInfo.enabled)}
                      style={{ display: 'flex', alignItems: 'center', gap: 4 }}
                    >
                      <Power size={12} /> {userInfo.enabled ? 'Disable' : 'Enable'}
                    </button>
                  )}
                  {canEditAccessLevel && userInfo && (
                    <button
                      className="btn-sm"
                      onClick={() => handleDelete(userInfo.id, p.fullName)}
                      style={{ display: 'flex', alignItems: 'center', gap: 4, color: '#e11d48' }}
                    >
                      <Trash2 size={12} /> Delete
                    </button>
                  )}
                  <span onClick={() => toggleExpand(p.id)} style={{ cursor: 'pointer' }}>
                    {isOpen ? <ChevronUp size={18} color="#94a3b8" /> : <ChevronDown size={18} color="#94a3b8" />}
                  </span>
                </div>
              </div>

              {isOpen && (
                <div style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #f1f5f9' }}>

                  {isEditing && (
                    <div style={{ background: '#f8fafc', borderRadius: 8, padding: 16, marginBottom: 16 }}>
                      <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap', marginBottom: 14 }}>
                        <div className="form-group" style={{ marginBottom: 0, minWidth: 200 }}>
                          <label>Department</label>
                          <select value={editDeptId} onChange={(e) => setEditDeptId(e.target.value)}>
                            <option value="">No department</option>
                            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                          </select>
                        </div>
                        <div className="form-group" style={{ marginBottom: 0, minWidth: 220 }}>
                          <label>Role Title</label>
                          <select value={editRole} onChange={(e) => setEditRole(e.target.value)}>
                            <option value="">No role set</option>
                            {ROLE_OPTIONS.map((r) => <option key={r} value={r}>{r}</option>)}
                          </select>
                        </div>
                      </div>

                      {canEditAccessLevel && (
                        <div style={{ marginBottom: 14 }}>
                          <label style={{ display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 8 }}>
                            Access Level (RBAC Roles)
                          </label>
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10 }}>
                            {ACCESS_LEVELS.map((role) => (
                              <label key={role} style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 12.5 }}>
                                <input
                                  type="checkbox"
                                  checked={editAccessLevels.includes(role)}
                                  onChange={() => toggleAccessLevel(role)}
                                />
                                {role.replaceAll('_', ' ')}
                              </label>
                            ))}
                          </div>
                        </div>
                      )}

                      <div style={{ display: 'flex', gap: 8 }}>
                        <button className="btn-primary" style={{ width: 'auto', padding: '9px 18px', display: 'flex', alignItems: 'center', gap: 6 }} onClick={() => saveEdit(p.userId)} disabled={saving}>
                          <Check size={14} /> {saving ? 'Saving...' : 'Save All Changes'}
                        </button>
                        <button className="btn-sm" onClick={cancelEdit} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                          <X size={12} /> Cancel
                        </button>
                      </div>
                    </div>
                  )}

                  {userInfo && !isEditing && (
                    <div style={{ marginBottom: 16 }}>
                      <div className="label" style={{ marginBottom: 8 }}>Access Level (RBAC Roles)</div>
                      <div>
                        {userInfo.roles.map((r) => (
                          <span key={r} className="role-badge">{r.replaceAll('_', ' ')}</span>
                        ))}
                      </div>
                    </div>
                  )}

                  {gap && gap.frameworkFound && (
                    <div style={{ marginBottom: 16 }}>
                      <div className="label" style={{ marginBottom: 8 }}>Gap Analysis Summary</div>
                      <div style={{ fontSize: 13, color: '#334155' }}>
                        {gap.skillsMeetingRequirement} of {gap.totalRequiredSkills} required skills met ·{' '}
                        <span style={{ color: gap.skillsWithGap > 0 ? '#e11d48' : '#0d9488', fontWeight: 600 }}>
                          {gap.skillsWithGap} gap{gap.skillsWithGap === 1 ? '' : 's'}
                        </span>
                      </div>
                    </div>
                  )}

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 20 }}>
                    <div>
                      <div className="label" style={{ marginBottom: 8 }}>Skills ({p.skills?.length || 0})</div>
                      {(!p.skills || p.skills.length === 0) ? (
                        <div style={{ fontSize: 13, color: '#94a3b8' }}>No skills logged</div>
                      ) : (
                        p.skills.map((s) => (
                          <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}>
                            <span>{s.skillName}</span>
                            <span style={{ color: LEVEL_COLORS[s.proficiencyLevel], fontWeight: 600 }}>
                              {s.proficiencyLevel}
                            </span>
                          </div>
                        ))
                      )}
                    </div>

                    <div>
                      <div className="label" style={{ marginBottom: 8 }}>Certifications ({p.certifications?.length || 0})</div>
                      {(!p.certifications || p.certifications.length === 0) ? (
                        <div style={{ fontSize: 13, color: '#94a3b8' }}>No certifications</div>
                      ) : (
                        p.certifications.map((c) => (
                          <div key={c.id} style={{ fontSize: 13, marginBottom: 6 }}>
                            <div style={{ fontWeight: 500 }}>{c.name}</div>
                            <div style={{ color: '#94a3b8', fontSize: 12 }}>{c.issuingBody || '—'}</div>
                          </div>
                        ))
                      )}
                    </div>

                    <div>
                      <div className="label" style={{ marginBottom: 8 }}>Work Experience ({p.workExperience?.length || 0})</div>
                      {(!p.workExperience || p.workExperience.length === 0) ? (
                        <div style={{ fontSize: 13, color: '#94a3b8' }}>No experience logged</div>
                      ) : (
                        p.workExperience.map((w) => (
                          <div key={w.id} style={{ fontSize: 13, marginBottom: 6 }}>
                            <div style={{ fontWeight: 500 }}>{w.roleTitle || w.companyOrProject}</div>
                            <div style={{ color: '#94a3b8', fontSize: 12 }}>{w.companyOrProject}</div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })
      )}
    </Layout>
  );
}
