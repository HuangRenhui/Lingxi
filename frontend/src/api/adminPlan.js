import request from '@/utils/request'

/** 管理端套餐列表（含已下架） */
export function listAdminPlans({ page = 1, size = 100, keyword } = {}) {
  return request.get('/admin/subscription-plans', { params: { page, size, keyword } })
}

export function getAdminPlan(id) {
  return request.get(`/admin/subscription-plans/${id}`)
}

/** 新建套餐：{ planKey, name, monthlyPrice, yearlyPrice, creditsPerMonth, tag, features, enabled, sortOrder } */
export function createAdminPlan(data) {
  return request.post('/admin/subscription-plans', data)
}

export function updateAdminPlan(id, data) {
  return request.put(`/admin/subscription-plans/${id}`, data)
}

export function deleteAdminPlan(id) {
  return request.delete(`/admin/subscription-plans/${id}`)
}
