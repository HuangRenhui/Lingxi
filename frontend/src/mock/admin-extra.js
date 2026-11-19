import dayjs from 'dayjs'

/**
 * 运营后台扩展演示数据。
 *
 * 覆盖退款对账、兑换码、优惠券、订阅套餐、内容审核、敏感词、
 * 计费规则、操作日志、实时监控与报表。
 * 数据以 localStorage 持久化，接入后端后可直接替换对应的读写函数。
 */

const KEYS = {
  refunds: 'lingxi-admin-refunds',
  reconciliations: 'lingxi-admin-reconciliations',
  redeemCodes: 'lingxi-admin-redeem-codes',
  coupons: 'lingxi-admin-coupons',
  plans: 'lingxi-admin-plans',
  moderation: 'lingxi-admin-moderation',
  sensitiveWords: 'lingxi-admin-sensitive-words',
  billingRules: 'lingxi-admin-billing-rules',
  operationLogs: 'lingxi-admin-operation-logs'
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

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

function seed(key, data) {
  const cached = read(key)
  if (Array.isArray(cached)) return cached
  write(key, data)
  return clone(data)
}

/* ---------- 退款与对账 ---------- */

const REFUND_SEED = [
  {
    id: 'rf-3001',
    orderId: 'LX202611180106',
    user: '沈凉',
    username: 'shenliang',
    amount: 68,
    type: '订阅',
    reason: '重复购买，需要退回本月订阅费用',
    status: 'PENDING',
    appliedAt: stamp(0, 10, 24)
  },
  {
    id: 'rf-3002',
    orderId: 'LX202611170112',
    user: '林一诺',
    username: 'linyinuo',
    amount: 200,
    type: '充值',
    reason: '充值金额选错，未产生消费',
    status: 'PENDING',
    appliedAt: stamp(1, 19, 6)
  },
  {
    id: 'rf-3003',
    orderId: 'LX202611150003',
    user: '周墨',
    username: 'zhoumo',
    amount: 4.2,
    type: '积分',
    reason: '视频任务失败未出片，申请退回积分',
    status: 'APPROVED',
    appliedAt: stamp(3, 11, 32),
    handledAt: stamp(3, 14, 2)
  },
  {
    id: 'rf-3004',
    orderId: 'LX202611120008',
    user: '苏晚',
    username: 'suwan',
    amount: 168,
    type: '订阅',
    reason: '升级旗舰版后不再需要专业版',
    status: 'REJECTED',
    appliedAt: stamp(6, 9, 48),
    handledAt: stamp(6, 15, 20),
    note: '已超过 7 天无理由退款期限'
  },
  {
    id: 'rf-3005',
    orderId: 'LX202611100005',
    user: '何川',
    username: 'hechuan',
    amount: 50,
    type: '充值',
    reason: '支付渠道异常重复扣款',
    status: 'APPROVED',
    appliedAt: stamp(8, 20, 14),
    handledAt: stamp(9, 9, 30)
  }
]

const RECONCILIATION_SEED = Array.from({ length: 7 }, (_, index) => {
  const day = dayjs().subtract(index, 'day')
  const channels = ['支付宝', '微信支付']
  return channels.map((channel, ci) => {
    const receivable = [1280, 960, 1540, 880, 1720, 640, 1180][index] + ci * 320
    const diff = index === 2 && ci === 0 ? -18.6 : index === 5 && ci === 1 ? 6.2 : 0
    return {
      id: `rc-${index}-${ci}`,
      date: day.format('YYYY-MM-DD'),
      channel,
      receivable,
      received: Number((receivable + diff).toFixed(2)),
      diff,
      status: diff === 0 ? 'BALANCED' : 'DIFF'
    }
  })
})

export function loadRefunds() {
  return seed(KEYS.refunds, REFUND_SEED)
}

export function saveRefunds(list) {
  write(KEYS.refunds, list)
}

export function loadReconciliations() {
  return seed(KEYS.reconciliations, RECONCILIATION_SEED)
}

export function saveReconciliations(list) {
  write(KEYS.reconciliations, list)
}

/* ---------- 兑换码与优惠券 ---------- */

const REDEEM_SEED = [
  { code: 'LX2026-WELCOME-50', amount: 50, status: 'UNUSED', expiresAt: '2027-06-30', createdAt: stamp(30, 10, 0), usedBy: '' },
  { code: 'LX2026-PRO-300', amount: 300, status: 'USED', expiresAt: '2027-06-30', createdAt: stamp(28, 11, 20), usedBy: 'chenyu' },
  { code: 'LX2026-GIFT-100', amount: 100, status: 'UNUSED', expiresAt: '2026-12-31', createdAt: stamp(20, 15, 40), usedBy: '' },
  { code: 'LX2026-VIP-800', amount: 800, status: 'VOID', expiresAt: '2026-10-31', createdAt: stamp(60, 9, 12), usedBy: '' },
  { code: 'LX2026-TRY-20', amount: 20, status: 'USED', expiresAt: '2027-01-31', createdAt: stamp(15, 18, 6), usedBy: 'linyinuo' }
]

const COUPON_SEED = [
  {
    id: 'cp-1',
    name: '首充立减 10 元',
    type: '满减',
    value: 10,
    threshold: 50,
    validFrom: '2026-11-01',
    validTo: '2026-12-31',
    claimed: 126,
    total: 500,
    enabled: true
  },
  {
    id: 'cp-2',
    name: '专业版 8 折',
    type: '折扣',
    value: 0.8,
    threshold: 0,
    validFrom: '2026-11-11',
    validTo: '2026-11-30',
    claimed: 88,
    total: 200,
    enabled: true
  },
  {
    id: 'cp-3',
    name: '老用户充值满 300 减 30',
    type: '满减',
    value: 30,
    threshold: 300,
    validFrom: '2026-10-01',
    validTo: '2026-10-31',
    claimed: 64,
    total: 100,
    enabled: false
  }
]

export function loadRedeemCodes() {
  return seed(KEYS.redeemCodes, REDEEM_SEED)
}

export function saveRedeemCodes(list) {
  write(KEYS.redeemCodes, list)
}

export function loadCoupons() {
  return seed(KEYS.coupons, COUPON_SEED)
}

export function saveCoupons(list) {
  write(KEYS.coupons, list)
}

/* ---------- 订阅套餐 ---------- */

const PLAN_SEED = [
  {
    key: 'free',
    name: '体验版',
    monthly: 0,
    yearly: 0,
    credits: 200,
    parallelTasks: 1,
    enabled: true,
    tag: '',
    features: ['每月 200 积分', '全部对话应用', '基础图片生成', '任务保留 7 天'],
    updatedAt: stamp(40, 10, 0)
  },
  {
    key: 'pro',
    name: '专业版',
    monthly: 68,
    yearly: 680,
    credits: 3000,
    parallelTasks: 5,
    enabled: true,
    tag: '最受欢迎',
    features: ['每月 3000 积分', '视频生成 4K 输出', '并行任务 5 个', '任务保留 90 天'],
    updatedAt: stamp(12, 10, 0)
  },
  {
    key: 'ultra',
    name: '旗舰版',
    monthly: 168,
    yearly: 1680,
    credits: 12000,
    parallelTasks: 20,
    enabled: true,
    tag: '',
    features: ['每月 12000 积分', '全部模型最高档位', '并行任务 20 个', '产物永久保留'],
    updatedAt: stamp(12, 10, 0)
  }
]

export function loadAdminPlans() {
  return seed(KEYS.plans, PLAN_SEED)
}

export function saveAdminPlans(list) {
  write(KEYS.plans, list)
}

/* ---------- 内容审核 ---------- */

const MODERATION_SEED = [
  {
    id: 'md-1',
    type: 'image',
    title: '雨夜街区品牌海报',
    user: '陈屿',
    tool: 'OpenAI Image',
    prompt: '雨夜霓虹街区，品牌主视觉海报，带中文标题排版',
    url: '/images/showcase-sc-gpt-poster.jpg',
    risk: '低',
    createdAt: stamp(0, 19, 42),
    status: 'PENDING'
  },
  {
    id: 'md-2',
    type: 'video',
    title: '沙海日落环拍',
    user: '林一诺',
    tool: 'Sora',
    prompt: '云层之上的日落，镜头环绕，电影感',
    url: '/images/showcase-sc-gpt-lamp.jpg',
    risk: '低',
    createdAt: stamp(0, 16, 8),
    status: 'PENDING'
  },
  {
    id: 'md-3',
    type: 'image',
    title: '人物肖像尝试',
    user: '周墨',
    tool: 'Nano Banana',
    prompt: '写实人物肖像，室内自然光',
    url: '/images/showcase-sc-nano-portrait.jpg',
    risk: '中',
    createdAt: stamp(1, 11, 26),
    status: 'PENDING'
  },
  {
    id: 'md-4',
    type: 'image',
    title: '工业设计渲染',
    user: '苏晚',
    tool: 'SeeDream',
    prompt: '家用雨水收集器，工业设计渲染图',
    url: '/images/showcase-sc-nano-rain.jpg',
    risk: '低',
    createdAt: stamp(2, 14, 50),
    status: 'APPROVED'
  },
  {
    id: 'md-5',
    type: 'video',
    title: '水墨转场素材',
    user: '何川',
    tool: 'SeeDance',
    prompt: '水墨笔触流动转场，东方风格',
    url: '/images/showcase-sc-seedance-ink.jpg',
    risk: '低',
    createdAt: stamp(3, 10, 12),
    status: 'APPROVED'
  },
  {
    id: 'md-6',
    type: 'image',
    title: '竞品对比图',
    user: '陈屿',
    tool: 'Ideogram',
    prompt: '产品对比海报，含品牌标识',
    url: '/images/showcase-sc-maestro-china.jpg',
    risk: '高',
    createdAt: stamp(4, 17, 34),
    status: 'REJECTED'
  }
]

const WORD_SEED = [
  { id: 'sw-1', word: '违禁品交易', category: '交易', level: '高', hits: 12, enabled: true, createdAt: stamp(30, 9, 0) },
  { id: 'sw-2', word: '暴力血腥', category: '暴力', level: '高', hits: 8, enabled: true, createdAt: stamp(30, 9, 2) },
  { id: 'sw-3', word: '政治敏感', category: '政治', level: '高', hits: 3, enabled: true, createdAt: stamp(30, 9, 4) },
  { id: 'sw-4', word: '赌博推广', category: '违法', level: '高', hits: 21, enabled: true, createdAt: stamp(28, 10, 0) },
  { id: 'sw-5', word: '银行卡号', category: '隐私', level: '中', hits: 6, enabled: true, createdAt: stamp(25, 11, 0) },
  { id: 'sw-6', word: '身份证照片', category: '隐私', level: '中', hits: 4, enabled: true, createdAt: stamp(25, 11, 2) },
  { id: 'sw-7', word: '代开发票', category: '交易', level: '中', hits: 9, enabled: true, createdAt: stamp(20, 14, 0) },
  { id: 'sw-8', word: '刷单返利', category: '交易', level: '中', hits: 15, enabled: true, createdAt: stamp(18, 16, 0) },
  { id: 'sw-9', word: '黑客接单', category: '违法', level: '高', hits: 2, enabled: false, createdAt: stamp(15, 10, 0) },
  { id: 'sw-10', word: '软色情', category: '色情', level: '高', hits: 7, enabled: true, createdAt: stamp(12, 10, 0) }
]

export function loadModerationQueue() {
  return seed(KEYS.moderation, MODERATION_SEED)
}

export function saveModerationQueue(list) {
  write(KEYS.moderation, list)
}

export function loadSensitiveWords() {
  return seed(KEYS.sensitiveWords, WORD_SEED)
}

export function saveSensitiveWords(list) {
  write(KEYS.sensitiveWords, list)
}

/* ---------- 计费规则 ---------- */

const BILLING_SEED = [
  { id: 'br-1', category: '对话', target: 'ChatGPT', model: 'GPT-6 Astra', unit: '1K tokens', price: 0.02, multiplier: 1, enabled: true, updatedAt: stamp(6, 11, 0) },
  { id: 'br-2', category: '对话', target: 'ChatGPT', model: 'GPT-5.6 Luna', unit: '1K tokens', price: 0.004, multiplier: 1, enabled: true, updatedAt: stamp(6, 11, 0) },
  { id: 'br-3', category: '对话', target: 'Claude', model: 'Claude Opus 5.5', unit: '1K tokens', price: 0.03, multiplier: 1.2, enabled: true, updatedAt: stamp(4, 9, 30) },
  { id: 'br-4', category: '对话', target: 'DeepSeek', model: 'DeepSeek V4 Pro', unit: '1K tokens', price: 0.002, multiplier: 1, enabled: true, updatedAt: stamp(4, 9, 30) },
  { id: 'br-5', category: '图片', target: 'Nano Banana', model: 'nano-banana', unit: '张', price: 0.14, multiplier: 1, enabled: true, updatedAt: stamp(3, 14, 0) },
  { id: 'br-6', category: '图片', target: 'OpenAI Image', model: 'gpt-image-2', unit: '张', price: 0.19, multiplier: 1, enabled: true, updatedAt: stamp(3, 14, 0) },
  { id: 'br-7', category: '图片', target: 'SeeDream', model: 'doubao-seedream-5.0-lite', unit: '张', price: 0.37, multiplier: 1, enabled: true, updatedAt: stamp(3, 14, 0) },
  { id: 'br-8', category: '视频', target: 'SeeDance', model: 'doubao-seedance-2.0', unit: '5s', price: 8.4, multiplier: 1, enabled: true, updatedAt: stamp(2, 10, 20) },
  { id: 'br-9', category: '视频', target: 'Kling', model: 'v2.5-Turbo', unit: '5s', price: 2.1, multiplier: 1, enabled: true, updatedAt: stamp(2, 10, 20) },
  { id: 'br-10', category: '视频', target: 'Sora', model: 'Sora 2', unit: '10s', price: 4.2, multiplier: 1, enabled: false, updatedAt: stamp(9, 16, 0) },
  { id: 'br-11', category: '音乐', target: 'Suno', model: 'v6', unit: '首', price: 0.56, multiplier: 1, enabled: true, updatedAt: stamp(5, 15, 40) },
  { id: 'br-12', category: '音乐', target: 'Producer', model: 'FUZZ-2.0 Pro', unit: '首', price: 0.58, multiplier: 1, enabled: true, updatedAt: stamp(5, 15, 40) }
]

export function loadBillingRules() {
  return seed(KEYS.billingRules, BILLING_SEED)
}

export function saveBillingRules(list) {
  write(KEYS.billingRules, list)
}

/* ---------- 操作日志 ---------- */

const LOG_SEED = [
  { id: 'lg-1', createdAt: stamp(0, 20, 12), operator: 'admin', module: '用户管理', action: '重置密码', target: 'chenyu', ip: '192.168.1.24', result: 'SUCCESS', detail: '管理员触发密码重置，已发送一次性链接' },
  { id: 'lg-2', createdAt: stamp(0, 18, 30), operator: 'admin', module: '积分管理', action: '调整积分', target: 'linyinuo', ip: '192.168.1.24', result: 'SUCCESS', detail: '增加 200 积分，备注：活动补偿' },
  { id: 'lg-3', createdAt: stamp(0, 15, 4), operator: 'admin', module: '支付平台', action: '更新渠道配置', target: '支付宝', ip: '192.168.1.24', result: 'SUCCESS', detail: '费率 3.6% → 3.2%，结算周期 T+1' },
  { id: 'lg-4', createdAt: stamp(0, 11, 46), operator: 'ops', module: '模型接入', action: '切换速率档位', target: 'OpenAI', ip: '10.0.3.18', result: 'SUCCESS', detail: '正常 → 降速，RPM 1200 → 600' },
  { id: 'lg-5', createdAt: stamp(0, 9, 20), operator: 'admin', module: '内容审核', action: '驳回产物', target: 'md-6', ip: '192.168.1.24', result: 'SUCCESS', detail: '驳回原因：含未授权品牌标识' },
  { id: 'lg-6', createdAt: stamp(1, 21, 8), operator: 'ops', module: '系统设置', action: '开启维护模式', target: '站点', ip: '10.0.3.18', result: 'FAILED', detail: '校验失败：存在运行中任务，操作被拒绝' },
  { id: 'lg-7', createdAt: stamp(1, 16, 32), operator: 'admin', module: '订单管理', action: '导出订单', target: '2026-11 已完成', ip: '192.168.1.24', result: 'SUCCESS', detail: '导出 42 条记录' },
  { id: 'lg-8', createdAt: stamp(2, 10, 15), operator: 'admin', module: '订阅套餐', action: '修改套餐价格', target: '专业版', ip: '192.168.1.24', result: 'SUCCESS', detail: '月付 ¥78 → ¥68' },
  { id: 'lg-9', createdAt: stamp(2, 9, 2), operator: 'system', module: '调度器', action: '自动重试', target: 'tk-2048', ip: '127.0.0.1', result: 'SUCCESS', detail: '第 2 轮重试成功' },
  { id: 'lg-10', createdAt: stamp(3, 19, 40), operator: 'admin', module: '推荐管理', action: '发放奖励', target: 'invite-8821', ip: '192.168.1.24', result: 'SUCCESS', detail: '发放 20 积分' },
  { id: 'lg-11', createdAt: stamp(4, 14, 22), operator: 'ops', module: '支付平台', action: '测试渠道', target: '微信支付 · 沙箱', ip: '10.0.3.18', result: 'SUCCESS', detail: '回调验签通过，耗时 320ms' },
  { id: 'lg-12', createdAt: stamp(5, 11, 10), operator: 'admin', module: '用户管理', action: '封禁账号', target: 'spam-bot-01', ip: '192.168.1.24', result: 'SUCCESS', detail: '原因：批量注册刷积分' }
]

export function loadOperationLogs() {
  return seed(KEYS.operationLogs, LOG_SEED)
}

export function saveOperationLogs(list) {
  write(KEYS.operationLogs, list)
}

/* ---------- 实时监控 ---------- */

export const monitorQueue = [
  { id: 'q-1', name: '每日资讯简报', user: '陈屿', round: '3 / 6', status: 'RUNNING', startedAt: stamp(0, 20, 10), duration: '1m 42s', location: '云端' },
  { id: 'q-2', name: '竞品动态跟踪', user: '林一诺', round: '1 / 4', status: 'RUNNING', startedAt: stamp(0, 20, 18), duration: '0m 36s', location: '本机' },
  { id: 'q-3', name: '数据日报', user: '周墨', round: '0 / 3', status: 'PENDING', startedAt: '', duration: '—', location: '云端' },
  { id: 'q-4', name: '社媒内容排期', user: '苏晚', round: '6 / 6', status: 'SUCCESS', startedAt: stamp(0, 19, 2), duration: '4m 08s', location: '云端' },
  { id: 'q-5', name: '长文摘要', user: '何川', round: '2 / 5', status: 'FAILED', startedAt: stamp(0, 18, 44), duration: '2m 12s', location: '本机' }
]

export const channelHealth = [
  { name: 'OpenAI', requests: 12840, successRate: 99.6, latency: 820, status: 'HEALTHY', note: '主通道' },
  { name: 'Anthropic', requests: 6420, successRate: 99.2, latency: 1160, status: 'HEALTHY', note: '主通道' },
  { name: 'DeepSeek', requests: 9830, successRate: 99.8, latency: 540, status: 'HEALTHY', note: '主通道' },
  { name: '火山引擎', requests: 5210, successRate: 97.4, latency: 1480, status: 'SLOW', note: '已自动降速' },
  { name: 'Midjourney', requests: 1860, successRate: 92.1, latency: 3260, status: 'DEGRADED', note: '队列积压' }
]

export const hourlyPoints = Array.from({ length: 12 }, (_, index) => {
  const hour = (index * 2 + 1) % 24
  const requests = [120, 86, 64, 52, 78, 210, 386, 452, 398, 512, 468, 320][index]
  return {
    date: `${String(hour).padStart(2, '0')}:00`,
    requests,
    credits: Number((requests * 0.31).toFixed(2))
  }
})

/* ---------- 报表 ---------- */

export const reportKpis = {
  revenue: { total: 28640.5, month: 6820, growth: 12.4 },
  usage: { requests: 128460, credits: 42380, growth: 8.6 },
  user: { total: 1286, newThisMonth: 96, retention: 63.2 }
}

export const revenuePoints = Array.from({ length: 12 }, (_, index) => ({
  date: dayjs().subtract(11 - index, 'month').format('YYYY-MM'),
  requests: [4200, 4680, 5120, 4860, 5640, 6120, 6480, 6820, 7240, 6980, 7520, 8180][index],
  credits: [1820, 1980, 2210, 2080, 2460, 2680, 2840, 2960, 3180, 3020, 3340, 3620][index]
}))

export const topModels = [
  { name: 'GPT-6 Astra', category: '对话', requests: 38620, credits: 12480, share: 30.1 },
  { name: 'DeepSeek V4 Pro', category: '对话', requests: 29410, credits: 6820, share: 22.9 },
  { name: 'doubao-seedream-5.0-lite', category: '图片', requests: 18640, credits: 9420, share: 14.5 },
  { name: 'doubao-seedance-2.0', category: '视频', requests: 9820, credits: 11260, share: 7.6 },
  { name: 'nano-banana', category: '图片', requests: 8410, credits: 3260, share: 6.5 },
  { name: 'Suno v6', category: '音乐', requests: 5240, credits: 2940, share: 4.1 }
]

export const topUsers = [
  { name: '陈屿', username: 'chenyu', plan: '专业版', requests: 4860, credits: 1284, amount: 268 },
  { name: '林一诺', username: 'linyinuo', plan: '旗舰版', requests: 3920, credits: 2160, amount: 1680 },
  { name: '周墨', username: 'zhoumo', plan: '专业版', requests: 2840, credits: 860, amount: 68 },
  { name: '苏晚', username: 'suwan', plan: '专业版', requests: 1960, credits: 640, amount: 68 },
  { name: '何川', username: 'hechuan', plan: '体验版', requests: 820, credits: 180, amount: 0 }
]
