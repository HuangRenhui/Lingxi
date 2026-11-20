import request from '@/utils/request'

export function listSkills({ page = 1, size = 200 } = {}) {
  return request.get('/skills', { params: { page, size } })
}

export function getSkill(id) {
  return request.get(`/skills/${id}`)
}

/** 创建技能：{ name, description, content, category }，服务端会自动补全开头的 / */
export function createSkill(data) {
  return request.post('/skills', data)
}

/** 更新技能：字段 + 可选 status（1 启用 / 0 停用） */
export function updateSkill(id, data) {
  return request.put(`/skills/${id}`, data)
}

export function deleteSkill(id) {
  return request.delete(`/skills/${id}`)
}
