import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user, session, hydrated } = useAuth();
  const location = useLocation();

  if (!hydrated) return null;
  if (!session?.token || !user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}
