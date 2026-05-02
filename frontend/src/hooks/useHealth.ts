import { useQuery } from '@tanstack/react-query';
import { rawAxios } from '@/lib/api';

export interface HealthStatus {
  status: string;
}

async function fetchHealth(): Promise<HealthStatus> {
  const { data } = await rawAxios.get<HealthStatus>('/actuator/health', {
    timeout: 4000,
  });
  return data;
}

export function useHealth() {
  return useQuery({
    queryKey: ['health'],
    queryFn: fetchHealth,
    refetchInterval: 10000,
    retry: 0,
  });
}
