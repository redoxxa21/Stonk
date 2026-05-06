export function buildSparkPath(values, width = 120, height = 44, padding = 4) {
  if (!values || values.length === 0) return '';
  if (values.length === 1) {
    const x = width / 2;
    const y = height / 2;
    return `M ${x} ${y} L ${x + 0.1} ${y}`;
  }

  const numeric = values.map((value) => Number(value)).filter((value) => !Number.isNaN(value));
  if (!numeric.length) return '';

  const min = Math.min(...numeric);
  const max = Math.max(...numeric);
  const spread = max - min || 1;
  const step = (width - padding * 2) / (numeric.length - 1);

  return numeric
    .map((value, index) => {
      const x = padding + step * index;
      const y = height - padding - ((value - min) / spread) * (height - padding * 2);
      return `${index === 0 ? 'M' : 'L'} ${x.toFixed(2)} ${y.toFixed(2)}`;
    })
    .join(' ');
}
