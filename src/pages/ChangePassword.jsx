import { useState } from 'react';
import { Mail, ShieldCheck } from 'lucide-react';
import Layout from '../components/Layout';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function ChangePassword() {
  const { user } = useAuth();
  const [otpSent, setOtpSent] = useState(false);
  const [sendingOtp, setSendingOtp] = useState(false);
  const [otp, setOtp] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const requestOtp = async () => {
    setError('');
    setSuccess('');
    setSendingOtp(true);
    try {
      await api.post('/auth/change-password/request-otp');
      setOtpSent(true);
      setSuccess(`A 6-digit verification code has been sent to ${user?.email}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send verification code');
    } finally {
      setSendingOtp(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (newPassword !== confirmPassword) {
      setError('New passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      await api.post('/auth/change-password', { otp, currentPassword, newPassword });
      setSuccess('Password changed successfully!');
      setOtpSent(false);
      setOtp('');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change password. Check your code and current password.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout title="Change Password" subtitle="For your security, changing your password requires a verification code sent to your email">
      <div className="info-card" style={{ maxWidth: 480 }}>
        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        {!otpSent ? (
          <div style={{ textAlign: 'center', padding: '20px 0' }}>
            <div style={{
              width: 56, height: 56, borderRadius: '50%', background: '#f0fdfa', color: '#0d9488',
              display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px',
            }}>
              <Mail size={24} />
            </div>
            <p style={{ color: '#64748b', fontSize: 14, marginBottom: 20 }}>
              Click below to receive a 6-digit verification code at <strong>{user?.email}</strong>
            </p>
            <button className="btn-primary" style={{ width: 'auto', padding: '10px 28px' }} onClick={requestOtp} disabled={sendingOtp}>
              {sendingOtp ? 'Sending...' : 'Send Verification Code'}
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <ShieldCheck size={14} /> 6-Digit Verification Code
              </label>
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
              <label>Current Password</label>
              <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
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
            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? 'Changing password...' : 'Change Password'}
            </button>
            <button
              type="button"
              onClick={requestOtp}
              disabled={sendingOtp}
              style={{ width: '100%', background: 'none', border: 'none', color: '#0d9488', fontSize: 13, marginTop: 10, cursor: 'pointer' }}
            >
              {sendingOtp ? 'Resending...' : "Didn't get a code? Resend"}
            </button>
          </form>
        )}
      </div>
    </Layout>
  );
}
