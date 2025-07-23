// import { render, screen, fireEvent } from '@testing-library/react';
// import { Sidebar } from '../Sidebar';
// import { vi, describe, it, expect, beforeEach } from 'vitest';

// // Mock react-router-dom
// vi.mock('react-router-dom', async () => {
//   const actual = await vi.importActual('react-router-dom');
//   return {
//     ...actual,
//     NavLink: ({
//       to,
//       children,
//       className,
//       onClick,
//     }: {
//       to: string;
//       children: React.ReactNode;
//       className?: string | (({ isActive }: { isActive: boolean }) => string);
//       onClick?: () => void;
//     }) => {
//       const isActive = to === '/dashboard';
//       const classes =
//         typeof className === 'function' ? className({ isActive }) : className;
//       return (
//         <a
//           href={to}
//           className={classes}
//           data-testid={`navlink-${to}`}
//           onClick={onClick}
//         >
//           {children}
//         </a>
//       );
//     },
//   };
// });

// describe('Sidebar', () => {
//   const mockOnClose = vi.fn();

//   beforeEach(() => {
//     mockOnClose.mockClear();
//     vi.spyOn(document, 'addEventListener');
//     vi.spyOn(document, 'removeEventListener');

//     // Mock window.innerWidth
//     Object.defineProperty(window, 'innerWidth', {
//       writable: true,
//       configurable: true,
//       value: 1024, // Desktop by default
//     });
//   });

//   it('renders the sidebar with navigation items', () => {
//     render(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     // Check for navigation items
//     expect(screen.getByText('Dashboard')).toBeInTheDocument();
//     expect(screen.getByText('Transactions')).toBeInTheDocument();
//     expect(screen.getByText('Transfer Money')).toBeInTheDocument();
//     expect(screen.getByText('Add Funds')).toBeInTheDocument();
//     expect(screen.getByText('Settings')).toBeInTheDocument();

//     // Check for support section
//     expect(screen.getByText('Support & Info')).toBeInTheDocument();
//     expect(screen.getByText('Help Center')).toBeInTheDocument();
//     expect(screen.getByText('Contact Us')).toBeInTheDocument();
//     expect(screen.getByText('Terms of Service')).toBeInTheDocument();
//     expect(screen.getByText('Privacy Policy')).toBeInTheDocument();
//   });

//   it('applies correct styling based on isOpen prop', () => {
//     const { rerender } = render(
//       <Sidebar isOpen={false} onClose={mockOnClose} />
//     );

//     // When closed, sidebar should have -translate-x-full class
//     const closedSidebar = screen.getByRole('complementary');
//     expect(closedSidebar.className).toContain('-translate-x-full');

//     // Rerender with isOpen=true
//     rerender(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     // When open, sidebar should have translate-x-0 class
//     const openSidebar = screen.getByRole('complementary');
//     expect(openSidebar.className).toContain('translate-x-0');
//   });

//   it('renders overlay when sidebar is open on mobile', () => {
//     // Set window width to mobile size
//     Object.defineProperty(window, 'innerWidth', {
//       value: 768,
//     });

//     const { rerender } = render(
//       <Sidebar isOpen={true} onClose={mockOnClose} />
//     );

//     // Overlay should be present
//     const overlay = screen.getByTestId('sidebar-overlay');
//     expect(overlay).toBeInTheDocument();

//     // Rerender with isOpen=false
//     rerender(<Sidebar isOpen={false} onClose={mockOnClose} />);

//     // Overlay should not be present
//     expect(screen.queryByTestId('sidebar-overlay')).not.toBeInTheDocument();
//   });

//   it('calls onClose when close button is clicked', () => {
//     render(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     const closeButton = screen.getByLabelText('Close sidebar');
//     fireEvent.click(closeButton);

//     expect(mockOnClose).toHaveBeenCalledTimes(1);
//   });

//   it('calls onClose when overlay is clicked', () => {
//     // Set window width to mobile size
//     Object.defineProperty(window, 'innerWidth', {
//       value: 768,
//     });

//     render(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     const overlay = screen.getByTestId('sidebar-overlay');
//     fireEvent.click(overlay);

//     expect(mockOnClose).toHaveBeenCalledTimes(1);
//   });

//   it('calls onClose when Escape key is pressed', () => {
//     render(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     // Simulate Escape key press
//     fireEvent.keyDown(document, { key: 'Escape' });

//     expect(mockOnClose).toHaveBeenCalledTimes(1);
//   });

//   it('closes sidebar when clicking a navigation link on mobile', () => {
//     // Set window width to mobile size
//     Object.defineProperty(window, 'innerWidth', {
//       value: 768,
//     });

//     render(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     // Click a navigation link
//     const dashboardLink = screen.getByTestId('navlink-/dashboard');
//     fireEvent.click(dashboardLink);

//     expect(mockOnClose).toHaveBeenCalledTimes(1);
//   });

//   it('does not close sidebar when clicking a navigation link on desktop', () => {
//     // Set window width to desktop size
//     Object.defineProperty(window, 'innerWidth', {
//       value: 1200,
//     });

//     render(<Sidebar isOpen={true} onClose={mockOnClose} />);

//     // Click a navigation link
//     const dashboardLink = screen.getByTestId('navlink-/dashboard');
//     fireEvent.click(dashboardLink);

//     expect(mockOnClose).not.toHaveBeenCalled();
//   });
// });
