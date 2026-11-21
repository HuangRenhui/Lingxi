import request from '@/utils/request'

/** 管理端优惠券列表 */
export function listAdminCoupons({ page = 1, size = 100, keyword, enabled } = {}) {
  return request.get('/admin/coupons', { params: { page, size, keyword, enabled } })
}

export function getAdminCoupon(id) {
  return request.get(`/admin/coupons/${id}`)
}

/**
 * 新建优惠券：{ name, type, value, threshold, total, validFrom, validTo, enabled }
 * type 取值 FULL_REDUCTION（满减）/ DISCOUNT（折扣）
 */
export function createAdminCoupon(data) {
  return request.post('/admin/coupons', data)
}

export function updateAdminCoupon(id, data) {
  return request.put(`/admin/coupons/${id}`, data)
}

export function deleteAdminCoupon(id) {
  return request.delete(`/admin/coupons/${id}`)
}
