import request from '@/utils/request'

/** 钱包账户余额与累计统计 */
export function getWallet() {
  return request.get('/wallet')
}

/**
 * 钱包流水（分页）。
 *
 * <p>后端返回 List 而非分页对象，页面仍使用本地分页组件，
 * 因此这里一次性取较大页容量；超过 cap 条数的历史流水需改造成服务端分页。</p>
 */
export function listWalletTransactions({ page = 1, size = 100, type } = {}) {
  return request.get('/wallet/transactions', { params: { page, size, type } })
}
