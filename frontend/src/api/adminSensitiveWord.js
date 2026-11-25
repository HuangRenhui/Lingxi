import request from '@/utils/adminRequest'

/** 敏感词列表 */
export function listAdminSensitiveWords(params) {
  return request.get('/admin/moderation-words', { params })
}

/** 批量新增敏感词 */
export function createAdminSensitiveWords(data) {
  return request.post('/admin/moderation-words', data)
}

/** 启用/停用敏感词 */
export function updateAdminSensitiveWord(id, data) {
  return request.put(`/admin/moderation-words/${id}`, data)
}

/** 删除敏感词 */
export function deleteAdminSensitiveWord(id) {
  return request.delete(`/admin/moderation-words/${id}`)
}
