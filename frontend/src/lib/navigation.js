import {
  Activity,
  ArrowUpDown,
  CandlestickChart,
  CircleHelp,
  LayoutDashboard,
  Shield,
  UserCircle2,
  WalletCards,
} from 'lucide-react';

export const navigationItems = [
  {
    to: '/',
    label: 'Dashboard',
    description: 'Command center',
    icon: LayoutDashboard,
    roles: ['USER', 'ADMIN'],
  },
  {
    to: '/market',
    label: 'Market',
    description: 'Live symbols',
    icon: CandlestickChart,
    roles: ['USER', 'ADMIN'],
  },
  {
    to: '/portfolio',
    label: 'Portfolio',
    description: 'Positions',
    icon: Activity,
    roles: ['USER', 'ADMIN'],
  },
  {
    to: '/wallet',
    label: 'Wallet',
    description: 'Funding',
    icon: WalletCards,
    roles: ['USER', 'ADMIN'],
  },
  {
    to: '/trades',
    label: 'Trades',
    description: 'Execution log',
    icon: ArrowUpDown,
    roles: ['USER', 'ADMIN'],
  },
  {
    to: '/profile',
    label: 'Profile',
    description: 'Identity',
    icon: UserCircle2,
    roles: ['USER', 'ADMIN'],
  },
  {
    to: '/admin',
    label: 'Admin',
    description: 'User control',
    icon: Shield,
    roles: ['ADMIN'],
  },
  {
    to: '/help',
    label: 'Help Center',
    description: 'Guides',
    icon: CircleHelp,
    roles: ['USER', 'ADMIN'],
  },
];

export function getNavigationForRole(role) {
  if (role === 'ADMIN') {
    return [
      {
        to: '/admin',
        label: 'Control Center',
        description: 'Oversight',
        icon: Shield,
        roles: ['ADMIN'],
      },
      {
        to: '/help',
        label: 'Help Center',
        description: 'Guides',
        icon: CircleHelp,
        roles: ['ADMIN'],
      },
    ];
  }

  return navigationItems.filter((item) => item.roles.includes(role || 'USER'));
}

export function findNavigationItem(pathname, role) {
  const items = getNavigationForRole(role);
  return (
    items.find((item) => item.to !== '/' && pathname.startsWith(item.to)) ||
    items.find((item) => item.to === pathname) ||
    items.find((item) => item.to === '/')
  );
}
