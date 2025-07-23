import { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { Footer } from './Footer';

export interface AppLayoutProps {
  /**
   * Optional className for additional styling
   */
  className?: string;
  /**
   * Children to render in the app layout
   */
  children?: React.ReactNode;
}

export const AppLayout = ({ className = '', children }: AppLayoutProps) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const location = useLocation();

  // Close sidebar on route change (mobile only)
  useEffect(() => {
    if (window.innerWidth < 1024) {
      setIsSidebarOpen(false);
    }
  }, [location.pathname]);

  // Handle window resize
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 1024) {
        // On desktop, sidebar should always be visible
        setIsSidebarOpen(true);
      } else {
        // On mobile, sidebar should be hidden by default
        setIsSidebarOpen(false);
      }
    };

    // Set initial state based on screen size
    handleResize();

    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  return (
    <div className={`min-h-screen bg-gray-50 flex flex-col ${className}`}>
      <Header onMenuToggle={toggleSidebar} />

      <div className="flex flex-1 relative">
        <Sidebar
          isOpen={isSidebarOpen}
          onClose={() => setIsSidebarOpen(false)}
        />

        <main className="flex-1 p-4 sm:p-6 lg:p-8 transition-all duration-200 overflow-x-hidden">
          {/* Page content */}
          <div className="max-w-7xl mx-auto">{children || <Outlet />}</div>

          {/* Back to top button - visible when scrolled down */}
          <button
            type="button"
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
            className="fixed bottom-4 right-4 bg-primary-600 text-white p-2 rounded-full shadow-md hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 transition-opacity opacity-0 translate-y-10 scroll-visible"
            aria-label="Back to top"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"
                clipRule="evenodd"
              />
            </svg>
          </button>
        </main>
      </div>

      <Footer />

      {/* Add scroll behavior styles */}
      <style>{`
        .scroll-visible {
          transition:
            opacity 0.3s,
            transform 0.3s;
        }

        @media (min-width: 640px) {
          :root {
            scroll-padding-top: 4rem;
          }

          html {
            scroll-behavior: smooth;
          }

          body:not([data-scroll='0']) .scroll-visible {
            opacity: 1;
            transform: translateY(0);
          }
        }
      `}</style>
    </div>
  );
};
