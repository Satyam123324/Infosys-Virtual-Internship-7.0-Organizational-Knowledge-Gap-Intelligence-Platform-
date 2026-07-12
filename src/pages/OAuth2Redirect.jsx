import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function OAuth2Redirect() {
  const [searchParams] = useSearchParams();
  const { loginWithTokens } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');

  useEffect(() => {
    const accessToken = searchParams.get('accessToken');
    const refreshToken = searchParams.get('refreshToken');
    const oauthError = searchParams.get('error');

    if (oauthError) {
      setError(oauthError);
      return;
    }

    if (accessToken && refreshToken) {
      loginWithTokens(accessToken, refreshToken).then(() => {
        navigate('/dashboard', { replace: true });
      });
    } else {
      setError('No authentication tokens were returned. Please try signing in again.');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="auth-wrapper">
      <div className="auth-card" style={{ textAlign: 'center' }}>
        {error ? (
          <>
            <h1>Sign-in failed</h1>
            <p className="subtitle">{error}</p>
            <Link to="/login" className="btn-primary" style={{ display: 'inline-block', textDecoration: 'none', width: 'auto', padding: '10px 24px' }}>
              Back to Login
            </Link>
          </>
        ) : (
          <>
            <h1>Signing you in...</h1>
            <p className="subtitle">Please wait while we complete your login.</p>
          </>
        )}
      </div>
    </div>
  );
}
