import React from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle, ArrowRight } from 'lucide-react';
import { Button } from '../../../components/ui/Button';

export interface RegistrationSuccessProps {
  userName?: string;
  onContinue?: () => void;
}

export const RegistrationSuccess: React.FC<RegistrationSuccessProps> = ({
  userName,
  onContinue,
}) => {
  return (
    <div className="w-full max-w-md mx-auto text-center">
      <div className="mx-auto w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mb-6">
        <CheckCircle className="w-12 h-12 text-green-600" />
      </div>

      <h1 className="text-2xl font-bold text-gray-900 mb-2">
        Welcome to Mobile Banking!
      </h1>

      {userName && (
        <p className="text-lg text-gray-700 mb-4">
          Hi {userName}, your account has been created successfully.
        </p>
      )}

      <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
        <h2 className="text-sm font-semibold text-green-800 mb-2">
          What's next?
        </h2>
        <ul className="text-sm text-green-700 space-y-1 text-left">
          <li>• Your account is ready to use</li>
          <li>• You can start transferring money</li>
          <li>• Add funds to your wallet</li>
          <li>• View your transaction history</li>
        </ul>
      </div>

      <div className="space-y-4">
        <Button
          variant="primary"
          size="lg"
          className="w-full"
          onClick={onContinue}
        >
          Get Started
          <ArrowRight className="w-5 h-5 ml-2" />
        </Button>

        <p className="text-sm text-gray-600">
          Need help?{' '}
          <Link
            to="/help"
            className="text-primary-600 hover:text-primary-500 font-medium"
          >
            Visit our Help Center
          </Link>
        </p>
      </div>

      <div className="mt-8 p-4 bg-blue-50 border border-blue-200 rounded-lg">
        <h3 className="text-sm font-semibold text-blue-800 mb-2">
          Security Tips
        </h3>
        <ul className="text-xs text-blue-700 space-y-1 text-left">
          <li>• Never share your PIN with anyone</li>
          <li>• Always log out when using shared devices</li>
          <li>• Contact us immediately if you notice suspicious activity</li>
        </ul>
      </div>
    </div>
  );
};
