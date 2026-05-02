import { describe, expect, it, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HealthIndicator } from './HealthIndicator';
import { rawAxios } from '@/lib/api';

vi.mock('@/lib/api', () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
  rawAxios: { get: vi.fn() },
}));

function renderWithClient(ui: React.ReactElement) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={qc}>{ui}</QueryClientProvider>);
}

describe('HealthIndicator', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows healthy when status UP', async () => {
    (rawAxios.get as unknown as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: { status: 'UP' },
    });
    renderWithClient(<HealthIndicator />);
    await waitFor(() => {
      expect(screen.getByTestId('health-indicator')).toHaveAttribute('data-healthy', 'true');
    });
    expect(screen.getByText('Healthy')).toBeInTheDocument();
  });

  it('shows offline on error', async () => {
    (rawAxios.get as unknown as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('boom'));
    renderWithClient(<HealthIndicator />);
    await waitFor(() => {
      expect(screen.getByText('Offline')).toBeInTheDocument();
    });
    expect(screen.getByTestId('health-indicator')).toHaveAttribute('data-healthy', 'false');
  });
});
