import dayjs from 'dayjs'
import { loadWallet, saveWallet } from '@/mock/account'

/**
 * 积分账户（购买积分 + 奖励积分）。
 *
 * <p>业务背景：平台积分按来源分为两类，用途相同但生命周期不同——</p>
 * <ul>
 *   <li><b>购买积分</b>：充值/兑换码获得，不过期；</li>
 *   <li><b>奖励积分</b>：邀请好友、签到、活动礼包等获得，按批次管理，有到期时间，可查明细。</li>
 * </ul>
 *
 * <p>扣费顺序遵循「先过期先用」（FEFO）：奖励批次按到期时间升序消耗，
 * 无到期时间的批次排在最后，奖励用尽后再扣购买积分。
 * 这样不会出现「奖励积分过期作废、购买积分却先被扣掉」的情况。</p>
 *
 * <p>后端暂未提供积分接口，这里用 localStorage 模拟；接入真实后端时，
 * 只需把本文件的读写替换为 HTTP 调用，页面层无需改动。</p>
 */

const KEY = 'lingxi-credit-account'

/** 购买积分在分摊结果中的虚拟批次 id */
export const PURCHASED_POOL = 'purchased'

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
    // 本地存储不可用（隐私模式/配额耗尽）时静默降级，不阻断页面
  }
}

function money(value) {
  return Number(Number(value || 0).toFixed(2))
}

function stamp(daysAgo, hour = 10, minute = 0) {
  return dayjs().subtract(daysAgo, 'day').hour(hour).minute(minute).second(0).format('YYYY-MM-DD HH:mm')
}

function expiresIn(days, hour = 15, minute = 57) {
  return dayjs().add(days, 'day').hour(hour).minute(minute).second(0).format('YYYY-MM-DD HH:mm')
}

/**
 * 奖励批次种子。
 *
 * <p>约束：每个批次的 used 必须等于 entries 中「消耗」金额之和，
 * 否则页面上的「剩余 / 总量」与明细对不上。</p>
 */
const SEED_REWARDS = [
  {
    id: 'rw-signup',
    name: '新用户礼包',
    source: 'signup',
    total: 100,
    used: 71.4,
    expiresAt: expiresIn(21),
    createdAt: stamp(30, 10, 0),
    entries: [
      { id: 'ce-s1', type: '获得', amount: 100, note: '注册礼包发放', createdAt: stamp(30, 10, 0) },
      { id: 'ce-s2', type: '消耗', amount: -50, note: 'Kling · 视频生成', createdAt: stamp(12, 16, 20) },
      { id: 'ce-s3', type: '消耗', amount: -21.4, note: 'SeeDance · 视频生成', createdAt: stamp(6, 11, 5) }
    ]
  },
  {
    id: 'rw-referral',
    name: '邀请好友奖励',
    source: 'referral',
    total: 200,
    used: 160,
    expiresAt: expiresIn(52),
    createdAt: stamp(24, 9, 30),
    entries: [
      { id: 'ce-r1', type: '获得', amount: 80, note: '好友「林岚」注册成功', createdAt: stamp(24, 9, 30) },
      { id: 'ce-r2', type: '获得', amount: 70, note: '好友「周牧」首次充值', createdAt: stamp(18, 14, 12) },
      { id: 'ce-r3', type: '获得', amount: 50, note: '好友「陈屿」完成首单', createdAt: stamp(9, 20, 40) },
      { id: 'ce-r4', type: '消耗', amount: -100, note: 'MiniMax H3 · 视频生成', createdAt: stamp(5, 10, 15) },
      { id: 'ce-r5', type: '消耗', amount: -60, note: 'Suno · 音乐生成', createdAt: stamp(2, 18, 30) }
    ]
  },
  {
    id: 'rw-checkin',
    name: '每日签到',
    source: 'checkin',
    total: 30,
    used: 10,
    expiresAt: null,
    createdAt: stamp(9, 8, 0),
    entries: [
      { id: 'ce-c1', type: '获得', amount: 10, note: '连续签到 3 天', createdAt: stamp(9, 8, 0) },
      { id: 'ce-c2', type: '获得', amount: 10, note: '连续签到 5 天', createdAt: stamp(7, 8, 0) },
      { id: 'ce-c3', type: '获得', amount: 10, note: '连续签到 7 天', createdAt: stamp(4, 8, 0) },
      { id: 'ce-c4', type: '消耗', amount: -10, note: 'Nano Banana · 图片生成', createdAt: stamp(1, 21, 18) }
    ]
  }
]

/** 批次归一：补齐 remaining、过滤非法数值，并保证 used 不会被写成负数或超过 total */
function normalizeBatch(raw, index) {
  const total = Math.max(0, Number(raw?.total) || 0)
  const rawUsed = Number(raw?.used)
  const used = Math.min(total, Math.max(0, Number.isFinite(rawUsed) ? rawUsed : 0))
  const entries = Array.isArray(raw?.entries) ? raw.entries : []
  return {
    id: String(raw?.id || `rw-${index}`),
    name: String(raw?.name || '奖励积分'),
    source: String(raw?.source || 'activity'),
    total: money(total),
    used: money(used),
    remaining: money(total - used),
    expiresAt: raw?.expiresAt ? String(raw.expiresAt) : null,
    createdAt: raw?.createdAt ? String(raw.createdAt) : stamp(0),
    entries: entries.filter(item => item && Number.isFinite(Number(item.amount)))
  }
}

function totalOf(account) {
  return money(account.purchased + account.rewards.reduce((sum, batch) => sum + batch.remaining, 0))
}

/** 把积分账户总额同步到钱包余额，保证两处展示一致（布局层余额胶囊读的是钱包） */
function syncWallet(account) {
  const wallet = loadWallet()
  const total = totalOf(account)
  if (money(wallet.balance) !== total) {
    wallet.balance = total
    saveWallet(wallet)
  }
}

function persist(account) {
  write(KEY, account)
  syncWallet(account)
  return account
}

/**
 * 读取积分账户。
 *
 * <p>首次使用（或无缓存）时用钱包余额反推购买积分：奖励批次总额已知，
 * 差额即购买积分。若钱包余额小于奖励总额（历史数据/脏数据），
 * 直接清空奖励批次，避免出现「奖励比总额还多」的负余额。</p>
 */
export function loadCreditAccount() {
  const cached = read(KEY)
  if (cached && typeof cached === 'object' && !Array.isArray(cached)) {
    const rewards = Array.isArray(cached.rewards)
      ? cached.rewards.map((batch, index) => normalizeBatch(batch, index))
      : []
    const purchased = Math.max(0, Number(cached.purchased) || 0)
    return { purchased: money(purchased), rewards }
  }

  const wallet = loadWallet()
  const balance = money(wallet.balance)
  const rewards = SEED_REWARDS.map((batch, index) => normalizeBatch(batch, index))
  const rewardTotal = money(rewards.reduce((sum, batch) => sum + batch.remaining, 0))
  if (balance >= rewardTotal) {
    return persist({ purchased: money(balance - rewardTotal), rewards })
  }
  return persist({ purchased: balance, rewards: [] })
}

export function saveCreditAccount(account) {
  return persist(account)
}

/** 汇总：总可用 / 购买 / 奖励 */
export function creditSummary() {
  const account = loadCreditAccount()
  const reward = money(account.rewards.reduce((sum, batch) => sum + batch.remaining, 0))
  return {
    total: totalOf(account),
    purchased: account.purchased,
    reward,
    // 最近一个即将到期的奖励批次，用于顶部提示
    expiringSoon: [...account.rewards]
      .filter(batch => batch.remaining > 0 && batch.expiresAt)
      .sort((a, b) => dayjs(a.expiresAt).valueOf() - dayjs(b.expiresAt).valueOf())[0] || null
  }
}

/** 奖励批次列表（可用积分视图：按剩余>0 且未过期过滤由页面决定） */
export function loadRewardBatches() {
  return loadCreditAccount()
    .rewards.map(batch => ({
      ...batch,
      expired: isExpired(batch)
    }))
    .sort((a, b) => {
      // 未过期的排前面，其次按到期时间升序（先过期的靠前）
      if (a.expired !== b.expired) return a.expired ? 1 : -1
      return expiryValue(a) - expiryValue(b)
    })
}

/** 批次明细（获得与消耗，按时间倒序） */
export function loadBatchEntries(batchId) {
  const batch = loadCreditAccount().rewards.find(item => item.id === batchId)
  if (!batch) return []
  return [...batch.entries].sort(
    (a, b) => dayjs(b.createdAt).valueOf() - dayjs(a.createdAt).valueOf()
  )
}

function isExpired(batch) {
  return Boolean(batch.expiresAt) && !dayjs(batch.expiresAt).isAfter(dayjs())
}

function expiryValue(batch) {
  return batch.expiresAt ? dayjs(batch.expiresAt).valueOf() : Number.MAX_SAFE_INTEGER
}

/**
 * 扣减积分：奖励批次按到期时间升序消耗（FEFO），不足部分再扣购买积分。
 *
 * @param {number} amount 扣减金额
 * @param {{ note?: string, service?: string }} meta 用于写入批次明细的备注
 * @returns {{ ok: boolean, balance: number, allocations: Array<{ batchId: string, amount: number }> }}
 */
export function spendFromPools(amount, meta = {}) {
  const account = loadCreditAccount()
  const cost = money(amount)
  const total = totalOf(account)
  if (!(cost > 0)) {
    return { ok: true, balance: total, allocations: [] }
  }
  if (cost > total) {
    return { ok: false, balance: total, allocations: [] }
  }

  const now = dayjs().format('YYYY-MM-DD HH:mm')
  const note = meta.note || meta.service || '积分消费'
  const allocations = []
  let left = cost

  const consumable = account.rewards
    .filter(batch => batch.remaining > 0 && !isExpired(batch))
    .sort((a, b) => expiryValue(a) - expiryValue(b))

  consumable.forEach(batch => {
    if (left <= 0) return
    const take = money(Math.min(left, batch.remaining))
    if (!(take > 0)) return
    batch.used = money(batch.used + take)
    batch.remaining = money(batch.total - batch.used)
    batch.entries.unshift({
      id: `ce-${Date.now()}-${batch.id}`,
      type: '消耗',
      amount: -take,
      note,
      createdAt: now
    })
    allocations.push({ batchId: batch.id, amount: take })
    left = money(left - take)
  })

  if (left > 0) {
    account.purchased = money(account.purchased - left)
    allocations.push({ batchId: PURCHASED_POOL, amount: left })
  }

  persist(account)
  return { ok: true, balance: totalOf(account), allocations }
}

/**
 * 退回积分。
 *
 * <p>传入 allocations 时按原扣费来源逐批回滚（奖励仍回到原批次）；
 * 未传时回滚到购买积分——这是保守选择，避免把积分凭空塞进某个奖励批次。</p>
 */
export function refundToPools(amount, allocations) {
  const account = loadCreditAccount()
  const cost = money(amount)
  if (!(cost > 0)) {
    return { ok: true, balance: totalOf(account) }
  }
  const now = dayjs().format('YYYY-MM-DD HH:mm')
  const list = Array.isArray(allocations) ? allocations : []

  if (list.length) {
    list.forEach(item => {
      const delta = money(item.amount)
      if (!(delta > 0)) return
      if (item.batchId === PURCHASED_POOL) {
        account.purchased = money(account.purchased + delta)
        return
      }
      const batch = account.rewards.find(entry => entry.id === item.batchId)
      if (!batch) {
        account.purchased = money(account.purchased + delta)
        return
      }
      batch.used = money(Math.max(0, batch.used - delta))
      batch.remaining = money(batch.total - batch.used)
      batch.entries.unshift({
        id: `ce-${Date.now()}-${batch.id}-r`,
        type: '退回',
        amount: delta,
        note: '消费失败自动退回',
        createdAt: now
      })
    })
  } else {
    account.purchased = money(account.purchased + cost)
  }

  persist(account)
  return { ok: true, balance: totalOf(account) }
}

/**
 * 管理员直接调整购买积分（可正可负）。
 * 负数不会导致余额为负：不足时扣到 0 为止，避免演示数据出现负余额。
 */
export function adjustPurchased(delta) {
  const account = loadCreditAccount()
  const value = money(delta)
  if (value === 0) return { ok: true, balance: totalOf(account) }
  if (value > 0) {
    account.purchased = money(account.purchased + value)
  } else {
    account.purchased = money(Math.max(0, account.purchased + value))
  }
  persist(account)
  return { ok: true, balance: totalOf(account) }
}

/** 充值/兑换得到的积分统一进入购买池（不过期） */
export function rechargeToPurchased(amount) {
  const account = loadCreditAccount()
  const value = money(amount)
  if (!(value > 0)) return { ok: true, balance: totalOf(account) }
  account.purchased = money(account.purchased + value)
  persist(account)
  return { ok: true, balance: totalOf(account) }
}

/** 发放奖励积分（邀请、签到、活动），用于演示数据闭环 */
export function grantReward({ name, source = 'activity', amount, expiresAt = null, note = '' }) {
  const account = loadCreditAccount()
  const value = money(amount)
  if (!(value > 0)) return account
  const now = dayjs().format('YYYY-MM-DD HH:mm')
  const existing = account.rewards.find(
    batch => batch.name === name && batch.source === source && !isExpired(batch)
  )
  if (existing) {
    existing.total = money(existing.total + value)
    existing.remaining = money(existing.total - existing.used)
    existing.entries.unshift({
      id: `ce-${Date.now()}-${existing.id}`,
      type: '获得',
      amount: value,
      note: note || '奖励发放',
      createdAt: now
    })
  } else {
    account.rewards.unshift({
      id: `rw-${Date.now()}`,
      name,
      source,
      total: value,
      used: 0,
      remaining: value,
      expiresAt,
      createdAt: now,
      entries: [
        { id: `ce-${Date.now()}`, type: '获得', amount: value, note: note || '奖励发放', createdAt: now }
      ]
    })
  }
  persist(account)
  return account
}
