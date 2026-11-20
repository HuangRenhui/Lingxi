import request from '@/utils/request'

/** 作品列表；type 可筛选类型，trashed=1 查回收站 */
export function listArtifacts({ page = 1, size = 200, type, keyword, trashed } = {}) {
  return request.get('/artifacts', { params: { page, size, type, keyword, trashed } })
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

export function deleteArtifact(id) {
  return request.delete(`/artifacts/${id}`)
}
