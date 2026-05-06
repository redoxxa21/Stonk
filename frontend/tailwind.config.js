export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        bg: '#08111f',
        panel: '#0f1728',
        panel2: '#16213a',
        line: '#24324d',
        text: '#e7eefc',
        muted: '#97a8c7',
        accent: '#5eead4',
        accent2: '#60a5fa',
        danger: '#fb7185',
        warning: '#f59e0b',
      },
      boxShadow: {
        soft: '0 18px 50px rgba(0,0,0,0.28)',
      },
    },
  },
  plugins: [],
};
