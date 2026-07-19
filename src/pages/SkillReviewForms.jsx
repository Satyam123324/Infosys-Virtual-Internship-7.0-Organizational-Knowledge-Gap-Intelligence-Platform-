import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { UserCircle, Users, ShieldCheck, Star, Inbox } from 'lucide-react';
import { skillReviewApi } from '../api/skillReviewApi';
import { employeeApi } from '../api/employeeApi';
import { useAuth } from '../context/AuthContext';

const RATING_LABEL = { 1: 'Unaware', 2: 'Beginner', 3: 'Intermediate', 4: 'Advanced', 5: 'Expert' };
const MANAGER_TIER_ROLES = ['TEAM_LEAD_MANAGER', 'DEPARTMENT_HEAD', 'HR_SPECIALIST', 'LEARNING_DEVELOPMENT_ADMIN', 'SYSTEM_ADMINISTRATOR'];

const TABS = [
  { key: 'SELF', label: 'Self Assessment', Icon: UserCircle },
  { key: 'PEER', label: 'Peer Assessment', Icon: Users },
  { key: 'MANAGER', label: 'Manager Assessment', Icon: ShieldCheck },
];

const TYPE_BADGE = {
  SELF: { bg: '#eff6ff', color: '#2563eb' },
  PEER: { bg: '#f0fdfa', color: '#0d9488' },
  MANAGER: { bg: '#faf5ff', color: '#9333ea' },
};

function StarRating({ value, onChange }) {
  return (
    <div style={{ display: 'flex', gap: 4 }}>
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          type="button"
          key={n}
          onClick={() => onChange(n)}
          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 2 }}
          title={RATING_LABEL[n]}
        >
          <Star size={26} fill={n <= value ? '#f59e0b' : 'none'} color={n <= value ? '#f59e0b' : '#cbd5e1'} />
        </button>
      ))}
      <span style={{ marginLeft: 8, fontSize: 13, color: '#64748b', alignSelf: 'center' }}>
        {value ? RATING_LABEL[value] : 'Select a rating'}
      </span>
    </div>
  );
}

export default function SkillReviewForms() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('SELF');
  const [skills, setSkills] = useState([]);
  const [colleagues, setColleagues] = useState([]);
  const [received, setReceived] = useState([]);
  const [submitted, setSubmitted] = useState([]);
  const [loading, setLoading] = useState(true);

  const [form, setForm] = useState({ skillId: '', assessedUserId: '', rating: 0, comments: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const isManagerTier = user?.roles?.some((r) => MANAGER_TIER_ROLES.includes(r));

  const loadAll = async () => {
    setLoading(true);
    try {
      const [skillsRes, colleaguesRes, receivedRes, submittedRes] = await Promise.all([
        employeeApi.getAllSkills(),
        skillReviewApi.getReviewableUsers(),
        skillReviewApi.getReceived(),
        skillReviewApi.getSubmitted(),
      ]);
      setSkills(skillsRes.data.data);
      setColleagues(colleaguesRes.data.data);
      setReceived(receivedRes.data.data);
      setSubmitted(submittedRes.data.data);
    } catch (err) {
      // fail quietly, form still usable once catalogs load on retry
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAll(); }, []);

  const handleTabChange = (key) => {
    setActiveTab(key);
    setForm({ skillId: '', assessedUserId: '', rating: 0, comments: '' });
    setError('');
    setSuccessMsg('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');

    if (!form.skillId || !form.rating) {
      setError('Please select a skill and a rating');
      return;
    }
    if (activeTab !== 'SELF' && !form.assessedUserId) {
      setError('Please select who you are assessing');
      return;
    }

    setSaving(true);
    try {
      await skillReviewApi.submit({
        assessedUserId: activeTab === 'SELF' ? user.id : Number(form.assessedUserId),
        skillId: Number(form.skillId),
        type: activeTab,
        rating: form.rating,
        comments: form.comments,
      });
      setSuccessMsg('Assessment submitted successfully.');
      setForm({ skillId: '', assessedUserId: '', rating: 0, comments: '' });
      loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit assessment');
    } finally {
      setSaving(false);
    }
  };

  const canUseTab = activeTab !== 'MANAGER' || isManagerTier;

  return (
    <Layout
      title="Skill Assessment Forms"
      subtitle="Rate your own proficiency, review a colleague's, or — if you're a manager — assess your team"
    >
      <div style={{ display: 'flex', gap: 8, marginBottom: 18, flexWrap: 'wrap' }}>
        {TABS.map(({ key, label, Icon }) => (
          <button
            key={key}
            onClick={() => handleTabChange(key)}
            className="status-pill"
            style={{
              border: '1px solid ' + (activeTab === key ? '#0d9488' : '#e2e8f0'),
              background: activeTab === key ? '#0d9488' : 'white',
              color: activeTab === key ? 'white' : '#475569',
              cursor: 'pointer', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6, padding: '8px 14px',
            }}
          >
            <Icon size={14} /> {label}
          </button>
        ))}
      </div>

      <div className="info-card" style={{ marginBottom: 24 }}>
        {activeTab === 'MANAGER' && !isManagerTier ? (
          <div style={{ color: '#94a3b8', fontSize: 13.5, textAlign: 'center', padding: '16px 0' }}>
            Manager assessments are limited to accounts with a manager, HR, or admin role. Your current role doesn't have access to this form.
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}
            {successMsg && <div className="alert" style={{ background: '#f0fdfa', color: '#0d9488', marginBottom: 12, padding: '10px 14px', borderRadius: 8, fontSize: 13.5 }}>{successMsg}</div>}

            {activeTab !== 'SELF' && (
              <div className="form-group">
                <label>{activeTab === 'PEER' ? 'Colleague' : 'Team member'}</label>
                <select value={form.assessedUserId} onChange={(e) => setForm({ ...form, assessedUserId: e.target.value })}>
                  <option value="">Select a person...</option>
                  {colleagues.map((c) => (
                    <option key={c.userId} value={c.userId}>
                      {c.fullName}{c.designation ? ` — ${c.designation}` : ''}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div className="form-group">
              <label>Skill</label>
              <select value={form.skillId} onChange={(e) => setForm({ ...form, skillId: e.target.value })}>
                <option value="">Select a skill...</option>
                {skills.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Proficiency Rating</label>
              <StarRating value={form.rating} onChange={(v) => setForm({ ...form, rating: v })} />
            </div>

            <div className="form-group">
              <label>Comments (optional)</label>
              <textarea
                rows={3}
                value={form.comments}
                onChange={(e) => setForm({ ...form, comments: e.target.value })}
                placeholder="Specific examples help — e.g. a project where this skill showed up"
              />
            </div>

            <button type="submit" className="btn-primary" disabled={saving || !canUseTab}>
              {saving ? 'Submitting...' : 'Submit Assessment'}
            </button>
          </form>
        )}
      </div>

      <div className="section-title">Assessments About Me</div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : received.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '24px 20px', color: '#94a3b8', marginBottom: 24 }}>
          <Inbox size={22} style={{ marginBottom: 8, opacity: 0.6 }} />
          <div style={{ fontSize: 13.5 }}>No one has assessed your skills yet.</div>
        </div>
      ) : (
        <div style={{ marginBottom: 24 }}>
          {received.map((r) => {
            const badge = TYPE_BADGE[r.type];
            return (
              <div key={r.id} className="info-card" style={{ marginBottom: 10 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                    <span style={{ fontWeight: 600, fontSize: 14 }}>{r.skillName}</span>
                    <span className="status-pill" style={{ background: badge.bg, color: badge.color, fontSize: 11 }}>{r.type}</span>
                  </div>
                  <div style={{ display: 'flex', gap: 2 }}>
                    {[1, 2, 3, 4, 5].map((n) => (
                      <Star key={n} size={14} fill={n <= r.rating ? '#f59e0b' : 'none'} color={n <= r.rating ? '#f59e0b' : '#cbd5e1'} />
                    ))}
                  </div>
                </div>
                <div style={{ fontSize: 12.5, color: '#64748b', marginTop: 4 }}>
                  {r.type === 'SELF' ? 'Self-rated' : `By ${r.assessorName}`} · {new Date(r.submittedAt).toLocaleDateString()}
                </div>
                {r.comments && <div style={{ fontSize: 13, color: '#334155', marginTop: 6 }}>{r.comments}</div>}
              </div>
            );
          })}
        </div>
      )}

      {submitted.length > 0 && (
        <>
          <div className="section-title">Reviews I've Submitted</div>
          {submitted.map((r) => {
            const badge = TYPE_BADGE[r.type];
            return (
              <div key={r.id} className="info-card" style={{ marginBottom: 10 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                  <span style={{ fontWeight: 600, fontSize: 14 }}>{r.assessedUserName}</span>
                  <span className="status-pill" style={{ background: badge.bg, color: badge.color, fontSize: 11 }}>{r.type}</span>
                  <span style={{ fontSize: 12.5, color: '#64748b' }}>· {r.skillName} · {RATING_LABEL[r.rating]}</span>
                </div>
              </div>
            );
          })}
        </>
      )}
    </Layout>
  );
}
