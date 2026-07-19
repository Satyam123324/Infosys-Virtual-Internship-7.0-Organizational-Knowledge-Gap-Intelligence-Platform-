import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { Search, UserRound, CalendarPlus, CalendarClock, X, Check, Ban } from 'lucide-react';
import { mentorshipApi } from '../api/mentorshipApi';

const STATUS_STYLE = {
  SCHEDULED: { bg: '#eff6ff', color: '#2563eb' },
  COMPLETED: { bg: '#f0fdfa', color: '#0d9488' },
  CANCELLED: { bg: '#f1f5f9', color: '#64748b' },
};

function BookingModal({ expert, onClose, onBooked }) {
  const [topic, setTopic] = useState('');
  const [scheduledAt, setScheduledAt] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await mentorshipApi.bookSession({
        mentorId: expert.userId,
        topic,
        scheduledAt: new Date(scheduledAt).toISOString(),
      });
      onBooked();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to book session');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(15,23,42,0.45)', zIndex: 100,
      display: 'flex', alignItems: 'center', justifyContent: 'center', padding: 20,
    }}>
      <div className="info-card" style={{ width: 420, maxWidth: '100%', background: 'white' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
          <div style={{ fontWeight: 700, fontSize: 16 }}>Book a session with {expert.fullName}</div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8' }}>
            <X size={18} />
          </button>
        </div>
        <div style={{ fontSize: 12.5, color: '#64748b', marginBottom: 16 }}>
          {expert.skillName} · {expert.proficiencyLevel}
        </div>

        {error && <div className="alert alert-error" style={{ marginBottom: 12 }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>What would you like to discuss?</label>
            <input value={topic} onChange={(e) => setTopic(e.target.value)} placeholder="e.g. React performance patterns" required />
          </div>
          <div className="form-group">
            <label>When</label>
            <input type="datetime-local" value={scheduledAt} onChange={(e) => setScheduledAt(e.target.value)} required />
          </div>
          <button type="submit" className="btn-primary" disabled={saving}>
            {saving ? 'Booking...' : 'Book Session'}
          </button>
        </form>
      </div>
    </div>
  );
}

export default function MentorshipDirectory() {
  const [skillName, setSkillName] = useState('');
  const [experts, setExperts] = useState([]);
  const [expertsLoading, setExpertsLoading] = useState(true);
  const [expertsError, setExpertsError] = useState('');
  const [bookingExpert, setBookingExpert] = useState(null);

  const [sessions, setSessions] = useState([]);
  const [sessionsLoading, setSessionsLoading] = useState(true);

  const loadExperts = async (search) => {
    setExpertsLoading(true);
    setExpertsError('');
    try {
      const { data } = await mentorshipApi.findExperts(search);
      setExperts(data.data);
    } catch (err) {
      setExpertsError(err.response?.data?.message || 'Failed to load experts');
      setExperts([]);
    } finally {
      setExpertsLoading(false);
    }
  };

  const loadSessions = async () => {
    setSessionsLoading(true);
    try {
      const { data } = await mentorshipApi.getMySessions();
      setSessions(data.data);
    } catch (err) {
      // fail quietly
    } finally {
      setSessionsLoading(false);
    }
  };

  useEffect(() => {
    loadExperts('');
    loadSessions();
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    loadExperts(skillName);
  };

  const handleSessionStatus = async (id, status) => {
    try {
      await mentorshipApi.updateSessionStatus(id, status);
      loadSessions();
    } catch (err) { /* ignore */ }
  };

  const upcoming = sessions.filter((s) => s.status === 'SCHEDULED');
  const past = sessions.filter((s) => s.status !== 'SCHEDULED');

  return (
    <Layout
      title="Mentorship & Expert Directory"
      subtitle="Find someone who knows the skill you're stuck on, and book time with them directly"
    >
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: 8, marginBottom: 18 }}>
        <div className="auth-input-icon-group" style={{ flex: 1, maxWidth: 360 }}>
          <Search size={15} />
          <input
            value={skillName}
            onChange={(e) => setSkillName(e.target.value)}
            placeholder="Search by skill, e.g. React, SQL, Leadership..."
          />
        </div>
        <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '0 20px' }}>Search</button>
        {skillName && (
          <button type="button" className="btn-sm" onClick={() => { setSkillName(''); loadExperts(''); }}>
            Clear
          </button>
        )}
      </form>

      {expertsError && <div className="alert alert-error">{expertsError}</div>}

      {expertsLoading ? (
        <div className="loading-text">Finding experts...</div>
      ) : experts.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '28px 20px', color: '#94a3b8', marginBottom: 24 }}>
          <UserRound size={24} style={{ marginBottom: 8, opacity: 0.6 }} />
          <div style={{ fontSize: 13.5 }}>
            {skillName ? `No one is rated ADVANCED or EXPERT in "${skillName}" yet.` : 'No experts found yet — encourage teammates to fill in their skill inventory.'}
          </div>
        </div>
      ) : (
        <div className="card-grid" style={{ marginBottom: 24 }}>
          {experts.map((ex) => (
            <div key={`${ex.userId}-${ex.skillName}`} className="info-card">
              <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
                <div style={{
                  width: 36, height: 36, borderRadius: 999, background: '#0d9488', color: 'white',
                  display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 13, flexShrink: 0,
                }}>
                  {ex.fullName.split(' ').map((n) => n[0]).slice(0, 2).join('').toUpperCase()}
                </div>
                <div style={{ minWidth: 0 }}>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{ex.fullName}</div>
                  <div style={{ fontSize: 12, color: '#64748b' }}>{ex.designation || 'No role set'}</div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 6, marginBottom: 12 }}>
                <span className="role-badge">{ex.skillName}</span>
                <span className="status-pill" style={{ background: '#faf5ff', color: '#9333ea', fontSize: 11 }}>{ex.proficiencyLevel}</span>
              </div>
              <button
                className="btn-sm"
                style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6 }}
                onClick={() => setBookingExpert(ex)}
              >
                <CalendarPlus size={14} /> Book a session
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="section-title">My Sessions</div>
      {sessionsLoading ? (
        <div className="loading-text">Loading sessions...</div>
      ) : sessions.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '24px 20px', color: '#94a3b8' }}>
          <CalendarClock size={22} style={{ marginBottom: 8, opacity: 0.6 }} />
          <div style={{ fontSize: 13.5 }}>No mentorship sessions yet.</div>
        </div>
      ) : (
        <>
          {[...upcoming, ...past].map((s) => {
            const style = STATUS_STYLE[s.status];
            const counterpart = s.isMentor ? s.menteeName : s.mentorName;
            const roleLabel = s.isMentor ? 'Mentoring' : 'Learning from';
            return (
              <div key={s.id} className="info-card" style={{ marginBottom: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
                <div>
                  <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                    <span style={{ fontWeight: 600, fontSize: 14 }}>{s.topic}</span>
                    <span className="status-pill" style={{ background: style.bg, color: style.color, fontSize: 11 }}>{s.status}</span>
                  </div>
                  <div style={{ fontSize: 12.5, color: '#64748b', marginTop: 3 }}>
                    {roleLabel} <strong>{counterpart}</strong> · {new Date(s.scheduledAt).toLocaleString()}
                  </div>
                </div>
                {s.status === 'SCHEDULED' && (
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button className="btn-sm" onClick={() => handleSessionStatus(s.id, 'COMPLETED')} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <Check size={13} /> Mark done
                    </button>
                    <button className="btn-sm" onClick={() => handleSessionStatus(s.id, 'CANCELLED')} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <Ban size={13} /> Cancel
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </>
      )}

      {bookingExpert && (
        <BookingModal
          expert={bookingExpert}
          onClose={() => setBookingExpert(null)}
          onBooked={() => { setBookingExpert(null); loadSessions(); }}
        />
      )}
    </Layout>
  );
}
