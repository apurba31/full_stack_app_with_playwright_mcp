import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Dashboard } from './Dashboard';
import * as tasksApi from '@/api/tasks';
import { rawAxios } from '@/lib/api';
import type { Page, TaskDto } from '@/types/task';

vi.mock('@/api/tasks');
vi.mock('@/lib/api', () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
  rawAxios: { get: vi.fn() },
}));

function renderDashboard() {
  const qc = new QueryClient({
    defaultOptions: { queries: { retry: false, refetchInterval: false } },
  });
  return render(
    <QueryClientProvider client={qc}>
      <Dashboard />
    </QueryClientProvider>,
  );
}

const samplePage: Page<TaskDto> = {
  content: [
    {
      id: 'a1',
      title: 'Buy milk',
      description: 'whole',
      status: 'TODO',
      priority: 'HIGH',
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-01T00:00:00Z',
    },
    {
      id: 'a2',
      title: 'Ship feature',
      description: '',
      status: 'IN_PROGRESS',
      priority: 'MEDIUM',
      createdAt: '2025-01-02T00:00:00Z',
      updatedAt: '2025-01-02T00:00:00Z',
    },
  ],
  totalElements: 2,
  totalPages: 1,
  number: 0,
  size: 20,
};

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (rawAxios.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: { status: 'UP' },
    });
    (tasksApi.listTasks as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(samplePage);
  });

  it('renders header, stats and tasks once query resolves', async () => {
    renderDashboard();
    expect(screen.getByText('FullStack Tasks')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Buy milk')).toBeInTheDocument();
    });
    expect(screen.getByText('Ship feature')).toBeInTheDocument();
    expect(screen.getByTestId('stat-total-value')).toHaveTextContent('2');
    expect(screen.getByTestId('stat-todo-value')).toHaveTextContent('1');
    expect(screen.getByTestId('stat-in-progress-value')).toHaveTextContent('1');
  });
});
