import request from '@/utils/request'

/** 可开通的套餐列表 */
export function listPlans() {
  return request.get('/subscriptions/plans')
}

/** 当前生效订阅，未订阅时 data 为 null */
export function getCurrentSubscription() {
  return request.get('/subscriptions/current')
}

/**
 * 开通/切换套餐：{ planKey, cycle }，cycle 取值 MONTHLY / YEARLY。
 * 付费套餐会从积分余额扣费，余额不足返回业务码 8802（request 拦截器已统一引导充值）。
 */
export function subscribe(data) {
  return request.post('/subscriptions', data)
}

/** 修改当前订阅：{ autoRenew: 0 | 1 } */
export function updateCurrentSubscription(data) {
  return request.put('/subscriptions/current', data)
}

export function cancelSubscription() {
  return request.delete('/subscriptions/current')
}

export function listSubscriptionHistory({ page = 1, size = 100 } = {}) {
  return request.get('/subscriptions/history', { params: { page, size } })
}
