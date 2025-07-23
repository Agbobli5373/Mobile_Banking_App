import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { AppLayout, AuthLayout } from './components/layout';
import {
  AuthGuard,
  ProtectedRoute,
  PublicRoute,
} from './components/auth/AuthGuard';
import { ToastContainer } from './components/ui';
import { RegisterForm } from './features/auth';

// This is a placeholder component for demonstration purposes
// In a real app, these would be actual feature components
const PlaceholderPage = ({ title }: { title: string }) => (
  <div className="bg-white rounded-xl shadow-soft p-6">
    <h1 className="text-2xl font-bold text-gray-900 mb-4">{title}</h1>
    <p className="text-gray-600">This is a placeholder for the {title} page.</p>
  </div>
);

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes - redirect authenticated users to dashboard */}
          <Route
            path="/login"
            element={
              <PublicRoute>
                <AuthLayout>
                  <PlaceholderPage title="Login" />
                </AuthLayout>
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <AuthLayout>
                  <RegisterForm />
                </AuthLayout>
              </PublicRoute>
            }
          />
          <Route
            path="/forgot-password"
            element={
              <PublicRoute>
                <AuthLayout>
                  <PlaceholderPage title="Forgot Password" />
                </AuthLayout>
              </PublicRoute>
            }
          />

          {/* Protected routes - require authentication */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <PlaceholderPage title="Dashboard" />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/transactions"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <PlaceholderPage title="Transactions" />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/transfer"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <PlaceholderPage title="Transfer Money" />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/add-funds"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <PlaceholderPage title="Add Funds" />
                </AppLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <AppLayout>
                  <PlaceholderPage title="Settings" />
                </AppLayout>
              </ProtectedRoute>
            }
          />

          {/* Static pages - accessible to all users */}
          <Route
            path="/terms"
            element={
              <AppLayout>
                <PlaceholderPage title="Terms of Service" />
              </AppLayout>
            }
          />
          <Route
            path="/privacy"
            element={
              <AppLayout>
                <PlaceholderPage title="Privacy Policy" />
              </AppLayout>
            }
          />
          <Route
            path="/help"
            element={
              <AppLayout>
                <PlaceholderPage title="Help Center" />
              </AppLayout>
            }
          />
          <Route
            path="/contact"
            element={
              <AppLayout>
                <PlaceholderPage title="Contact Us" />
              </AppLayout>
            }
          />

          {/* Root redirect - redirect to dashboard if authenticated, login if not */}
          <Route
            path="/"
            element={
              <AuthGuard>
                <Navigate to="/dashboard" replace />
              </AuthGuard>
            }
          />

          {/* Catch all - 404 */}
          <Route
            path="*"
            element={
              <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <PlaceholderPage title="404 - Page Not Found" />
              </div>
            }
          />
        </Routes>

        {/* Global toast notifications */}
        <ToastContainer toasts={[]} onClose={() => {}} />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
