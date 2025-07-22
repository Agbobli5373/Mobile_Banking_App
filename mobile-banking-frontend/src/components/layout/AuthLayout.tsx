import { Outlet, Link } from 'react-router-dom';
import { APP_NAME } from '@/constants';
import { CreditCard, Shield, HelpCircle } from 'lucide-react';

export interface AuthLayoutProps {
  /**
   * Optional className for additional styling
   */
  className?: string;
}

export const AuthLayout = ({ className = '' }: AuthLayoutProps) => {
  return (
    <div
      className={`min-h-screen bg-gradient-to-br from-primary-50 to-primary-100 flex flex-col ${className}`}
    >
      {/* Header with logo */}
      <header className="pt-6 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <Link
            to="/"
            className="flex items-center justify-center sm:justify-start"
          >
            <div className="h-10 w-10 rounded-full bg-primary-600 text-white flex items-center justify-center mr-2">
              <CreditCard size={20} />
            </div>
            <span className="text-2xl font-bold text-primary-900">
              {APP_NAME}
            </span>
          </Link>
        </div>
      </header>

      {/* Main content */}
      <div className="flex-1 flex items-center justify-center p-4 sm:p-6 lg:p-8">
        <div className="w-full max-w-md">
          {/* Auth form card */}
          <div className="bg-white rounded-xl shadow-soft p-6 sm:p-8 border border-gray-100">
            <div className="text-center mb-6">
              <h1 className="text-2xl font-bold text-gray-900">Welcome</h1>
              <p className="text-gray-600 mt-2">Secure Mobile Banking</p>
            </div>

            <Outlet />
          </div>

          {/* Security features */}
          <div className="mt-8 grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-white bg-opacity-80 rounded-lg p-4 flex flex-col items-center text-center">
              <Shield size={24} className="text-primary-600 mb-2" />
              <h3 className="text-sm font-medium text-gray-900">Secure</h3>
              <p className="text-xs text-gray-600 mt-1">
                End-to-end encryption
              </p>
            </div>

            <div className="bg-white bg-opacity-80 rounded-lg p-4 flex flex-col items-center text-center">
              <CreditCard size={24} className="text-primary-600 mb-2" />
              <h3 className="text-sm font-medium text-gray-900">Fast</h3>
              <p className="text-xs text-gray-600 mt-1">Instant transfers</p>
            </div>

            <div className="bg-white bg-opacity-80 rounded-lg p-4 flex flex-col items-center text-center">
              <HelpCircle size={24} className="text-primary-600 mb-2" />
              <h3 className="text-sm font-medium text-gray-900">Support</h3>
              <p className="text-xs text-gray-600 mt-1">24/7 assistance</p>
            </div>
          </div>

          {/* Footer links */}
          <div className="mt-8 text-center">
            <div className="flex justify-center space-x-4 mb-4">
              <Link
                to="/terms"
                className="text-xs text-gray-600 hover:text-primary-600 transition-colors"
              >
                Terms
              </Link>
              <Link
                to="/privacy"
                className="text-xs text-gray-600 hover:text-primary-600 transition-colors"
              >
                Privacy
              </Link>
              <Link
                to="/help"
                className="text-xs text-gray-600 hover:text-primary-600 transition-colors"
              >
                Help
              </Link>
            </div>

            <p className="text-xs text-gray-500">
              © {new Date().getFullYear()} {APP_NAME}. All rights reserved.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
