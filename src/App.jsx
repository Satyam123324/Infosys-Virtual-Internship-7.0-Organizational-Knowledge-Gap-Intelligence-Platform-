import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import OAuth2Redirect from './pages/OAuth2Redirect';
import ForgotPassword from './pages/ForgotPassword';
import ChangePassword from './pages/ChangePassword';
import MyProfile from './pages/MyProfile';
import Dashboard from './pages/Dashboard';
import AdminConsole from './pages/AdminConsole';
import SkillInventory from './pages/SkillInventory';
import AssessmentTest from './pages/AssessmentTest';
import GapAnalysis from './pages/GapAnalysis';
import AdminGapDashboard from './pages/AdminGapDashboard';
import AdminDashboard from './pages/AdminDashboard';
import SkillsReview from './pages/SkillsReview';
import Certifications from './pages/Certifications';
import NotificationCenter from './pages/NotificationCenter';
import CompetencyFrameworks from './pages/CompetencyFrameworks';
import AdminEmployeeProfiles from './pages/AdminEmployeeProfiles';
import PeerAssessment from './pages/PeerAssessment';
import Mentorship from './pages/Mentorship';
import TrainingEnrollment from './pages/TrainingEnrollment';
import KnowledgeArticles from './pages/KnowledgeArticles';

function RootRedirect() {
  const { user, loading } = useAuth();
  if (loading) return null;
  return <Navigate to={user ? '/dashboard' : '/login'} replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/change-password"
            element={
              <ProtectedRoute>
                <ChangePassword />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-profile"
            element={
              <ProtectedRoute>
                <MyProfile />
              </ProtectedRoute>
            }
          />
          <Route
            path="/peer-assessment"
            element={
              <ProtectedRoute>
                <PeerAssessment />
              </ProtectedRoute>
            }
          />
          <Route
            path="/skill-review"
            element={
              <ProtectedRoute>
                <SkillsReview />
              </ProtectedRoute>
            }
          />
          <Route
            path="/certifications"
            element={
              <ProtectedRoute>
                <Certifications />
              </ProtectedRoute>
            }
          />
          <Route
            path="/notifications"
            element={
              <ProtectedRoute>
                <NotificationCenter />
              </ProtectedRoute>
            }
          />
          <Route
            path="/skills"
            element={
              <ProtectedRoute>
                <SkillInventory />
              </ProtectedRoute>
            }
          />
          <Route
            path="/assessment"
            element={
              <ProtectedRoute>
                <AssessmentTest />
              </ProtectedRoute>
            }
          />
          <Route
            path="/gap-analysis"
            element={
              <ProtectedRoute>
                <GapAnalysis />
              </ProtectedRoute>
            }
          />
          <Route
            path="/mentorship"
            element={
              <ProtectedRoute>
                <Mentorship />
              </ProtectedRoute>
            }
          />
          <Route
            path="/training"
            element={
              <ProtectedRoute>
                <TrainingEnrollment />
              </ProtectedRoute>
            }
          />
          <Route
            path="/knowledge-articles"
            element={
              <ProtectedRoute>
                <KnowledgeArticles />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/dashboard"
            element={
              <ProtectedRoute allowedRoles={['SYSTEM_ADMINISTRATOR']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/gap-dashboard"
            element={
              <ProtectedRoute allowedRoles={['SYSTEM_ADMINISTRATOR', 'HR_SPECIALIST']}>
                <AdminGapDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/competency-frameworks"
            element={
              <ProtectedRoute allowedRoles={['SYSTEM_ADMINISTRATOR', 'HR_SPECIALIST', 'LEARNING_DEVELOPMENT_ADMIN']}>
                <CompetencyFrameworks />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/employee-profiles"
            element={
              <ProtectedRoute allowedRoles={['SYSTEM_ADMINISTRATOR', 'HR_SPECIALIST', 'DEPARTMENT_HEAD']}>
                <AdminEmployeeProfiles />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute adminOnly>
                <AdminConsole />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
