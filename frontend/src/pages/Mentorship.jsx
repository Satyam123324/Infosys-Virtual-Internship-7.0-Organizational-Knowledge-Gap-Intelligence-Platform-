import { useEffect, useState } from 'react';
import { Search, GraduationCap, CalendarClock, CheckCircle2, XCircle } from 'lucide-react';
import Layout from '../components/Layout';
import { mentorshipApi } from '../api/mentorshipApi';

const STATUS_CLASS = {
  SCHEDULED: 'status-active',
  COMPLETED: 'status-active',
  CANCELLED: 'status-disabled',
};

export default function Mentorship() {
  const [experts, setExperts] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [skillQuery, setSkillQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [mentorId, setMentorId] = useState('');
  const [topic, setTopic] = useState('');
  const [scheduledAt, setScheduledAt] = useState('');
  const [booking, setBooking] = useState(false);

  const loadExperts = async (skillName) => {
    try {
      const res = await mentorshipApi.findExperts(skillName);
      setExperts(res.data.data);
    } catch (err) {
      setError('Failed to load experts');
    }
  };

  const loadAll = async () => {
    setLoading(true);
    try {
      const [sessionsRes] = await Promise.all([
        mentorshipApi.getMySessions(),
        loadExperts(),
      ]);
      setSessions(sessionsRes.data.data);
    } catch (err) {
      setError('Failed to load mentorship data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    loadExperts(skillQuery.trim() || undefined);
  };

  const handleBook = async (e) => {
    e.preventDefault();
    if (!mentorId || !topic.trim() || !scheduledAt) return;
    setBooking(true);
    setError('');
    setSuccess('');
    try {
      await mentorshipApi.bookSession({ mentorId: Number(mentorId), topic, scheduledAt });
      setSuccess('Mentorship session booked!');
      setMentorId('');
      setTopic('');
      setScheduledAt('');
      const res = await mentorshipApi.getMySessions();
      setSessions(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to book session');
    } finally {
      setBooking(false);
    }
  };

  const handleStatusUpdate = async (id, status) => {
    try {
      await mentorshipApi.updateSessionStatus(id, status);
      const res = await mentorshipApi.getMySessions();
      setSessions(res.data.data);
    } catch (err) {
      setError('Failed to update session');
    }
  };

  return (
    <Layout title="Mentorship" subtitle="Find internal subject-matter experts and book knowledge-sharing sessions">
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="info-card" style={{ marginBottom: 24 }}>
        <div className="section-title" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
          <CalendarClock size={16} /> Book a Session
        </div>
        <form onSubmit={handleBook}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 220 }}>
              <label>Mentor</label>
              <select value={mentorId} onChange={(e) => setMentorId(e.target.value)} required>
                <option value="">Select an expert...</option>
                {experts.map((ex) => (
                  <option key={`${ex.userId}-${ex.skillName}`} value={ex.userId}>
                    {ex.fullName} — {ex.skillName} ({ex.proficiencyLevel})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 220 }}>
              <label>Topic</label>
              <input value={topic} onChange={(e) => setTopic(e.target.value)} placeholder="What do you want to discuss?" required />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 200 }}>
              <label>Scheduled At</label>
              <input type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} required />
            </div>
          </div>
          <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} disabled={booking}>
            {booking ? 'Booking...' : 'Book Session'}
          </button>
        </form>
      </div>

      <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <GraduationCap size={16} /> Expert Directory
      </div>
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 16, maxWidth: 420 }}>
        <input
          value={skillQuery}
          onChange={(e) => setSkillQuery(e.target.value)}
          placeholder="Search by skill, e.g. Java"
          style={{ flex: 1, padding: '9px 12px', borderRadius: 8, border: '1px solid #cbd5e1', fontSize: 13 }}
        />
        <button type="submit" className="btn-sm" style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
          <Search size={14} /> Search
        </button>
      </form>

      <div className="card-grid" style={{ marginBottom: 24 }}>
        {experts.length === 0 ? (
          <div className="info-card" style={{ color: '#64748b' }}>No experts found for this search.</div>
        ) : (
          experts.map((ex) => (
            <div key={`${ex.userId}-${ex.skillName}`} className="info-card">
              <div className="label">{ex.skillName}</div>
              <div className="value" style={{ fontSize: 15 }}>{ex.fullName}</div>
              <div style={{ fontSize: 12, color: '#64748b', marginTop: 4 }}>{ex.designation} · {ex.department}</div>
              <span className="role-badge" style={{ marginTop: 8, display: 'inline-block' }}>{ex.proficiencyLevel}</span>
            </div>
          ))
        )}
      </div>

      <div className="section-title">My Sessions</div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : sessions.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>No mentorship sessions yet.</div>
      ) : (
        sessions.map((s) => (
          <div key={s.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
              <div>
                <span style={{ fontWeight: 600, fontSize: 14 }}>{s.topic}</span>
                <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>
                  {s.isMentor ? `mentoring ${s.menteeName}` : `with ${s.mentorName}`} · {new Date(s.scheduledAt).toLocaleString()}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span className={`status-pill ${STATUS_CLASS[s.status]}`}>{s.status}</span>
                {s.status === 'SCHEDULED' && (
                  <>
                    <button className="btn-sm" onClick={() => handleStatusUpdate(s.id, 'COMPLETED')} title="Mark completed">
                      <CheckCircle2 size={14} />
                    </button>
                    <button className="btn-sm" onClick={() => handleStatusUpdate(s.id, 'CANCELLED')} style={{ color: '#e11d48' }} title="Cancel">
                      <XCircle size={14} />
                    </button>
                  </>
                )}
              </div>
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
