import { useEffect, useRef, useState } from 'react';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Search } from 'lucide-react';
import type { TaskPriority, TaskStatus } from '@/types/task';

export interface TaskFilterValues {
  status: TaskStatus | '';
  priority: TaskPriority | '';
  q: string;
}

export interface TaskFiltersProps {
  value: TaskFilterValues;
  onChange: (next: TaskFilterValues) => void;
  debounceMs?: number;
}

const ALL = '__ALL__';

export function TaskFilters({ value, onChange, debounceMs = 300 }: TaskFiltersProps) {
  const [localQ, setLocalQ] = useState(value.q);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    setLocalQ(value.q);
  }, [value.q]);

  function handleSearchChange(next: string) {
    setLocalQ(next);
    if (timer.current) clearTimeout(timer.current);
    timer.current = setTimeout(() => {
      onChange({ ...value, q: next });
    }, debounceMs);
  }

  return (
    <div className="flex flex-col gap-3 md:flex-row md:items-center">
      <div className="relative flex-1">
        <Search
          className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden
        />
        <Input
          placeholder="Search tasks…"
          aria-label="Search tasks"
          className="pl-9"
          value={localQ}
          onChange={(e) => handleSearchChange(e.target.value)}
        />
      </div>

      <div className="flex gap-3">
        <Select
          value={value.status === '' ? ALL : value.status}
          onValueChange={(v) =>
            onChange({ ...value, status: v === ALL ? '' : (v as TaskStatus) })
          }
        >
          <SelectTrigger className="w-40" aria-label="Filter by status">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL}>All statuses</SelectItem>
            <SelectItem value="TODO">To Do</SelectItem>
            <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
            <SelectItem value="DONE">Done</SelectItem>
          </SelectContent>
        </Select>

        <Select
          value={value.priority === '' ? ALL : value.priority}
          onValueChange={(v) =>
            onChange({ ...value, priority: v === ALL ? '' : (v as TaskPriority) })
          }
        >
          <SelectTrigger className="w-40" aria-label="Filter by priority">
            <SelectValue placeholder="Priority" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={ALL}>All priorities</SelectItem>
            <SelectItem value="LOW">Low</SelectItem>
            <SelectItem value="MEDIUM">Medium</SelectItem>
            <SelectItem value="HIGH">High</SelectItem>
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}
