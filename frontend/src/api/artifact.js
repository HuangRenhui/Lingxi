import request from '@/utils/request'

/** 作品列表：返回 { list, total, page, size }；筛选与排序均在服务端完成 */
export function listArtifacts({ page = 1, size = 12, type, keyword, trashed, favorite, sort } = {}) {
  return request.get('/artifacts', { params: { page, size, type, keyword, trashed, favorite, sort } })
}

export function getArtifact(id) {
  return request.get(`/artifacts/${id}`)
}

/** 创建作品：{ title, type, toolKey, toolName, prompt, url, posterUrl, sizeBytes } */
export function createArtifact(data) {
  return request.post('/artifacts', data)
}

/** 更新作品：支持改名、收藏（favorite=1/0）、移入回收站（trashed=1/0） */
export function updateArtifact(id, data) {
  return request.put(`/artifacts/${id}`, data)
}

/** 批量修改收藏/回收站状态：一次请求完成 */
export function batchUpdateArtifacts({ ids, favorite, trashed }) {
  return request.patch('/artifacts/batch', { ids, favorite, trashed })
}

/** 批量删除作品：一次请求完成 */
export function deleteArtifacts(ids) {
  return request.delete('/artifacts/batch', { data: { ids } })
}

export function deleteArtifact(id) {
  return request.delete(`/artifacts/${id}`)
}
