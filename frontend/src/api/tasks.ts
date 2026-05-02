import { api } from '@/lib/api';
import type {
  CreateTaskInput,
  ListTasksParams,
  Page,
  TaskDto,
  TaskStatus,
  UpdateTaskInput,
} from '@/types/task';

export async function listTasks(params: ListTasksParams = {}): Promise<Page<TaskDto>> {
  const query: Record<string, string | number> = {
    page: params.page ?? 0,
    size: params.size ?? 20,
    sort: params.sort ?? 'createdAt,desc',
  };
  if (params.status) query.status = params.status;
  if (params.priority) query.priority = params.priority;
  if (params.q && params.q.trim()) query.q = params.q.trim();

  const { data } = await api.get<Page<TaskDto>>('/v1/tasks', { params: query });
  return data;
}

export async function getTask(id: string): Promise<TaskDto> {
  const { data } = await api.get<TaskDto>(`/v1/tasks/${id}`);
  return data;
}

export async function createTask(input: CreateTaskInput): Promise<TaskDto> {
  const { data } = await api.post<TaskDto>('/v1/tasks', input);
  return data;
}

export async function updateTask(id: string, input: UpdateTaskInput): Promise<TaskDto> {
  const { data } = await api.put<TaskDto>(`/v1/tasks/${id}`, input);
  return data;
}

export async function patchStatus(id: string, status: TaskStatus): Promise<TaskDto> {
  const { data } = await api.patch<TaskDto>(`/v1/tasks/${id}/status`, { status });
  return data;
}

export async function deleteTask(id: string): Promise<void> {
  await api.delete(`/v1/tasks/${id}`);
}
