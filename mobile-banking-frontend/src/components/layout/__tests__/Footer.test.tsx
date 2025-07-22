import { render, screen } from '@testing-library/react';
import { Footer } from '../Footer';
import { vi, describe, it, expect } from 'vitest';
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

describe('Footer', () => {
  it('renders the footer with copyright information', () => {
    render(<Footer />);

    const currentYear = new Date().getFullYear();
    expect(
      screen.getByText(`© ${currentYear} ${APP_NAME}. All rights reserved.`)
    ).toBeInTheDocument();
  });

  it('renders footer links', () => {
    render(<Footer />);

    // Check for footer links
    expect(screen.getByText('Terms')).toBeInTheDocument();
    expect(screen.getByText('Privacy')).toBeInTheDocument();
    expect(screen.getByText('Help Center')).toBeInTheDocument();
    expect(screen.getByText('Contact')).toBeInTheDocument();

    // Check that links have correct hrefs
    expect(screen.getByTestId('link-/terms')).toBeInTheDocument();
    expect(screen.getByTestId('link-/privacy')).toBeInTheDocument();
    expect(screen.getByTestId('link-/help')).toBeInTheDocument();
    expect(screen.getByTestId('link-/contact')).toBeInTheDocument();
  });

  it('has responsive layout', () => {
    render(<Footer />);

    // Check that the footer has responsive classes
    const footerElement = screen.getByText(
      `© ${new Date().getFullYear()} ${APP_NAME}. All rights reserved.`
    ).parentElement?.parentElement;
    expect(footerElement).toHaveClass('md:flex');
    expect(footerElement).toHaveClass('md:items-center');
    expect(footerElement).toHaveClass('md:justify-between');

    // Check that the links container has responsive classes
    const linksContainer =
      screen.getByText('Terms').parentElement?.parentElement;
    expect(linksContainer).toHaveClass('mt-4');
    expect(linksContainer).toHaveClass('md:mt-0');
  });
});
