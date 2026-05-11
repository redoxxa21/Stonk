import { Link } from 'react-router-dom';
import { BookOpen, CircleAlert, LifeBuoy, Radio, Shield, WalletCards } from 'lucide-react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import { useAuth } from '../context/AuthContext';

const helpSections = [
  {
    icon: Radio,
    title: 'Live market data',
    body:
      'Track price movement, market activity, and timely updates in one place throughout the trading session.',
  },
  {
    icon: WalletCards,
    title: 'Trading flow',
    body:
      'Add funds, review live symbols, place trades, and follow results across your wallet, portfolio, and trade history.',
  },
  {
    icon: Shield,
    title: 'Admin responsibility',
    body:
      'Administrative tools support account oversight, user management, and platform monitoring for authorized teams.',
  },
];

export default function HelpCenterPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <PageHeader
        title="Help Center"
        subtitle="Guidance for trading, account management, and platform navigation."
      />

      <div className="grid gap-4 xl:grid-cols-3">
        {helpSections.map(({ icon: Icon, title, body }) => (
          <SectionCard key={title} title={title} subtitle="Helpful overview">
            <div className="flex items-start gap-3">
              <div className="rounded-2xl border border-line bg-[#242412] p-3 text-text">
                <Icon className="h-5 w-5" />
              </div>
              <p className="text-sm leading-6 text-muted">{body}</p>
            </div>
          </SectionCard>
        ))}
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <SectionCard title="User playbook" subtitle="A simple path for everyday trading activity.">
          <div className="space-y-4 text-sm text-muted">
            <div className="rounded-2xl border border-line bg-[#242412] p-4">
              <div className="font-semibold text-text">1. Sign in and verify your role</div>
              <p className="mt-2">Your current role is <span className="text-text">{user?.role || 'USER'}</span>. Available tools and views are tailored to your account permissions.</p>
            </div>
            <div className="rounded-2xl border border-line bg-[#242412] p-4">
              <div className="font-semibold text-text">2. Fund the wallet before trading</div>
              <p className="mt-2">Before placing an order, confirm that your account balance is funded and your portfolio reflects your intended position.</p>
            </div>
            <div className="rounded-2xl border border-line bg-[#242412] p-4">
              <div className="font-semibold text-text">3. Use Market for discovery, Trades for audit</div>
              <p className="mt-2">Use Market for discovery and trade entry, then review Trades to monitor execution status and completed activity.</p>
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Quick links" subtitle="Go directly to the tools you need most often.">
          <div className="space-y-3">
            {[
              { to: '/market', label: 'Open Market', note: 'Live symbols and trade ticket', icon: BookOpen },
              { to: '/wallet', label: 'Open Wallet', note: 'Create or fund the account wallet', icon: WalletCards },
              { to: '/trades', label: 'Open Trades', note: 'Review execution status', icon: LifeBuoy },
            ].map(({ to, label, note, icon: Icon }) => (
              <Link key={to} to={to} className="flex items-center justify-between rounded-2xl border border-line bg-panel2 px-4 py-3 transition hover:border-[#b7df43] hover:bg-[#1a1f0f]">
                <div className="flex items-center gap-3">
                  <div className="rounded-xl border border-line bg-[#242412] p-2 text-text">
                    <Icon className="h-4 w-4" />
                  </div>
                  <div>
                    <div className="font-medium text-text">{label}</div>
                    <div className="text-xs text-muted">{note}</div>
                  </div>
                </div>
                <span className="text-xs text-muted">Open</span>
              </Link>
            ))}
          </div>

          <div className="mt-4 rounded-2xl border border-amber-900 bg-[rgba(183,121,31,0.16)] p-4 text-sm text-amber-200">
            <div className="flex items-center gap-2 font-medium">
              <CircleAlert className="h-4 w-4" />
              Security note
            </div>
            <p className="mt-2 text-amber-100/85">
              Administrative tools are available only to authorized accounts with the required permissions.
            </p>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}
