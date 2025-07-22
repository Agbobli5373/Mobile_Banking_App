import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('App', () => {
  it('renders the mobile banking app', () => {
    render(<App />);
    expect(screen.getByText('Mobile Banking')).toBeInTheDocument();
    expect(
      screen.getByText('Welcome to your mobile banking application')
    ).toBeInTheDocument();
  });

  it('renders action buttons', () => {
    render(<App />);
    expect(
      screen.getByRole('button', { name: 'Get Started' })
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: 'Learn More' })
    ).toBeInTheDocument();
  });
});
