import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Building2, Briefcase, Brain } from 'lucide-react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { employeeApi } from '../api/employeeApi';
import AuthRadarChart from '../components/AuthRadarChart';

const BACKEND_BASE_URL = 'http://localhost:8080';

// Matches the role titles seeded on the backend (Competency Frameworks) so a new
// employee's Gap Analysis works immediately without a separate profile-edit step.
const ROLE_OPTIONS = [
  'Software Developer',
  'Senior Software Developer',
  'Frontend Developer',
  'DevOps Engineer',
  'Data Analyst',
  'HR Specialist',
  'Team Lead',
];

export default function Register() {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    department: '',
    designation: '',
  });
  const [departments, setDepartments] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    employeeApi.getAllDepartments()
      .then((res) => setDepartments(res.data.data))
      .catch(() => setDepartments([]));
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match. Please re-enter.');
      return;
    }

    setLoading(true);
    try {
      const selectedDept = departments.find((d) => String(d.id) === form.department);

      const { data } = await api.post('/auth/register', {
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        department: selectedDept ? selectedDept.name : '',
        designation: form.designation,
      });
      login(data.data);

      // Immediately link the department + role to the Employee Profile so
      // Gap Analysis has everything it needs from the very first login.
      try {
        await employeeApi.updateMyProfile({
          departmentId: selectedDept ? selectedDept.id : null,
          currentRoleTitle: form.designation,
        });
      } catch (linkErr) {
        // Non-fatal — user can still set this later from the Gap Analysis page.
      }

      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const oauthButtonStyle = {
    width: '100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    padding: '10px 12px',
    borderRadius: 8,
    border: '1px solid #cbd5e1',
    background: 'white',
    color: '#334155',
    fontSize: 14,
    fontWeight: 600,
    cursor: 'pointer',
    textDecoration: 'none',
    marginBottom: 10,
  };

  return (
    <div className="auth-shell">
      <div className="auth-brand-panel">
        <div className="auth-brand-mark">
          <div className="mark"><Brain size={18} color="#0b1120" /></div>
          <div className="wordmark">Knowledge Gap<br />Intelligence Platform</div>
        </div>

        <div className="auth-headline">
          Know your team's<br /><span className="accent">real</span> skill gaps.
        </div>
        <div className="auth-subline">
          Build your skill profile once — get verified assessments, live gap analysis,
          and AI-guided training recommendations from day one.
        </div>

        <div className="auth-radar-wrap">
          <AuthRadarChart />
        </div>

        <div className="auth-stat-row">
          <div>
            <div className="stat-num">16</div>
            <div className="stat-label">Skills tracked</div>
          </div>
          <div>
            <div className="stat-num">7</div>
            <div className="stat-label">Role frameworks</div>
          </div>
          <div>
            <div className="stat-num">AI</div>
            <div className="stat-label">Gap recommendations</div>
          </div>
        </div>
      </div>

      <div className="auth-form-panel">
        <div className="auth-form-card">
          <h1>Create your account</h1>
          <p className="subtitle">Join the Knowledge Gap Intelligence Platform</p>

          {error && <div className="alert alert-error">{error}</div>}

          <a href={`${BACKEND_BASE_URL}/oauth2/authorization/google`} style={oauthButtonStyle}>
            <svg width="16" height="16" viewBox="0 0 24 24"><path fill="#4285F4" d="M23.49 12.27c0-.79-.07-1.54-.19-2.27H12v4.51h6.47c-.28 1.48-1.13 2.73-2.4 3.58v3h3.86c2.26-2.09 3.56-5.17 3.56-8.82z"/><path fill="#34A853" d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.86-3c-1.08.72-2.45 1.15-4.07 1.15-3.13 0-5.78-2.11-6.73-4.96H1.29v3.09C3.26 21.3 7.31 24 12 24z"/><path fill="#FBBC05" d="M5.27 14.28A7.2 7.2 0 0 1 4.9 12c0-.79.14-1.56.38-2.28V6.63H1.29A11.97 11.97 0 0 0 0 12c0 1.94.46 3.77 1.29 5.37l3.98-3.09z"/><path fill="#EA4335" d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.94 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.63l3.98 3.09C6.22 6.86 8.87 4.75 12 4.75z"/></svg>
            Continue with Google
          </a>
          <a href={`${BACKEND_BASE_URL}/oauth2/authorization/github`} style={oauthButtonStyle}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="#1f2328"><path d="M12 .5C5.65.5.5 5.65.5 12c0 5.08 3.29 9.39 7.86 10.91.57.1.78-.25.78-.55v-2.17c-3.2.7-3.87-1.36-3.87-1.36-.53-1.34-1.29-1.7-1.29-1.7-1.05-.72.08-.71.08-.71 1.17.08 1.78 1.2 1.78 1.2 1.03 1.77 2.7 1.26 3.36.96.1-.75.4-1.26.73-1.55-2.56-.29-5.25-1.28-5.25-5.7 0-1.26.45-2.29 1.19-3.09-.12-.29-.52-1.46.11-3.05 0 0 .97-.31 3.18 1.18a11.06 11.06 0 0 1 5.79 0c2.2-1.49 3.17-1.18 3.17-1.18.63 1.59.23 2.76.11 3.05.74.8 1.19 1.83 1.19 3.09 0 4.43-2.7 5.4-5.27 5.69.41.36.78 1.06.78 2.14v3.17c0 .3.21.66.79.55A10.99 10.99 0 0 0 23.5 12c0-6.35-5.15-11.5-11.5-11.5z"/></svg>
            Continue with GitHub
          </a>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10, margin: '18px 0', color: '#94a3b8', fontSize: 12 }}>
            <div style={{ flex: 1, height: 1, background: '#e2e8f0' }} />
            OR
            <div style={{ flex: 1, height: 1, background: '#e2e8f0' }} />
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Full Name</label>
              <div className="auth-input-icon-group">
                <User size={15} />
                <input name="fullName" value={form.fullName} onChange={handleChange} required />
              </div>
            </div>
            <div className="form-group">
              <label>Email</label>
              <div className="auth-input-icon-group">
                <Mail size={15} />
                <input type="email" name="email" value={form.email} onChange={handleChange} required />
              </div>
            </div>
            <div className="form-group">
              <label>Password</label>
              <div className="auth-input-icon-group">
                <Lock size={15} />
                <input type="password" name="password" value={form.password} onChange={handleChange} required />
              </div>
              <small style={{ color: '#94a3b8', fontSize: 12 }}>
                Min 8 chars, with uppercase, lowercase, digit & special character
              </small>
            </div>
            <div className="form-group">
              <label>Confirm Password</label>
              <div className="auth-input-icon-group">
                <Lock size={15} />
                <input type="password" name="confirmPassword" value={form.confirmPassword} onChange={handleChange} required />
              </div>
            </div>
            <div className="form-group">
              <label>Department / Category</label>
              <div className="auth-input-icon-group">
                <Building2 size={15} />
                <select name="department" value={form.department} onChange={handleChange} required>
                  <option value="">Select your department...</option>
                  {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Role You're Working Toward</label>
              <div className="auth-input-icon-group">
                <Briefcase size={15} />
                <select name="designation" value={form.designation} onChange={handleChange} required>
                  <option value="">Select a role...</option>
                  {ROLE_OPTIONS.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
              </div>
              <small style={{ color: '#94a3b8', fontSize: 12 }}>
                This lets Gap Analysis compare your skills against this role right away.
              </small>
            </div>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Creating account...' : 'Create Account'}
            </button>
          </form>

          <div className="auth-switch">
            Already have an account? <Link to="/login">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
