export default function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="flex flex-col gap-3 border-b border-line pb-5 md:flex-row md:items-end md:justify-between">
      <div>
        <h1 className="text-xl font-semibold text-text">{title}</h1>
        {subtitle ? <p className="mt-1 max-w-3xl text-sm text-muted">{subtitle}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-2">{actions}</div> : null}
    </div>
  );
}
