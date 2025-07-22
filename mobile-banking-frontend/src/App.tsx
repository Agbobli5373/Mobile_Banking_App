import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout, AuthLayout } from '@/components/layout';
import { ToastContainer } from '@/components/ui';

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
      <Routes>
        {/* Auth routes */}
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<PlaceholderPage title="Login" />} />
          <Route
            path="/register"
            element={<PlaceholderPage title="Register" />}
          />
          <Route
            path="/forgot-password"
            element={<PlaceholderPage title="Forgot Password" />}
          />
        </Route>

        {/* App routes */}
        <Route element={<AppLayout />}>
          <Route
            path="/dashboard"
            element={<PlaceholderPage title="Dashboard" />}
          />
          <Route
            path="/transactions"
            element={<PlaceholderPage title="Transactions" />}
          />
          <Route
            path="/transfer"
            element={<PlaceholderPage title="Transfer Money" />}
          />
          <Route
            path="/add-funds"
            element={<PlaceholderPage title="Add Funds" />}
          />
          <Route
            path="/settings"
            element={<PlaceholderPage title="Settings" />}
          />

          {/* Static pages */}
          <Route
            path="/terms"
            element={<PlaceholderPage title="Terms of Service" />}
          />
          <Route
            path="/privacy"
            element={<PlaceholderPage title="Privacy Policy" />}
          />
          <Route
            path="/help"
            element={<PlaceholderPage title="Help Center" />}
          />
          <Route
            path="/contact"
            element={<PlaceholderPage title="Contact Us" />}
          />
        </Route>

        {/* Redirect root to dashboard */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />

        {/* Catch all - 404 */}
        <Route
          path="*"
          element={<PlaceholderPage title="404 - Page Not Found" />}
        />
      </Routes>

      {/* Global toast notifications */}
      <ToastContainer toasts={[]} onClose={function (): void {
        throw new Error('Function not implemented.');
      } } />
    </BrowserRouter>
  );
}

export default App;
