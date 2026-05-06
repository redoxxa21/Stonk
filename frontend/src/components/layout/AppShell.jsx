import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import Topbar from './Topbar';

export default function AppShell() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-bg text-text">
      <div className="flex min-h-screen">
        <Sidebar />

        <div className="flex min-w-0 flex-1 flex-col">
          <Topbar
            onToggleSidebar={() => setMobileOpen((current) => !current)}
            onSearch={(value) => {
              const query = value.trim().toLowerCase();
              if (!query) return;
              if (query === 'market') navigate('/market');
              else if (query === 'wallet') navigate('/wallet');
              else if (query === 'portfolio') navigate('/portfolio');
              else if (query === 'orders') navigate('/orders');
              else if (query === 'trades') navigate('/trades');
              else if (query === 'profile') navigate('/profile');
            }}
          />

          <main className="flex-1 px-4 py-5 lg:px-6">
            <div className="mx-auto w-full max-w-7xl">
              <Outlet />
            </div>
          </main>
        </div>
      </div>

      {mobileOpen ? (
        <div className="fixed inset-0 z-30 bg-black/50 lg:hidden" onClick={() => setMobileOpen(false)}>
          <div className="absolute left-0 top-0 h-full w-72 border-r border-line bg-panel" onClick={(e) => e.stopPropagation()}>
            <Sidebar mobile onNavigate={() => setMobileOpen(false)} />
          </div>
        </div>
      ) : null}
    </div>
  );
}
