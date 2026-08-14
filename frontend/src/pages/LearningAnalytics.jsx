import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { GraduationCap, CheckCircle2, TrendingUp, Trophy } from 'lucide-react';
import Layout from '../components/Layout';
import { trainingApi } from '../api/trainingApi';

export default function LearningAnalytics() {
  const [enrollments, setEnrollments] = useState([]);
  const [milestones, setMilestones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [enrRes, msRes] = await Promise.all([
          trainingApi.getMyEnrollments(),
          trainingApi.getMyMilestones().catch(() => ({ data: { data: [] } })),
        ]);
        setEnrollments(enrRes.data.data || []);
        setMilestones(msRes.data.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load learning analytics');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const total = enrollments.length;
  const completed = enrollments.filter((e) => e.completed).length;
  const completionRate = total ? Math.round((completed / total) * 100) : 0;
  const avgProgress = total
    ? Math.round(enrollments.reduce((s, e) => s + e.progressPercent, 0) / total)
    : 0;

  // Learning velocity: milestones achieved per month, chronological.
  const sortedMs = [...milestones].sort((a, b) => new Date(a.achievedAt) - new Date(b.achievedAt));
  const order = [];
  const counts = {};
  sortedMs.forEach((m) => {
    if (!m.achievedAt) return;
    const key = new Date(m.achievedAt).toLocaleDateString(undefined, { month: 'short', year: '2-digit' });
    if (!(key in counts)) { counts[key] = 0; order.push(key); }
    counts[key] += 1;
  });
  const velocityData = order.map((k) => ({ month: k, milestones: counts[k] }));

  const active = enrollments.filter((e) => !e.completed);

  return (
    <Layout title="Learning Analytics" subtitle="Your learning velocity, course progress, and achievements over time">
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading your learning analytics...</div>
      ) : (
        <>
          <div className="card-grid">
            <div className="stat-card">
              <div className="stat-icon teal"><GraduationCap size={17} /></div>
              <div className="stat-label">Enrollments</div>
              <div className="stat-value">{total}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon slate"><CheckCircle2 size={17} /></div>
              <div className="stat-label">Completion Rate</div>
              <div className="stat-value">{completionRate}%</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon amber"><TrendingUp size={17} /></div>
              <div className="stat-label">Avg Progress</div>
              <div className="stat-value">{avgProgress}%</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon teal"><Trophy size={17} /></div>
              <div className="stat-label">Milestones Earned</div>
              <div className="stat-value">{milestones.length}</div>
            </div>
          </div>

          <div className="chart-card" style={{ marginBottom: 24 }}>
            <div className="chart-title">Learning Velocity</div>
            <div className="chart-sub">Milestones achieved per month</div>
            {velocityData.length === 0 ? (
              <div className="loading-text">No milestones yet — complete a course to start building velocity.</div>
            ) : (
              <ResponsiveContainer width="100%" height={240}>
                <BarChart data={velocityData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                  <XAxis dataKey="month" tick={{ fontSize: 11, fill: '#64748b' }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11, fill: '#64748b' }} />
                  <Tooltip />
                  <Bar dataKey="milestones" name="Milestones" fill="#0d9488" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="section-title">Courses In Progress ({active.length})</div>
          {active.length === 0 ? (
            <div className="info-card" style={{ color: '#64748b', marginBottom: 24 }}>No active courses right now.</div>
          ) : (
            active.map((e) => (
              <div key={e.id} className="info-card" style={{ marginBottom: 10 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                  <span style={{ fontWeight: 600, fontSize: 14 }}>{e.courseName}</span>
                  <span style={{ fontSize: 12, color: e.overdue ? '#e11d48' : '#64748b', fontWeight: 600 }}>
                    {e.progressPercent}%{e.overdue ? ' · overdue' : ''}
                  </span>
                </div>
                <div style={{ height: 6, background: '#f1f5f9', borderRadius: 999, overflow: 'hidden' }}>
                  <div style={{ height: '100%', width: `${e.progressPercent}%`, background: e.overdue ? '#e11d48' : '#0d9488', borderRadius: 999 }} />
                </div>
              </div>
            ))
          )}

          <div className="section-title">Recent Achievements</div>
          {milestones.length === 0 ? (
            <div className="info-card" style={{ color: '#64748b' }}>No achievements yet.</div>
          ) : (
            milestones.slice(0, 8).map((m) => (
              <div key={m.id} className="info-card" style={{ marginBottom: 10, display: 'flex', gap: 12, alignItems: 'center' }}>
                <div style={{ width: 34, height: 34, borderRadius: 9, background: '#faf5ff', color: '#9333ea', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <Trophy size={17} />
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 13.5 }}>{m.title}</div>
                  <div style={{ fontSize: 12, color: '#94a3b8' }}>
                    {m.achievedAt ? new Date(m.achievedAt).toLocaleDateString() : ''}
                  </div>
                </div>
              </div>
            ))
          )}
        </>
      )}
    </Layout>
  );
}
