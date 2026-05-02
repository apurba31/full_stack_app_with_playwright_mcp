import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createTask,
  deleteTask,
  listTasks,
  patchStatus,
  updateTask,
} from '@/api/tasks';
import type {
  CreateTaskInput,
  ListTasksParams,
  TaskStatus,
  UpdateTaskInput,
} from '@/types/task';

export const TASKS_QUERY_KEY = ['tasks'] as const;

export function useTasksQuery(params: ListTasksParams) {
  return useQuery({
    queryKey: [...TASKS_QUERY_KEY, params],
    queryFn: () => listTasks(params),
    refetchInterval: 5000,
    refetchIntervalInBackground: false,
    placeholderData: (prev) => prev,
  });
}

export function useCreateTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateTaskInput) => createTask(input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: TASKS_QUERY_KEY });
    },
  });
}

export function useUpdateTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, input }: { id: string; input: UpdateTaskInput }) =>
      updateTask(id, input),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: TASKS_QUERY_KEY });
    },
  });
}

export function useUpdateStatus() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: TaskStatus }) =>
      patchStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: TASKS_QUERY_KEY });
    },
  });
}

export function useDeleteTask() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteTask(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: TASKS_QUERY_KEY });
    },
  });
}
