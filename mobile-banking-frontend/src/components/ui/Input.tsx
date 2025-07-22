import React, { forwardRef, useId } from 'react';
import { AlertCircle } from 'lucide-react';

export interface InputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label: string;
  error?: string;
  helperText?: string;
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      error,
      helperText,
      id: providedId,
      required,
      size = 'md',
      fullWidth = false,
      className = '',
      type = 'text',
      ...props
    },
    ref
  ) => {
    const uniqueId = useId();
    const id = providedId || `input-${uniqueId}`;
    const errorId = `${id}-error`;
    const helperId = `${id}-helper`;
    const describedBy = error ? errorId : helperText ? helperId : undefined;

    const sizeClasses = {
      sm: 'py-1.5 text-sm',
      md: 'py-2 text-base',
      lg: 'py-3 text-lg',
    };

    const baseClasses =
      'block w-full rounded-lg border px-3 shadow-sm focus:outline-none focus:ring-2 focus:ring-offset-1';

    const stateClasses = error
      ? 'border-danger-300 text-danger-900 placeholder-danger-300 focus:border-danger-500 focus:ring-danger-500'
      : 'border-secondary-300 text-secondary-900 placeholder-secondary-400 focus:border-primary-500 focus:ring-primary-500';

    const widthClasses = fullWidth ? 'w-full' : '';

    const inputClasses = `${baseClasses} ${stateClasses} ${sizeClasses[size]} ${widthClasses} ${className}`;

    return (
      <div className={`${fullWidth ? 'w-full' : ''} mb-4`}>
        <div className="flex justify-between">
          <label
            htmlFor={id}
            className="mb-1 block text-sm font-medium text-secondary-700"
          >
            {label}
            {required && <span className="ml-1 text-danger-500">*</span>}
          </label>
        </div>
        <div className="relative">
          <input
            id={id}
            ref={ref}
            type={type}
            className={inputClasses}
            aria-invalid={error ? 'true' : 'false'}
            aria-describedby={describedBy}
            required={required}
            {...props}
          />
          {error && (
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
              <AlertCircle
                className="h-5 w-5 text-danger-500"
                aria-hidden="true"
              />
            </div>
          )}
        </div>
        {error && (
          <div
            className="mt-1 flex items-center text-sm text-danger-600"
            role="alert"
            id={errorId}
          >
            <AlertCircle className="mr-1 h-4 w-4" aria-hidden="true" />
            {error}
          </div>
        )}
        {!error && helperText && (
          <p className="mt-1 text-sm text-secondary-500" id={helperId}>
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

export { Input };
