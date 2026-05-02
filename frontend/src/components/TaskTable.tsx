import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { StatusBadge } from './StatusBadge';
import { PriorityBadge } from './PriorityBadge';
import { Pencil, Trash2, ChevronDown } from 'lucide-react';
import { formatDate } from '@/lib/utils';
import type { TaskDto, TaskStatus } from '@/types/task';

export interface TaskTableProps {
  tasks: TaskDto[];
  loading?: boolean;
  onEdit: (task: TaskDto) => void;
  onDelete: (task: TaskDto) => void;
  onStatusChange: (task: TaskDto, status: TaskStatus) => void;
}

const STATUS_OPTIONS: { value: TaskStatus; label: string }[] = [
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'DONE', label: 'Done' },
];

export function TaskTable({ tasks, loading, onEdit, onDelete, onStatusChange }: TaskTableProps) {
  if (loading && tasks.length === 0) {
    return (
      <div className="space-y-2" data-testid="task-table-loading">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-12 animate-pulse rounded-md bg-muted" />
        ))}
      </div>
    );
  }

  if (tasks.length === 0) {
    return (
      <div
        className="flex h-48 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground"
        data-testid="task-table-empty"
      >
        No tasks yet. Create your first task to get started.
      </div>
    );
  }

  return (
    <div className="rounded-md border">
      <Table data-testid="task-table">
        <TableHeader>
          <TableRow>
            <TableHead>Title</TableHead>
            <TableHead className="hidden md:table-cell">Description</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Priority</TableHead>
            <TableHead className="hidden md:table-cell">Created</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {tasks.map((task) => (
            <TableRow key={task.id} data-testid={`task-row-${task.id}`}>
              <TableCell className="font-medium">{task.title}</TableCell>
              <TableCell className="hidden max-w-xs truncate text-muted-foreground md:table-cell">
                {task.description || '—'}
              </TableCell>
              <TableCell>
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button
                      type="button"
                      className="inline-flex items-center gap-1"
                      aria-label={`Change status for ${task.title}`}
                    >
                      <StatusBadge status={task.status} />
                      <ChevronDown className="h-3 w-3 text-muted-foreground" />
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent>
                    {STATUS_OPTIONS.map((opt) => (
                      <DropdownMenuItem
                        key={opt.value}
                        onSelect={() => onStatusChange(task, opt.value)}
                        disabled={opt.value === task.status}
                      >
                        {opt.label}
                      </DropdownMenuItem>
                    ))}
                  </DropdownMenuContent>
                </DropdownMenu>
              </TableCell>
              <TableCell>
                <PriorityBadge priority={task.priority} />
              </TableCell>
              <TableCell className="hidden text-muted-foreground md:table-cell">
                {formatDate(task.createdAt)}
              </TableCell>
              <TableCell className="text-right">
                <div className="flex justify-end gap-1">
                  <Button
                    type="button"
                    size="icon"
                    variant="ghost"
                    aria-label={`Edit ${task.title}`}
                    onClick={() => onEdit(task)}
                  >
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button
                    type="button"
                    size="icon"
                    variant="ghost"
                    aria-label={`Delete ${task.title}`}
                    onClick={() => onDelete(task)}
                  >
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
}
