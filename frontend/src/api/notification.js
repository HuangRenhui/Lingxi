import request from '@/utils/request'

export function listNotifications({ page = 1, size = 100 } = {}) {
  return request.get('/notifications', { params: { page, size } })
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

export function deleteNotification(id) {
  return request.delete(`/notifications/${id}`)
}
