import { useEffect, useState } from 'react';
import { Users, Send, Inbox } from 'lucide-react';
import Layout from '../components/Layout';
import { employeeApi } from '../api/employeeApi';
import { peerAssessmentApi } from '../api/peerAssessmentApi';

const LEVELS = ['UNAWARE', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'];

const LEVEL_COLORS = {
  UNAWARE: '#e11d48',
  BEGINNER: '#f59e0b',
  INTERMEDIATE: '#2563eb',
  ADVANCED: '#7c3aed',
  EXPERT: '#0d9488',
};

export default function PeerAssessment() {
  const [colleagues, setColleagues] = useState([]);
  const [skills, setSkills] = useState([]);
  const [received, setReceived] = useState([]);
  const [given, setGiven] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [targetUserId, setTargetUserId] = useState('');
  const [skillId, setSkillId] = useState('');
  const [ratedLevel, setRatedLevel] = useState('INTERMEDIATE');
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [colleaguesRes, skillsRes, receivedRes, givenRes] = await Promise.all([
        peerAssessmentApi.getColleagues(),
        employeeApi.getAllSkills(),
        peerAssessmentApi.getReceived(),
        peerAssessmentApi.getGiven(),
      ]);
      setColleagues(colleaguesRes.data.data);
      setSkills(skillsRes.data.data);
      setReceived(receivedRes.data.data);
      setGiven(givenRes.data.data);
    } catch (err) {
      setError('Failed to load peer assessment data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!targetUserId || !skillId) return;
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await peerAssessmentApi.submit({
        targetUserId: Number(targetUserId),
        skillId: Number(skillId),
        ratedLevel,
        comment,
      });
      setSuccess('Peer assessment submitted successfully!');
      setTargetUserId('');
      setSkillId('');
      setComment('');
      loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit peer assessment');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout title="Peer Assessment" subtitle="Rate a colleague's skill proficiency, and see how others have rated you">
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="info-card" style={{ marginBottom: 24 }}>
        <div className="section-title" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
          <Send size={16} /> Rate a Colleague
        </div>
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 200 }}>
              <label>Colleague</label>
              <select value={targetUserId} onChange={(e) => setTargetUserId(e.target.value)} required>
                <option value="">Select a colleague...</option>
                {colleagues.map((c) => (
                  <option key={c.userId} value={c.userId}>
                    {c.fullName} {c.departmentName ? `(${c.departmentName})` : ''}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
              <label>Skill</label>
              <select value={skillId} onChange={(e) => setSkillId(e.target.value)} required>
                <option value="">Select a skill...</option>
                {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 160 }}>
              <label>Your Rating</label>
              <select value={ratedLevel} onChange={(e) => setRatedLevel(e.target.value)}>
                {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Comment (optional)</label>
            <input value={comment} onChange={(e) => setComment(e.target.value)} placeholder="Any context for this rating?" />
          </div>
          <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit Peer Assessment'}
          </button>
        </form>
      </div>

      <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Inbox size={16} /> Received from Colleagues ({received.length})
      </div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : received.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b', marginBottom: 20 }}>No peer assessments received yet.</div>
      ) : (
        received.map((r) => (
          <div key={r.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{r.skillName}</span>
                <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>rated by {r.raterName}</span>
              </div>
              <span className="role-badge" style={{ background: LEVEL_COLORS[r.ratedLevel] + '22', color: LEVEL_COLORS[r.ratedLevel] }}>
                {r.ratedLevel}
              </span>
            </div>
            {r.comment && <div style={{ fontSize: 13, color: '#334155', marginTop: 6 }}>"{r.comment}"</div>}
          </div>
        ))
      )}

      <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Users size={16} /> Given by Me ({given.length})
      </div>
      {given.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>You haven't rated any colleagues yet.</div>
      ) : (
        given.map((g) => (
          <div key={g.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{g.skillName}</span>
                <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>for {g.targetName}</span>
              </div>
              <span className="role-badge" style={{ background: LEVEL_COLORS[g.ratedLevel] + '22', color: LEVEL_COLORS[g.ratedLevel] }}>
                {g.ratedLevel}
              </span>
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
