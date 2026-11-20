import request from '@/utils/request'

/** 列表不返回明文 key（key 恒为 null），只给前缀用于识别 */
export function listApiKeys({ page = 1, size = 100 } = {}) {
  return request.get('/api-keys', { params: { page, size } })
}

/** 创建后仅在本次响应中返回一次明文 key，前端必须提示用户立即保存 */
export function createApiKey(data) {
  return request.post('/api-keys', data)
}

/** 仅允许改名与启停：{ name, status } */
export function updateApiKey(id, data) {
  return request.put(`/api-keys/${id}`, data)
}

export function deleteApiKey(id) {
  return request.delete(`/api-keys/${id}`)
}
