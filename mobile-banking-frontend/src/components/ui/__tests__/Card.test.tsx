import { render, screen } from '@testing-library/react';
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from '../Card';
import { describe, it, expect, vi } from 'vitest';

describe('Card', () => {
  it('renders children correctly', () => {
    render(<Card>Card Content</Card>);
    expect(screen.getByText('Card Content')).toBeInTheDocument();
  });

  it('applies different padding sizes', () => {
    const { rerender } = render(<Card padding="sm">Small Padding</Card>);
    const smallCard = screen.getByText('Small Padding');
    expect(smallCard.closest('div')).toHaveClass('p-3');

    rerender(<Card padding="md">Medium Padding</Card>);
    const mediumCard = screen.getByText('Medium Padding');
    expect(mediumCard.closest('div')).toHaveClass('p-4');

    rerender(<Card padding="lg">Large Padding</Card>);
    const largeCard = screen.getByText('Large Padding');
    expect(largeCard.closest('div')).toHaveClass('p-6');
  });

  it('applies different shadow options', () => {
    const { rerender } = render(<Card shadow="none">No Shadow</Card>);
    const noShadowCard = screen.getByText('No Shadow');
    expect(noShadowCard.closest('div')).not.toHaveClass(
      'shadow-sm',
      'shadow-md',
      'shadow-lg'
    );

    rerender(<Card shadow="sm">Small Shadow</Card>);
    const smallShadowCard = screen.getByText('Small Shadow');
    expect(smallShadowCard.closest('div')).toHaveClass('shadow-sm');

    rerender(<Card shadow="md">Medium Shadow</Card>);
    const mediumShadowCard = screen.getByText('Medium Shadow');
    expect(mediumShadowCard.closest('div')).toHaveClass('shadow-md');

    rerender(<Card shadow="lg">Large Shadow</Card>);
    const largeShadowCard = screen.getByText('Large Shadow');
    expect(largeShadowCard.closest('div')).toHaveClass('shadow-lg');
  });

  it('applies custom className', () => {
    render(<Card className="custom-class">Custom Class</Card>);
    const card = screen.getByText('Custom Class');
    expect(card.closest('div')).toHaveClass('custom-class');
  });

  it('forwards ref correctly', () => {
    const ref = vi.fn();
    render(<Card ref={ref}>Card with Ref</Card>);
    expect(ref).toHaveBeenCalled();
  });
});

describe('CardHeader', () => {
  it('renders children correctly', () => {
    render(<CardHeader>Header Content</CardHeader>);
    expect(screen.getByText('Header Content')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    render(<CardHeader className="custom-header">Header</CardHeader>);
    const header = screen.getByText('Header');
    expect(header.closest('div')).toHaveClass('custom-header');
  });
});

describe('CardTitle', () => {
  it('renders children correctly', () => {
    render(<CardTitle>Card Title</CardTitle>);
    expect(screen.getByText('Card Title')).toBeInTheDocument();
  });

  it('renders as h3 element', () => {
    render(<CardTitle>Card Title</CardTitle>);
    expect(screen.getByRole('heading', { level: 3 })).toBeInTheDocument();
  });

  it('applies custom className', () => {
    render(<CardTitle className="custom-title">Title</CardTitle>);
    expect(screen.getByText('Title')).toHaveClass('custom-title');
  });
});

describe('CardDescription', () => {
  it('renders children correctly', () => {
    render(<CardDescription>Card Description</CardDescription>);
    expect(screen.getByText('Card Description')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    render(
      <CardDescription className="custom-desc">Description</CardDescription>
    );
    expect(screen.getByText('Description')).toHaveClass('custom-desc');
  });
});

describe('CardContent', () => {
  it('renders children correctly', () => {
    render(<CardContent>Content</CardContent>);
    expect(screen.getByText('Content')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    render(<CardContent className="custom-content">Content</CardContent>);
    const content = screen.getByText('Content');
    expect(content.closest('div')).toHaveClass('custom-content');
  });
});

describe('CardFooter', () => {
  it('renders children correctly', () => {
    render(<CardFooter>Footer Content</CardFooter>);
    expect(screen.getByText('Footer Content')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    render(<CardFooter className="custom-footer">Footer</CardFooter>);
    const footer = screen.getByText('Footer');
    expect(footer.closest('div')).toHaveClass('custom-footer');
  });
});

describe('Card Composition', () => {
  it('renders a complete card with all subcomponents', () => {
    render(
      <Card>
        <CardHeader>
          <CardTitle>Card Title</CardTitle>
          <CardDescription>Card Description</CardDescription>
        </CardHeader>
        <CardContent>Card Content</CardContent>
        <CardFooter>Card Footer</CardFooter>
      </Card>
    );

    expect(screen.getByText('Card Title')).toBeInTheDocument();
    expect(screen.getByText('Card Description')).toBeInTheDocument();
    expect(screen.getByText('Card Content')).toBeInTheDocument();
    expect(screen.getByText('Card Footer')).toBeInTheDocument();
  });
});
