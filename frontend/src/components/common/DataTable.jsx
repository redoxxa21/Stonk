import { EmptyState } from './AsyncState';

export default function DataTable({ columns, rows, rowKey, emptyTitle = 'No records found', emptyDescription }) {
  if (!rows?.length) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  return (
    <div className="app-elevated-card overflow-auto rounded-3xl border border-line shadow-sm">
      <table className="min-w-full divide-y divide-line text-sm">
        <thead className="app-table-head text-left text-[11px] uppercase tracking-[0.18em] text-muted">
          <tr>
            {columns.map((column) => (
              <th key={column.key} className="px-4 py-3 font-medium">
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-line bg-transparent">
          {rows.map((row) => (
            <tr key={rowKey(row)} className="hover:bg-[rgba(195,245,60,0.08)]">
              {columns.map((column) => (
                <td key={column.key} className="px-4 py-3 align-top text-text">
                  {column.render ? column.render(row) : row[column.key] ?? '-'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
