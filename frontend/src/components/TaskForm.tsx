import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import type { TaskDto, TaskPriority, TaskStatus } from '@/types/task';

const formSchema = z.object({
  title: z.string().trim().min(1, 'Title is required').max(200, 'Title too long'),
  description: z.string().max(2000).optional().or(z.literal('')),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH']),
  status: z.enum(['TODO', 'IN_PROGRESS', 'DONE']),
});

export type TaskFormValues = z.infer<typeof formSchema>;

export interface TaskFormProps {
  initial?: Partial<TaskDto>;
  showStatus?: boolean;
  submitLabel?: string;
  onSubmit: (values: TaskFormValues) => void | Promise<void>;
  onCancel?: () => void;
  submitting?: boolean;
}

export function TaskForm({
  initial,
  showStatus = false,
  submitLabel = 'Save',
  onSubmit,
  onCancel,
  submitting,
}: TaskFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
  } = useForm<TaskFormValues>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      title: initial?.title ?? '',
      description: initial?.description ?? '',
      priority: (initial?.priority as TaskPriority) ?? 'MEDIUM',
      status: (initial?.status as TaskStatus) ?? 'TODO',
    },
  });

  useEffect(() => {
    reset({
      title: initial?.title ?? '',
      description: initial?.description ?? '',
      priority: (initial?.priority as TaskPriority) ?? 'MEDIUM',
      status: (initial?.status as TaskStatus) ?? 'TODO',
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initial?.id]);

  const priority = watch('priority');
  const status = watch('status');

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
      <div className="space-y-2">
        <Label htmlFor="task-title">Title</Label>
        <Input id="task-title" placeholder="Short, clear title" {...register('title')} />
        {errors.title && (
          <p role="alert" className="text-xs text-destructive">
            {errors.title.message}
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="task-description">Description</Label>
        <Textarea
          id="task-description"
          placeholder="What needs to be done? (optional)"
          rows={3}
          {...register('description')}
        />
      </div>

      <div className={showStatus ? 'grid grid-cols-2 gap-4' : ''}>
        <div className="space-y-2">
          <Label>Priority</Label>
          <Select value={priority} onValueChange={(v) => setValue('priority', v as TaskPriority)}>
            <SelectTrigger aria-label="Priority">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="LOW">Low</SelectItem>
              <SelectItem value="MEDIUM">Medium</SelectItem>
              <SelectItem value="HIGH">High</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {showStatus && (
          <div className="space-y-2">
            <Label>Status</Label>
            <Select value={status} onValueChange={(v) => setValue('status', v as TaskStatus)}>
              <SelectTrigger aria-label="Status">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="TODO">To Do</SelectItem>
                <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
                <SelectItem value="DONE">Done</SelectItem>
              </SelectContent>
            </Select>
          </div>
        )}
      </div>

      <div className="flex justify-end gap-2 pt-2">
        {onCancel && (
          <Button type="button" variant="outline" onClick={onCancel} disabled={submitting}>
            Cancel
          </Button>
        )}
        <Button type="submit" disabled={submitting}>
          {submitting ? 'Saving…' : submitLabel}
        </Button>
      </div>
    </form>
  );
}
