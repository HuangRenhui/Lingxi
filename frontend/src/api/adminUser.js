import request from '@/utils/request'

export function listAdminUsers({ page = 1, size = 200, keyword } = {}) {
  return request.get('/admin/users', { params: { page, size, keyword } })
}

export function getAdminUser(id) {
  return request.get(`/admin/users/${id}`)
}

/** 创建用户：{ username, password, nickname, role } */
export function createAdminUser(data) {
  return request.post('/admin/users', data)
}

/** 更新用户：{ password?, nickname, role, status } */
export function updateAdminUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteAdminUser(id) {
  return request.delete(`/admin/users/${id}`)
}
