import Badge from './Badge';
import { formatMoney } from '../../lib/format';

export default function StatCard({ label, value, note, tone = 'neutral' }) {
  return (
    <div className="rounded-lg border border-line bg-panel2 p-4 shadow-soft">
      <div className="flex items-center justify-between gap-2">
        <div className="text-xs uppercase tracking-wide text-muted">{label}</div>
        {note ? <Badge tone={tone}>{note}</Badge> : null}
      </div>
      <div className="mt-2 text-2xl font-semibold text-text">{typeof value === 'number' ? formatMoney(value) : value}</div>
    </div>
  );
}
