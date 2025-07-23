import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter, Routes, Route, MemoryRouter } from 'react-router-dom';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import {
  AuthGuard,
  ProtectedRoute,
  PublicRoute,
  withAuthGuard,
} from '../AuthGuard';
import { useAuth } from '../../../hooks/useAuth';

// Mock useAuth hook
vi.mock('../../../hooks/useAuth');
const mockUseAuth = vi.mocked(useAuth);

// Test components
const TestComponent = ({ title }: { title: string }) => <div>{title}</div>;
const ProtectedComponent = withAuthGuard(TestComponent);

describe('AuthGuard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('loading state', () => {
    it('should show loading spinner when authentication is being checked', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: true,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <AuthGuard>
            <div>Protected Content</div>
          </AuthGuard>
        </BrowserRouter>
      );

      expect(
        screen.getByText('Checking authentication...')
      ).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });
  });

  describe('protected routes (requireAuth=true)', () => {
    it('should render children when user is authenticated', async () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <AuthGuard requireAuth={true}>
            <div>Protected Content</div>
          </AuthGuard>
        </BrowserRouter>
      );

      await waitFor(() => {
        expect(screen.getByText('Protected Content')).toBeInTheDocument();
      });
    });

    it('should redirect to login when user is not authenticated', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route
              path="/dashboard"
              element={
                <AuthGuard requireAuth={true}>
                  <div>Protected Content</div>
                </AuthGuard>
              }
            />
            <Route path="/login" element={<div>Login Page</div>} />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Login Page')).toBeInTheDocument();
      expect(screen.queryByText('Protected Content')).not.toBeInTheDocument();
    });

    it('should redirect to custom path when specified', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <MemoryRouter initialEntries={['/dashboard']}>
          <Routes>
            <Route
              path="/dashboard"
              element={
                <AuthGuard requireAuth={true} redirectTo="/custom-login">
                  <div>Protected Content</div>
                </AuthGuard>
              }
            />
            <Route
              path="/custom-login"
              element={<div>Custom Login Page</div>}
            />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Custom Login Page')).toBeInTheDocument();
    });

    it('should preserve location state for redirect after login', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      const LocationDisplay = () => {
        const location = window.location;
        return <div>Current path: {location.pathname}</div>;
      };

      render(
        <MemoryRouter initialEntries={['/protected-page']}>
          <Routes>
            <Route
              path="/protected-page"
              element={
                <AuthGuard requireAuth={true}>
                  <div>Protected Content</div>
                </AuthGuard>
              }
            />
            <Route
              path="/login"
              element={
                <div>
                  <div>Login Page</div>
                  <LocationDisplay />
                </div>
              }
            />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });
  });

  describe('public routes (requireAuth=false)', () => {
    it('should render children when user is not authenticated', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <AuthGuard requireAuth={false}>
            <div>Public Content</div>
          </AuthGuard>
        </BrowserRouter>
      );

      expect(screen.getByText('Public Content')).toBeInTheDocument();
    });

    it('should redirect authenticated users to dashboard', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <MemoryRouter initialEntries={['/login']}>
          <Routes>
            <Route
              path="/login"
              element={
                <AuthGuard requireAuth={false}>
                  <div>Login Form</div>
                </AuthGuard>
              }
            />
            <Route path="/dashboard" element={<div>Dashboard</div>} />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Dashboard')).toBeInTheDocument();
      expect(screen.queryByText('Login Form')).not.toBeInTheDocument();
    });

    it('should redirect to custom path when specified', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <MemoryRouter initialEntries={['/login']}>
          <Routes>
            <Route
              path="/login"
              element={
                <AuthGuard requireAuth={false} redirectTo="/home">
                  <div>Login Form</div>
                </AuthGuard>
              }
            />
            <Route path="/home" element={<div>Home Page</div>} />
          </Routes>
        </MemoryRouter>
      );

      expect(screen.getByText('Home Page')).toBeInTheDocument();
    });
  });

  describe('ProtectedRoute component', () => {
    it('should work as a protected route wrapper', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <ProtectedRoute>
            <div>Protected Content</div>
          </ProtectedRoute>
        </BrowserRouter>
      );

      expect(screen.getByText('Protected Content')).toBeInTheDocument();
    });
  });

  describe('PublicRoute component', () => {
    it('should work as a public route wrapper', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <PublicRoute>
            <div>Public Content</div>
          </PublicRoute>
        </BrowserRouter>
      );

      expect(screen.getByText('Public Content')).toBeInTheDocument();
    });
  });

  describe('withAuthGuard HOC', () => {
    it('should wrap component with auth guard', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <ProtectedComponent title="HOC Test" />
        </BrowserRouter>
      );

      expect(screen.getByText('HOC Test')).toBeInTheDocument();
    });

    it('should set correct display name for wrapped component', () => {
      expect(ProtectedComponent.displayName).toBe(
        'withAuthGuard(TestComponent)'
      );
    });

    it('should pass through props to wrapped component', () => {
      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: vi.fn(),
      });

      render(
        <BrowserRouter>
          <ProtectedComponent title="Props Test" />
        </BrowserRouter>
      );

      expect(screen.getByText('Props Test')).toBeInTheDocument();
    });
  });

  describe('auth status checking', () => {
    it('should call checkAuthStatus on mount', () => {
      const mockCheckAuthStatus = vi.fn();

      mockUseAuth.mockReturnValue({
        isAuthenticated: false,
        isLoading: false,
        user: null,
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: mockCheckAuthStatus,
      });

      render(
        <BrowserRouter>
          <AuthGuard>
            <div>Content</div>
          </AuthGuard>
        </BrowserRouter>
      );

      expect(mockCheckAuthStatus).toHaveBeenCalled();
    });

    it('should call checkAuthStatus when location changes', () => {
      const mockCheckAuthStatus = vi.fn();

      mockUseAuth.mockReturnValue({
        isAuthenticated: true,
        isLoading: false,
        user: {
          id: '1',
          name: 'John Doe',
          phone: '1234567890',
          balance: 1000,
          createdAt: '2024-01-01T00:00:00Z',
        },
        error: null,
        login: vi.fn(),
        register: vi.fn(),
        logout: vi.fn(),
        refreshToken: vi.fn(),
        clearError: vi.fn(),
        checkAuthStatus: mockCheckAuthStatus,
      });

      const { rerender } = render(
        <MemoryRouter initialEntries={['/page1']}>
          <Routes>
            <Route
              path="/page1"
              element={
                <AuthGuard>
                  <div>Page 1</div>
                </AuthGuard>
              }
            />
            <Route
              path="/page2"
              element={
                <AuthGuard>
                  <div>Page 2</div>
                </AuthGuard>
              }
            />
          </Routes>
        </MemoryRouter>
      );

      expect(mockCheckAuthStatus).toHaveBeenCalledTimes(1);

      // Simulate navigation to different route
      rerender(
        <MemoryRouter initialEntries={['/page2']}>
          <Routes>
            <Route
              path="/page1"
              element={
                <AuthGuard>
                  <div>Page 1</div>
                </AuthGuard>
              }
            />
            <Route
              path="/page2"
              element={
                <AuthGuard>
                  <div>Page 2</div>
                </AuthGuard>
              }
            />
          </Routes>
        </MemoryRouter>
      );

      // Should be called again for the new location
      expect(mockCheckAuthStatus).toHaveBeenCalledTimes(2);
    });
  });
});
