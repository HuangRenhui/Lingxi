import request from '@/utils/request'

export function listTasks() {
  return request.get('/agent/tasks')
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
