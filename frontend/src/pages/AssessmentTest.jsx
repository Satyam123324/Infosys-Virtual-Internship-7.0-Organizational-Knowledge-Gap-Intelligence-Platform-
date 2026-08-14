import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { employeeApi } from '../api/employeeApi';
import { assessmentApi } from '../api/assessmentApi';

const LEVEL_COLORS = {
  UNAWARE: '#dc2626',
  BEGINNER: '#ca8a04',
  INTERMEDIATE: '#2563eb',
  ADVANCED: '#7c3aed',
  EXPERT: '#16a34a',
};

export default function AssessmentTest() {
  const [skills, setSkills] = useState([]);
  const [selectedSkillId, setSelectedSkillId] = useState('');
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [result, setResult] = useState(null);
  const [myResults, setMyResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadSkillsAndResults();
  }, []);

  const loadSkillsAndResults = async () => {
    try {
      const [skillsRes, resultsRes] = await Promise.all([
        employeeApi.getAllSkills(),
        assessmentApi.getMyResults().catch(() => ({ data: { data: [] } })),
      ]);
      setSkills(skillsRes.data.data);
      setMyResults(resultsRes.data.data);
    } catch (err) {
      setError('Failed to load data');
    }
  };

  const handleSelectSkill = async (skillId) => {
    setSelectedSkillId(skillId);
    setResult(null);
    setAnswers({});
    setError('');
    if (!skillId) {
      setQuestions([]);
      return;
    }
    setLoading(true);
    try {
      const { data } = await assessmentApi.getQuestions(skillId);
      setQuestions(data.data);
    } catch (err) {
      setError(err.response?.data?.message || 'No questions available for this skill yet.');
      setQuestions([]);
    } finally {
      setLoading(false);
    }
  };

  const selectAnswer = (questionId, optionIndex) => {
    setAnswers((prev) => ({ ...prev, [questionId]: optionIndex }));
  };

  const handleSubmit = async () => {
    if (Object.keys(answers).length !== questions.length) {
      alert('Please answer all questions before submitting.');
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        skillId: Number(selectedSkillId),
        answers: questions.map((q) => ({
          questionId: q.id,
          selectedOptionIndex: answers[q.id],
        })),
      };
      const { data } = await assessmentApi.submit(payload);
      setResult(data.data);
      loadSkillsAndResults();
    } catch (err) {
      alert('Failed to submit: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setSubmitting(false);
    }
  };

  const retake = () => {
    setResult(null);
    setAnswers({});
    handleSelectSkill(selectedSkillId);
  };

  return (
    <Layout title="Skill Assessment Test" subtitle="Take a short quiz to auto-verify your proficiency — your skill inventory updates automatically">
      {error && <div className="alert alert-error">{error}</div>}

        <div className="info-card" style={{ marginBottom: 24 }}>
          <div className="form-group" style={{ marginBottom: 0, maxWidth: 320 }}>
            <label>Choose a skill to be assessed on</label>
            <select value={selectedSkillId} onChange={(e) => handleSelectSkill(e.target.value)}>
              <option value="">Select a skill...</option>
              {skills.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
        </div>

        {loading && <div className="loading-text">Loading questions...</div>}

        {!loading && result && (
          <div className="info-card" style={{ textAlign: 'center', padding: 32 }}>
            <div style={{ fontSize: 14, color: '#6b7280', marginBottom: 8 }}>Assessment Complete</div>
            <div style={{ fontSize: 36, fontWeight: 700, marginBottom: 4 }}>
              {result.correctAnswers} / {result.totalQuestions} correct
            </div>
            <div style={{ fontSize: 16, color: '#6b7280', marginBottom: 16 }}>
              Score: {result.scorePercent.toFixed(0)}%
            </div>
            <span
              className="role-badge"
              style={{
                background: LEVEL_COLORS[result.computedLevel] + '22',
                color: LEVEL_COLORS[result.computedLevel],
                fontSize: 14,
                padding: '6px 16px',
              }}
            >
              {result.skillName}: {result.computedLevel}
            </span>
            <div style={{ marginTop: 20 }}>
              <button className="btn-sm" onClick={retake}>Retake Assessment</button>
            </div>
          </div>
        )}

        {!loading && !result && questions.length > 0 && (
          <div>
            {questions.map((q, idx) => (
              <div key={q.id} className="info-card" style={{ marginBottom: 14 }}>
                <div style={{ fontWeight: 600, marginBottom: 12 }}>
                  {idx + 1}. {q.questionText}
                </div>
                {q.codeSnippet && (
                  <pre style={{
                    background: '#1e1e2e',
                    color: '#e2e8f0',
                    padding: '14px 16px',
                    borderRadius: 8,
                    fontSize: 13,
                    overflowX: 'auto',
                    marginBottom: 12,
                    fontFamily: 'Consolas, Monaco, monospace',
                    lineHeight: 1.5,
                  }}>
                    <code>{q.codeSnippet}</code>
                  </pre>
                )}
                {q.options.map((opt, optIdx) => (
                  <label
                    key={optIdx}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 8,
                      padding: '8px 10px',
                      borderRadius: 6,
                      cursor: 'pointer',
                      background: answers[q.id] === optIdx ? '#eef2ff' : 'transparent',
                      marginBottom: 4,
                    }}
                  >
                    <input
                      type="radio"
                      name={`q-${q.id}`}
                      checked={answers[q.id] === optIdx}
                      onChange={() => selectAnswer(q.id, optIdx)}
                    />
                    <span style={{ fontSize: 14 }}>{opt}</span>
                  </label>
                ))}
              </div>
            ))}
            <button className="btn-primary" style={{ width: 'auto', padding: '10px 28px' }} onClick={handleSubmit} disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Assessment'}
            </button>
          </div>
        )}

        <div className="section-title">My Past Assessment Results</div>
        {myResults.length === 0 ? (
          <div className="info-card" style={{ color: '#6b7280' }}>No assessments taken yet.</div>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Skill</th>
                <th>Score</th>
                <th>Level</th>
                <th>Taken At</th>
              </tr>
            </thead>
            <tbody>
              {myResults.map((r) => (
                <tr key={r.id}>
                  <td>{r.skillName}</td>
                  <td>{r.correctAnswers}/{r.totalQuestions} ({r.scorePercent.toFixed(0)}%)</td>
                  <td>
                    <span className="role-badge" style={{ background: LEVEL_COLORS[r.computedLevel] + '22', color: LEVEL_COLORS[r.computedLevel] }}>
                      {r.computedLevel}
                    </span>
                  </td>
                  <td>{new Date(r.takenAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
    </Layout>
  );
}
