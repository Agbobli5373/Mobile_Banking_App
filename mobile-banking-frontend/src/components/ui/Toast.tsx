import React, { useEffect, useState } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastProps {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
  onClose: (id: string) => void;
}

export interface ToastContainerProps {
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left';
  toasts: ToastProps[];
  onClose: (id: string) => void;
}

const Toast: React.FC<ToastProps> = ({
  id,
  type,
  title,
  message,
  duration = 5000,
  onClose,
}) => {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        setIsVisible(false);
        setTimeout(() => onClose(id), 300); // Allow time for exit animation
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [duration, id, onClose]);

  const handleClose = () => {
    setIsVisible(false);
    setTimeout(() => onClose(id), 300); // Allow time for exit animation
  };

  const typeClasses = {
    success: 'bg-success-50 border-success-500 text-success-700',
    error: 'bg-danger-50 border-danger-500 text-danger-700',
    warning: 'bg-warning-50 border-warning-500 text-warning-700',
    info: 'bg-primary-50 border-primary-500 text-primary-700',
  };

  const iconMap = {
    success: <CheckCircle className="h-5 w-5 text-success-500" />,
    error: <AlertCircle className="h-5 w-5 text-danger-500" />,
    warning: <AlertTriangle className="h-5 w-5 text-warning-500" />,
    info: <Info className="h-5 w-5 text-primary-500" />,
  };

  return (
    <div
      className={`transform transition-all duration-300 ease-in-out ${
        isVisible ? 'translate-x-0 opacity-100' : 'translate-x-full opacity-0'
      }`}
      role="alert"
      aria-live="assertive"
    >
      <div
        className={`mb-4 flex w-full max-w-sm overflow-hidden rounded-lg border-l-4 shadow-md ${typeClasses[type]}`}
      >
        <div className="flex w-full items-center p-4">
          <div className="mr-3 flex-shrink-0">{iconMap[type]}</div>
          <div className="flex-1">
            <div className="font-medium">{title}</div>
            {message && <div className="mt-1 text-sm">{message}</div>}
          </div>
          <button
            type="button"
            className="ml-4 inline-flex flex-shrink-0 rounded-md p-1.5 focus:outline-none focus:ring-2 focus:ring-offset-2"
            onClick={handleClose}
            aria-label="Close"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
};

const ToastContainer: React.FC<ToastContainerProps> = ({
  position = 'top-right',
  toasts,
  onClose,
}) => {
  const positionClasses = {
    'top-right': 'top-0 right-0',
    'top-left': 'top-0 left-0',
    'bottom-right': 'bottom-0 right-0',
    'bottom-left': 'bottom-0 left-0',
  };

  return (
    <div
      className={`fixed z-50 m-4 flex max-h-screen w-full max-w-sm flex-col ${positionClasses[position]}`}
      aria-label="Notifications"
    >
      {toasts.map(toast => (
        <Toast key={toast.id} {...toast} onClose={onClose} />
      ))}
    </div>
  );
};

export { Toast, ToastContainer };
