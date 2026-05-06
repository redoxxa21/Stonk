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
import OrdersPage from './pages/OrdersPage';
import TradesPage from './pages/TradesPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
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
          <p className="mt-2 text-sm text-muted">This area is reserved for users with the required role.</p>
        </div>
      </div>
    );
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
        <Route index element={<DashboardPage />} />
        <Route path="market" element={<MarketPage />} />
        <Route path="market/:symbol" element={<MarketSymbolPage />} />
        <Route path="wallet" element={<WalletPage />} />
        <Route path="portfolio" element={<PortfolioPage />} />
        <Route path="orders" element={<OrdersPage />} />
        <Route path="trades" element={<TradesPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route
          path="admin"
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
