import { useEffect, useMemo, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import StatCard from '../components/common/StatCard';
import { LoadingState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { usePlatform } from '../context/PlatformContext';
import { apiClient } from '../lib/apiClient';
import { formatMoney } from '../lib/format';

export default function ProfilePage() {
  const { user } = useAuth();
  const { stocks } = usePlatform();
  const [holdings, setHoldings] = useState([]);
  const [loadingPortfolio, setLoadingPortfolio] = useState(true);

  const priceMap = useMemo(() => new Map(stocks.map((stock) => [stock.symbol, Number(stock.currentPrice)])), [stocks]);

  useEffect(() => {
    async function loadPortfolio() {
      if (!user?.id) return;
      setLoadingPortfolio(true);
      try {
        const data = await apiClient.get(`/portfolio/${user.id}`);
        setHoldings(Array.isArray(data) ? data : []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoadingPortfolio(false);
      }
    }
    loadPortfolio();
  }, [user?.id]);

  const summary = useMemo(() => {
    const invested = holdings.reduce((total, holding) => total + Number(holding.totalInvested || 0), 0);
    const current = holdings.reduce((total, holding) => {
      const marketPrice = priceMap.get(holding.symbol) ?? Number(holding.averagePrice || 0);
      return total + marketPrice * Number(holding.quantity || 0);
    }, 0);
    return { invested, current, pnl: current - invested };
  }, [holdings, priceMap]);

  if (!user) return <LoadingState label="Loading profile..." />;

  const isGain = summary.pnl >= 0;

  return (
    <div className="space-y-6">
      <PageHeader title="Profile" subtitle="Manage your personal details and account information." />

      <div className="grid gap-8 md:grid-cols-2">
        <div className="flex flex-col gap-4">
          {/* <StatCard label="User ID" value={user.id} note="Profile" format="plain" /> */}
          <StatCard label="Username" value={user.username} note="Login name" />
          <StatCard label="Email" value={user.email} note="Contact" />
          
          {!loadingPortfolio && (
            <StatCard 
              label="Unrealized P/L" 
              value={`${isGain ? '+' : ''}${formatMoney(summary.pnl)}`} 
              note={isGain ? 'Gain' : 'Loss'} 
              tone={isGain ? 'success' : 'danger'} 
            />
          )}
        </div>

        {!loadingPortfolio && (
          <div className="flex items-center justify-center">
            {isGain ? (
              <img src="/stonk-raise.webp" alt="Stonks Up" className="w-full max-w-md rounded-lg shadow-lg object-contain" />
            ) : (
              <img src="/not-stonks.png" alt="Stonks Down" className="w-full max-w-md rounded-lg shadow-lg object-contain" />
            )}
          </div>
        )}
      </div>
    </div>
  );
}
