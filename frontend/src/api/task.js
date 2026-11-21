import request from '@/utils/request'

export function listTasks({ page = 1, size = 100 } = {}) {
  return request.get('/agent/tasks', { params: { page, size } })
}

export function createTask(data) {
  return request.post('/agent/tasks', data)
}

export function updateTask(id, data) {
  return request.put(`/agent/tasks/${id}`, data)
}

export function updateTaskStatus(id, status) {
  return request.patch(`/agent/tasks/${id}/status`, { status })
}

export function deleteTask(id) {
  return request.delete(`/agent/tasks/${id}`)
}
