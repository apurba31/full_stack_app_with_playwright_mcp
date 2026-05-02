import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { CheckCircle2, Clock, ListTodo, Loader2 } from 'lucide-react';
import type { TaskDto } from '@/types/task';

export interface TaskStatsProps {
  tasks: TaskDto[];
  total?: number;
}

export function TaskStats({ tasks, total }: TaskStatsProps) {
  const todo = tasks.filter((t) => t.status === 'TODO').length;
  const inProgress = tasks.filter((t) => t.status === 'IN_PROGRESS').length;
  const done = tasks.filter((t) => t.status === 'DONE').length;
  const totalCount = total ?? tasks.length;

  const cards = [
    { label: 'Total', value: totalCount, Icon: ListTodo, color: 'text-slate-700', testId: 'stat-total' },
    { label: 'To Do', value: todo, Icon: Clock, color: 'text-slate-500', testId: 'stat-todo' },
    {
      label: 'In Progress',
      value: inProgress,
      Icon: Loader2,
      color: 'text-amber-600',
      testId: 'stat-in-progress',
    },
    { label: 'Done', value: done, Icon: CheckCircle2, color: 'text-emerald-600', testId: 'stat-done' },
  ];

  return (
    <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
      {cards.map(({ label, value, Icon, color, testId }) => (
        <Card key={label} data-testid={testId}>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
            <Icon className={`h-4 w-4 ${color}`} aria-hidden />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold" data-testid={`${testId}-value`}>
              {value}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
