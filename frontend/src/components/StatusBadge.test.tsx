import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatusBadge } from './StatusBadge';

describe('StatusBadge', () => {
  it('renders TODO with To Do label and slate color', () => {
    render(<StatusBadge status="TODO" />);
    const el = screen.getByTestId('status-badge-TODO');
    expect(el).toHaveTextContent('To Do');
    expect(el.className).toMatch(/slate/);
  });

  it('renders IN_PROGRESS with amber color', () => {
    render(<StatusBadge status="IN_PROGRESS" />);
    const el = screen.getByTestId('status-badge-IN_PROGRESS');
    expect(el).toHaveTextContent('In Progress');
    expect(el.className).toMatch(/amber/);
  });

  it('renders DONE with emerald color', () => {
    render(<StatusBadge status="DONE" />);
    const el = screen.getByTestId('status-badge-DONE');
    expect(el).toHaveTextContent('Done');
    expect(el.className).toMatch(/emerald/);
  });
});
