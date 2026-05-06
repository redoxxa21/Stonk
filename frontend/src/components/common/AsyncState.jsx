import { Loader2 } from 'lucide-react';

export function LoadingState({ label = 'Loading...' }) {
  return (
    <div className="flex items-center gap-3 rounded-lg border border-line bg-panel px-4 py-3 text-sm text-muted">
      <Loader2 className="h-4 w-4 animate-spin text-accent" />
      {label}
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
      <div className="flex items-center justify-between gap-3">
        <span>{error}</span>
        {onRetry ? (
          <button type="button" onClick={onRetry} className="rounded-md border border-rose-400/30 px-3 py-1.5 text-xs">
            Retry
          </button>
        ) : null}
      </div>
    </div>
  );
}

export function EmptyState({ title, description }) {
  return (
    <div className="rounded-lg border border-dashed border-line bg-panel px-4 py-6 text-sm text-muted">
      <div className="font-medium text-text">{title}</div>
      {description ? <div className="mt-1">{description}</div> : null}
    </div>
  );
}
