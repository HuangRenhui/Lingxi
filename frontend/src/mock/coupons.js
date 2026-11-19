import dayjs from 'dayjs'
import { getUser } from '@/utils/auth'
import { loadCoupons, loadRedeemCodes, saveCoupons, saveRedeemCodes } from './admin-extra'
import { appendWalletLog, loadWallet, saveWallet } from './account'
import { rechargeToPurchased } from './credits'

/**
 * 用户端兑换码与优惠券演示数据。
 *
 * 兑换码与后台共用 lingxi-admin-redeem-codes，
 * 优惠券发放数量与后台 lingxi-admin-coupons 同步，
 * 接入后端后替换为对应的用户接口即可。
 */

const KEYS = {
  userCoupons: 'lingxi-user-coupons',
  redeemRecords: 'lingxi-user-redeem-records'
}

function stamp(daysAgo, hour = 10, minute = 0) {
  return dayjs().subtract(daysAgo, 'day').hour(hour).minute(minute).second(0).format('YYYY-MM-DD HH:mm')
}

function read(key) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : null
  } catch (error) {
    return null
  }
}

function write(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    // 忽略本地存储异常
  }
}

const COUPON_SEED = [
  {
    id: 'uc-1001',
    couponId: 'cp-1',
    name: '首充立减 10 元',
    type: '满减',
    value: 10,
    threshold: 50,
    validFrom: '2026-11-01',
    validTo: '2026-12-31',
    status: 'AVAILABLE',
    claimedAt: stamp(2, 14, 20)
  },
  {
    id: 'uc-1003',
    couponId: 'cp-3',
    name: '老用户充值满 300 减 30',
    type: '满减',
    value: 30,
    threshold: 300,
    validFrom: '2026-10-01',
    validTo: '2026-10-31',
    status: 'EXPIRED',
    claimedAt: stamp(40, 11, 10)
  },
  {
    id: 'uc-1004',
    couponId: 'cp-0',
    name: '新人见面礼 满 100 减 15',
    type: '满减',
    value: 15,
    threshold: 100,
    validFrom: '2026-09-01',
    validTo: '2026-10-15',
    status: 'USED',
    claimedAt: stamp(60, 16, 0),
    usedAt: stamp(50, 21, 12)
  }
]

const REDEEM_RECORD_SEED = [
  { id: 'rd-1001', code: 'LX2025-GIFT-100', amount: 100, redeemedAt: stamp(30, 14, 20) },
  { id: 'rd-1002', code: 'LX2025-WELCOME-50', amount: 50, redeemedAt: stamp(62, 10, 5) }
]

export function loadUserCoupons() {
  const cached = read(KEYS.userCoupons)
  const list = Array.isArray(cached) ? cached : COUPON_SEED
  const now = dayjs()
  const normalized = list.map(item => {
    if (item.status === 'AVAILABLE' && dayjs(item.validTo).endOf('day').isBefore(now)) {
      return { ...item, status: 'EXPIRED' }
    }
    return item
  })
  if (!Array.isArray(cached)) write(KEYS.userCoupons, normalized)
  return normalized
}

export function saveUserCoupons(list) {
  write(KEYS.userCoupons, list)
}

export function loadRedeemRecords() {
  const cached = read(KEYS.redeemRecords)
  if (Array.isArray(cached)) return cached
  write(KEYS.redeemRecords, REDEEM_RECORD_SEED)
  return REDEEM_RECORD_SEED.map(item => ({ ...item }))
}

export function saveRedeemRecords(list) {
  write(KEYS.redeemRecords, list)
}

/** 优惠券是否可用于指定充值金额 */
export function isCouponUsable(coupon, amount) {
  if (!coupon || coupon.status !== 'AVAILABLE') return false
  const price = Number(amount) || 0
  if (coupon.threshold && price < Number(coupon.threshold)) return false
  return !dayjs(coupon.validTo).endOf('day').isBefore(dayjs())
}

/** 计算使用优惠券后的实付金额 */
export function couponPayable(coupon, amount) {
  const price = Number(amount) || 0
  if (!coupon) return price
  if (coupon.type === '满减') return Number(Math.max(0, price - Number(coupon.value)).toFixed(2))
  return Number((price * Number(coupon.value)).toFixed(2))
}

export function markCouponUsed(couponInstanceId) {
  if (!couponInstanceId) return
  const list = loadUserCoupons()
  const target = list.find(item => item.id === couponInstanceId)
  if (!target) return
  target.status = 'USED'
  target.usedAt = dayjs().format('YYYY-MM-DD HH:mm')
  saveUserCoupons(list)
}

/** 领取后台已启用的优惠券 */
export function claimCoupon(couponId) {
  const coupons = loadCoupons()
  const target = coupons.find(item => item.id === couponId)
  if (!target) throw new Error('优惠券不存在')
  if (!target.enabled) throw new Error('该优惠券已停止发放')
  if (dayjs(target.validTo).endOf('day').isBefore(dayjs())) throw new Error('该优惠券已过期')
  if (Number(target.claimed) >= Number(target.total)) throw new Error('该优惠券已被领完')

  const mine = loadUserCoupons()
  if (mine.some(item => item.couponId === couponId && item.status === 'AVAILABLE')) {
    throw new Error('你已经领取过该优惠券')
  }
  if (mine.some(item => item.couponId === couponId && item.status === 'USED')) {
    throw new Error('该优惠券每人限领一次')
  }

  target.claimed = Number(target.claimed) + 1
  saveCoupons(coupons)

  const instance = {
    id: `uc-${Date.now()}`,
    couponId: target.id,
    name: target.name,
    type: target.type,
    value: target.value,
    threshold: target.threshold,
    validFrom: target.validFrom,
    validTo: target.validTo,
    status: 'AVAILABLE',
    claimedAt: dayjs().format('YYYY-MM-DD HH:mm')
  }
  saveUserCoupons([instance, ...mine])
  return instance
}

/** 使用兑换码：与后台共用数据，校验通过后发放积分 */
export function redeemCode(rawCode) {
  const code = String(rawCode || '').trim().toUpperCase()
  if (!code) throw new Error('请输入兑换码')

  const codes = loadRedeemCodes()
  const target = codes.find(item => String(item.code).toUpperCase() === code)
  if (!target) throw new Error('兑换码不存在，请核对后重试')
  if (target.status === 'USED') throw new Error('该兑换码已被使用')
  if (target.status === 'VOID') throw new Error('该兑换码已作废')
  if (dayjs(target.expiresAt).endOf('day').isBefore(dayjs())) throw new Error('该兑换码已过期')

  const user = getUser() || {}
  target.status = 'USED'
  target.usedBy = user.username || user.email || 'demo'
  saveRedeemCodes(codes)

  const credit = Number(target.amount) || 0
  // 兑换码积分视同购买的积分：进入购买池、不过期，并计入累计充值
  const result = rechargeToPurchased(credit)
  const wallet = loadWallet()
  wallet.totalRecharge = Number((wallet.totalRecharge + credit).toFixed(2))
  saveWallet(wallet)

  const now = dayjs().format('YYYY-MM-DD HH:mm')
  appendWalletLog({
    id: `wl-${Date.now()}`,
    createdAt: now,
    type: '奖励',
    amount: credit,
    balance: result.balance,
    note: `兑换码 ${target.code}`
  })

  const records = loadRedeemRecords()
  records.unshift({ id: `rd-${Date.now()}`, code: target.code, amount: Number(target.amount), redeemedAt: now })
  saveRedeemRecords(records)

  return { code: target.code, amount: Number(target.amount), balance: wallet.balance }
}
