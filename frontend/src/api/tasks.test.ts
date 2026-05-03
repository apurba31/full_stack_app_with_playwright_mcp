import { describe, expect, it, vi, beforeEach } from 'vitest';

vi.mock('@/lib/api', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}));

import { api } from '@/lib/api';
import {
  createTask,
  deleteTask,
  getTask,
  listTasks,
  patchStatus,
  updateTask,
} from './tasks';

const mockedApi = api as unknown as {
  get: ReturnType<typeof vi.fn>;
  post: ReturnType<typeof vi.fn>;
  put: ReturnType<typeof vi.fn>;
  patch: ReturnType<typeof vi.fn>;
  delete: ReturnType<typeof vi.fn>;
};

describe('tasks api client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('listTasks sends defaults and merges filters', async () => {
    mockedApi.get.mockResolvedValueOnce({ data: { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 } });
    await listTasks({ status: 'TODO', priority: 'HIGH', q: '  hello  ', page: 2, size: 5, sort: 'title,asc' });
    expect(mockedApi.get).toHaveBeenCalledWith('/v1/tasks', {
      params: {
        page: 2,
        size: 5,
        sort: 'title,asc',
        status: 'TODO',
        priority: 'HIGH',
        q: 'hello',
      },
    });
  });

  it('listTasks omits q when blank, uses default page/size/sort', async () => {
    mockedApi.get.mockResolvedValueOnce({ data: { content: [] } });
    await listTasks({ q: '   ' });
    const params = mockedApi.get.mock.calls[0][1].params;
    expect(params.page).toBe(0);
    expect(params.size).toBe(20);
    expect(params.sort).toBe('createdAt,desc');
    expect(params.q).toBeUndefined();
    expect(params.status).toBeUndefined();
  });

  it('listTasks works with no params', async () => {
    mockedApi.get.mockResolvedValueOnce({ data: { content: [] } });
    await listTasks();
    expect(mockedApi.get).toHaveBeenCalled();
  });

  it('getTask hits /v1/tasks/:id', async () => {
    mockedApi.get.mockResolvedValueOnce({ data: { id: 'x', title: 't' } });
    const task = await getTask('x');
    expect(mockedApi.get).toHaveBeenCalledWith('/v1/tasks/x');
    expect(task).toEqual({ id: 'x', title: 't' });
  });

  it('createTask POSTs body', async () => {
    mockedApi.post.mockResolvedValueOnce({ data: { id: 'n' } });
    const out = await createTask({ title: 'a', priority: 'LOW' });
    expect(mockedApi.post).toHaveBeenCalledWith('/v1/tasks', { title: 'a', priority: 'LOW' });
    expect(out).toEqual({ id: 'n' });
  });

  it('updateTask PUTs to /v1/tasks/:id', async () => {
    mockedApi.put.mockResolvedValueOnce({ data: { id: 'u' } });
    await updateTask('u', { title: 'b', priority: 'HIGH', status: 'DONE' });
    expect(mockedApi.put).toHaveBeenCalledWith('/v1/tasks/u', {
      title: 'b',
      priority: 'HIGH',
      status: 'DONE',
    });
  });

  it('patchStatus PATCHes status field', async () => {
    mockedApi.patch.mockResolvedValueOnce({ data: { id: 'p' } });
    await patchStatus('p', 'IN_PROGRESS');
    expect(mockedApi.patch).toHaveBeenCalledWith('/v1/tasks/p/status', { status: 'IN_PROGRESS' });
  });

  it('deleteTask hits DELETE /v1/tasks/:id', async () => {
    mockedApi.delete.mockResolvedValueOnce({});
    await deleteTask('d');
    expect(mockedApi.delete).toHaveBeenCalledWith('/v1/tasks/d');
  });
});
