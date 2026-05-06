import { Menu, RefreshCw, Search, LogOut } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { usePlatform } from '../../context/PlatformContext';
import Badge from '../common/Badge';

export default function Topbar({ onToggleSidebar, onSearch }) {
  const { user, logout } = useAuth();
  const { refreshMarket, marketLoading } = usePlatform();

  return (
    <header className="sticky top-0 z-20 border-b border-line bg-bg/90 backdrop-blur">
      <div className="flex items-center gap-3 px-4 py-3 lg:px-6">
        <button
          type="button"
          onClick={onToggleSidebar}
          className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-line bg-panel text-text lg:hidden"
        >
          <Menu className="h-4 w-4" />
        </button>

        <div className="hidden flex-1 items-center gap-3 rounded-lg border border-line bg-panel px-3 py-2 md:flex">
          <Search className="h-4 w-4 text-muted" />
          <input
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                onSearch?.(event.currentTarget.value);
              }
            }}
            placeholder="Search symbol, user, order, or page"
            className="w-full bg-transparent text-sm outline-none placeholder:text-muted"
          />
        </div>

        <div className="ml-auto flex items-center gap-2">
          <button
            type="button"
            onClick={refreshMarket}
            className="inline-flex items-center gap-2 rounded-lg border border-line bg-panel px-3 py-2 text-sm text-text"
          >
            <RefreshCw className={`h-4 w-4 ${marketLoading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
          <Badge tone={user?.role === 'ADMIN' ? 'warning' : 'accent'}>{user?.role || 'USER'}</Badge>
          <button
            type="button"
            onClick={logout}
            className="inline-flex items-center gap-2 rounded-lg border border-line bg-panel px-3 py-2 text-sm text-text"
          >
            <LogOut className="h-4 w-4" />
            Sign out
          </button>
        </div>
      </div>
    </header>
  );
}
