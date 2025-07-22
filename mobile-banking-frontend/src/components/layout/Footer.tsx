import { Link } from 'react-router-dom';
import { APP_NAME, APP_VERSION } from '@/constants';
import { CreditCard, Facebook, Twitter, Instagram, Mail } from 'lucide-react';

export const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-white border-t border-gray-200 py-6 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        {/* Main footer content */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-8">
          {/* Company info */}
          <div className="flex flex-col items-center md:items-start">
            <div className="flex items-center">
              <div className="h-8 w-8 rounded-full bg-primary-600 text-white flex items-center justify-center mr-2">
                <CreditCard size={18} />
              </div>
              <span className="text-xl font-bold text-primary-900">
                {APP_NAME}
              </span>
            </div>
            <p className="mt-2 text-sm text-gray-500 text-center md:text-left">
              Secure, fast, and reliable mobile banking for everyone.
            </p>
            <div className="flex space-x-4 mt-4">
              <a
                href="#"
                className="text-gray-400 hover:text-primary-600 transition-colors"
                aria-label="Facebook"
              >
                <Facebook size={20} />
              </a>
              <a
                href="#"
                className="text-gray-400 hover:text-primary-600 transition-colors"
                aria-label="Twitter"
              >
                <Twitter size={20} />
              </a>
              <a
                href="#"
                className="text-gray-400 hover:text-primary-600 transition-colors"
                aria-label="Instagram"
              >
                <Instagram size={20} />
              </a>
              <a
                href="mailto:contact@mobilebanking.com"
                className="text-gray-400 hover:text-primary-600 transition-colors"
                aria-label="Email"
              >
                <Mail size={20} />
              </a>
            </div>
          </div>

          {/* Quick links */}
          <div className="flex flex-col items-center md:items-start">
            <h3 className="text-sm font-semibold text-gray-900 mb-4">
              Quick Links
            </h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/dashboard"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Dashboard
                </Link>
              </li>
              <li>
                <Link
                  to="/transactions"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Transactions
                </Link>
              </li>
              <li>
                <Link
                  to="/transfer"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Transfer Money
                </Link>
              </li>
              <li>
                <Link
                  to="/add-funds"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Add Funds
                </Link>
              </li>
            </ul>
          </div>

          {/* Support */}
          <div className="flex flex-col items-center md:items-start">
            <h3 className="text-sm font-semibold text-gray-900 mb-4">
              Support
            </h3>
            <ul className="space-y-2">
              <li>
                <Link
                  to="/help"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Help Center
                </Link>
              </li>
              <li>
                <Link
                  to="/contact"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Contact Us
                </Link>
              </li>
              <li>
                <Link
                  to="/terms"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Terms of Service
                </Link>
              </li>
              <li>
                <Link
                  to="/privacy"
                  className="text-sm text-gray-500 hover:text-primary-600 transition-colors"
                >
                  Privacy Policy
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Copyright and version */}
        <div className="border-t border-gray-200 pt-4 md:flex md:items-center md:justify-between">
          <div className="flex justify-center md:justify-start">
            <p className="text-gray-500 text-sm">
              © {currentYear} {APP_NAME}. All rights reserved.
            </p>
          </div>

          <div className="mt-4 md:mt-0 flex justify-center md:justify-end">
            <p className="text-xs text-gray-400">Version {APP_VERSION}</p>
          </div>
        </div>
      </div>
    </footer>
  );
};
