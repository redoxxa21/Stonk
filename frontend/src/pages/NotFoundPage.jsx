import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="grid min-h-screen place-items-center p-6">
      <div className="max-w-lg rounded-lg border border-line bg-panel p-8 shadow-soft text-center">
        <div className="text-sm uppercase tracking-[0.2em] text-muted">404</div>
        <h1 className="mt-3 text-2xl font-semibold">Page not found</h1>
        <p className="mt-2 text-sm text-muted">The route does not exist in this frontend.</p>
        <Link to="/" className="mt-6 inline-flex rounded-lg bg-gradient-to-r from-accent to-accent2 px-4 py-2.5 font-semibold text-bg">
          Back to dashboard
        </Link>
      </div>
    </div>
  );
}
