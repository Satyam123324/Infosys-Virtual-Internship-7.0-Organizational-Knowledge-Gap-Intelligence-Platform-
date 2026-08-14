import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { History, TrendingUp, TrendingDown, Minus, ClipboardCheck } from 'lucide-react';
import Layout from '../components/Layout';
import { assessmentApi } from '../api/assessmentApi';

export default function AssessmentHistory() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const { data } = await assessmentApi.getMyResults();
        setResults(data.data || []);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load assessment history');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  // Group attempts by skill, oldest → newest.
  const bySkill = {};
  results.forEach((r) => {
    (bySkill[r.skillName] = bySkill[r.skillName] || []).push(r);
  });
  Object.values(bySkill).forEach((arr) => arr.sort((a, b) => new Date(a.takenAt) - new Date(b.takenAt)));

  const comparison = Object.entries(bySkill)
    .map(([skill, attempts]) => {
      const first = attempts[0];
      const latest = attempts[attempts.length - 1];
      return {
        skill,
        attempts: attempts.length,
        firstScore: Math.round(first.scorePercent),
        latestScore: Math.round(latest.scorePercent),
        change: Math.round(latest.scorePercent - first.scorePercent),
        level: latest.computedLevel,
      };
    })
    .sort((a, b) => b.attempts - a.attempts);

  const chronological = [...results]
    .sort((a, b) => new Date(a.takenAt) - new Date(b.takenAt))
    .map((r) => ({
      date: new Date(r.takenAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
      score: Math.round(r.scorePercent),
      skill: r.skillName,
    }));

  const totalAssessments = results.length;
  const skillsAssessed = Object.keys(bySkill).length;
  const avgLatest = comparison.length
    ? Math.round(comparison.reduce((s, c) => s + c.latestScore, 0) / comparison.length)
    : 0;

  return (
    <Layout title="Assessment History" subtitle="Your past assessments and how your scores have changed over time">
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading your assessment history...</div>
      ) : results.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '40px 20px', color: '#94a3b8' }}>
          <ClipboardCheck size={28} style={{ marginBottom: 10, opacity: 0.6 }} />
          <div style={{ fontSize: 14 }}>No assessments taken yet — take an Assessment Test to start tracking your progress.</div>
        </div>
      ) : (
        <>
          <div className="card-grid">
            <div className="stat-card">
              <div className="stat-icon teal"><ClipboardCheck size={17} /></div>
              <div className="stat-label">Assessments Taken</div>
              <div className="stat-value">{totalAssessments}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon slate"><History size={17} /></div>
              <div className="stat-label">Skills Assessed</div>
              <div className="stat-value">{skillsAssessed}</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon amber"><TrendingUp size={17} /></div>
              <div className="stat-label">Avg Latest Score</div>
              <div className="stat-value">{avgLatest}%</div>
            </div>
          </div>

          <div className="chart-card" style={{ marginBottom: 24 }}>
            <div className="chart-title">Score Over Time</div>
            <div className="chart-sub">Every assessment attempt, chronologically</div>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={chronological}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="date" tick={{ fontSize: 11, fill: '#64748b' }} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 11, fill: '#64748b' }} />
                <Tooltip formatter={(v, n, p) => [`${v}%`, p.payload.skill]} />
                <Line type="monotone" dataKey="score" name="Score %" stroke="#0d9488" strokeWidth={2} dot={{ r: 3 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="section-title">Skill-by-Skill Comparison</div>
          <table>
            <thead>
              <tr>
                <th>Skill</th>
                <th>Attempts</th>
                <th>First Score</th>
                <th>Latest Score</th>
                <th>Change</th>
                <th>Current Level</th>
              </tr>
            </thead>
            <tbody>
              {comparison.map((c) => (
                <tr key={c.skill}>
                  <td>{c.skill}</td>
                  <td>{c.attempts}</td>
                  <td>{c.firstScore}%</td>
                  <td>{c.latestScore}%</td>
                  <td>
                    <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontWeight: 600, color: c.change > 0 ? '#0d9488' : c.change < 0 ? '#e11d48' : '#94a3b8' }}>
                      {c.change > 0 ? <TrendingUp size={14} /> : c.change < 0 ? <TrendingDown size={14} /> : <Minus size={14} />}
                      {c.change > 0 ? '+' : ''}{c.change}%
                    </span>
                  </td>
                  <td><span className="status-pill" style={{ background: '#f1f5f9', color: '#334155' }}>{c.level}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </Layout>
  );
}
