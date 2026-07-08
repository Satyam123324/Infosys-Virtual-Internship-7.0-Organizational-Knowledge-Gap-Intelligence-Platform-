import { useEffect, useState } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import Layout from '../components/Layout';
import { frameworkApi } from '../api/frameworkApi';
import { employeeApi } from '../api/employeeApi';

export default function CompetencyFrameworks() {
  const [frameworks, setFrameworks] = useState([]);
  const [skills, setSkills] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [roleTitle, setRoleTitle] = useState('');
  const [departmentId, setDepartmentId] = useState('');
  const [rows, setRows] = useState([{ skillId: '', requiredLevel: 'INTERMEDIATE', mandatory: true }]);
  const [saving, setSaving] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [fwRes, skillsRes, deptRes] = await Promise.all([
        frameworkApi.getAll(),
        employeeApi.getAllSkills(),
        employeeApi.getAllDepartments(),
      ]);
      setFrameworks(fwRes.data.data);
      setSkills(skillsRes.data.data);
      setDepartments(deptRes.data.data);
    } catch (err) {
      setError('Failed to load frameworks');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const addRow = () => setRows([...rows, { skillId: '', requiredLevel: 'INTERMEDIATE', mandatory: true }]);
  const removeRow = (idx) => setRows(rows.filter((_, i) => i !== idx));
  const updateRow = (idx, field, value) => {
    const newRows = [...rows];
    newRows[idx][field] = value;
    setRows(newRows);
  };

  const handleSave = async () => {
    if (!roleTitle.trim()) return alert('Please enter a role title');
    const validRows = rows.filter((r) => r.skillId);
    if (validRows.length === 0) return alert('Add at least one skill requirement');

    setSaving(true);
    try {
      await frameworkApi.createOrUpdate({
        roleTitle,
        departmentId: departmentId ? Number(departmentId) : null,
        requirements: validRows.map((r) => ({
          skillId: Number(r.skillId),
          requiredLevel: r.requiredLevel,
          mandatory: r.mandatory,
        })),
      });
      setRoleTitle('');
      setDepartmentId('');
      setRows([{ skillId: '', requiredLevel: 'INTERMEDIATE', mandatory: true }]);
      loadAll();
    } catch (err) {
      alert('Failed to save framework: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this competency framework?')) return;
    try {
      await frameworkApi.delete(id);
      loadAll();
    } catch (err) {
      alert('Failed to delete');
    }
  };

  return (
    <Layout title="Competency Frameworks" subtitle="Define the required skill levels for each role — this powers the Gap Analysis engine">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="info-card" style={{ marginBottom: 24 }}>
        <div className="section-title" style={{ marginTop: 0 }}>Create / Update a Role Framework</div>

        <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
          <div className="form-group" style={{ marginBottom: 0, minWidth: 220 }}>
            <label>Role Title</label>
            <input value={roleTitle} onChange={(e) => setRoleTitle(e.target.value)} placeholder="e.g. Software Developer" />
          </div>
          <div className="form-group" style={{ marginBottom: 0, minWidth: 200 }}>
            <label>Department (optional)</label>
            <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
              <option value="">No specific department</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </div>
        </div>

        <div className="label" style={{ marginBottom: 8 }}>Required Skills</div>
        {rows.map((row, idx) => (
          <div key={idx} style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 10 }}>
            <select
              value={row.skillId}
              onChange={(e) => updateRow(idx, 'skillId', e.target.value)}
              style={{ flex: 2, padding: '8px 10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
            >
              <option value="">Select a skill...</option>
              {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
            <select
              value={row.requiredLevel}
              onChange={(e) => updateRow(idx, 'requiredLevel', e.target.value)}
              style={{ flex: 1, padding: '8px 10px', borderRadius: 8, border: '1px solid #cbd5e1' }}
            >
              {['UNAWARE', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'].map((l) => (
                <option key={l} value={l}>{l}</option>
              ))}
            </select>
            <label style={{ fontSize: 13, display: 'flex', alignItems: 'center', gap: 4, whiteSpace: 'nowrap' }}>
              <input type="checkbox" checked={row.mandatory} onChange={(e) => updateRow(idx, 'mandatory', e.target.checked)} />
              Mandatory
            </label>
            <button className="btn-sm" onClick={() => removeRow(idx)} style={{ color: '#e11d48' }}>
              <Trash2 size={14} />
            </button>
          </div>
        ))}

        <button className="btn-sm" onClick={addRow} style={{ display: 'flex', alignItems: 'center', gap: 4, marginBottom: 16 }}>
          <Plus size={14} /> Add Skill Requirement
        </button>

        <div>
          <button className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save Framework'}
          </button>
        </div>
      </div>

      <div className="section-title">Existing Frameworks</div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : frameworks.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>No competency frameworks defined yet.</div>
      ) : (
        frameworks.map((fw) => (
          <div key={fw.id} className="info-card" style={{ marginBottom: 14 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 15 }}>{fw.roleTitle}</span>
                {fw.departmentName && <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>({fw.departmentName})</span>}
              </div>
              <button className="btn-sm" onClick={() => handleDelete(fw.id)} style={{ color: '#e11d48' }}>Delete</button>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
              {fw.requirements.map((r) => (
                <span key={r.id} className="role-badge">{r.skillName}: {r.requiredLevel}</span>
              ))}
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
