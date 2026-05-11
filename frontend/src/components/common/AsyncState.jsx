import { Loader2 } from 'lucide-react';

export function LoadingState({ label = 'Loading...' }) {
  return (
    <div className="app-elevated-card flex items-center gap-3 rounded-2xl border border-line px-4 py-3 text-sm text-muted shadow-sm">
      <Loader2 className="h-4 w-4 animate-spin text-accent" />
      {label}
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <div className="rounded-2xl border border-rose-900 bg-[rgba(217,45,32,0.14)] px-4 py-3 text-sm text-rose-300">
      <div className="flex items-center justify-between gap-3">
        <span>{error}</span>
        {onRetry ? (
          <button type="button" onClick={onRetry} className="rounded-xl border border-rose-800 bg-black/20 px-3 py-1.5 text-xs text-rose-300">
            Retry
          </button>
        ) : null}
      </div>
    </div>
  );
}

export function EmptyState({ title, description }) {
  return (
    <div className="app-empty-panel rounded-2xl border border-dashed border-line px-4 py-6 text-sm text-muted">
      <div className="font-medium text-text">{title}</div>
      {description ? <div className="mt-1">{description}</div> : null}
    </div>
  );
}
