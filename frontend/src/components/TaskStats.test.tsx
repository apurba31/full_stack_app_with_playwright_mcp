import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { TaskStats } from './TaskStats';
import type { TaskDto } from '@/types/task';

const t = (overrides: Partial<TaskDto>): TaskDto => ({
  id: '1',
  title: 'x',
  status: 'TODO',
  priority: 'LOW',
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
  ...overrides,
});

describe('TaskStats', () => {
  it('counts tasks by status', () => {
    const tasks: TaskDto[] = [
      t({ id: '1', status: 'TODO' }),
      t({ id: '2', status: 'TODO' }),
      t({ id: '3', status: 'IN_PROGRESS' }),
      t({ id: '4', status: 'DONE' }),
    ];
    render(<TaskStats tasks={tasks} />);
    expect(screen.getByTestId('stat-total-value')).toHaveTextContent('4');
    expect(screen.getByTestId('stat-todo-value')).toHaveTextContent('2');
    expect(screen.getByTestId('stat-in-progress-value')).toHaveTextContent('1');
    expect(screen.getByTestId('stat-done-value')).toHaveTextContent('1');
  });

  it('uses total override when provided', () => {
    render(<TaskStats tasks={[t({ id: '1' })]} total={42} />);
    expect(screen.getByTestId('stat-total-value')).toHaveTextContent('42');
  });
});
