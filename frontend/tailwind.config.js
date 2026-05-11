export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        bg: '#050505',
        panel: '#0d0d0d',
        panel2: '#161616',
        line: '#2a2a2a',
        text: '#f5f5f0',
        muted: '#a1a19a',
        accent: '#c3f53c',
        accent2: '#8ccf20',
        danger: '#d92d20',
        warning: '#b7791f',
      },
      boxShadow: {
        soft: '0 24px 64px rgba(0, 0, 0, 0.38)',
        glow: '0 18px 44px rgba(195, 245, 60, 0.24)',
      },
    },
  },
  plugins: [],
};
