import request from '@/utils/request'

export function listApplications({ page = 1, size = 200 } = {}) {
  return request.get('/applications', { params: { page, size } })
}

export function getApplication(id) {
  return request.get(`/applications/${id}`)
}

/** 创建应用：{ name, description, appType } */
export function createApplication(data) {
  return request.post('/applications', data)
}

/** 更新应用：字段 + 可选 status（1 启用 / 0 停用） */
export function updateApplication(id, data) {
  return request.put(`/applications/${id}`, data)
}

export function deleteApplication(id) {
  return request.delete(`/applications/${id}`)
}
