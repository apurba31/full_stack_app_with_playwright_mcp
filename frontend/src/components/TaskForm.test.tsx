import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { TaskForm } from './TaskForm';

describe('TaskForm', () => {
  it('shows required error when title is empty', async () => {
    const onSubmit = vi.fn();
    render(<TaskForm onSubmit={onSubmit} submitLabel="Create" />);
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(/title is required/i);
    });
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('submits parsed values', async () => {
    const onSubmit = vi.fn();
    render(
      <TaskForm
        onSubmit={onSubmit}
        submitLabel="Create"
        initial={{ priority: 'HIGH', status: 'TODO' }}
      />,
    );
    fireEvent.change(screen.getByLabelText(/title/i), { target: { value: 'Write tests' } });
    fireEvent.change(screen.getByLabelText(/description/i), {
      target: { value: 'Cover the form' },
    });
    fireEvent.click(screen.getByRole('button', { name: /create/i }));

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledTimes(1);
    });
    const submitted = onSubmit.mock.calls[0][0];
    expect(submitted.title).toBe('Write tests');
    expect(submitted.description).toBe('Cover the form');
    expect(submitted.priority).toBe('HIGH');
    expect(submitted.status).toBe('TODO');
  });
});
