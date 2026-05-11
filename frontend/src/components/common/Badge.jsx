export default function Badge({ children, tone = 'neutral' }) {
  const tones = {
    neutral: 'border-line bg-[#131313] text-muted',
    success: 'border-[#b7df43] bg-[rgba(195,245,60,0.14)] text-accent',
    danger: 'border-rose-900 bg-[rgba(217,45,32,0.14)] text-rose-300',
    warning: 'border-amber-900 bg-[rgba(183,121,31,0.16)] text-amber-300',
    accent: 'border-[#b7df43] bg-[rgba(195,245,60,0.14)] text-accent',
  };

  return <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs ${tones[tone] || tones.neutral}`}>{children}</span>;
}
