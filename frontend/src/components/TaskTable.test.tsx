import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { TaskTable } from './TaskTable';
import type { TaskDto } from '@/types/task';

const tasks: TaskDto[] = [
  {
    id: '1',
    title: 'First task',
    description: 'desc 1',
    status: 'TODO',
    priority: 'HIGH',
    createdAt: '2025-01-01T10:00:00Z',
    updatedAt: '2025-01-01T10:00:00Z',
  },
  {
    id: '2',
    title: 'Second task',
    description: '',
    status: 'DONE',
    priority: 'LOW',
    createdAt: '2025-01-02T10:00:00Z',
    updatedAt: '2025-01-02T10:00:00Z',
  },
];

describe('TaskTable', () => {
  it('renders rows', () => {
    render(
      <TaskTable
        tasks={tasks}
        onEdit={vi.fn()}
        onDelete={vi.fn()}
        onStatusChange={vi.fn()}
      />,
    );
    expect(screen.getByText('First task')).toBeInTheDocument();
    expect(screen.getByText('Second task')).toBeInTheDocument();
    expect(screen.getByTestId('task-row-1')).toBeInTheDocument();
  });

  it('shows empty state', () => {
    render(
      <TaskTable tasks={[]} onEdit={vi.fn()} onDelete={vi.fn()} onStatusChange={vi.fn()} />,
    );
    expect(screen.getByTestId('task-table-empty')).toBeInTheDocument();
  });

  it('calls onEdit and onDelete', () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    render(
      <TaskTable
        tasks={tasks}
        onEdit={onEdit}
        onDelete={onDelete}
        onStatusChange={vi.fn()}
      />,
    );
    fireEvent.click(screen.getByRole('button', { name: /edit first task/i }));
    expect(onEdit).toHaveBeenCalledWith(tasks[0]);
    fireEvent.click(screen.getByRole('button', { name: /delete second task/i }));
    expect(onDelete).toHaveBeenCalledWith(tasks[1]);
  });
});
