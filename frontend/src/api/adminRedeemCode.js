import request from '@/utils/adminRequest'

/** 管理端兑换码列表 */
export function listAdminRedeemCodes({ page = 1, size = 100, status, keyword } = {}) {
  return request.get('/admin/redeem-codes', { params: { page, size, status, keyword } })
}

export function getAdminRedeemCode(id) {
  return request.get(`/admin/redeem-codes/${id}`)
}

/** 新建兑换码：{ code?, amount, expiresAt? } */
export function createAdminRedeemCode(data) {
  return request.post('/admin/redeem-codes', data)
}

/** 批量生成：{ amount, count, expiresAt?, prefix? } */
export function batchCreateAdminRedeemCodes(data) {
  return request.post('/admin/redeem-codes/batch', data)
}

export function updateAdminRedeemCode(id, data) {
  return request.put(`/admin/redeem-codes/${id}`, data)
}

export function deleteAdminRedeemCode(id) {
  return request.delete(`/admin/redeem-codes/${id}`)
}
