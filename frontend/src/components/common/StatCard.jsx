import Badge from './Badge';
import { formatMoney } from '../../lib/format';

export default function StatCard({ label, value, note, tone = 'neutral', format = 'money' }) {
  const displayValue =
    format === 'money' && typeof value === 'number'
      ? formatMoney(value)
      : value;

  return (
    <div className="app-elevated-card rounded-[24px] border border-line p-4 shadow-soft">
      <div className="flex items-center justify-between gap-2">
        <div className="text-xs uppercase tracking-[0.22em] text-muted">{label}</div>
        {note ? <Badge tone={tone}>{note}</Badge> : null}
      </div>
      <div className="mt-2 break-all text-2xl font-semibold text-text">{displayValue}</div>
    </div>
  );
}
