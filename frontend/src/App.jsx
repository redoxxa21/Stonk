import { Navigate, Route, Routes } from 'react-router-dom';
import AppShell from './components/layout/AppShell';
import ProtectedRoute from './components/common/ProtectedRoute';
import { useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import MarketPage from './pages/MarketPage';
import MarketSymbolPage from './pages/MarketSymbolPage';
import WalletPage from './pages/WalletPage';
import PortfolioPage from './pages/PortfolioPage';
import TradesPage from './pages/TradesPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import HelpCenterPage from './pages/HelpCenterPage';
import NotFoundPage from './pages/NotFoundPage';
import { Shield } from 'lucide-react';

function RoleGate({ role, children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) {
    return (
      <div className="mx-auto max-w-3xl p-6">
        <div className="rounded-lg border border-line bg-panel p-6 shadow-soft">
          <div className="flex items-center gap-3 text-text">
            <Shield className="h-5 w-5 text-warning" />
            <h1 className="text-lg font-semibold">Access restricted</h1>
          </div>
          <p className="mt-2 text-sm text-muted">Your account does not have permission to view this area.</p>
        </div>
      </div>
    );
  }
  return children;
}

function UserOnly({ children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  }
  return children;
}

export default function App() {
  const { hydrated, user } = useAuth();

  if (!hydrated) {
    return (
      <div className="min-h-screen bg-bg text-text grid place-items-center">
        <div className="rounded-lg border border-line bg-panel px-5 py-4 shadow-soft">Loading session...</div>
      </div>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/register" element={user ? <Navigate to="/" replace /> : <RegisterPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        }
      >
        <Route index element={user?.role === 'ADMIN' ? <Navigate to="/admin" replace /> : <DashboardPage />} />
        <Route path="market" element={<UserOnly><MarketPage /></UserOnly>} />
        <Route path="market/:symbol" element={<UserOnly><MarketSymbolPage /></UserOnly>} />
        <Route path="wallet" element={<UserOnly><WalletPage /></UserOnly>} />
        <Route path="portfolio" element={<UserOnly><PortfolioPage /></UserOnly>} />
        <Route path="orders" element={<Navigate to="/trades" replace />} />
        <Route path="trades" element={<UserOnly><TradesPage /></UserOnly>} />
        <Route path="profile" element={<UserOnly><ProfilePage /></UserOnly>} />
        <Route path="help" element={<HelpCenterPage />} />
        <Route
          path="admin/*"
          element={
            <RoleGate role="ADMIN">
              <AdminPage />
            </RoleGate>
          }
        />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
