import { render, screen } from '@testing-library/react';
import { AuthLayout } from '../AuthLayout';
import { vi, describe, it, expect } from 'vitest';
import { APP_NAME } from '@/constants';

// Mock the Outlet component from react-router-dom
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    Outlet: () => <div data-testid="outlet">Outlet Content</div>,
  };
});

describe('AuthLayout', () => {
  it('renders the auth layout with branding and outlet content', () => {
    render(<AuthLayout />);

    // Check for app name in the header
    expect(screen.getByText(APP_NAME)).toBeInTheDocument();

    // Check for the tagline
    expect(screen.getByText('Secure Mobile Banking')).toBeInTheDocument();

    // Check that outlet content is rendered
    expect(screen.getByTestId('outlet')).toBeInTheDocument();

    // Check for copyright in footer
    const currentYear = new Date().getFullYear();
    expect(
      screen.getByText(`© ${currentYear} ${APP_NAME}. All rights reserved.`)
    ).toBeInTheDocument();
  });

  it('applies custom className when provided', () => {
    render(<AuthLayout className="custom-class" />);

    const layoutContainer =
      screen.getByText(APP_NAME).parentElement?.parentElement?.parentElement;
    expect(layoutContainer).toHaveClass('custom-class');
  });
});
