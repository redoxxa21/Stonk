import { EmptyState } from './AsyncState';

export default function DataTable({ columns, rows, rowKey, emptyTitle = 'No records found', emptyDescription }) {
  if (!rows?.length) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  return (
    <div className="overflow-auto rounded-lg border border-line">
      <table className="min-w-full divide-y divide-line text-sm">
        <thead className="bg-white/5 text-left text-xs uppercase tracking-wide text-muted">
          <tr>
            {columns.map((column) => (
              <th key={column.key} className="px-4 py-3 font-medium">
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-line bg-panel">
          {rows.map((row) => (
            <tr key={rowKey(row)} className="hover:bg-white/[0.02]">
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
