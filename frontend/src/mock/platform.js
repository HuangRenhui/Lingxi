import dayjs from 'dayjs'
import {
  plans as fallbackPlans,
  appendWalletLog,
  loadAssets,
  loadDemoTasks,
  loadNotifications,
  loadOrders,
  loadProfile,
  loadSubscription,
  loadUsages,
  loadWallet,
  saveAssets,
  saveNotifications,
  saveOrders,
  saveUsages
} from '@/mock/account'
import {
  adminOrders,
  adminUsageRecords,
  loadAdminTasks,
  loadAdminUsers,
  saveAdminUsers
} from '@/mock/admin'
import {
  loadAdminPlans,
  loadModerationQueue,
  loadRefunds,
  saveModerationQueue,
  saveRefunds
} from '@/mock/admin-extra'
import {
  adjustPurchased,
  rechargeToPurchased,
  refundToPools,
  spendFromPools
} from '@/mock/credits'

/**
 * 平台数据中枢（前端演示版）
 *
 * 用户端写入 account.js 的本地账本，后台通过这里读取同一份数据，
 * 保证「用户做了什么 → 后台立刻能看到」；后台的调整、退款、审核
 * 也会写回用户端，形成闭环。
 *
 * 接入真实后端后，这个文件就是唯一需要替换成 HTTP 调用的地方。
 */

const PLAN_NAMES = { free: '体验版', pro: '专业版', ultra: '旗舰版' }

function money(value) {
  return Number(Number(value || 0).toFixed(2))
}

/**
 * 用户端可见的套餐：以后台「订阅套餐」配置为准，
 * 后台停用的套餐不再展示给用户。
 */
export function loadPlans() {
  const admin = loadAdminPlans()
  const list = Array.isArray(admin) ? admin.filter(plan => plan.enabled !== false) : []
  if (list.length) return list
  return fallbackPlans
}

export function currentIdentity() {
  const profile = loadProfile()
  return {
    username: profile.username || 'demo',
    nickname: profile.nickname || '体验用户',
    email: profile.email || 'demo@lingxi.local'
  }
}

function currentPlanName() {
  const subscription = loadSubscription()
  return PLAN_NAMES[subscription.planKey] || '体验版'
}

function normalizeOrderType(description = '') {
  if (description.includes('订阅')) return '订阅'
  if (description.includes('兑换码')) return '兑换码'
  if (description.includes('充值')) return '充值'
  return '消费'
}

const ORDER_STATUS = {
  paid: 'PAID',
  pending: 'PENDING',
  cancelled: 'CANCELLED',
  refunding: 'REFUNDING',
  refunded: 'REFUNDED'
}

/* ---------- 用户 ---------- */

export function listAdminUsers() {
  const identity = currentIdentity()
  const wallet = loadWallet()
  const orders = loadOrders()
  const base = loadAdminUsers().filter(
    user => user.username !== identity.username && user.username !== 'chenyu'
  )

  const spending = orders
    .filter(order => order.status === 'paid' && normalizeOrderType(order.description) === '消费')
    .reduce((sum, order) => sum + order.amount, 0)

  const self = {
    id: 1,
    username: identity.username,
    nickname: identity.nickname,
    email: identity.email,
    role: 'USER',
    status: 'ACTIVE',
    plan: currentPlanName(),
    credits: money(wallet.balance),
    spent: money(spending),
    tasks: 3,
    registeredAt: dayjs().subtract(96, 'day').format('YYYY-MM-DD HH:mm'),
    lastActiveAt: dayjs().format('YYYY-MM-DD HH:mm')
  }

  return [self, ...base]
}

export function saveUsers(list) {
  const identity = currentIdentity()
  const self = list.find(user => user.username === identity.username)
  const others = list.filter(user => user.username !== identity.username)
  saveAdminUsers(others)
  if (self) {
    const wallet = loadWallet()
    const nextBalance = Number(self.credits)
    if (Number.isFinite(nextBalance) && nextBalance !== wallet.balance) {
      const diff = money(nextBalance - wallet.balance)
      // 余额写入统一交给积分账户，保证「购买 + 奖励」拆分不被覆盖
      const result = adjustPurchased(diff)
      if (diff !== 0) {
        appendWalletLog({
          id: `wl-${Date.now()}`,
          createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
          type: diff > 0 ? '奖励' : '消费',
          amount: diff,
          balance: result.balance,
          note: '管理员调整积分'
        })
      }
    }
  }
}

export function adjustCredits(username, amount, note = '管理员调整') {
  const identity = currentIdentity()
  if (username === identity.username) {
    const result = adjustPurchased(Number(amount))
    appendWalletLog({
      id: `wl-${Date.now()}`,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
      type: amount >= 0 ? '奖励' : '消费',
      amount: money(amount),
      balance: result.balance,
      note
    })
    notify({
      category: 'billing',
      title: '积分已调整',
      content: `管理员为你${amount >= 0 ? '增加' : '扣除'} ${Math.abs(amount)} 积分，当前余额 ${wallet.balance}。`
    })
    return wallet.balance
  }
  const users = loadAdminUsers()
  const target = users.find(user => user.username === username)
  if (target) {
    target.credits = money(Number(target.credits) + Number(amount))
    saveAdminUsers(users)
  }
  return target ? target.credits : null
}

export function setUserStatus(username, status) {
  const identity = currentIdentity()
  if (username === identity.username) {
    notify({
      category: 'system',
      title: `账号状态变更为${status === 'BANNED' ? '封禁' : '正常'}`,
      content: '如对处理结果有异议，请联系客服。'
    })
    return
  }
  const users = loadAdminUsers()
  const target = users.find(user => user.username === username)
  if (target) {
    target.status = status
    saveAdminUsers(users)
  }
}

/* ---------- 订单 ---------- */

export function listAdminOrders() {
  const identity = currentIdentity()
  const own = loadOrders().map(order => ({
    id: order.id,
    user: identity.nickname,
    username: identity.username,
    type: normalizeOrderType(order.description),
    amount: order.amount,
    payMethod: order.payMethod || '支付宝',
    status: ORDER_STATUS[order.status] || 'PAID',
    createdAt: order.createdAt
  }))
  return [...own, ...adminOrders].sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
}

/* ---------- 定时任务 ---------- */

function scheduleText(task) {
  const time = task.executeTime || '09:00'
  if (task.scheduleType === 'INTERVAL') return `每 ${task.intervalMinutes || 60} 分钟`
  if (task.scheduleType === 'HOURLY') return '每小时'
  if (task.scheduleType === 'WEEKLY') return `每周${task.weekDays || '1'} ${time}`
  if (task.scheduleType === 'CRON') return task.cronExpr || '自定义'
  return `每天 ${time}`
}

export function listAdminTasks() {
  const identity = currentIdentity()
  const own = loadDemoTasks().map(task => ({
    id: task.id,
    name: task.name,
    user: identity.nickname,
    username: identity.username,
    model: task.model,
    frequency: scheduleText(task),
    location: task.runLocation === 'CLOUD' ? '云端' : '本机',
    timezone: task.timezone || 'Asia/Shanghai',
    status: task.status,
    runCount: task.runCount || 0,
    lastStatus: '成功',
    lastRunAt: task.createdAt,
    maxRounds: 50,
    createdAt: task.createdAt
  }))
  const base = loadAdminTasks().filter(
    task => task.username !== identity.username && task.username !== 'chenyu'
  )
  return [...own, ...base]
}

/* ---------- 用量 ---------- */

export function listAdminUsageRecords() {
  const identity = currentIdentity()
  const own = loadUsages().map(usage => ({
    id: `own-${usage.id}`,
    time: usage.createdAt,
    user: identity.nickname,
    service: usage.service,
    operation: usage.operation,
    statusCode: usage.statusCode,
    duration: usage.duration,
    cost: usage.cost,
    credits: usage.cost
  }))
  return [...own, ...adminUsageRecords].sort((a, b) => (a.time < b.time ? 1 : -1))
}

/* ---------- 退款 ---------- */

export function listRefunds() {
  const identity = currentIdentity()
  const stored = loadRefunds()
  const storedOrderIds = new Set(stored.map(item => item.orderId))
  const derived = loadOrders()
    .filter(order => order.status === 'refunding' && !storedOrderIds.has(order.id))
    .map(order => ({
      id: `rf-${order.id}`,
      orderId: order.id,
      user: identity.nickname,
      username: identity.username,
      amount: order.amount,
      type: normalizeOrderType(order.description),
      reason: '用户申请退款',
      status: 'PENDING',
      appliedAt: order.createdAt
    }))
  return [...derived, ...stored]
}

export function handleRefund(refundId, action, note = '') {
  const refunds = loadRefunds()
  let target = refunds.find(item => item.id === refundId)
  if (!target) {
    // 订单已进入退款流程但退款单未落盘（写库中断）：补一条再审批，避免处理后条目凭空消失
    const derived = listRefunds().find(item => item.id === refundId)
    if (derived) {
      target = { ...derived }
      refunds.unshift(target)
    }
  }
  if (target) {
    // 已处理过的退款单不再重复审批，避免重复点击导致同一笔退款多次入账
    if (target.status && target.status !== 'PENDING') return
    target.status = action
    target.handledAt = dayjs().format('YYYY-MM-DD HH:mm')
    target.note = note
    saveRefunds(refunds)
  }

  const identity = currentIdentity()
  // 其他用户的退款只更新状态与余额，不写当前账号钱包
  const owner = target?.username || identity.username
  if (owner !== identity.username) {
    if (action === 'APPROVED' && target?.amount) {
      const users = loadAdminUsers()
      const other = users.find(user => user.username === owner)
      if (other) {
        other.credits = money(Number(other.credits) + Number(target.amount))
        saveAdminUsers(users)
      }
    }
    return
  }

  const orderId = target?.orderId || refundId.replace(/^rf-/, '')
  const order = loadOrders().find(item => item.id === orderId)
  if (!order) return

  if (action === 'APPROVED') {
    order.status = 'refunded'
    saveOrders(loadOrders().map(item => (item.id === order.id ? order : item)))
    // 派生退款单的 target 可能为 undefined（订单处于 refunding 但退款单未落盘）
    if (!target?.username || target.username === identity.username) {
      // 退款原路退回购买积分池：奖励积分不能凭空变成不过期的余额
      const result = rechargeToPurchased(order.amount)
      appendWalletLog({
        id: `wl-${Date.now()}`,
        createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
        type: '退款',
        amount: money(order.amount),
        balance: result.balance,
        note: `订单退款 · ${order.id}`
      })
      notify({
        category: 'billing',
        title: '退款已到账',
        content: `订单 ${order.id} 退款 ¥${order.amount} 已退回余额。`
      })
    }
  } else if (action === 'REJECTED') {
    order.status = 'paid'
    saveOrders(loadOrders().map(item => (item.id === order.id ? order : item)))
    if (target.username === identity.username) {
      notify({
        category: 'billing',
        title: '退款申请未通过',
        content: note || '退款申请未通过，如有疑问请联系客服。'
      })
    }
  }
}

export function requestRefund(orderId, reason) {
  const orders = loadOrders()
  const target = orders.find(order => order.id === orderId)
  if (!target) return false
  // 幂等：只有「已支付」的订单可以申请退款；refunding/refunded/pending 一律拒绝
  if (target.status !== 'paid') return false
  const refunds = loadRefunds()
  const active = refunds.find(
    item => item.orderId === target.id && (item.status === 'PENDING' || item.status === 'APPROVED')
  )
  // 同一订单已存在待审/已通过的退款单时不再新建，避免重复退款入账
  if (active) return false
  const identity = currentIdentity()
  // 先落退款单再改订单状态：顺序反过来时一旦写库中断，会残留「退款中但查不到退款单」的订单
  // id 与派生规则（rf-<orderId>）保持一致，重复申请只会覆盖同一条，不会堆积
  saveRefunds([
    {
      id: `rf-${target.id}`,
      orderId: target.id,
      user: identity.nickname,
      username: identity.username,
      amount: target.amount,
      type: normalizeOrderType(target.description),
      reason,
      status: 'PENDING',
      appliedAt: dayjs().format('YYYY-MM-DD HH:mm')
    },
    ...refunds.filter(item => item.orderId !== target.id)
  ])
  target.status = 'refunding'
  saveOrders(orders)
  return true
}

/* ---------- 内容审核 ---------- */

export function listModeration() {
  const identity = currentIdentity()
  const derived = loadAssets()
    .filter(asset => asset.status === 'PENDING' || asset.status === 'REJECTED')
    .map(asset => ({
      id: asset.id,
      type: asset.type,
      title: asset.title,
      user: identity.nickname,
      tool: asset.tool,
      prompt: asset.prompt,
      url: asset.thumb || asset.url,
      risk: '低',
      createdAt: asset.createdAt,
      status: asset.status === 'PENDING' ? 'PENDING' : 'REJECTED'
    }))
  return [...derived, ...loadModerationQueue()]
}

export function moderateAsset(id, action, reason = '') {
  const assets = loadAssets()
  const target = assets.find(asset => asset.id === id)
  if (!target) {
    const queue = loadModerationQueue()
    const item = queue.find(entry => entry.id === id)
    if (item) {
      item.status = action
      saveModerationQueue(queue)
    }
    return
  }
  target.status = action
  if (action === 'REJECTED') {
    target.trashed = false
    target.rejectReason = reason
  }
  saveAssets(assets)
  notify({
    category: 'system',
    title: action === 'APPROVED' ? '作品审核通过' : '作品未通过审核',
    content:
      action === 'APPROVED'
        ? `「${target.title}」已通过审核，可在公开画廊展示。`
        : `「${target.title}」未通过审核：${reason || '内容不符合规范'}。`
  })
}

/* ---------- 写入：用户行为 → 账本 ---------- */

export function chargeCredits({ amount, service, operation, model, duration = '—', status = 'success' }) {
  const cost = money(amount)
  // 扣费走积分池：奖励积分先过期先用，不足部分再扣购买积分
  const spend = spendFromPools(cost, { note: `${service} · ${model}`, service })
  if (!spend.ok) {
    return { ok: false, balance: spend.balance }
  }
  const balance = spend.balance

  if (cost > 0) {
    appendWalletLog({
      id: `wl-${Date.now()}`,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
      type: '消费',
      amount: -cost,
      balance,
      note: `${service} · ${model}`
    })
  }

  const usages = loadUsages()
  saveUsages([
    {
      id: `us-${Date.now()}`,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
      service,
      operation,
      statusCode: status === 'success' ? 200 : 500,
      duration,
      cost,
      balance,
      metadata: model
    },
    ...usages
  ])

  if (balance < 5) {
    notify({
      category: 'billing',
      title: '积分余额不足',
      content: `当前余额 ${balance} 积分，请及时充值以免影响任务运行。`
    })
  }

  // allocations 供调用方在需要回滚时原路退回（奖励回到原批次、购买回到购买池）
  return { ok: true, balance, allocations: spend.allocations }
}

/**
 * 退回积分：用于「已扣费但本地落盘失败」等需要回滚的场景。
 * 只冲正余额并写一条退回流水，不产生新的用量记录。
 *
 * @param allocations 可选。传入扣费时返回的分摊结果可以原路退回，
 *                    否则统一退回购买积分池。
 */
export function refundCredits({ amount, service = '系统', model = '—', reason = '异常回滚', allocations = null }) {
  const cost = money(amount)
  if (!(cost > 0)) return { ok: true, balance: loadWallet().balance }
  const result = refundToPools(cost, allocations)
  appendWalletLog({
    id: `wl-${Date.now()}`,
    createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
    type: '退回',
    amount: cost,
    balance: result.balance,
    note: `${service} · ${model} · ${reason}`
  })
  return { ok: true, balance: result.balance }
}

export function addAsset(asset) {
  const assets = loadAssets()
  saveAssets([asset, ...assets])
  notify({
    category: 'task',
    title: '生成完成',
    content: `「${asset.title}」已生成并保存到作品库，等待审核后可在画廊展示。`
  })
}

export function listRefundCandidates() {
  return loadOrders().filter(order => order.status === 'paid' && order.amount > 0)
}

export function notify({ category = 'system', title, content }) {
  const list = loadNotifications()
  saveNotifications([
    {
      id: `nt-${Date.now()}`,
      category,
      title,
      content,
      createdAt: dayjs().format('YYYY-MM-DD HH:mm'),
      read: false
    },
    ...list
  ])
}
