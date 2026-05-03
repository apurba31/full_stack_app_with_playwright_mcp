import { describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';

vi.mock('@/api/tasks', () => ({
  listTasks: vi.fn().mockResolvedValue({
    content: [],
    totalElements: 0,
    totalPages: 0,
    number: 0,
    size: 20,
  }),
  createTask: vi.fn(),
  updateTask: vi.fn(),
  patchStatus: vi.fn(),
  deleteTask: vi.fn(),
  getTask: vi.fn(),
}));

vi.mock('@/lib/api', () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
  rawAxios: { get: vi.fn().mockResolvedValue({ data: { status: 'UP' } }) },
}));

import App from './App';

describe('App', () => {
  it('renders the dashboard route at /', async () => {
    window.history.pushState({}, '', '/');
    render(<App />);
    await waitFor(() => {
      expect(screen.getByText('FullStack Tasks')).toBeInTheDocument();
    });
  });

  it('redirects unknown routes to /', async () => {
    window.history.pushState({}, '', '/some-unknown-path');
    render(<App />);
    await waitFor(() => {
      expect(screen.getByText('FullStack Tasks')).toBeInTheDocument();
    });
    // Confirm we're at root
    expect(window.location.pathname).toBe('/');
  });
});
