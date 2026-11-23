import dayjs from 'dayjs'

/**
 * 个人空间演示数据。
 *
 * 后端暂未提供作品、钱包、订阅与账号相关接口，
 * 这里用 localStorage 模拟，刷新后保留用户改动，便于完整体验交互。
 */

const KEYS = {
  assets: 'lingxi-assets',
  wallet: 'lingxi-wallet',
  walletLogs: 'lingxi-wallet-logs',
  orders: 'lingxi-orders',
  usages: 'lingxi-usages',
  taskRuns: 'lingxi-task-runs',
  demoTasks: 'lingxi-agent-tasks',
  subscription: 'lingxi-subscription',
  planHistory: 'lingxi-plan-history',
  notifications: 'lingxi-notifications',
  devices: 'lingxi-devices',
  profile: 'lingxi-profile',
  security: 'lingxi-security'
}

function stamp(daysAgo, hour = 10, minute = 0) {
  return dayjs().subtract(daysAgo, 'day').hour(hour).minute(minute).second(0).format('YYYY-MM-DD HH:mm')
}

function read(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

function write(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (error) {
    // 本地存储不可用时忽略，保证页面交互不中断
  }
}

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

/* ---------- 作品库 ---------- */

const ASSET_SEED = [
  {
    id: 'as-1001',
    type: 'image',
    toolKey: 'nanobanana',
    tool: 'Nano Banana',
    title: '蓝调雪后山地旅舍',
    prompt: '覆雪木石旅舍坐落冷蓝山林，窗内暖光与入口灯笼，安静归宿感，电影感',
    url: '/images/showcase-sc-nano-lodge.jpg',
    size: '2.4 MB',
    createdAt: stamp(0, 20, 12),
    favorite: true,
    trashed: false
  },
  {
    id: 'as-1002',
    type: 'image',
    toolKey: 'seedream',
    tool: 'SeeDream',
    title: '星河沙海旅人营地',
    prompt: '月隐沙海上银河横贯夜空，毡帐、骆驼与微小旅人围绕暖色营火休憩，超广角',
    url: '/images/showcase-sc-seedream-desert.jpg',
    size: '3.1 MB',
    createdAt: stamp(0, 18, 40),
    favorite: false,
    trashed: false
  },
  {
    id: 'as-1003',
    type: 'video',
    toolKey: 'seedance',
    tool: 'SeeDance',
    title: '墨河山径初醒',
    prompt: '黑色河流笔触向下流动，纸层山峦徐徐展开，朱红小径从雾中浮现，东方水墨动画',
    url: '/media/demo-video-ink.mp4',
    poster: '/images/showcase-sc-seedance-ink.jpg',
    size: '18.6 MB',
    createdAt: stamp(1, 15, 22),
    favorite: true,
    trashed: false
  },
  {
    id: 'as-1004',
    type: 'image',
    toolKey: 'openai-image',
    tool: 'OpenAI Image',
    title: '静默之形陶艺展海报',
    prompt: '静默之形陶艺展海报，大量留白，器物轮廓，中文标题排版克制清晰，极简设计',
    url: '/images/showcase-sc-gpt-poster.jpg',
    size: '1.8 MB',
    createdAt: stamp(2, 11, 5),
    favorite: false,
    trashed: false
  },
  {
    id: 'as-1005',
    type: 'music',
    toolKey: 'suno',
    tool: 'Suno',
    title: '雨夜便利店（Demo）',
    prompt: 'City Pop 风格，雨夜霓虹，温暖女声，副歌带合成器',
    url: '/media/demo-music.wav',
    size: '5.2 MB',
    createdAt: stamp(3, 21, 30),
    favorite: true,
    trashed: false
  },
  {
    id: 'as-1006',
    type: 'video',
    toolKey: 'sora',
    tool: 'Sora',
    title: '沙海日落环拍',
    prompt: '云层之上的日落，镜头缓慢环绕，暖调电影质感',
    url: '/media/demo-video-desert.mp4',
    poster: '/images/showcase-sc-gpt-lamp.jpg',
    size: '22.4 MB',
    createdAt: stamp(4, 9, 18),
    favorite: false,
    trashed: false
  },
  {
    id: 'as-1007',
    type: 'image',
    toolKey: 'flux',
    tool: 'Flux',
    title: '清晨玻璃温室',
    prompt: '清晨薄雾中的玻璃温室，露水与丁达尔光，写实摄影',
    url: '/images/showcase-sc-nano-rain.jpg',
    size: '2.0 MB',
    createdAt: stamp(6, 8, 46),
    favorite: false,
    trashed: true
  },
  {
    id: 'as-1008',
    type: 'image',
    toolKey: 'ideogram',
    tool: 'Ideogram',
    title: '爵士音乐节海报',
    prompt: '瑞士风格爵士音乐节海报，标题排版醒目，红黑对比',
    url: '/images/showcase-sc-maestro-china.jpg',
    size: '1.6 MB',
    createdAt: stamp(8, 16, 12),
    favorite: false,
    trashed: true
  }
]

export function loadAssets() {
  const cached = read(KEYS.assets, null)
  const source = Array.isArray(cached) ? cached : ASSET_SEED
  if (!Array.isArray(cached)) write(KEYS.assets, ASSET_SEED)
  // 统一补齐后端 VO 字段，避免演示数据与真实数据两套形状互相踩踏
  return clone(source).map(item => ({
    ...item,
    type: String(item.type || '').toUpperCase(),
    toolName: item.toolName || item.tool || '',
    posterUrl: item.posterUrl || item.poster || '',
    sizeBytes: Number(item.sizeBytes || 0),
    favorite: item.favorite === true || Number(item.favorite) === 1,
    trashed: item.trashed === true || Number(item.trashed) === 1
  }))
}

export function saveAssets(list) {
  write(KEYS.assets, list)
}

/* ---------- 钱包 ---------- */

const WALLET_SEED = {
  balance: 128.6,
  frozen: 6.4,
  totalRecharge: 500,
  totalSpent: 371.4
}

// 按时间倒序排列，balance 为每笔变动后的余额，保证首条与钱包余额一致
const WALLET_LOG_SEED = [
  { id: 'wl-2001', createdAt: stamp(0, 20, 14), type: '消费', amount: -2.1, balance: 128.6, note: 'Kling · 视频生成' },
  { id: 'wl-2002', createdAt: stamp(0, 18, 44), type: '消费', amount: -0.37, balance: 130.7, note: 'SeeDream · 图片生成' },
  { id: 'wl-2003', createdAt: stamp(1, 15, 26), type: '消费', amount: -8.4, balance: 131.07, note: 'SeeDance · 视频生成' },
  { id: 'wl-2004', createdAt: stamp(2, 10, 2), type: '充值', amount: 30, balance: 139.47, note: '支付宝充值 · ¥30.00' },
  { id: 'wl-2005', createdAt: stamp(2, 10, 1), type: '奖励', amount: 20, balance: 109.47, note: '邀请好友注册奖励' },
  { id: 'wl-2006', createdAt: stamp(3, 21, 35), type: '消费', amount: -0.56, balance: 89.47, note: 'Suno · 音乐生成' },
  { id: 'wl-2007', createdAt: stamp(5, 14, 8), type: '退款', amount: 4.2, balance: 90.03, note: 'Sora 任务失败自动退回' },
  { id: 'wl-2008', createdAt: stamp(7, 9, 30), type: '充值', amount: 50, balance: 85.83, note: '微信支付充值 · ¥50.00' },
  { id: 'wl-2009', createdAt: stamp(9, 19, 12), type: '消费', amount: -3.84, balance: 35.83, note: 'MiniMax H3 · 视频生成' }
]

export function loadWallet() {
  const cached = read(KEYS.wallet, null)
  const source = cached && typeof cached === 'object' && !Array.isArray(cached) ? cached : null
  // 缓存被写成 {} 时四个字段全是 undefined，会呈现「余额 0」这种误导态，此时回落到种子
  const valid =
    source &&
    ['balance', 'frozen', 'totalRecharge', 'totalSpent'].some(key => Number.isFinite(Number(source[key])))
  const base = valid ? source : clone(WALLET_SEED)
  // 旧缓存可能缺少后续新增字段，统一按数字归一，避免页面出现 NaN 并回写污染数据
  return {
    ...base,
    balance: Number(base.balance) || 0,
    frozen: Number(base.frozen) || 0,
    totalRecharge: Number(base.totalRecharge) || 0,
    totalSpent: Number(base.totalSpent) || 0
  }
}

export function saveWallet(wallet) {
  write(KEYS.wallet, wallet)
  // 通知布局层刷新余额胶囊，避免跨页面操作后显示滞后
  try {
    window.dispatchEvent(new CustomEvent('lingxi:wallet-changed'))
  } catch (error) {
    // 非浏览器环境忽略
  }
}

export function loadWalletLogs() {
  const cached = read(KEYS.walletLogs, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.walletLogs, WALLET_LOG_SEED)
  return clone(WALLET_LOG_SEED)
}

export function saveWalletLogs(list) {
  write(KEYS.walletLogs, list)
}

export function appendWalletLog(log) {
  const list = loadWalletLogs()
  const next = [log, ...list].slice(0, 200)
  saveWalletLogs(next)
  return next
}

/* ---------- 订阅 ---------- */

export const plans = [
  {
    key: 'free',
    name: '体验版',
    monthly: 0,
    yearly: 0,
    credits: 200,
    tag: '',
    features: ['每月 200 积分', '全部对话应用', '基础图片生成', '任务最多保留 7 天', '社区支持']
  },
  {
    key: 'pro',
    name: '专业版',
    monthly: 68,
    yearly: 680,
    credits: 3000,
    tag: '最受欢迎',
    features: [
      '每月 3000 积分',
      '全部对话与创作工具',
      '视频生成 4K 输出',
      '并行任务 5 个',
      '定时任务保留 90 天',
      '优先客服支持'
    ]
  },
  {
    key: 'ultra',
    name: '旗舰版',
    monthly: 168,
    yearly: 1680,
    credits: 12000,
    tag: '',
    features: [
      '每月 12000 积分',
      '全部模型与最高档位',
      '并行任务 20 个',
      '任务与产物永久保留',
      '自定义 Skills 与连接器',
      '专属技术支持'
    ]
  }
]

const SUBSCRIPTION_SEED = {
  planKey: 'pro',
  cycle: 'monthly',
  status: 'ACTIVE',
  startedAt: stamp(12, 10, 0),
  expiresAt: dayjs().add(18, 'day').format('YYYY-MM-DD'),
  creditsPerMonth: 3000,
  creditsUsed: 1284,
  autoRenew: true
}

export function loadSubscription() {
  const cached = read(KEYS.subscription, null)
  // 缓存被写成数组/字符串/空对象时逐字段回落：只判 typeof 会让 {} 通过并渲染出 Invalid Date 与 NaN%
  const source = cached && typeof cached === 'object' && !Array.isArray(cached) ? cached : {}
  const merged = { ...clone(SUBSCRIPTION_SEED), ...source }
  merged.creditsPerMonth = Number(merged.creditsPerMonth) || 0
  merged.creditsUsed = Number(merged.creditsUsed) || 0
  merged.autoRenew = Boolean(merged.autoRenew)
  if (!merged.planKey) merged.planKey = SUBSCRIPTION_SEED.planKey
  if (!merged.status) merged.status = SUBSCRIPTION_SEED.status
  if (!merged.startedAt) merged.startedAt = SUBSCRIPTION_SEED.startedAt
  if (!merged.expiresAt) merged.expiresAt = SUBSCRIPTION_SEED.expiresAt
  return merged
}

export function saveSubscription(subscription) {
  write(KEYS.subscription, subscription)
}

export function loadPlanHistory() {
  const cached = read(KEYS.planHistory, null)
  if (Array.isArray(cached)) return cached
  const seed = [
    { id: 'ph-1', planName: '专业版', cycle: '月付', amount: 68, operator: '本人', status: '已完成', createdAt: stamp(12, 10, 0) },
    { id: 'ph-2', planName: '体验版', cycle: '免费', amount: 0, operator: '本人', status: '已完成', createdAt: stamp(96, 14, 36) }
  ]
  write(KEYS.planHistory, seed)
  return clone(seed)
}

export function appendPlanHistory(record) {
  const list = loadPlanHistory()
  const next = [record, ...list]
  write(KEYS.planHistory, next)
  return next
}

/* ---------- 通知 ---------- */

const NOTIFICATION_SEED = [
  {
    id: 'nt-1',
    category: 'task',
    title: '定时任务「每日资讯简报」运行完成',
    content: '本轮共执行 6 轮，产物已保存到作品库，可在任务详情中查看运行记录。',
    createdAt: stamp(0, 9, 12),
    read: false
  },
  {
    id: 'nt-2',
    category: 'billing',
    title: '订阅即将到期',
    content: '专业版将于 18 天后到期，续费后将继续保留 5 个并行任务额度。',
    createdAt: stamp(0, 8, 30),
    read: false
  },
  {
    id: 'nt-3',
    category: 'system',
    title: '新模型上线：Claude Opus 5.5',
    content: 'Claude 应用已支持 Opus 5.5，擅长复杂智能体任务与长程推理。',
    createdAt: stamp(1, 20, 6),
    read: false
  },
  {
    id: 'nt-4',
    category: 'billing',
    title: '退款已到账',
    content: 'Sora 任务「沙海日落环拍」因超时失败，已自动退回 4.20 积分。',
    createdAt: stamp(5, 14, 12),
    read: true
  },
  {
    id: 'nt-5',
    category: 'task',
    title: '定时任务「竞品动态跟踪」失败',
    content: '本机执行通道未连接，任务已跳过本轮，可在任务页切换为云端执行。',
    createdAt: stamp(6, 7, 45),
    read: true
  },
  {
    id: 'nt-6',
    category: 'system',
    title: '安全提醒：新设备登录',
    content: '检测到 iPhone · Safari 在杭州登录，如非本人操作请及时修改密码。',
    createdAt: stamp(9, 22, 18),
    read: true
  },
  {
    id: 'nt-7',
    category: 'billing',
    title: '充值成功',
    content: '支付宝充值 ¥200.00 已到账，当前余额 139.47 积分。',
    createdAt: stamp(2, 10, 2),
    read: true
  },
  {
    id: 'nt-8',
    category: 'system',
    title: '功能更新：作品库支持批量下载',
    content: '现在可以在作品库多选后一次打包下载，支持图片、视频与音频。',
    createdAt: stamp(12, 11, 20),
    read: true
  }
]

export function loadNotifications() {
  const cached = read(KEYS.notifications, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.notifications, NOTIFICATION_SEED)
  return clone(NOTIFICATION_SEED)
}

export function saveNotifications(list) {
  write(KEYS.notifications, list)
}

/* ---------- 账号与安全 ---------- */

const PROFILE_SEED = {
  nickname: '陈屿',
  username: 'chenyu',
  email: 'chenyu@example.com',
  phone: '138****6021',
  bio: '用 AI 做内容与产品设计，长期折腾自动化工作流。',
  timezone: 'Asia/Shanghai',
  language: '简体中文'
}

export function loadProfile() {
  const cached = read(KEYS.profile, null)
  // 空对象也能通过 typeof 校验，必须逐字段确认是字符串，否则昵称/邮箱会渲染成 undefined
  const source = cached && typeof cached === 'object' && !Array.isArray(cached) ? cached : {}
  const merged = clone(PROFILE_SEED)
  Object.keys(PROFILE_SEED).forEach(key => {
    if (typeof source[key] === 'string') merged[key] = source[key]
  })
  return merged
}

export function saveProfile(profile) {
  write(KEYS.profile, profile)
}

const SECURITY_SEED = {
  twoFactor: false,
  loginAlert: true,
  passwordUpdatedAt: stamp(32, 10, 12)
}

export function loadSecurity() {
  const cached = read(KEYS.security, null)
  const source = cached && typeof cached === 'object' && !Array.isArray(cached) ? cached : {}
  // 开关必须是布尔值、时间必须是字符串，否则页面会渲染出异常文案
  return {
    twoFactor: typeof source.twoFactor === 'boolean' ? source.twoFactor : SECURITY_SEED.twoFactor,
    loginAlert: typeof source.loginAlert === 'boolean' ? source.loginAlert : SECURITY_SEED.loginAlert,
    passwordUpdatedAt:
      typeof source.passwordUpdatedAt === 'string' ? source.passwordUpdatedAt : SECURITY_SEED.passwordUpdatedAt
  }
}

export function saveSecurity(security) {
  write(KEYS.security, security)
}

const DEVICE_SEED = [
  {
    id: 'dv-1',
    name: 'Windows · Chrome',
    os: 'Windows 11',
    browser: 'Chrome 141',
    ip: '192.168.1.24',
    location: '本机',
    lastActiveAt: stamp(0, 20, 30),
    current: true
  },
  {
    id: 'dv-2',
    name: 'iPhone · Safari',
    os: 'iOS 19',
    browser: 'Safari',
    ip: '117.136.12.88',
    location: '杭州',
    lastActiveAt: stamp(9, 22, 18),
    current: false
  },
  {
    id: 'dv-3',
    name: 'macOS · Chrome',
    os: 'macOS 15',
    browser: 'Chrome 140',
    ip: '58.60.1.203',
    location: '深圳',
    lastActiveAt: stamp(21, 15, 6),
    current: false
  }
]

export function loadDevices() {
  const cached = read(KEYS.devices, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.devices, DEVICE_SEED)
  return clone(DEVICE_SEED)
}

export function saveDevices(list) {
  write(KEYS.devices, list)
}

/* ---------- 概览辅助数据 ---------- */

export const recentUsages = [
  { id: 'u-1', createdAt: stamp(0, 20, 14), app: 'Kling', model: 'v2.5-Turbo', cost: 2.1, duration: '48s', status: '成功' },
  { id: 'u-2', createdAt: stamp(0, 18, 44), app: 'SeeDream', model: 'doubao-seedream-5.0-lite', cost: 0.37, duration: '12s', status: '成功' },
  { id: 'u-3', createdAt: stamp(0, 11, 2), app: 'ChatGPT', model: 'GPT-6 Astra', cost: 0.12, duration: '6s', status: '成功' },
  { id: 'u-4', createdAt: stamp(1, 15, 26), app: 'SeeDance', model: 'doubao-seedance-2.0', cost: 8.4, duration: '96s', status: '成功' },
  { id: 'u-5', createdAt: stamp(2, 16, 40), app: 'Suno', model: 'v6', cost: 0.56, duration: '35s', status: '成功' },
  { id: 'u-6', createdAt: stamp(3, 9, 18), app: 'Sora', model: 'Sora 2', cost: 4.2, duration: '—', status: '失败' }
]

export const runningTasks = [
  { id: 'tk-1', name: '每日资讯简报', schedule: '每天 09:00', location: '云端', status: 'RUNNING', lastRunAt: stamp(0, 9, 12) },
  { id: 'tk-2', name: '竞品动态跟踪', schedule: '每 6 小时', location: '本机', status: 'PENDING', lastRunAt: stamp(1, 18, 0) },
  { id: 'tk-3', name: '社媒内容排期', schedule: '每周一 10:00', location: '云端', status: 'SUCCESS', lastRunAt: stamp(2, 10, 4) }
]

export const usagePoints = Array.from({ length: 14 }, (_, index) => {
  const day = dayjs().subtract(13 - index, 'day')
  const requests = [6, 9, 4, 12, 8, 15, 11, 7, 18, 14, 9, 21, 16, 12][index]
  return { date: day.format('MM-DD'), requests, credits: Number((requests * 0.42).toFixed(2)) }
})

/* ---------- 订单 ---------- */

const ORDER_SEED = [
  { id: 'LX202611180006', amount: 68, description: '专业版订阅 · 月付', status: 'paid', payMethod: '支付宝', createdAt: stamp(0, 10, 24) },
  { id: 'LX202611170012', amount: 200, description: '积分充值', status: 'paid', payMethod: '微信支付', createdAt: stamp(1, 19, 6) },
  { id: 'LX202611150003', amount: 8.4, description: 'SeeDance · 视频生成', status: 'paid', payMethod: '支付宝', createdAt: stamp(3, 11, 32) },
  { id: 'LX202611130009', amount: 50, description: '积分充值', status: 'pending', payMethod: '微信支付', createdAt: stamp(5, 9, 18) },
  { id: 'LX202611090004', amount: 100, description: '兑换码兑换 · 100 积分', status: 'paid', payMethod: '兑换码', createdAt: stamp(9, 14, 20) },
  { id: 'LX202611020001', amount: 68, description: '专业版订阅 · 月付', status: 'paid', payMethod: '支付宝', createdAt: stamp(16, 10, 0) },
  { id: 'LX202610280007', amount: 30, description: '积分充值', status: 'cancelled', payMethod: '支付宝', createdAt: stamp(21, 16, 42) }
]

export function loadOrders() {
  const cached = read(KEYS.orders, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.orders, ORDER_SEED)
  return clone(ORDER_SEED)
}

export function saveOrders(list) {
  write(KEYS.orders, list)
}

export function appendOrder(order) {
  const list = loadOrders()
  const next = [order, ...list].slice(0, 200)
  saveOrders(next)
  return next
}

/* ---------- 使用记录 ---------- */

const USAGE_SEED = Array.from({ length: 18 }, (_, index) => {
  const templates = [
    { service: '对话', operation: 'chat/completions', model: 'GPT-6 Astra', cost: 0.12 },
    { service: '图像生成', operation: 'images/generations', model: 'nano-banana', cost: 0.14 },
    { service: '视频生成', operation: 'videos/generations', model: 'doubao-seedance-2.0', cost: 8.4 },
    { service: '对话', operation: 'chat/completions', model: 'DeepSeek V4 Pro', cost: 0.04 },
    { service: '图像生成', operation: 'images/generations', model: 'doubao-seedream-5.0-lite', cost: 0.37 },
    { service: '音乐生成', operation: 'music/generations', model: 'Suno v6', cost: 0.56 },
    { service: '视频生成', operation: 'videos/generations', model: 'Kling v2.5-Turbo', cost: 2.1 }
  ]
  const template = templates[index % templates.length]
  const failed = index % 9 === 4
  return {
    id: `us-${3001 + index}`,
    createdAt: stamp(Math.floor(index / 3), 9 + (index % 9), (index * 13) % 60),
    service: template.service,
    operation: template.operation,
    statusCode: failed ? 500 : 200,
    duration: failed ? '—' : `${6 + index * 3}s`,
    cost: failed ? 0 : template.cost,
    balance: Number(Math.max(0, 128.6 + index * 0.8).toFixed(2)),
    metadata: template.model
  }
})

export function loadUsages() {
  const cached = read(KEYS.usages, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.usages, USAGE_SEED)
  return clone(USAGE_SEED)
}

export function saveUsages(list) {
  write(KEYS.usages, list)
}

/* ---------- 推荐收益 ---------- */

export const referralSummary = {
  totalAmount: 4280,
  totalReward: 342.4,
  rate: 8,
  invitees: 12,
  level: 'L2',
  nextLevel: 'L3',
  progress: 40,
  needMore: 3
}

export const referralRecords = [
  { id: 'rf-1', name: '林一诺', username: 'linyinuo', registeredAt: stamp(2, 14, 8), status: '有效', spent: 1680, reward: 134.4, level: 'L1' },
  { id: 'rf-2', name: '周墨', username: 'zhoumo', registeredAt: stamp(6, 10, 26), status: '有效', spent: 860, reward: 68.8, level: 'L1' },
  { id: 'rf-3', name: '苏晚', username: 'suwan', registeredAt: stamp(11, 20, 2), status: '有效', spent: 640, reward: 51.2, level: 'L1' },
  { id: 'rf-4', name: '何川', username: 'hechuan', registeredAt: stamp(15, 9, 44), status: '有效', spent: 520, reward: 41.6, level: 'L2' },
  { id: 'rf-5', name: '许澄', username: 'xucheng', registeredAt: stamp(19, 16, 18), status: '有效', spent: 380, reward: 30.4, level: 'L2' },
  { id: 'rf-6', name: '顾遥', username: 'guyao', registeredAt: stamp(24, 11, 30), status: '待激活', spent: 0, reward: 0, level: '—' },
  { id: 'rf-7', name: '叶青', username: 'yeqing', registeredAt: stamp(28, 13, 12), status: '有效', spent: 200, reward: 16, level: 'L2' },
  { id: 'rf-8', name: '沈舟', username: 'shenzhou', registeredAt: stamp(33, 8, 56), status: '已失效', spent: 0, reward: 0, level: '—' }
]

/**
 * 清空本地演示数据（保留登录态与账号列表）。
 * 作品、钱包、订单、用量、订阅、通知、设备、推荐与后台扩展数据统一在此重置，
 * 避免各页面各自维护一份 key 清单导致漏清。
 */
export function resetDemoState() {
  const keys = [
    'lingxi-assets',
    'lingxi-wallet',
    'lingxi-wallet-logs',
    'lingxi-orders',
    'lingxi-usages',
    'lingxi-agent-tasks',
    'lingxi-task-runs',
    'lingxi-subscription',
    'lingxi-plan-history',
    'lingxi-notifications',
    'lingxi-notification-prefs',
    'lingxi-devices',
    'lingxi-browser-devices',
    'lingxi-profile',
    'lingxi-security',
    'lingxi-referral-state',
    'lingxi-settings',
    'lingxi-connectors',
    'lingxi-skills',
    'lingxi-mock-tasks',
    'lingxi-email-codes',
    // 用户侧兑换码与优惠券
    'lingxi-user-coupons',
    'lingxi-user-redeem-records',
    // 管理后台数据（与 mock/admin.js、admin-extra.js、model-catalog.js 的 STORAGE_KEYS 对齐）
    'lingxi-admin-users',
    'lingxi-admin-tasks',
    'lingxi-admin-models',
    'lingxi-admin-settings',
    'lingxi-admin-payments',
    'lingxi-admin-providers',
    'lingxi-admin-credit-transactions',
    'lingxi-admin-referrals',
    'lingxi-admin-referral-rules',
    'lingxi-admin-refunds',
    'lingxi-admin-reconciliations',
    'lingxi-admin-redeem-codes',
    'lingxi-admin-coupons',
    'lingxi-admin-plans',
    'lingxi-admin-plan-logs',
    'lingxi-admin-moderation',
    'lingxi-admin-sensitive-words',
    'lingxi-admin-billing-rules',
    'lingxi-admin-model-catalog',
    'lingxi-admin-operation-logs',
    'lingxi-admin-monitor-queue'
  ]
  keys.forEach(key => {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      // 忽略清理失败
    }
  })
  // 按前缀存储的键（对话会话、各工具任务）统一清理，避免逐个硬编码
  const prefixes = ['lingxi-chats-', 'lingxi-tool-tasks-']
  try {
    Object.keys(localStorage)
      .filter(key => prefixes.some(prefix => key.startsWith(prefix)))
      .forEach(key => localStorage.removeItem(key))
  } catch (error) {
    // 忽略清理失败
  }
}

const TASK_RUN_SEED = [
  { id: 'tr-1', taskName: '每日资讯简报', trigger: '定时', startedAt: stamp(0, 9, 0), duration: '1m 42s', rounds: '6 / 6', status: 'SUCCESS', result: '汇总 12 条行业资讯并生成摘要，产物已保存到作品库。' },
  { id: 'tr-2', taskName: '竞品动态跟踪', trigger: '定时', startedAt: stamp(1, 18, 0), duration: '0m 12s', rounds: '1 / 6', status: 'FAILED', result: '本机执行通道未连接，任务跳过本轮。可在任务设置中切换为云端执行。' },
  { id: 'tr-3', taskName: '数据日报', trigger: '定时', startedAt: stamp(1, 9, 0), duration: '2m 08s', rounds: '4 / 6', status: 'SUCCESS', result: '生成昨日用量日报，包含调用量、积分消耗与失败率。' },
  { id: 'tr-4', taskName: '社媒内容排期', trigger: '手动', startedAt: stamp(2, 10, 4), duration: '3m 20s', rounds: '6 / 6', status: 'SUCCESS', result: '产出 5 条社媒文案与配图提示词，已写入排期表。' },
  { id: 'tr-5', taskName: '每日资讯简报', trigger: '定时', startedAt: stamp(2, 9, 0), duration: '1m 36s', rounds: '6 / 6', status: 'SUCCESS', result: '汇总 10 条资讯，去重后生成摘要。' },
  { id: 'tr-6', taskName: '竞品动态跟踪', trigger: '定时', startedAt: stamp(3, 18, 0), duration: '2m 44s', rounds: '6 / 6', status: 'SUCCESS', result: '抓取 3 家竞品官网更新，输出变更对比。' },
  { id: 'tr-7', taskName: '数据日报', trigger: '定时', startedAt: stamp(4, 9, 0), duration: '—', rounds: '0 / 6', status: 'CANCELLED', result: '手动取消运行。' },
  { id: 'tr-8', taskName: '社媒内容排期', trigger: '定时', startedAt: stamp(5, 10, 0), duration: '2m 58s', rounds: '6 / 6', status: 'SUCCESS', result: '产出 4 条内容，2 条待人工复核。' }
]

const DEMO_TASK_SEED = [
  {
    id: 'tk-demo-1',
    name: '每日资讯简报',
    promptTemplate: '请汇总 {{date}} AI 行业最重要的三条动态，每条包含标题、关键事实、影响判断和原文链接。',
    model: 'gpt-5.6-sol',
    status: 'ENABLED',
    runLocation: 'CLOUD',
    timezone: 'Asia/Shanghai',
    scheduleType: 'DAILY',
    executeTime: '09:00',
    runCount: 36,
    createdAt: stamp(36, 10, 0)
  },
  {
    id: 'tk-demo-2',
    name: '竞品动态跟踪',
    promptTemplate: '请检查主要竞品当周的官网、定价页与更新日志，整理变化清单并标注影响程度。',
    model: 'claude-opus-5',
    status: 'DISABLED',
    runLocation: 'LOCAL',
    timezone: 'Asia/Shanghai',
    scheduleType: 'WEEKLY',
    executeTime: '10:00',
    weekDays: '1',
    runCount: 8,
    createdAt: stamp(24, 14, 20)
  },
  {
    id: 'tk-demo-3',
    name: '数据日报',
    promptTemplate: '请汇总 {{date}} 的关键业务指标，指出环比异常并给出两条可执行的排查建议。',
    model: 'deepseek-4.0',
    status: 'ENABLED',
    runLocation: 'CLOUD',
    timezone: 'Asia/Shanghai',
    scheduleType: 'DAILY',
    executeTime: '18:00',
    runCount: 21,
    createdAt: stamp(18, 9, 46)
  }
]

export function loadDemoTasks() {
  const cached = read(KEYS.demoTasks, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.demoTasks, DEMO_TASK_SEED)
  return clone(DEMO_TASK_SEED)
}

export function saveDemoTasks(list) {
  write(KEYS.demoTasks, list)
}

export function loadTaskRuns() {
  const cached = read(KEYS.taskRuns, null)
  if (Array.isArray(cached)) return cached
  write(KEYS.taskRuns, TASK_RUN_SEED)
  return clone(TASK_RUN_SEED)
}

export function saveTaskRuns(list) {
  write(KEYS.taskRuns, list)
}

export const referralTemplates = [
  {
    title: '社群分享',
    content: '我在用灵犀做 AI 内容和自动化任务，图片、视频、音乐一个地方搞定。用我的链接注册可以多拿 50 积分：https://lingxi.app/register?ref=AI-STUDIO'
  },
  {
    title: '朋友圈',
    content: '做图、剪视频、写文案，现在都交给灵犀了。邀请链接：https://lingxi.app/register?ref=AI-STUDIO，注册即送体验积分。'
  },
  {
    title: '邮件签名',
    content: '—— 使用灵犀创作：https://lingxi.app/register?ref=AI-STUDIO（邀请码 AI-STUDIO）'
  }
]
