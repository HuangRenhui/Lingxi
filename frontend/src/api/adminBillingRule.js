import request from '@/utils/adminRequest'

/** 管理端计费规则列表 */
export function listAdminBillingRules({ page = 1, size = 100, keyword, category, enabled } = {}) {
  return request.get('/admin/billing-rules', { params: { page, size, keyword, category, enabled } })
}

export function getAdminBillingRule(id) {
  return request.get(`/admin/billing-rules/${id}`)
}

/**
 * 新建计费规则：
 * { category, target, model, unit, priceIn, priceOut, priceCachedIn, costIn, costOut, multiplier, enabled }
 */
export function createAdminBillingRule(data) {
  return request.post('/admin/billing-rules', data)
}

export function updateAdminBillingRule(id, data) {
  return request.put(`/admin/billing-rules/${id}`, data)
}

export function deleteAdminBillingRule(id) {
  return request.delete(`/admin/billing-rules/${id}`)
}
