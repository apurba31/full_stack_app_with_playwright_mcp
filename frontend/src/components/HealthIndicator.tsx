import { useHealth } from '@/hooks/useHealth';
import { cn } from '@/lib/utils';

export function HealthIndicator() {
  const { data, isError, isLoading } = useHealth();
  const healthy = !isError && data?.status === 'UP';
  const label = isLoading ? 'Checking…' : healthy ? 'Healthy' : 'Offline';

  return (
    <div
      className="flex items-center gap-2 text-sm text-muted-foreground"
      data-testid="health-indicator"
      data-healthy={healthy ? 'true' : 'false'}
    >
      <span
        className={cn(
          'inline-block h-2.5 w-2.5 rounded-full',
          healthy ? 'bg-emerald-500' : 'bg-red-500',
          isLoading && 'animate-pulse',
        )}
        aria-hidden
      />
      <span>{label}</span>
    </div>
  );
}
