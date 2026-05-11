export default function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="app-surface flex flex-col gap-4 rounded-[28px] border border-line px-5 py-5 shadow-soft md:flex-row md:items-end md:justify-between">
      <div>
        <div className="rh-kicker">Overview</div>
        <h1 className="mt-2 text-3xl font-semibold text-text">{title}</h1>
        {subtitle ? <p className="mt-1 max-w-3xl text-sm text-muted">{subtitle}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
    </div>
  );
}
