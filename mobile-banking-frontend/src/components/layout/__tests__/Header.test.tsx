import { render, screen, fireEvent } from '@testing-library/react';
import { Header } from '../Header';
import { vi, expect, describe, beforeEach, it } from 'vitest';
import { APP_NAME } from '@/constants';

// Mock react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    Link: ({
      to,
      children,
      className,
    }: {
      to: string;
      children: React.ReactNode;
      className?: string;
    }) => (
      <a href={to} className={className} data-testid={`link-${to}`}>
        {children}
      </a>
    ),
  };
});

describe('Header', () => {
  const mockOnMenuToggle = vi.fn();

  beforeEach(() => {
    mockOnMenuToggle.mockClear();
  });

  it('renders the header with app name and menu button', () => {
    render(<Header onMenuToggle={mockOnMenuToggle} />);

    // Check for app name
    expect(screen.getByText(APP_NAME)).toBeInTheDocument();

    // Check for menu button
    const menuButton = screen.getByLabelText('Open sidebar');
    expect(menuButton).toBeInTheDocument();
  });

  it('calls onMenuToggle when menu button is clicked', () => {
    render(<Header onMenuToggle={mockOnMenuToggle} />);

    const menuButton = screen.getByLabelText('Open sidebar');
    fireEvent.click(menuButton);

    expect(mockOnMenuToggle).toHaveBeenCalledTimes(1);
  });

  it('toggles user dropdown menu when user button is clicked', () => {
    render(<Header onMenuToggle={mockOnMenuToggle} />);

    // User dropdown should not be visible initially
    expect(screen.queryByText('Sign out')).not.toBeInTheDocument();

    // Click the user button to open dropdown
    const userButton = screen.getByText('John Doe');
    fireEvent.click(userButton);

    // User dropdown should now be visible
    expect(screen.getByText('Sign out')).toBeInTheDocument();

    // Click again to close
    fireEvent.click(userButton);

    // User dropdown should be hidden again
    expect(screen.queryByText('Sign out')).not.toBeInTheDocument();
  });

  it('handles logout when sign out button is clicked', () => {
    // Mock console.log to check if it's called
    const consoleSpy = vi.spyOn(console, 'log');

    render(<Header onMenuToggle={mockOnMenuToggle} />);

    // Open the user dropdown
    const userButton = screen.getByText('John Doe');
    fireEvent.click(userButton);

    // Click the sign out button
    const signOutButton = screen.getByText('Sign out');
    fireEvent.click(signOutButton);

    // Check if logout function was called
    expect(consoleSpy).toHaveBeenCalledWith('Logging out...');

    // Dropdown should be closed after logout
    expect(screen.queryByText('Sign out')).not.toBeInTheDocument();

    consoleSpy.mockRestore();
  });

  it('displays notifications when notification button is clicked', () => {
    render(<Header onMenuToggle={mockOnMenuToggle} />);

    // Notifications should not be visible initially
    expect(screen.queryByText('Notifications')).not.toBeInTheDocument();

    // Click the notifications button
    const notificationsButton = screen.getByLabelText('Notifications');
    fireEvent.click(notificationsButton);

    // Notifications dropdown should now be visible
    expect(screen.getByText('Notifications')).toBeInTheDocument();
    expect(screen.getByText('Mark all as read')).toBeInTheDocument();
    expect(
      screen.getByText('Your transfer of $50 was successful')
    ).toBeInTheDocument();

    // Click again to close
    fireEvent.click(notificationsButton);

    // Notifications dropdown should be hidden again
    expect(screen.queryByText('Notifications')).not.toBeInTheDocument();
  });
});
