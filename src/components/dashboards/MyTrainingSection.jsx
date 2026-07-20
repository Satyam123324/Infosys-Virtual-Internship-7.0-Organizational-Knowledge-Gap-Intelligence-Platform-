import { useEffect, useState } from 'react';
import { GraduationCap, Plus, X, Trophy } from 'lucide-react';
import { trainingApi } from '../../api/trainingApi';

function progressColor(enrollment) {
  if (enrollment.completed) return '#0d9488';
  if (enrollment.overdue) return '#e11d48';
  return '#2563eb';
}

function EnrollmentCard({ enrollment, onUpdateProgress, onCancel }) {
  const color = progressColor(enrollment);
  return (
    <div className="info-card" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
        <div style={{ minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
            <span style={{ fontWeight: 600, fontSize: 14.5 }}>{enrollment.courseName}</span>
            {enrollment.completed && (
              <span className="status-pill" style={{ background: '#f0fdfa', color: '#0d9488', fontSize: 11, display: 'flex', alignItems: 'center', gap: 4 }}>
                <Trophy size={11} /> Completed
              </span>
            )}
            {!enrollment.completed && enrollment.overdue && (
              <span className="status-pill" style={{ background: '#fff1f2', color: '#e11d48', fontSize: 11 }}>Overdue</span>
            )}
          </div>
          <div style={{ fontSize: 12.5, color: '#64748b', marginTop: 3 }}>
            {enrollment.provider ? enrollment.provider + ' · ' : ''}
            Due {new Date(enrollment.deadline).toLocaleDateString()}
          </div>
        </div>
        <button onClick={() => onCancel(enrollment.id)} title="Cancel enrollment" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8', flexShrink: 0 }}>
          <X size={16} />
        </button>
      </div>

      <div style={{ marginTop: 12 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, color: '#64748b', marginBottom: 4 }}>
          <span>Progress</span>
          <span style={{ fontWeight: 600, color }}>{enrollment.progressPercent}%</span>
        </div>
        <div style={{ height: 8, background: '#f1f5f9', borderRadius: 999, overflow: 'hidden' }}>
          <div style={{
            height: '100%', width: `${enrollment.progressPercent}%`, background: color,
            borderRadius: 999, transition: 'width 0.3s ease',
          }} />
        </div>

        {!enrollment.completed && (
          <input
            type="range"
            min={0}
            max={100}
            step={5}
            value={enrollment.progressPercent}
            onChange={(e) => onUpdateProgress(enrollment.id, Number(e.target.value))}
            style={{ width: '100%', marginTop: 10, accentColor: color, cursor: 'pointer' }}
          />
        )}
      </div>
    </div>
  );
}

export default function MyTrainingSection() {
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ courseName: '', provider: '', deadline: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const { data } = await trainingApi.getMyEnrollments();
      setEnrollments(data.data);
    } catch (err) {
      // fail quietly — dashboard still shows the rest
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleUpdateProgress = async (id, progressPercent) => {
    // optimistic update so the slider feels responsive
    setEnrollments((prev) => prev.map((e) => (e.id === id ? { ...e, progressPercent } : e)));
    try {
      await trainingApi.updateProgress(id, progressPercent);
      load(); // resync — picks up completed/milestone state if it hit 100%
    } catch (err) {
      load(); // revert to server truth on failure
    }
  };

  const handleCancel = async (id) => {
    try {
      await trainingApi.cancelEnrollment(id);
      load();
    } catch (err) { /* ignore */ }
  };

  const handleEnroll = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await trainingApi.enroll(form);
      setForm({ courseName: '', provider: '', deadline: '' });
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to enroll');
    } finally {
      setSaving(false);
    }
  };

  const active = enrollments.filter((e) => !e.completed);
  const completed = enrollments.filter((e) => e.completed);

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div className="section-title" style={{ marginBottom: 0 }}>My Training</div>
        <button className="btn-sm" onClick={() => setShowForm((s) => !s)} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Plus size={14} /> Enroll in a course
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleEnroll} className="info-card" style={{ marginTop: 10, marginBottom: 14 }}>
          {error && <div className="alert alert-error" style={{ marginBottom: 10 }}>{error}</div>}
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            <div className="form-group" style={{ flex: '1 1 220px', marginBottom: 0 }}>
              <label>Course Name</label>
              <input
                value={form.courseName}
                onChange={(e) => setForm({ ...form, courseName: e.target.value })}
                required
              />
            </div>
            <div className="form-group" style={{ flex: '1 1 160px', marginBottom: 0 }}>
              <label>Provider (optional)</label>
              <input
                value={form.provider}
                onChange={(e) => setForm({ ...form, provider: e.target.value })}
                placeholder="e.g. Coursera"
              />
            </div>
            <div className="form-group" style={{ flex: '1 1 160px', marginBottom: 0 }}>
              <label>Deadline</label>
              <input
                type="date"
                value={form.deadline}
                onChange={(e) => setForm({ ...form, deadline: e.target.value })}
                required
              />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
            <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '9px 18px' }} disabled={saving}>
              {saving ? 'Enrolling...' : 'Enroll'}
            </button>
            <button type="button" className="btn-sm" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="loading-text">Loading your training...</div>
      ) : enrollments.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '28px 20px', color: '#94a3b8' }}>
          <GraduationCap size={24} style={{ marginBottom: 8, opacity: 0.6 }} />
          <div style={{ fontSize: 13.5 }}>No training enrollments yet — enroll in a course to start tracking progress.</div>
        </div>
      ) : (
        <>
          {active.map((e) => (
            <EnrollmentCard key={e.id} enrollment={e} onUpdateProgress={handleUpdateProgress} onCancel={handleCancel} />
          ))}
          {completed.length > 0 && (
            <>
              <div style={{ fontSize: 12.5, fontWeight: 600, color: '#94a3b8', margin: '14px 0 8px' }}>COMPLETED</div>
              {completed.map((e) => (
                <EnrollmentCard key={e.id} enrollment={e} onUpdateProgress={handleUpdateProgress} onCancel={handleCancel} />
              ))}
            </>
          )}
        </>
      )}
    </>
  );
}
