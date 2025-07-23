import { afterEach, beforeEach, vi } from 'vitest';
import '@testing-library/jest-dom/vitest';

// Mock localStorage
const localStorageMock = (() => {
    let store: Record<string, string> = {};

    return {
        getItem: (key: string) => store[key] || null,
        setItem: (key: string, value: string) => {
            store[key] = value.toString();
        },
        removeItem: (key: string) => {
            delete store[key];
        },
        clear: () => {
            store = {};
        },
    };
})();

// Mock environment variables
vi.stubGlobal('localStorage', localStorageMock);

vi.mock('axios', async () => {
    const actual = await vi.importActual('axios');
    return {
        ...actual,
        create: vi.fn(() => ({
            interceptors: {
                request: { use: vi.fn(), eject: vi.fn() },
                response: { use: vi.fn(), eject: vi.fn() },
            },
            get: vi.fn(),
            post: vi.fn(),
            put: vi.fn(),
            delete: vi.fn(),
        })),
    };
});

// Reset mocks between tests
beforeEach(() => {
    localStorage.clear();
});

afterEach(() => {
    vi.clearAllMocks();
});