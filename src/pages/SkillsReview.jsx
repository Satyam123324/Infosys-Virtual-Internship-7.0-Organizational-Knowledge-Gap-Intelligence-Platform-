import { useEffect, useState } from 'react';
import { Send, Inbox, Users } from 'lucide-react';
import Layout from '../components/Layout';
import { employeeApi } from '../api/employeeApi';
import { skillReviewApi } from '../api/skillReviewApi';
import { useAuth } from '../context/AuthContext';

const TYPES = [
  { value: 'SELF', label: 'Self-assessment' },
  { value: 'PEER', label: 'Peer review' },
  { value: 'MANAGER', label: 'Manager review' },
];
const RATING_LABELS = { 1: 'Unaware', 2: 'Beginner', 3: 'Intermediate', 4: 'Advanced', 5: 'Expert' };
const TYPE_COLORS = { SELF: '#2563eb', PEER: '#7c3aed', MANAGER: '#0d9488' };

export default function SkillsReview() {
  const { user } = useAuth();
  const [reviewableUsers, setReviewableUsers] = useState([]);
  const [skills, setSkills] = useState([]);
  const [received, setReceived] = useState([]);
  const [submitted, setSubmitted] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [type, setType] = useState('SELF');
  const [assessedUserId, setAssessedUserId] = useState('');
  const [skillId, setSkillId] = useState('');
  const [rating, setRating] = useState('3');
  const [comments, setComments] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [usersRes, skillsRes, receivedRes, submittedRes] = await Promise.all([
        skillReviewApi.getReviewableUsers(),
        employeeApi.getAllSkills(),
        skillReviewApi.getReceived(),
        skillReviewApi.getSubmitted(),
      ]);
      setReviewableUsers(usersRes.data.data || []);
      setSkills(skillsRes.data.data || []);
      setReceived(receivedRes.data.data || []);
      setSubmitted(submittedRes.data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load skill reviews');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!skillId) return;
    if (type !== 'SELF' && !assessedUserId) {
      setError('Please choose a person to review');
      return;
    }
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await skillReviewApi.submit({
        assessedUserId: type === 'SELF' ? user?.id : Number(assessedUserId),
        skillId: Number(skillId),
        type,
        rating: Number(rating),
        comments,
      });
      setSuccess('Skill review submitted!');
      setAssessedUserId('');
      setSkillId('');
      setComments('');
      loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit skill review');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout title="Skills Review" subtitle="360° skill assessments — self, peer, and manager reviews">
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="info-card" style={{ marginBottom: 24 }}>
        <div className="section-title" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
          <Send size={16} /> Submit a Review
        </div>
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 160 }}>
              <label>Review Type</label>
              <select value={type} onChange={(e) => setType(e.target.value)}>
                {TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
            {type !== 'SELF' && (
              <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 200 }}>
                <label>Person</label>
                <select value={assessedUserId} onChange={(e) => setAssessedUserId(e.target.value)} required>
                  <option value="">Select a person...</option>
                  {reviewableUsers.map((u) => (
                    <option key={u.userId} value={u.userId}>
                      {u.fullName} {u.department ? `(${u.department})` : ''}
                    </option>
                  ))}
                </select>
              </div>
            )}
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 160 }}>
              <label>Skill</label>
              <select value={skillId} onChange={(e) => setSkillId(e.target.value)} required>
                <option value="">Select a skill...</option>
                {skills.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
              <label>Rating</label>
              <select value={rating} onChange={(e) => setRating(e.target.value)}>
                {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n} — {RATING_LABELS[n]}</option>)}
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Comments (optional)</label>
            <input value={comments} onChange={(e) => setComments(e.target.value)} placeholder="Any context for this rating?" />
          </div>
          <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} disabled={submitting}>
            {submitting ? 'Submitting...' : 'Submit Review'}
          </button>
        </form>
      </div>

      <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Inbox size={16} /> Reviews About Me ({received.length})
      </div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : received.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b', marginBottom: 20 }}>No reviews about you yet.</div>
      ) : (
        received.map((r) => (
          <div key={r.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{r.skillName}</span>
                <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>by {r.assessorName}</span>
              </div>
              <span className="role-badge" style={{ background: (TYPE_COLORS[r.type] || '#64748b') + '22', color: TYPE_COLORS[r.type] || '#64748b' }}>
                {r.type} · {r.rating}/5
              </span>
            </div>
            {r.comments && <div style={{ fontSize: 13, color: '#334155', marginTop: 6 }}>"{r.comments}"</div>}
          </div>
        ))
      )}

      <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Users size={16} /> Submitted by Me ({submitted.length})
      </div>
      {submitted.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>You haven't submitted any reviews yet.</div>
      ) : (
        submitted.map((s) => (
          <div key={s.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{s.skillName}</span>
                <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>for {s.assessedUserName}</span>
              </div>
              <span className="role-badge" style={{ background: (TYPE_COLORS[s.type] || '#64748b') + '22', color: TYPE_COLORS[s.type] || '#64748b' }}>
                {s.type} · {s.rating}/5
              </span>
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
