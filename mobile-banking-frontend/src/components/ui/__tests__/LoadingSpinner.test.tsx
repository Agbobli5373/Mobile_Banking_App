import { render, screen } from '@testing-library/react';
import { LoadingSpinner } from '../LoadingSpinner';
import { describe, it, expect } from 'vitest';

describe('LoadingSpinner', () => {
  it('renders with default props', () => {
    render(<LoadingSpinner />);

    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(screen.getByText('Loading...')).toHaveClass('sr-only');

    const container = screen.getByText('Loading...').parentElement;
    expect(container).toHaveAttribute('aria-live', 'polite');
    expect(container).toHaveAttribute('aria-busy', 'true');
  });

  it('renders with custom label', () => {
    render(<LoadingSpinner label="Processing..." />);

    expect(screen.getByText('Processing...')).toBeInTheDocument();
  });

  it('applies different size classes', () => {
    const { rerender } = render(<LoadingSpinner size="sm" />);

    let svg = screen.getByText('Loading...').previousElementSibling;
    expect(svg).toHaveClass('w-4', 'h-4');

    rerender(<LoadingSpinner size="md" />);
    svg = screen.getByText('Loading...').previousElementSibling;
    expect(svg).toHaveClass('w-6', 'h-6');

    rerender(<LoadingSpinner size="lg" />);
    svg = screen.getByText('Loading...').previousElementSibling;
    expect(svg).toHaveClass('w-8', 'h-8');
  });

  it('applies custom className', () => {
    render(<LoadingSpinner className="custom-spinner" />);

    const container = screen.getByText('Loading...').parentElement;
    expect(container).toHaveClass('custom-spinner');
  });
});
