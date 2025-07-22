import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoadingButton } from '../LoadingButton';
import { describe, it, expect, vi } from 'vitest';

describe('LoadingButton', () => {
  it('renders children when not loading', () => {
    render(<LoadingButton>Click Me</LoadingButton>);

    expect(screen.getByRole('button')).toHaveTextContent('Click Me');
    expect(screen.queryByText('Loading...')).not.toBeInTheDocument();
  });

  it('shows loading state with spinner', () => {
    render(<LoadingButton loading>Click Me</LoadingButton>);

    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows loading text when provided', () => {
    render(
      <LoadingButton loading loadingText="Processing...">
        Click Me
      </LoadingButton>
    );

    expect(screen.getByRole('button')).toHaveTextContent('Processing...');
    expect(screen.queryByText('Click Me')).not.toBeInTheDocument();
  });

  it('disables the button when loading', () => {
    render(<LoadingButton loading>Click Me</LoadingButton>);

    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('disables the button when disabled prop is true', () => {
    render(<LoadingButton disabled>Click Me</LoadingButton>);

    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('calls onClick handler when clicked', async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();

    render(<LoadingButton onClick={handleClick}>Click Me</LoadingButton>);

    await user.click(screen.getByRole('button'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('does not call onClick when loading', async () => {
    const user = userEvent.setup();
    const handleClick = vi.fn();

    render(
      <LoadingButton loading onClick={handleClick}>
        Click Me
      </LoadingButton>
    );

    await user.click(screen.getByRole('button'));
    expect(handleClick).not.toHaveBeenCalled();
  });

  it('forwards ref correctly', () => {
    const ref = vi.fn();
    render(<LoadingButton ref={ref}>Button with Ref</LoadingButton>);
    expect(ref).toHaveBeenCalled();
  });

  it('passes variant and size props to Button component', () => {
    render(
      <LoadingButton variant="danger" size="lg">
        Danger Button
      </LoadingButton>
    );

    const button = screen.getByRole('button');
    expect(button).toHaveClass('bg-danger-600');
    expect(button).toHaveClass('px-6', 'py-3', 'text-lg');
  });
});
