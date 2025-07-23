import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, UserPlus } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { Input } from '../../../components/ui/Input';
import { useRegister } from '../hooks/useRegister';
import { RegistrationSuccess } from './RegistrationSuccess';
import {
  registerSchema,
  type RegisterFormData,
} from '../schemas/registerSchema';

export const RegisterForm: React.FC = () => {
  const navigate = useNavigate();
  const {
    register: registerUser,
    isLoading,
    error,
    isSuccess,
    clearError,
  } = useRegister();
  const [showPin, setShowPin] = useState(false);
  const [showConfirmPin, setShowConfirmPin] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
    clearErrors,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    mode: 'onChange',
  });

  const onSubmit = async (data: RegisterFormData) => {
    try {
      clearError();
      clearErrors();

      // Remove confirmPin from the data before sending to API
      const { confirmPin, ...registrationData } = data;

      await registerUser(registrationData);
      // Navigation is handled by the useRegister hook after successful registration
    } catch (error: any) {
      // Handle specific API errors
      if (error.response?.status === 409) {
        setError('phoneNumber', {
          type: 'manual',
          message: 'This phone number is already registered',
        });
      } else if (error.response?.data?.message) {
        // Server validation errors
        const serverMessage = error.response.data.message;
        if (serverMessage.toLowerCase().includes('phoneNumber')) {
          setError('phoneNumber', {
            type: 'manual',
            message: serverMessage,
          });
        } else if (serverMessage.toLowerCase().includes('name')) {
          setError('name', {
            type: 'manual',
            message: serverMessage,
          });
        } else if (serverMessage.toLowerCase().includes('pin')) {
          setError('pin', {
            type: 'manual',
            message: serverMessage,
          });
        }
      }
    }
  };

  const formatPhoneNumber = (value: string) => {
    // Remove all non-digits
    const digits = value.replace(/\D/g, '');

    // Ghana phone number formatting: 0XX XXX XXXX or +233 XX XXX XXXX
    if (digits.startsWith('233') && digits.length >= 12) {
      // +233 XX XXX XXXX
      return `+233 ${digits.slice(3, 5)} ${digits.slice(5, 8)} ${digits.slice(8, 12)}`;
    } else if (digits.startsWith('0') && digits.length >= 10) {
      // 0XX XXX XXXX
      return `0${digits.slice(1, 3)} ${digits.slice(3, 6)} ${digits.slice(6, 10)}`;
    } else if (digits.startsWith('233') && digits.length > 3) {
      // Partial +233 input
      return `+233 ${digits.slice(3)}`;
    } else if (digits.startsWith('0') && digits.length > 1) {
      // Partial 0XX input
      return `0${digits.slice(1)}`;
    }
    return digits;
  };

  // Show success component if registration was successful
  if (isSuccess) {
    return <RegistrationSuccess onContinue={() => navigate('/dashboard')} />;
  }

  return (
    <div className="w-full max-w-md mx-auto">
      <div className="text-center mb-8">
        <div className="mx-auto w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mb-4">
          <UserPlus className="w-8 h-8 text-primary-600" />
        </div>
        <h1 className="text-2xl font-bold text-gray-900 mb-2">
          Create Account
        </h1>
        <p className="text-gray-600">Join us to start managing your finances</p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6" noValidate>
        {/* Global error message */}
        {error && !Object.keys(errors).length && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}

        {/* Name Field */}
        <div>
          <Input
            {...register('name')}
            label="Full Name"
            type="text"
            placeholder="Enter your full name"
            error={errors.name?.message}
            required
            fullWidth
            autoComplete="name"
            aria-describedby="name-help"
          />
          <p id="name-help" className="mt-1 text-xs text-gray-500">
            Enter your legal name as it appears on your ID
          </p>
        </div>

        {/* Phone Field */}
        <div>
          <Input
            {...register('phoneNumber', {
              onChange: e => {
                // Format phone number as user types
                const formatted = formatPhoneNumber(e.target.value);
                e.target.value = formatted;
              },
            })}
            label="Phone Number"
            type="tel"
            placeholder="(555) 123-4567"
            error={errors.phone?.message}
            required
            fullWidth
            autoComplete="tel"
            aria-describedby="phone-help"
          />
          <p id="phone-help" className="mt-1 text-xs text-gray-500">
            We'll use this number for account verification and security
          </p>
        </div>

        {/* PIN Field */}
        <div>
          <div className="relative">
            <Input
              {...register('pin')}
              label="4-Digit PIN"
              type={showPin ? 'text' : 'password'}
              placeholder="Enter 4-digit PIN"
              error={errors.pin?.message}
              required
              fullWidth
              maxLength={4}
              autoComplete="new-password"
              aria-describedby="pin-help"
            />
            <button
              type="button"
              className="absolute right-3 top-9 text-gray-400 hover:text-gray-600 focus:outline-none focus:text-gray-600"
              onClick={() => setShowPin(!showPin)}
              aria-label={showPin ? 'Hide PIN' : 'Show PIN'}
            >
              {showPin ? (
                <EyeOff className="w-5 h-5" />
              ) : (
                <Eye className="w-5 h-5" />
              )}
            </button>
          </div>
          <p id="pin-help" className="mt-1 text-xs text-gray-500">
            Choose a secure 4-digit PIN for account access
          </p>
        </div>

        {/* Confirm PIN Field */}
        <div>
          <div className="relative">
            <Input
              {...register('confirmPin')}
              label="Confirm PIN"
              type={showConfirmPin ? 'text' : 'password'}
              placeholder="Re-enter your PIN"
              error={errors.confirmPin?.message}
              required
              fullWidth
              maxLength={4}
              autoComplete="new-password"
            />
            <button
              type="button"
              className="absolute right-3 top-9 text-gray-400 hover:text-gray-600 focus:outline-none focus:text-gray-600"
              onClick={() => setShowConfirmPin(!showConfirmPin)}
              aria-label={showConfirmPin ? 'Hide PIN' : 'Show PIN'}
            >
              {showConfirmPin ? (
                <EyeOff className="w-5 h-5" />
              ) : (
                <Eye className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>

        {/* Submit Button */}
        <Button
          type="submit"
          variant="primary"
          size="lg"
          loading={isLoading || isSubmitting}
          disabled={isLoading || isSubmitting}
          className="w-full"
        >
          {isLoading || isSubmitting ? 'Creating Account...' : 'Create Account'}
        </Button>

        {/* Login Link */}
        <div className="text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link
              to="/login"
              className="font-medium text-primary-600 hover:text-primary-500 focus:outline-none focus:underline"
            >
              Sign in here
            </Link>
          </p>
        </div>
      </form>

      {/* Terms and Privacy */}
      <div className="mt-8 text-center">
        <p className="text-xs text-gray-500">
          By creating an account, you agree to our{' '}
          <Link
            to="/terms"
            className="text-primary-600 hover:text-primary-500 underline"
          >
            Terms of Service
          </Link>{' '}
          and{' '}
          <Link
            to="/privacy"
            className="text-primary-600 hover:text-primary-500 underline"
          >
            Privacy Policy
          </Link>
        </p>
      </div>
    </div>
  );
};
