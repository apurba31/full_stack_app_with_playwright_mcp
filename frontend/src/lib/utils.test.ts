import { describe, expect, it } from 'vitest';
import { cn, formatDate } from './utils';

describe('cn', () => {
  it('merges class names', () => {
    expect(cn('a', 'b')).toContain('a');
    expect(cn('a', 'b')).toContain('b');
  });
  it('handles falsey values', () => {
    expect(cn('a', false, null, undefined, 'b')).toContain('a');
  });
});

describe('formatDate', () => {
  it('returns empty string for undefined', () => {
    expect(formatDate(undefined)).toBe('');
  });
  it('returns input for invalid ISO', () => {
    expect(formatDate('not-a-date')).toBe('not-a-date');
  });
  it('formats ISO date to a localized string', () => {
    const result = formatDate('2025-01-15T10:00:00Z');
    expect(result).toMatch(/2025/);
  });
});
