import { render, screen } from '@testing-library/react';
import { Toast, ToastContainer, type ToastProps } from '../Toast';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';

describe('Toast', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders with correct content', () => {
    const onClose = vi.fn();
    render(
      <Toast
        id="test-toast"
        type="success"
        title="Success Title"
        message="Success message"
        onClose={onClose}
      />
    );

    expect(screen.getByText('Success Title')).toBeInTheDocument();
    expect(screen.getByText('Success message')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toBeInTheDocument();
  });

  it('renders different toast types with correct styling', () => {
    const onClose = vi.fn();
    const { rerender } = render(
      <Toast
        id="test-toast"
        type="success"
        title="Success Toast"
        onClose={onClose}
      />
    );

    expect(screen.getByRole('alert').querySelector('div')).toHaveClass(
      'bg-success-50',
      'border-success-500'
    );

    rerender(
      <Toast
        id="test-toast"
        type="error"
        title="Error Toast"
        onClose={onClose}
      />
    );
    expect(screen.getByRole('alert').querySelector('div')).toHaveClass(
      'bg-danger-50',
      'border-danger-500'
    );

    rerender(
      <Toast
        id="test-toast"
        type="warning"
        title="Warning Toast"
        onClose={onClose}
      />
    );
    expect(screen.getByRole('alert').querySelector('div')).toHaveClass(
      'bg-warning-50',
      'border-warning-500'
    );

    rerender(
      <Toast id="test-toast" type="info" title="Info Toast" onClose={onClose} />
    );
    expect(screen.getByRole('alert').querySelector('div')).toHaveClass(
      'bg-primary-50',
      'border-primary-500'
    );
  });

  it('has a close button', () => {
    const onClose = vi.fn();

    render(
      <Toast
        id="test-toast"
        type="success"
        title="Success Toast"
        onClose={onClose}
      />
    );

    const closeButton = screen.getByRole('button', { name: /close/i });
    expect(closeButton).toBeInTheDocument();
  });

  it('auto-closes after duration', () => {
    const onClose = vi.fn();

    render(
      <Toast
        id="test-toast"
        type="success"
        title="Success Toast"
        duration={2000}
        onClose={onClose}
      />
    );

    // Advance timer to trigger auto-close
    vi.advanceTimersByTime(2000);

    // Wait for exit animation
    vi.advanceTimersByTime(300);

    expect(onClose).toHaveBeenCalledWith('test-toast');
  });

  it('does not auto-close when duration is 0', () => {
    const onClose = vi.fn();

    render(
      <Toast
        id="test-toast"
        type="success"
        title="Success Toast"
        duration={0}
        onClose={onClose}
      />
    );

    // Advance timer beyond normal duration
    vi.advanceTimersByTime(10000);

    expect(onClose).not.toHaveBeenCalled();
  });
});

describe('ToastContainer', () => {
  it('renders multiple toasts', () => {
    const toasts: ToastProps[] = [
      { id: '1', type: 'success', title: 'Success Toast', onClose: vi.fn() },
      { id: '2', type: 'error', title: 'Error Toast', onClose: vi.fn() },
      { id: '3', type: 'warning', title: 'Warning Toast', onClose: vi.fn() },
    ];

    const onClose = vi.fn();

    render(<ToastContainer toasts={toasts} onClose={onClose} />);

    expect(screen.getByText('Success Toast')).toBeInTheDocument();
    expect(screen.getByText('Error Toast')).toBeInTheDocument();
    expect(screen.getByText('Warning Toast')).toBeInTheDocument();
  });

  it('applies different position classes', () => {
    const toasts: ToastProps[] = [
      { id: '1', type: 'success', title: 'Success Toast', onClose: vi.fn() },
    ];
    const onClose = vi.fn();

    const { rerender } = render(
      <ToastContainer toasts={toasts} onClose={onClose} position="top-right" />
    );

    expect(screen.getByLabelText('Notifications')).toHaveClass(
      'top-0',
      'right-0'
    );

    rerender(
      <ToastContainer toasts={toasts} onClose={onClose} position="top-left" />
    );
    expect(screen.getByLabelText('Notifications')).toHaveClass(
      'top-0',
      'left-0'
    );

    rerender(
      <ToastContainer
        toasts={toasts}
        onClose={onClose}
        position="bottom-right"
      />
    );
    expect(screen.getByLabelText('Notifications')).toHaveClass(
      'bottom-0',
      'right-0'
    );

    rerender(
      <ToastContainer
        toasts={toasts}
        onClose={onClose}
        position="bottom-left"
      />
    );
    expect(screen.getByLabelText('Notifications')).toHaveClass(
      'bottom-0',
      'left-0'
    );
  });
});
