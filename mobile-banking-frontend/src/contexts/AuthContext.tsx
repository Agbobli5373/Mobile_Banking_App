import React, { createContext, useContext, ReactNode } from 'react';
import { useAuth, type UseAuthReturn } from '../hooks/useAuth';

/**
 * Authentication context type
 */
type AuthContextType = UseAuthReturn;

/**
 * Authentication context
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Props for AuthProvider component
 */
export interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Authentication provider component
 */
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const auth = useAuth();

  return <AuthContext.Provider value={auth}>{children}</AuthContext.Provider>;
};

/**
 * Hook to use authentication context
 */
export const useAuthContext = (): AuthContextType => {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }

  return context;
};

/**
 * Hook to get current user (convenience hook)
 */
export const useCurrentUser = () => {
  const { user } = useAuthContext();
  return user;
};

/**
 * Hook to check if user is authenticated (convenience hook)
 */
export const useIsAuthenticated = () => {
  const { isAuthenticated } = useAuthContext();
  return isAuthenticated;
};
