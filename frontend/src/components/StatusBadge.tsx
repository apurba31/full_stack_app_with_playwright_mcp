import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { TaskStatus } from '@/types/task';

const STATUS_LABEL: Record<TaskStatus, string> = {
  TODO: 'To Do',
  IN_PROGRESS: 'In Progress',
  DONE: 'Done',
};

const STATUS_CLASSES: Record<TaskStatus, string> = {
  TODO: 'bg-slate-200 text-slate-800 hover:bg-slate-200',
  IN_PROGRESS: 'bg-amber-200 text-amber-900 hover:bg-amber-200',
  DONE: 'bg-emerald-200 text-emerald-900 hover:bg-emerald-200',
};

export interface StatusBadgeProps {
  status: TaskStatus;
  className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  return (
    <Badge
      data-testid={`status-badge-${status}`}
      className={cn('border-transparent', STATUS_CLASSES[status], className)}
    >
      {STATUS_LABEL[status]}
    </Badge>
  );
}
