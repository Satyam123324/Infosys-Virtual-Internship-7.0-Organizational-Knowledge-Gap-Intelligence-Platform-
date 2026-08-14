import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, Brain } from 'lucide-react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import AuthRadarChart from '../components/AuthRadarChart';

const BACKEND_BASE_URL = 'http://localhost:8080';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/auth/login', form);
      login(data.data);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please check your credentials.');
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
      {/* Brand panel — signature radar chart shows a real gap, not a stock illustration */}
      <div className="auth-brand-panel">
        <div className="auth-brand-mark">
          <div className="mark"><Brain size={18} color="#0b1120" /></div>
          <div className="wordmark">Knowledge Gap<br />Intelligence Platform</div>
        </div>

        <div className="auth-headline">
          See the gap<br />before it <span className="accent">costs you.</span>
        </div>
        <div className="auth-subline">
          Every employee's skills, benchmarked against what their role actually requires —
          with AI-generated recommendations to close each gap.
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

      {/* Form panel */}
      <div className="auth-form-panel">
        <div className="auth-form-card">
          <h1>Welcome back</h1>
          <p className="subtitle">Sign in to your account to continue</p>

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
              <label>Email</label>
              <div className="auth-input-icon-group">
                <Mail size={15} />
                <input
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="you@company.com"
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>Password</label>
              <div className="auth-input-icon-group">
                <Lock size={15} />
                <input
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  required
                />
              </div>
              <div style={{ textAlign: 'right', marginTop: 6 }}>
                <Link to="/forgot-password" style={{ fontSize: 12, color: '#0d9488', textDecoration: 'none' }}>
                  Forgot password?
                </Link>
              </div>
            </div>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          <div className="auth-switch">
            Don't have an account? <Link to="/register">Create one</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
