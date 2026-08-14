import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { employeeApi } from '../api/employeeApi';

const LEVELS = ['UNAWARE', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

export default function SkillInventory() {
  const [mySkills, setMySkills] = useState([]);
  const [allSkills, setAllSkills] = useState([]);
  const [selectedSkillId, setSelectedSkillId] = useState('');
  const [selectedLevel, setSelectedLevel] = useState('BEGINNER');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadData = async () => {
    setLoading(true);
    try {
      const [skillsRes, catalogRes] = await Promise.all([
        employeeApi.getMySkills(),
        employeeApi.getAllSkills(),
      ]);
      setMySkills(skillsRes.data.data);
      setAllSkills(catalogRes.data.data);
    } catch (err) {
      setError('Failed to load skill data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleAddSkill = async (e) => {
    e.preventDefault();
    if (!selectedSkillId) return;
    try {
      await employeeApi.addSkill({
        skillId: Number(selectedSkillId),
        proficiencyLevel: selectedLevel,
        selfRating: LEVELS.indexOf(selectedLevel) + 1,
      });
      setSelectedSkillId('');
      loadData();
    } catch (err) {
      alert('Failed to add skill: ' + (err.response?.data?.message || 'Unknown error'));
    }
  };

  const handleRemove = async (skillId) => {
    if (!confirm('Remove this skill from your profile?')) return;
    try {
      await employeeApi.removeSkill(skillId);
      loadData();
    } catch (err) {
      alert('Failed to remove skill');
    }
  };

  const myCatalogSkillIds = new Set(mySkills.map((s) => s.skillId));
  const availableToAdd = allSkills.filter((s) => !myCatalogSkillIds.has(s.id));

  return (
    <Layout title="My Skill Inventory" subtitle="Self-assess your proficiency across the organization's skill catalog">
      {error && <div className="alert alert-error">{error}</div>}

        <div className="info-card" style={{ marginBottom: 24 }}>
          <div className="section-title" style={{ marginTop: 0 }}>Add a skill</div>
          <form onSubmit={handleAddSkill} style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div className="form-group" style={{ marginBottom: 0, minWidth: 220 }}>
              <label>Skill</label>
              <select value={selectedSkillId} onChange={(e) => setSelectedSkillId(e.target.value)} required>
                <option value="">Select a skill...</option>
                {availableToAdd.map((s) => (
                  <option key={s.id} value={s.id}>{s.name} ({s.categoryName})</option>
                ))}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0, minWidth: 180 }}>
              <label>Proficiency Level</label>
              <select value={selectedLevel} onChange={(e) => setSelectedLevel(e.target.value)}>
                {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
            <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 20px' }}>
              Add Skill
            </button>
          </form>
        </div>

        <div className="section-title">My Skills ({mySkills.length})</div>
        {loading ? (
          <div className="loading-text">Loading...</div>
        ) : mySkills.length === 0 ? (
          <div className="info-card" style={{ color: '#6b7280' }}>No skills added yet. Add your first skill above.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Skill</th>
                <th>Category</th>
                <th>Proficiency</th>
                <th>Last Assessed</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {mySkills.map((s) => (
                <tr key={s.id}>
                  <td>{s.skillName}</td>
                  <td>{s.categoryName || '—'}</td>
                  <td><span className="role-badge">{s.proficiencyLevel}</span></td>
                  <td>{s.lastAssessedDate || '—'}</td>
                  <td>
                    <button className="btn-sm" style={{ color: '#dc2626' }} onClick={() => handleRemove(s.skillId)}>
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
    </Layout>
  );
}
