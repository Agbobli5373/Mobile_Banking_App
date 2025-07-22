import { useEffect } from 'react';
import { NavLink } from 'react-router-dom';
import {
  X,
  Home,
  CreditCard,
  BarChart3,
  Send,
  PlusCircle,
  Settings,
  HelpCircle,
  Phone,
  FileText,
  Shield,
} from 'lucide-react';

export interface SidebarProps {
  /**
   * Whether the sidebar is open (for mobile)
   */
  isOpen: boolean;
  /**
   * Callback function when close button is clicked
   */
  onClose: () => void;
}

interface NavItem {
  path: string;
  label: string;
  icon: React.ReactNode;
  section?: string;
}

export const Sidebar = ({ isOpen, onClose }: SidebarProps) => {
  // Close sidebar when pressing escape key
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };

    document.addEventListener('keydown', handleEscape);

    // Prevent scrolling when sidebar is open on mobile
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = '';
    };
  }, [isOpen, onClose]);

  const navItems: NavItem[] = [
    // Main navigation
    {
      path: '/dashboard',
      label: 'Dashboard',
      icon: <Home size={20} />,
      section: 'main',
    },
    {
      path: '/transactions',
      label: 'Transactions',
      icon: <BarChart3 size={20} />,
      section: 'main',
    },
    {
      path: '/transfer',
      label: 'Transfer Money',
      icon: <Send size={20} />,
      section: 'main',
    },
    {
      path: '/add-funds',
      label: 'Add Funds',
      icon: <PlusCircle size={20} />,
      section: 'main',
    },
    {
      path: '/settings',
      label: 'Settings',
      icon: <Settings size={20} />,
      section: 'main',
    },

    // Support & Info
    {
      path: '/help',
      label: 'Help Center',
      icon: <HelpCircle size={20} />,
      section: 'support',
    },
    {
      path: '/contact',
      label: 'Contact Us',
      icon: <Phone size={20} />,
      section: 'support',
    },
    {
      path: '/terms',
      label: 'Terms of Service',
      icon: <FileText size={20} />,
      section: 'support',
    },
    {
      path: '/privacy',
      label: 'Privacy Policy',
      icon: <Shield size={20} />,
      section: 'support',
    },
  ];

  // Filter items by section
  const mainNavItems = navItems.filter(item => item.section === 'main');
  const supportNavItems = navItems.filter(item => item.section === 'support');

  // Mobile overlay
  const overlay = isOpen && (
    <div
      className="fixed inset-0 bg-gray-900 bg-opacity-50 z-40 lg:hidden"
      onClick={onClose}
      role="presentation"
      data-testid="sidebar-overlay"
      aria-hidden="true"
    />
  );

  return (
    <>
      {overlay}

      <aside
        className={`
          fixed top-0 left-0 z-50 h-full w-64 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out
          lg:translate-x-0 lg:static lg:z-auto
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
        `}
        role="complementary"
        aria-label="Main navigation"
      >
        <div className="h-16 flex items-center justify-between px-4 border-b border-gray-200 lg:hidden">
          <h2 className="text-xl font-bold text-blue-900">Menu</h2>
          <button
            type="button"
            className="p-2 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
            onClick={onClose}
            aria-label="Close sidebar"
          >
            <X size={24} />
          </button>
        </div>

        <div className="h-full flex flex-col overflow-y-auto">
          {/* Main navigation section */}
          <nav className="p-4 space-y-1" aria-label="Main">
            {mainNavItems.map(item => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) => `
                  flex items-center px-3 py-2 rounded-md text-sm font-medium transition-colors
                  ${
                    isActive
                      ? 'bg-primary-50 text-primary-700 border-l-4 border-primary-600 pl-2'
                      : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                  }
                `}
                onClick={() => {
                  // Close sidebar on mobile when clicking a link
                  if (window.innerWidth < 1024) {
                    onClose();
                  }
                }}
              >
                <span className="mr-3 text-current">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </nav>

          {/* Support & Info section */}
          <div className="mt-6 p-4">
            <h3 className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
              Support & Info
            </h3>
            <nav className="mt-2 space-y-1" aria-label="Support">
              {supportNavItems.map(item => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) => `
                    flex items-center px-3 py-2 rounded-md text-xs font-medium transition-colors
                    ${
                      isActive
                        ? 'bg-gray-100 text-gray-900'
                        : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                    }
                  `}
                  onClick={() => {
                    // Close sidebar on mobile when clicking a link
                    if (window.innerWidth < 1024) {
                      onClose();
                    }
                  }}
                >
                  <span className="mr-3 text-current">{item.icon}</span>
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>

          {/* Account status - pushed to bottom */}
          <div className="mt-auto p-4 border-t border-gray-200">
            <div className="flex items-center">
              <div className="h-8 w-8 rounded-full bg-primary-600 text-white flex items-center justify-center">
                <CreditCard size={16} />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-900">
                  Premium Account
                </p>
                <p className="text-xs text-gray-500">No fees on transfers</p>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
};
