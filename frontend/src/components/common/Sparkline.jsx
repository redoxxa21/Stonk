import { buildSparkPath } from '../../lib/chart';

export default function Sparkline({ values, className = '' }) {
  const path = buildSparkPath(values);
  if (!values?.length) return <div className={`h-11 rounded-md bg-white/5 ${className}`} />;

  return (
    <svg viewBox="0 0 120 44" className={`h-11 w-full ${className}`} preserveAspectRatio="none">
      <path d={path} fill="none" stroke="currentColor" strokeWidth="2.5" className="text-accent" />
    </svg>
  );
}
