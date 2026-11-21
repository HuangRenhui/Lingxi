import request from '@/utils/request'

/** 管理端举报列表 */
export function listAdminModerationReports({ page = 1, size = 100, status, keyword } = {}) {
  return request.get('/admin/moderation-reports', { params: { page, size, status, keyword } })
}

export function getAdminModerationReport(id) {
  return request.get(`/admin/moderation-reports/${id}`)
}

/** 新建举报：{ targetType, targetId, reason } */
export function createAdminModerationReport(data) {
  return request.post('/admin/moderation-reports', data)
}

/** 处理举报：{ status, result } */
export function updateAdminModerationReport(id, data) {
  return request.put(`/admin/moderation-reports/${id}`, data)
}

export function deleteAdminModerationReport(id) {
  return request.delete(`/admin/moderation-reports/${id}`)
}
