import request from '@/utils/request'

/** 分页查询通知：返回 { list, total, page, size } */
export function listNotifications({ page = 1, size = 20, unreadOnly = false, readOnly = false, type, keyword } = {}) {
  return request.get('/notifications', { params: { page, size, unreadOnly, readOnly, type, keyword } })
}

/** 未读数量，用于顶栏红点 */
export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}

export function markNotificationRead(id) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/notifications/read-all')
}

/** 一次请求批量删除通知 */
export function deleteNotifications(ids) {
  return request.delete('/notifications/batch', { data: { ids } })
}

export function deleteNotification(id) {
  return request.delete(`/notifications/${id}`)
}
