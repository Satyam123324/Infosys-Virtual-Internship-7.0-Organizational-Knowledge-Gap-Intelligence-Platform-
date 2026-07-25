import { useEffect, useState } from 'react';
import { BookOpen, Trash2, Users } from 'lucide-react';
import Layout from '../components/Layout';
import { trainingEnrollmentApi } from '../api/trainingEnrollmentApi';
import { useAuth } from '../context/AuthContext';

const TEAM_ROLES = ['SYSTEM_ADMINISTRATOR', 'HR_SPECIALIST', 'DEPARTMENT_HEAD', 'TEAM_LEAD_MANAGER'];

export default function TrainingEnrollment() {
  const { user } = useAuth();
  const canSeeTeam = user?.roles?.some((r) => TEAM_ROLES.includes(r));

  const [enrollments, setEnrollments] = useState([]);
  const [teamProgress, setTeamProgress] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [courseName, setCourseName] = useState('');
  const [provider, setProvider] = useState('');
  const [deadline, setDeadline] = useState('');
  const [enrolling, setEnrolling] = useState(false);
  const [progressDrafts, setProgressDrafts] = useState({});

  const loadAll = async () => {
    setLoading(true);
    try {
      const calls = [trainingEnrollmentApi.getMyEnrollments()];
      if (canSeeTeam) calls.push(trainingEnrollmentApi.getTeamProgress());
      const [enrollRes, teamRes] = await Promise.all(calls);
      setEnrollments(enrollRes.data.data);
      if (teamRes) setTeamProgress(teamRes.data.data);
    } catch (err) {
      setError('Failed to load training data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleEnroll = async (e) => {
    e.preventDefault();
    if (!courseName.trim() || !deadline) return;
    setEnrolling(true);
    setError('');
    setSuccess('');
    try {
      await trainingEnrollmentApi.enroll({ courseName, provider: provider || undefined, deadline });
      setSuccess('Enrolled successfully!');
      setCourseName('');
      setProvider('');
      setDeadline('');
      loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to enroll');
    } finally {
      setEnrolling(false);
    }
  };

  const handleProgressSave = async (id) => {
    const value = Number(progressDrafts[id]);
    if (Number.isNaN(value) || value < 0 || value > 100) return;
    try {
      await trainingEnrollmentApi.updateProgress(id, value);
      loadAll();
    } catch (err) {
      setError('Failed to update progress');
    }
  };

  const handleCancel = async (id) => {
    if (!confirm('Cancel this training enrollment?')) return;
    try {
      await trainingEnrollmentApi.cancelEnrollment(id);
      loadAll();
    } catch (err) {
      setError('Failed to cancel enrollment');
    }
  };

  return (
    <Layout title="Training & Learning Paths" subtitle="Enroll in courses, track your progress, and stay ahead of deadlines">
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="info-card" style={{ marginBottom: 24 }}>
        <div className="section-title" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
          <BookOpen size={16} /> Enroll in a Course
        </div>
        <form onSubmit={handleEnroll}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 2, minWidth: 220 }}>
              <label>Course Name</label>
              <input value={courseName} onChange={(e) => setCourseName(e.target.value)} placeholder="e.g. Advanced React Patterns" required />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
              <label>Provider (optional)</label>
              <input value={provider} onChange={(e) => setProvider(e.target.value)} placeholder="e.g. Udemy" />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 160 }}>
              <label>Deadline</label>
              <input type="date" value={deadline} onChange={(e) => setDeadline(e.target.value)} required />
            </div>
          </div>
          <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} disabled={enrolling}>
            {enrolling ? 'Enrolling...' : 'Enroll'}
          </button>
        </form>
      </div>

      <div className="section-title">My Enrollments</div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : enrollments.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b', marginBottom: 24 }}>No training enrollments yet.</div>
      ) : (
        <div style={{ marginBottom: 24 }}>
          {enrollments.map((en) => (
            <div key={en.id} className="info-card" style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                <div>
                  <span style={{ fontWeight: 600, fontSize: 14 }}>{en.courseName}</span>
                  {en.provider && <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>{en.provider}</span>}
                  <span style={{ fontSize: 12, color: '#64748b', marginLeft: 8 }}>Deadline: {en.deadline}</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  {en.completed ? (
                    <span className="status-pill status-active">COMPLETED</span>
                  ) : en.overdue ? (
                    <span className="status-pill status-disabled">OVERDUE</span>
                  ) : (
                    <span className="status-pill status-active">IN PROGRESS</span>
                  )}
                  {!en.completed && (
                    <button className="btn-sm" onClick={() => handleCancel(en.id)} style={{ color: '#e11d48' }}>
                      <Trash2 size={14} />
                    </button>
                  )}
                </div>
              </div>

              <div style={{ marginTop: 10, display: 'flex', alignItems: 'center', gap: 10 }}>
                <div style={{ flex: 1, height: 8, background: '#e2e8f0', borderRadius: 999 }}>
                  <div style={{
                    width: `${en.progressPercent}%`, height: '100%', borderRadius: 999,
                    background: en.completed ? '#0d9488' : '#2563eb', transition: 'width 0.2s',
                  }} />
                </div>
                <span style={{ fontSize: 12, fontWeight: 600, minWidth: 34, textAlign: 'right' }}>{en.progressPercent}%</span>
              </div>

              {!en.completed && (
                <div style={{ marginTop: 10, display: 'flex', gap: 8, alignItems: 'center' }}>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    placeholder="Update %"
                    value={progressDrafts[en.id] ?? ''}
                    onChange={(e) => setProgressDrafts({ ...progressDrafts, [en.id]: e.target.value })}
                    style={{ width: 100, padding: '6px 10px', borderRadius: 8, border: '1px solid #cbd5e1', fontSize: 13 }}
                  />
                  <button className="btn-sm" onClick={() => handleProgressSave(en.id)}>Save Progress</button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {canSeeTeam && (
        <>
          <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <Users size={16} /> Team Learning Progress
          </div>
          <div className="chart-card" style={{ overflowX: 'auto' }}>
            <table style={{ minWidth: 700 }}>
              <thead>
                <tr>
                  <th>Employee</th>
                  <th>Department</th>
                  <th style={{ textAlign: 'center' }}>Enrollments</th>
                  <th style={{ textAlign: 'center' }}>Completed</th>
                  <th style={{ textAlign: 'center' }}>Overdue</th>
                  <th style={{ textAlign: 'center' }}>Avg Progress</th>
                  <th style={{ textAlign: 'center' }}>Milestones</th>
                </tr>
              </thead>
              <tbody>
                {teamProgress.length === 0 ? (
                  <tr><td colSpan={7} style={{ color: '#64748b', textAlign: 'center', padding: 16 }}>No team data available.</td></tr>
                ) : (
                  teamProgress.map((t) => (
                    <tr key={t.userId}>
                      <td>{t.fullName}</td>
                      <td>{t.department}</td>
                      <td style={{ textAlign: 'center' }}>{t.totalEnrollments}</td>
                      <td style={{ textAlign: 'center' }}>{t.completedCount}</td>
                      <td style={{ textAlign: 'center', color: t.overdueCount > 0 ? '#e11d48' : 'inherit', fontWeight: t.overdueCount > 0 ? 600 : 400 }}>
                        {t.overdueCount}
                      </td>
                      <td style={{ textAlign: 'center' }}>{t.avgProgressPercent.toFixed(0)}%</td>
                      <td style={{ textAlign: 'center' }}>{t.milestonesEarned}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </>
      )}
    </Layout>
  );
}
