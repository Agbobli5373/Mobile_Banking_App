import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Input } from '../Input';
import { describe, it, expect, vi } from 'vitest';

describe('Input', () => {
  it('renders with label', () => {
    render(<Input label="Test Label" />);

    expect(screen.getByLabelText('Test Label')).toBeInTheDocument();
    expect(screen.getByText('Test Label')).toBeInTheDocument();
  });

  it('shows required indicator when required', () => {
    render(<Input label="Required Field" required />);

    expect(screen.getByText('*')).toBeInTheDocument();
    expect(screen.getByText('*')).toHaveClass('text-danger-500');
  });

  it('displays error message and styling', () => {
    render(<Input label="Test Input" error="This field is required" />);

    const input = screen.getByLabelText('Test Input');
    const errorMessage = screen.getByText('This field is required');

    expect(errorMessage).toBeInTheDocument();
    expect(errorMessage).toHaveAttribute('role', 'alert');
    expect(input).toHaveClass('border-danger-300', 'focus:ring-danger-500');
    expect(input).toHaveAttribute('aria-invalid', 'true');
  });

  it('displays helper text when no error', () => {
    render(<Input label="Test Input" helperText="This is helper text" />);

    expect(screen.getByText('This is helper text')).toBeInTheDocument();
    expect(screen.getByText('This is helper text')).toHaveClass(
      'text-secondary-500'
    );
  });

  it('prioritizes error over helper text', () => {
    render(
      <Input
        label="Test Input"
        error="Error message"
        helperText="Helper text"
      />
    );

    expect(screen.getByText('Error message')).toBeInTheDocument();
    expect(screen.queryByText('Helper text')).not.toBeInTheDocument();
  });

  it('handles user input', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();

    render(<Input label="Test Input" onChange={handleChange} />);

    const input = screen.getByLabelText('Test Input');
    await user.type(input, 'test value');

    expect(handleChange).toHaveBeenCalled();
    expect(input).toHaveValue('test value');
  });

  it('applies different input types', () => {
    const { rerender } = render(<Input label="Text Input" type="text" />);
    expect(screen.getByLabelText('Text Input')).toHaveAttribute('type', 'text');

    rerender(<Input label="Password Input" type="password" />);
    expect(screen.getByLabelText('Password Input')).toHaveAttribute(
      'type',
      'password'
    );

    rerender(<Input label="Tel Input" type="tel" />);
    expect(screen.getByLabelText('Tel Input')).toHaveAttribute('type', 'tel');

    rerender(<Input label="Number Input" type="number" />);
    expect(screen.getByLabelText('Number Input')).toHaveAttribute(
      'type',
      'number'
    );
  });

  it('shows error icon when there is an error', () => {
    render(<Input label="Test Input" error="Error message" />);

    // Check for AlertCircle icon in the input container
    const errorIcons = screen
      .getByText('Error message')
      .parentElement?.querySelectorAll('svg');
    expect(errorIcons).toHaveLength(2); // One in input container, one in error message
  });

  it('has proper accessibility attributes', () => {
    render(<Input label="Accessible Input" error="Error message" />);

    const input = screen.getByLabelText('Accessible Input');
    expect(input).toHaveAttribute('aria-invalid', 'true');
    expect(input).toHaveAttribute('aria-describedby');
  });

  it('generates unique IDs for multiple inputs', () => {
    render(
      <div>
        <Input label="Input 1" />
        <Input label="Input 2" />
      </div>
    );

    const input1 = screen.getByLabelText('Input 1');
    const input2 = screen.getByLabelText('Input 2');

    expect(input1.id).not.toBe(input2.id);
    expect(input1.id).toBeTruthy();
    expect(input2.id).toBeTruthy();
  });

  it('uses provided ID when given', () => {
    render(<Input label="Custom ID Input" id="custom-id" />);

    const input = screen.getByLabelText('Custom ID Input');
    expect(input).toHaveAttribute('id', 'custom-id');
  });

  it('forwards ref correctly', () => {
    const ref = vi.fn();
    render(<Input label="Ref Input" ref={ref} />);
    expect(ref).toHaveBeenCalled();
  });

  it('applies custom className', () => {
    render(<Input label="Custom Class Input" className="custom-class" />);
    expect(screen.getByLabelText('Custom Class Input')).toHaveClass(
      'custom-class'
    );
  });
});
