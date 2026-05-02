import { useMemo, useState } from 'react';
import { toast } from 'sonner';
import { Plus, ChevronLeft, ChevronRight, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { TaskFilters, type TaskFilterValues } from '@/components/TaskFilters';
import { TaskTable } from '@/components/TaskTable';
import { TaskStats } from '@/components/TaskStats';
import { TaskForm, type TaskFormValues } from '@/components/TaskForm';
import { HealthIndicator } from '@/components/HealthIndicator';
import {
  useCreateTask,
  useDeleteTask,
  useTasksQuery,
  useUpdateStatus,
  useUpdateTask,
} from '@/hooks/useTasks';
import type { TaskDto, TaskStatus } from '@/types/task';

export function Dashboard() {
  const [filters, setFilters] = useState<TaskFilterValues>({ status: '', priority: '', q: '' });
  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [createOpen, setCreateOpen] = useState(false);
  const [editTask, setEditTask] = useState<TaskDto | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TaskDto | null>(null);

  const queryParams = useMemo(
    () => ({
      status: filters.status,
      priority: filters.priority,
      q: filters.q,
      page,
      size,
      sort: 'createdAt,desc',
    }),
    [filters, page, size],
  );

  const tasksQuery = useTasksQuery(queryParams);
  const createMut = useCreateTask();
  const updateMut = useUpdateTask();
  const statusMut = useUpdateStatus();
  const deleteMut = useDeleteTask();

  const tasks = tasksQuery.data?.content ?? [];
  const totalElements = tasksQuery.data?.totalElements ?? 0;
  const totalPages = tasksQuery.data?.totalPages ?? 0;

  function handleFiltersChange(next: TaskFilterValues) {
    setFilters(next);
    setPage(0);
  }

  async function handleCreate(values: TaskFormValues) {
    try {
      await createMut.mutateAsync({
        title: values.title,
        description: values.description || undefined,
        priority: values.priority,
      });
      toast.success('Task created');
      setCreateOpen(false);
    } catch (err) {
      toast.error('Failed to create task');
      console.error(err);
    }
  }

  async function handleUpdate(values: TaskFormValues) {
    if (!editTask) return;
    try {
      await updateMut.mutateAsync({
        id: editTask.id,
        input: {
          title: values.title,
          description: values.description || undefined,
          priority: values.priority,
          status: values.status,
        },
      });
      toast.success('Task updated');
      setEditTask(null);
    } catch (err) {
      toast.error('Failed to update task');
      console.error(err);
    }
  }

  async function handleStatusChange(task: TaskDto, status: TaskStatus) {
    try {
      await statusMut.mutateAsync({ id: task.id, status });
      toast.success(`Moved to ${status.replace('_', ' ').toLowerCase()}`);
    } catch (err) {
      toast.error('Failed to update status');
      console.error(err);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await deleteMut.mutateAsync(deleteTarget.id);
      toast.success('Task deleted');
      setDeleteTarget(null);
    } catch (err) {
      toast.error('Failed to delete task');
      console.error(err);
    }
  }

  return (
    <div className="min-h-screen bg-muted/20">
      <header className="border-b bg-background">
        <div className="container flex h-16 items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary text-primary-foreground font-bold">
              FT
            </div>
            <div>
              <h1 className="text-lg font-semibold leading-tight">FullStack Tasks</h1>
              <p className="text-xs text-muted-foreground">Manage work in real time</p>
            </div>
          </div>
          <HealthIndicator />
        </div>
      </header>

      <main className="container py-8 space-y-6">
        <TaskStats tasks={tasks} total={totalElements} />

        <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="flex-1">
            <TaskFilters value={filters} onChange={handleFiltersChange} />
          </div>
          <Button onClick={() => setCreateOpen(true)}>
            <Plus className="mr-2 h-4 w-4" />
            New Task
          </Button>
        </div>

        {tasksQuery.isError ? (
          <div
            className="flex items-center gap-2 rounded-md border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive"
            data-testid="error-state"
          >
            <AlertCircle className="h-4 w-4" />
            Failed to load tasks. Will retry shortly.
          </div>
        ) : (
          <TaskTable
            tasks={tasks}
            loading={tasksQuery.isLoading}
            onEdit={(t) => setEditTask(t)}
            onDelete={(t) => setDeleteTarget(t)}
            onStatusChange={handleStatusChange}
          />
        )}

        {totalPages > 1 && (
          <div className="flex items-center justify-end gap-2">
            <span className="text-sm text-muted-foreground">
              Page {page + 1} of {totalPages}
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft className="h-4 w-4" />
              Prev
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        )}
      </main>

      {/* Create Dialog */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>New Task</DialogTitle>
            <DialogDescription>Add a new task to your board.</DialogDescription>
          </DialogHeader>
          <TaskForm
            submitLabel="Create"
            onSubmit={handleCreate}
            onCancel={() => setCreateOpen(false)}
            submitting={createMut.isPending}
          />
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={!!editTask} onOpenChange={(o) => !o && setEditTask(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Task</DialogTitle>
            <DialogDescription>Update the details of this task.</DialogDescription>
          </DialogHeader>
          {editTask && (
            <TaskForm
              key={editTask.id}
              initial={editTask}
              showStatus
              submitLabel="Save"
              onSubmit={handleUpdate}
              onCancel={() => setEditTask(null)}
              submitting={updateMut.isPending}
            />
          )}
        </DialogContent>
      </Dialog>

      {/* Delete Confirm */}
      <Dialog open={!!deleteTarget} onOpenChange={(o) => !o && setDeleteTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete task?</DialogTitle>
            <DialogDescription>
              This will permanently remove "{deleteTarget?.title}". This cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDelete} disabled={deleteMut.isPending}>
              {deleteMut.isPending ? 'Deleting…' : 'Delete'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default Dashboard;
