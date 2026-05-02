import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PriorityBadge } from './PriorityBadge';

describe('PriorityBadge', () => {
  it('renders LOW with blue color', () => {
    render(<PriorityBadge priority="LOW" />);
    const el = screen.getByTestId('priority-badge-LOW');
    expect(el).toHaveTextContent('LOW');
    expect(el.className).toMatch(/blue/);
  });

  it('renders MEDIUM with yellow color', () => {
    render(<PriorityBadge priority="MEDIUM" />);
    const el = screen.getByTestId('priority-badge-MEDIUM');
    expect(el.className).toMatch(/yellow/);
  });

  it('renders HIGH with red color', () => {
    render(<PriorityBadge priority="HIGH" />);
    const el = screen.getByTestId('priority-badge-HIGH');
    expect(el.className).toMatch(/red/);
  });
});
