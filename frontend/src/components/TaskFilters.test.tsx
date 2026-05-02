import { describe, expect, it, vi } from 'vitest';
import { act, fireEvent, render, screen } from '@testing-library/react';
import { TaskFilters, type TaskFilterValues } from './TaskFilters';

const baseValue: TaskFilterValues = { status: '', priority: '', q: '' };

describe('TaskFilters', () => {
  it('debounces search input', () => {
    vi.useFakeTimers();
    const onChange = vi.fn();
    render(<TaskFilters value={baseValue} onChange={onChange} debounceMs={300} />);
    const input = screen.getByLabelText('Search tasks');
    fireEvent.change(input, { target: { value: 'hello' } });
    expect(onChange).not.toHaveBeenCalled();
    act(() => {
      vi.advanceTimersByTime(310);
    });
    expect(onChange).toHaveBeenCalledWith({ status: '', priority: '', q: 'hello' });
    vi.useRealTimers();
  });
});
