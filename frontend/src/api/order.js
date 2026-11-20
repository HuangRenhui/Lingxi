import request from '@/utils/request'

/** 订单列表；status/type/keyword 均可选 */
export function listOrders({ page = 1, size = 200, status, type, keyword } = {}) {
  return request.get('/orders', { params: { page, size, status, type, keyword } })
}

export function getOrder(id) {
  return request.get(`/orders/${id}`)
}

/** 创建订单：{ description, amount, payMethod } */
export function createOrder(data) {
  return request.post('/orders', data)
}

/** 仅待支付订单可改：{ description, payMethod } */
export function updateOrder(id, data) {
  return request.put(`/orders/${id}`, data)
}

/** 用户侧只能取消待支付订单，status 传 CANCELLED */
export function changeOrderStatus(id, status) {
  return request.patch(`/orders/${id}/status`, { status })
}

export function deleteOrder(id) {
  return request.delete(`/orders/${id}`)
}
