# Mobile Banking Frontend

A modern React-based mobile banking application built with TypeScript, Vite, and TailwindCSS.

## 🚀 Tech Stack

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **Styling**: TailwindCSS with custom design tokens
- **State Management**: React Query (TanStack Query) + Zustand
- **Routing**: React Router v6
- **Forms**: React Hook Form with Zod validation
- **HTTP Client**: Axios
- **Testing**: Vitest + React Testing Library
- **Code Quality**: ESLint + Prettier

## 📁 Project Structure

```
src/
├── components/           # Shared UI components
│   ├── ui/              # Basic UI primitives (Button, Input, Card)
│   ├── forms/           # Form-specific components
│   ├── layout/          # Layout components (Header, Sidebar, Footer)
│   └── feedback/        # Loading, Error, Success components
├── features/            # Feature-based modules
│   ├── auth/            # Authentication feature
│   ├── dashboard/       # Dashboard feature
│   ├── wallet/          # Wallet operations feature
│   └── transactions/    # Transaction history feature
├── hooks/               # Custom React hooks
├── services/            # API services and external integrations
├── utils/               # Utility functions and helpers
├── types/               # TypeScript type definitions
├── constants/           # Application constants
└── styles/              # Global styles and Tailwind config
```

## 🛠️ Development

### Prerequisites

- Node.js 22.12.0 or higher
- npm 10.8.1 or higher

### Installation

```bash
npm install
```

### Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Fix ESLint issues
- `npm run format` - Format code with Prettier
- `npm run format:check` - Check code formatting
- `npm run type-check` - Run TypeScript type checking
- `npm run test` - Run tests once
- `npm run test:watch` - Run tests in watch mode
- `npm run test:coverage` - Run tests with coverage

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api

# App Configuration
VITE_APP_NAME=Mobile Banking
VITE_APP_VERSION=1.0.0
```

## 🎨 Design System

The application uses a custom design system built with TailwindCSS:

- **Colors**: Banking-appropriate color palette with primary, secondary, success, danger, and warning variants
- **Typography**: Inter font family with consistent sizing scale
- **Components**: Reusable UI components with consistent styling
- **Responsive**: Mobile-first responsive design

### Custom CSS Classes

- `.btn-primary` - Primary button styling
- `.btn-secondary` - Secondary button styling
- `.input-field` - Input field styling

## 🧪 Testing

The project uses Vitest and React Testing Library for testing:

- Unit tests for components and utilities
- Integration tests for features
- Accessibility testing with jest-dom

## 📝 Code Quality

- **ESLint**: Configured with TypeScript and React rules
- **Prettier**: Consistent code formatting
- **TypeScript**: Strict mode enabled for better type safety
- **Husky**: Git hooks for pre-commit checks (to be added)

## 🚀 Getting Started

1. Install dependencies: `npm install`
2. Start development server: `npm run dev`
3. Open http://localhost:5173 in your browser

## 📚 Architecture Decisions

- **Feature-based architecture**: Code organized by features rather than file types
- **Absolute imports**: Path aliases configured for cleaner imports
- **Strict TypeScript**: Enhanced type safety with strict mode
- **Component composition**: Reusable components with clear interfaces
- **Separation of concerns**: Clear separation between UI, business logic, and data

## 🔧 Configuration Files

- `vite.config.ts` - Vite configuration with path aliases and test setup
- `tailwind.config.js` - TailwindCSS configuration with custom design tokens
- `tsconfig.json` - TypeScript configuration with strict mode
- `eslint.config.js` - ESLint configuration for code quality
- `.prettierrc` - Prettier configuration for code formatting
