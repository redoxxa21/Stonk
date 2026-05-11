import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import { useAuth } from '../../context/AuthContext';
import { getNavigationForRole } from '../../lib/navigation';

export default function AppShell() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuth();
  const navItems = getNavigationForRole(user?.role);

  function handleSearch(value) {
    const query = value.trim().toLowerCase();
    if (!query) return;
    const matchedPage = navItems.find(
      (item) =>
        item.label.toLowerCase().includes(query) ||
        item.description.toLowerCase().includes(query),
    );

    if (matchedPage) {
      navigate(matchedPage.to);
      return;
    }

    if (/^[a-z]{1,5}$/.test(query)) {
      navigate(`/market/${query.toUpperCase()}`);
    }
  }

  return (
    <div className="app-shell-bg min-h-screen text-text">
      <div className="flex min-h-screen">
        <Sidebar />

        <div className="flex min-w-0 flex-1 flex-col">
          <Topbar
            onToggleSidebar={() => setMobileOpen((current) => !current)}
            onSearch={handleSearch}
          />

          <main className="flex-1 px-4 py-5 lg:px-6">
            <div className="mx-auto w-full max-w-7xl">
              <Outlet />
            </div>
          </main>
        </div>
      </div>

      {mobileOpen ? (
        <div className="app-overlay fixed inset-0 z-30 backdrop-blur-sm lg:hidden" onClick={() => setMobileOpen(false)}>
          <div className="absolute left-0 top-0 h-full w-72 border-r border-line bg-panel/95" onClick={(e) => e.stopPropagation()}>
            <Sidebar mobile onNavigate={() => setMobileOpen(false)} />
          </div>
        </div>
      ) : null}
    </div>
  );
}
