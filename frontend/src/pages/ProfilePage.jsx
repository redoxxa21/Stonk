import PageHeader from '../components/common/PageHeader';
import StatCard from '../components/common/StatCard';
import { LoadingState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();

  if (!user) return <LoadingState label="Loading profile..." />;

  return (
    <div className="space-y-6">
      <PageHeader title="Profile" subtitle="Manage your personal details and account information." />

      <div className="grid gap-4 md:grid-cols-4">
        <StatCard label="User ID" value={user.id} note="Profile" format="plain" />
        <StatCard label="Username" value={user.username} note="Login name" />
        <StatCard label="Email" value={user.email} note="Contact" />
        <StatCard label="Role" value={user.role} note="Access level" />
      </div>
    </div>
  );
}
