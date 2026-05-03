import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
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

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
  Toaster: () => null,
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

const mockedTasksApi = tasksApi as unknown as Record<string, ReturnType<typeof vi.fn>>;

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (rawAxios.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: { status: 'UP' },
    });
    mockedTasksApi.listTasks.mockResolvedValue(samplePage);
    mockedTasksApi.createTask.mockResolvedValue({ ...samplePage.content[0], id: 'new1', title: 'Created' });
    mockedTasksApi.updateTask.mockResolvedValue(samplePage.content[0]);
    mockedTasksApi.patchStatus.mockResolvedValue(samplePage.content[0]);
    mockedTasksApi.deleteTask.mockResolvedValue(undefined);
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

  it('shows error state when listTasks fails', async () => {
    mockedTasksApi.listTasks.mockRejectedValueOnce(new Error('boom'));
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByTestId('error-state')).toBeInTheDocument();
    });
  });

  it('opens the create dialog when clicking New Task', async () => {
    const user = userEvent.setup();
    renderDashboard();
    await waitFor(() => expect(screen.getByText('Buy milk')).toBeInTheDocument());
    await user.click(screen.getByRole('button', { name: /new task/i }));
    expect(await screen.findByRole('dialog')).toBeInTheDocument();
    expect(screen.getByText(/Add a new task to your board/i)).toBeInTheDocument();
  });

  it('submits create form and calls createTask', async () => {
    const user = userEvent.setup();
    renderDashboard();
    await waitFor(() => expect(screen.getByText('Buy milk')).toBeInTheDocument());
    await user.click(screen.getByRole('button', { name: /new task/i }));
    const dialog = await screen.findByRole('dialog');
    const titleInput = within(dialog).getByLabelText(/title/i);
    await user.type(titleInput, 'New thing');
    await user.click(within(dialog).getByRole('button', { name: /create/i }));
    await waitFor(() => {
      expect(mockedTasksApi.createTask).toHaveBeenCalledWith(
        expect.objectContaining({ title: 'New thing' }),
      );
    });
  });

  it('shows error toast when createTask fails', async () => {
    mockedTasksApi.createTask.mockRejectedValueOnce(new Error('nope'));
    const { toast } = await import('sonner');
    const user = userEvent.setup();
    renderDashboard();
    await waitFor(() => expect(screen.getByText('Buy milk')).toBeInTheDocument());
    await user.click(screen.getByRole('button', { name: /new task/i }));
    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText(/title/i), 'Will fail');
    await user.click(within(dialog).getByRole('button', { name: /create/i }));
    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('Failed to create task');
    });
  });

  it('clicking edit on a row opens the edit dialog populated with task title', async () => {
    const user = userEvent.setup();
    renderDashboard();
    await waitFor(() => expect(screen.getByText('Buy milk')).toBeInTheDocument());
    const editButtons = screen.getAllByRole('button', { name: /edit/i });
    await user.click(editButtons[0]);
    const dialog = await screen.findByRole('dialog');
    expect(within(dialog).getByText(/Edit Task/i)).toBeInTheDocument();
    expect(within(dialog).getByDisplayValue('Buy milk')).toBeInTheDocument();
  });

  it('clicking delete opens confirm and calling Delete invokes deleteTask', async () => {
    const user = userEvent.setup();
    renderDashboard();
    await waitFor(() => expect(screen.getByText('Buy milk')).toBeInTheDocument());
    const deleteButtons = screen.getAllByRole('button', { name: /delete/i });
    await user.click(deleteButtons[0]);
    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: /^Delete$/ }));
    await waitFor(() => {
      expect(mockedTasksApi.deleteTask).toHaveBeenCalledWith('a1');
    });
  });

  it('renders pagination when totalPages > 1', async () => {
    mockedTasksApi.listTasks.mockResolvedValueOnce({
      ...samplePage,
      totalPages: 3,
      totalElements: 50,
    });
    renderDashboard();
    await waitFor(() => expect(screen.getByText(/Page 1 of 3/i)).toBeInTheDocument());
    expect(screen.getByRole('button', { name: /next/i })).toBeEnabled();
    expect(screen.getByRole('button', { name: /prev/i })).toBeDisabled();
  });

  it('triggers refetch with page=0 when filter changes', async () => {
    const user = userEvent.setup();
    renderDashboard();
    await waitFor(() => expect(screen.getByText('Buy milk')).toBeInTheDocument());
    const searchInput = screen.getByPlaceholderText(/search/i);
    await user.type(searchInput, 'milk');
    await waitFor(() => {
      const lastCall = mockedTasksApi.listTasks.mock.calls.at(-1)?.[0];
      expect(lastCall?.q).toBe('milk');
      expect(lastCall?.page).toBe(0);
    });
  });
});
