import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import type { TaskPriority } from '@/types/task';

const PRIORITY_CLASSES: Record<TaskPriority, string> = {
  LOW: 'bg-blue-100 text-blue-800 hover:bg-blue-100',
  MEDIUM: 'bg-yellow-100 text-yellow-800 hover:bg-yellow-100',
  HIGH: 'bg-red-100 text-red-800 hover:bg-red-100',
};

export interface PriorityBadgeProps {
  priority: TaskPriority;
  className?: string;
}

export function PriorityBadge({ priority, className }: PriorityBadgeProps) {
  return (
    <Badge
      data-testid={`priority-badge-${priority}`}
      className={cn('border-transparent', PRIORITY_CLASSES[priority], className)}
    >
      {priority}
    </Badge>
  );
}
