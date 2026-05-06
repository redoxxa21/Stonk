export const money = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  maximumFractionDigits: 2,
});

export const number = new Intl.NumberFormat('en-US');

export function formatMoney(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '-';
  return money.format(Number(value));
}

export function formatNumber(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) return '-';
  return number.format(Number(value));
}

export function formatDateTime(value) {
  if (!value) return '-';
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function deltaClass(value) {
  const n = Number(value);
  if (Number.isNaN(n)) return 'text-muted';
  if (n > 0) return 'text-emerald-400';
  if (n < 0) return 'text-rose-400';
  return 'text-muted';
}
