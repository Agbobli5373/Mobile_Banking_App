import { useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { LoadingSpinner } from '../ui';

export interface AuthGuardProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  redirectTo?: string;
}

/**
 * AuthGuard component to protect routes based on authentication status
 */
export const AuthGuard: React.FC<AuthGuardProps> = ({
  children,
  requireAuth = true,
  redirectTo,
}) => {
  const { isAuthenticated, isLoading, checkAuthStatus } = useAuth();
  const location = useLocation();

  // Check auth status on mount and location change
  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus, location.pathname]);

  // Show loading spinner while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <LoadingSpinner size="lg" />
          <p className="mt-4 text-gray-600">Checking authentication...</p>
        </div>
      </div>
    );
  }

  // Handle authentication requirements
  if (requireAuth && !isAuthenticated) {
    // Store the attempted location for redirect after login
    const redirectPath = redirectTo || '/login';
    const state = location.pathname !== '/' ? { from: location } : undefined;

    return <Navigate to={redirectPath} state={state} replace />;
  }

  // Handle cases where authenticated users shouldn't access certain routes
  if (!requireAuth && isAuthenticated) {
    // Redirect authenticated users away from auth pages
    const redirectPath = redirectTo || '/dashboard';
    return <Navigate to={redirectPath} replace />;
  }

  return <>{children}</>;
};

/**
 * Higher-order component for protecting routes
 */
export const withAuthGuard = <P extends object>(
  Component: React.ComponentType<P>,
  options: Omit<AuthGuardProps, 'children'> = {}
) => {
  const WrappedComponent = (props: P) => (
    <AuthGuard {...options}>
      <Component {...props} />
    </AuthGuard>
  );

  WrappedComponent.displayName = `withAuthGuard(${Component.displayName || Component.name})`;

  return WrappedComponent;
};

/**
 * Component for protecting authenticated routes
 */
export const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => <AuthGuard requireAuth={true}>{children}</AuthGuard>;

/**
 * Component for protecting public routes (redirect authenticated users)
 */
export const PublicRoute: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => <AuthGuard requireAuth={false}>{children}</AuthGuard>;
