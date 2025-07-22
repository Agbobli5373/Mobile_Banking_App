import { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Menu,
  X,
  User,
  LogOut,
  Bell,
  Settings,
  CreditCard,
} from 'lucide-react';
import { APP_NAME } from '@/constants';
import { Button } from '@/components/ui';

export interface HeaderProps {
  /**
   * Callback function when menu button is clicked
   */
  onMenuToggle: () => void;
}

export const Header = ({ onMenuToggle }: HeaderProps) => {
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);
  const notificationsRef = useRef<HTMLDivElement>(null);

  // This would come from an auth context in a real implementation
  const user = { name: 'John Doe' };

  // Mock notifications for demonstration
  const notifications = [
    { id: 1, message: 'Your transfer of $50 was successful', isRead: false },
    { id: 2, message: 'Welcome to Mobile Banking!', isRead: true },
  ];

  const handleLogout = () => {
    // This would call the logout function from auth context
    console.log('Logging out...');
  };

  // Close menus when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        userMenuRef.current &&
        !userMenuRef.current.contains(event.target as Node) &&
        isUserMenuOpen
      ) {
        setIsUserMenuOpen(false);
      }

      if (
        notificationsRef.current &&
        !notificationsRef.current.contains(event.target as Node) &&
        isNotificationsOpen
      ) {
        setIsNotificationsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isUserMenuOpen, isNotificationsOpen]);

  // Close menus when pressing escape
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setIsUserMenuOpen(false);
        setIsNotificationsOpen(false);
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-30 shadow-sm">
      <div className="px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Left side - Logo and menu button */}
        <div className="flex items-center">
          <button
            type="button"
            className="lg:hidden p-2 rounded-md text-gray-600 hover:text-gray-900 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors"
            onClick={onMenuToggle}
            aria-label="Open sidebar"
          >
            <Menu size={24} />
          </button>

          <Link to="/dashboard" className="flex items-center ml-2 lg:ml-0">
            <div className="h-8 w-8 rounded-full bg-primary-600 text-white flex items-center justify-center mr-2">
              <CreditCard size={18} />
            </div>
            <span className="text-xl font-bold text-primary-900">
              {APP_NAME}
            </span>
          </Link>
        </div>

        {/* Right side - Notifications and User info */}
        <div className="flex items-center space-x-4">
          {/* Notifications */}
          <div className="relative" ref={notificationsRef}>
            <button
              type="button"
              className="p-2 rounded-full text-gray-600 hover:text-gray-900 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-primary-500 transition-colors relative"
              onClick={() => setIsNotificationsOpen(!isNotificationsOpen)}
              aria-expanded={isNotificationsOpen}
              aria-haspopup="true"
              aria-label="Notifications"
            >
              <Bell size={20} />
              {notifications.some(n => !n.isRead) && (
                <span className="absolute top-1 right-1 h-2 w-2 bg-danger-500 rounded-full"></span>
              )}
            </button>

            {/* Notifications dropdown */}
            {isNotificationsOpen && (
              <div className="absolute right-0 mt-2 w-80 bg-white rounded-md shadow-lg py-1 ring-1 ring-black ring-opacity-5 focus:outline-none z-10">
                <div className="px-4 py-2 border-b border-gray-200 flex justify-between items-center">
                  <h3 className="text-sm font-medium text-gray-900">
                    Notifications
                  </h3>
                  <button
                    className="text-xs text-primary-600 hover:text-primary-800"
                    onClick={() => console.log('Mark all as read')}
                  >
                    Mark all as read
                  </button>
                </div>

                <div className="max-h-60 overflow-y-auto">
                  {notifications.length > 0 ? (
                    notifications.map(notification => (
                      <div
                        key={notification.id}
                        className={`px-4 py-3 hover:bg-gray-50 ${
                          !notification.isRead ? 'bg-primary-50' : ''
                        }`}
                      >
                        <p className="text-sm text-gray-700">
                          {notification.message}
                        </p>
                        <p className="text-xs text-gray-500 mt-1">Just now</p>
                      </div>
                    ))
                  ) : (
                    <div className="px-4 py-6 text-center">
                      <p className="text-sm text-gray-500">No notifications</p>
                    </div>
                  )}
                </div>

                <div className="border-t border-gray-200 px-4 py-2">
                  <Link
                    to="/notifications"
                    className="text-xs text-primary-600 hover:text-primary-800"
                    onClick={() => setIsNotificationsOpen(false)}
                  >
                    View all notifications
                  </Link>
                </div>
              </div>
            )}
          </div>

          {/* User menu */}
          <div className="relative" ref={userMenuRef}>
            <button
              type="button"
              className="flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-primary-500 p-1 transition-colors"
              onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
              aria-expanded={isUserMenuOpen}
              aria-haspopup="true"
            >
              <span className="sr-only">Open user menu</span>
              <div className="h-8 w-8 rounded-full bg-primary-600 text-white flex items-center justify-center">
                <User size={16} />
              </div>
              <span className="hidden md:block ml-2 text-gray-700">
                {user.name}
              </span>
            </button>

            {/* User dropdown menu */}
            {isUserMenuOpen && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 ring-1 ring-black ring-opacity-5 focus:outline-none z-10">
                <div className="px-4 py-2 text-sm text-gray-700 border-b border-gray-200">
                  <p className="font-medium">{user.name}</p>
                  <p className="text-xs text-gray-500 mt-1">Premium Account</p>
                </div>

                <Link
                  to="/profile"
                  className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                  onClick={() => setIsUserMenuOpen(false)}
                >
                  Your Profile
                </Link>

                <Link
                  to="/settings"
                  className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition-colors"
                  onClick={() => setIsUserMenuOpen(false)}
                >
                  <Settings size={16} className="mr-2" />
                  Settings
                </Link>

                <div className="border-t border-gray-200 mt-1"></div>

                <Button
                  variant="ghost"
                  className="w-full justify-start px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                  onClick={() => {
                    handleLogout();
                    setIsUserMenuOpen(false);
                  }}
                >
                  <LogOut size={16} className="mr-2" />
                  Sign out
                </Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
