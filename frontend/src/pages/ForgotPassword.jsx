import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function ForgotPassword() {
  const [step, setStep] = useState(1); // 1 = enter email, 2 = enter OTP + new password
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [info, setInfo] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const requestOtp = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      setInfo('If an account with that email exists, a 6-digit code has been sent.');
      setStep(2);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send verification code.');
    } finally {
      setLoading(false);
    }
  };

  const resetPassword = async (e) => {
    e.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/reset-password', { email, otp, newPassword });
      navigate('/login', { state: { message: 'Password reset successfully. Please sign in.' } });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reset password. Check your code and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <h1>Forgot Password</h1>
        <p className="subtitle">
          {step === 1
            ? "Enter your email and we'll send you a 6-digit verification code"
            : 'Enter the code we emailed you, plus your new password'}
        </p>

        {error && <div className="alert alert-error">{error}</div>}
        {info && step === 2 && <div className="alert alert-success">{info}</div>}

        {step === 1 ? (
          <form onSubmit={requestOtp}>
            <div className="form-group">
              <label>Email</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Sending code...' : 'Send Verification Code'}
            </button>
          </form>
        ) : (
          <form onSubmit={resetPassword}>
            <div className="form-group">
              <label>6-Digit Code</label>
              <input
                value={otp}
                onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                maxLength={6}
                placeholder="123456"
                style={{ letterSpacing: '0.3em', textAlign: 'center', fontSize: 18 }}
                required
              />
            </div>
            <div className="form-group">
              <label>New Password</label>
              <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
              <small style={{ color: '#94a3b8', fontSize: 12 }}>
                Min 8 chars, with uppercase, lowercase, digit & special character
              </small>
            </div>
            <div className="form-group">
              <label>Confirm New Password</label>
              <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
            </div>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Resetting...' : 'Reset Password'}
            </button>
            <button
              type="button"
              onClick={() => { setStep(1); setError(''); setInfo(''); }}
              style={{ width: '100%', background: 'none', border: 'none', color: '#0d9488', fontSize: 13, marginTop: 10, cursor: 'pointer' }}
            >
              Didn't get a code? Try a different email
            </button>
          </form>
        )}

        <div className="auth-switch">
          <Link to="/login">Back to Sign In</Link>
        </div>
      </div>
    </div>
  );
}
