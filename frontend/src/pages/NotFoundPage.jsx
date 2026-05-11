import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="grid min-h-screen place-items-center p-6">
      <div className="max-w-lg rounded-[28px] border border-line bg-panel2 p-8 shadow-soft text-center">
        <div className="text-sm uppercase tracking-[0.2em] text-muted">404</div>
        <h1 className="mt-3 text-2xl font-semibold">Page not found</h1>
        <p className="mt-2 text-sm text-muted">The page you requested could not be found.</p>
        <Link to="/" className="rh-button-primary mt-6">
          Back to dashboard
        </Link>
      </div>
    </div>
  );
}
