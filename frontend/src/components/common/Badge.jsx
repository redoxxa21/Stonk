export default function Badge({ children, tone = 'neutral' }) {
  const tones = {
    neutral: 'border-line bg-white/5 text-muted',
    success: 'border-emerald-400/30 bg-emerald-400/10 text-emerald-300',
    danger: 'border-rose-400/30 bg-rose-400/10 text-rose-200',
    warning: 'border-amber-400/30 bg-amber-400/10 text-amber-200',
    accent: 'border-sky-400/30 bg-sky-400/10 text-sky-200',
  };

  return <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs ${tones[tone] || tones.neutral}`}>{children}</span>;
}
