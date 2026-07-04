import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading } = useAuth();

  if (loading) return <div className="loading-text" style={{ padding: 40, textAlign: 'center' }}>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;

  if (adminOnly && !user.roles.includes('SYSTEM_ADMINISTRATOR')) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
