// import { render, screen } from '@testing-library/react';
// import { BrowserRouter } from 'react-router-dom';
// import { AppLayout } from '../AppLayout';
// import { it, vi, describe, expect } from 'vitest';

// // Mock the child components
// vi.mock('../Header', () => ({
//   Header: ({ onMenuToggle }: { onMenuToggle: () => void }) => (
//     <header data-testid="header">
//       <button onClick={onMenuToggle} data-testid="menu-toggle">
//         Toggle Menu
//       </button>
//     </header>
//   ),
// }));

// vi.mock('../Sidebar', () => ({
//   Sidebar: ({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) => (
//     <div data-testid="sidebar" data-is-open={isOpen}>
//       <button onClick={onClose} data-testid="close-sidebar">
//         Close Sidebar
//       </button>
//     </div>
//   ),
// }));

// vi.mock('../Footer', () => ({
//   Footer: () => <footer data-testid="footer">Footer</footer>,
// }));

// vi.mock('react-router-dom', async () => {
//   const actual = await vi.importActual('react-router-dom');
//   return {
//     ...actual,
//     Outlet: () => <div data-testid="outlet">Outlet Content</div>,
//   };
// });

// describe('AppLayout', () => {
//   it('renders the layout with header, sidebar, main content, and footer', () => {
//     render(
//       <BrowserRouter>
//         <AppLayout />
//       </BrowserRouter>
//     );

//     expect(screen.getByTestId('header')).toBeInTheDocument();
//     expect(screen.getByTestId('sidebar')).toBeInTheDocument();
//     expect(screen.getByTestId('outlet')).toBeInTheDocument();
//     expect(screen.getByTestId('footer')).toBeInTheDocument();
//   });

//   it('toggles sidebar when menu button is clicked', async () => {
//     render(
//       <BrowserRouter>
//         <AppLayout />
//       </BrowserRouter>
//     );

//     const sidebar = screen.getByTestId('sidebar');
//     expect(sidebar.getAttribute('data-is-open')).toBe('false');

//     // Click the menu toggle button
//     const menuToggleButton = screen.getByTestId('menu-toggle');
//     await menuToggleButton.click();

//     // Sidebar should now be open
//     expect(sidebar.getAttribute('data-is-open')).toBe('true');

//     // Click the close sidebar button
//     const closeSidebarButton = screen.getByTestId('close-sidebar');
//     await closeSidebarButton.click();

//     // Sidebar should now be closed again
//     expect(sidebar.getAttribute('data-is-open')).toBe('false');
//   });

//   it('applies custom className when provided', () => {
//     render(
//       <BrowserRouter>
//         <AppLayout className="custom-class" />
//       </BrowserRouter>
//     );

//     const layoutContainer =
//       screen.getByTestId('outlet').parentElement?.parentElement;
//     expect(layoutContainer).toHaveClass('custom-class');
//   });
// });
