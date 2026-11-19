import dayjs from 'dayjs'

/**
 * 管理员控制台演示数据。
 *
 * 后端暂未提供管理员接口，管理页面统一读取本文件的数据。
 * 用户状态、任务状态、模型开关与系统设置会写入 localStorage，刷新后保留。
 */

const STORAGE_KEYS = {
  users: 'lingxi-admin-users',
  tasks: 'lingxi-admin-tasks',
  models: 'lingxi-admin-models',
  settings: 'lingxi-admin-settings',
  payments: 'lingxi-admin-payments',
  providers: 'lingxi-admin-providers',
  credits: 'lingxi-admin-credit-transactions',
  referrals: 'lingxi-admin-referrals',
  referralRules: 'lingxi-admin-referral-rules'
}

function stamp(daysAgo, hour = 10, minute = 0) {
  return dayjs().subtract(daysAgo, 'day').hour(hour).minute(minute).second(0).format('YYYY-MM-DD HH:mm')
}

function orderNo(daysAgo, seq) {
  return `LX${dayjs().subtract(daysAgo, 'day').format('YYYYMMDD')}${String(seq).padStart(4, '0')}`
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
    // 本地存储不可用时忽略异常，保证页面交互不中断
  }
}

/**
 * 读取列表缓存：命中必须是数组才返回。
 * 缓存被写成对象/字符串时直接 .filter/.map 会抛 TypeError 导致整页白屏，因此统一回落到种子。
 */
function readList(key, seed) {
  const cached = read(key)
  if (Array.isArray(cached)) return cached
  write(key, seed)
  return seed.map(item => (Array.isArray(item.models) ? { ...item, models: [...item.models] } : { ...item }))
}

/** 读取对象缓存：数组、原始值都不算命中，回落到默认值。 */
function readObject(key, fallback) {
  const cached = read(key)
  if (cached && typeof cached === 'object' && !Array.isArray(cached)) return cached
  return fallback
}

const USER_SEED = [
  {
    id: 1,
    username: 'admin',
    nickname: '演示管理员',
    email: 'admin@lingxi.local',
    role: 'ADMIN',
    status: 'ACTIVE',
    plan: '团队版',
    credits: 0,
    tasks: 0,
    registeredAt: stamp(180, 9, 12),
    lastActiveAt: stamp(0, 9, 20)
  },
  {
    id: 2,
    username: 'demo',
    nickname: '体验用户',
    email: 'demo@local',
    role: 'USER',
    status: 'ACTIVE',
    plan: '体验版',
    credits: 2,
    tasks: 2,
    registeredAt: stamp(96, 14, 36),
    lastActiveAt: stamp(0, 8, 45)
  },
  {
    id: 3,
    username: 'chenyu',
    nickname: '陈屿',
    email: 'chenyu@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '专业版',
    credits: 128.5,
    tasks: 4,
    registeredAt: stamp(62, 11, 8),
    lastActiveAt: stamp(0, 10, 12)
  },
  {
    id: 4,
    username: 'linwan',
    nickname: '林晚',
    email: 'linwan@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '专业版',
    credits: 86,
    tasks: 2,
    registeredAt: stamp(48, 16, 22),
    lastActiveAt: stamp(0, 11, 6)
  },
  {
    id: 5,
    username: 'zhouyi',
    nickname: '周屹',
    email: 'zhouyi@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '团队版',
    credits: 420,
    tasks: 3,
    registeredAt: stamp(75, 10, 45),
    lastActiveAt: stamp(1, 19, 30)
  },
  {
    id: 6,
    username: 'subai',
    nickname: '苏白',
    email: 'subai@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '免费版',
    credits: 0,
    tasks: 1,
    registeredAt: stamp(32, 9, 50),
    lastActiveAt: stamp(2, 21, 14)
  },
  {
    id: 7,
    username: 'shenliang',
    nickname: '沈凉',
    email: 'shenliang@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '专业版',
    credits: 52.4,
    tasks: 3,
    registeredAt: stamp(41, 13, 18),
    lastActiveAt: stamp(0, 14, 2)
  },
  {
    id: 8,
    username: 'guhai',
    nickname: '顾海',
    email: 'guhai@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '免费版',
    credits: 3,
    tasks: 1,
    registeredAt: stamp(28, 18, 5),
    lastActiveAt: stamp(4, 9, 40)
  },
  {
    id: 9,
    username: 'jiangning',
    nickname: '江柠',
    email: 'jiangning@example.com',
    role: 'USER',
    status: 'BANNED',
    plan: '免费版',
    credits: 0,
    tasks: 0,
    registeredAt: stamp(57, 15, 30),
    lastActiveAt: stamp(20, 12, 10)
  },
  {
    id: 10,
    username: 'xushi',
    nickname: '许拾',
    email: 'xushi@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '专业版',
    credits: 96.8,
    tasks: 2,
    registeredAt: stamp(23, 11, 26),
    lastActiveAt: stamp(0, 16, 48)
  },
  {
    id: 11,
    username: 'lunanzhou',
    nickname: '陆南舟',
    email: 'lunanzhou@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '团队版',
    credits: 310,
    tasks: 4,
    registeredAt: stamp(120, 10, 15),
    lastActiveAt: stamp(0, 7, 58)
  },
  {
    id: 12,
    username: 'zhaodeng',
    nickname: '赵灯',
    email: 'zhaodeng@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '免费版',
    credits: 1,
    tasks: 1,
    registeredAt: stamp(16, 20, 12),
    lastActiveAt: stamp(1, 8, 35)
  },
  {
    id: 13,
    username: 'heyue',
    nickname: '何月',
    email: 'heyue@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '专业版',
    credits: 44.2,
    tasks: 1,
    registeredAt: stamp(13, 14, 6),
    lastActiveAt: stamp(0, 13, 24)
  },
  {
    id: 14,
    username: 'songqiao',
    nickname: '宋桥',
    email: 'songqiao@example.com',
    role: 'USER',
    status: 'BANNED',
    plan: '免费版',
    credits: 0,
    tasks: 0,
    registeredAt: stamp(9, 9, 42),
    lastActiveAt: stamp(8, 17, 50)
  },
  {
    id: 15,
    username: 'wenyu',
    nickname: '温予',
    email: 'wenyu@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '专业版',
    credits: 73.5,
    tasks: 2,
    registeredAt: stamp(5, 12, 30),
    lastActiveAt: stamp(0, 10, 55)
  },
  {
    id: 16,
    username: 'luoxi',
    nickname: '罗曦',
    email: 'luoxi@example.com',
    role: 'USER',
    status: 'ACTIVE',
    plan: '团队版',
    credits: 268,
    tasks: 2,
    registeredAt: stamp(2, 19, 16),
    lastActiveAt: stamp(0, 15, 10)
  }
]

const TASK_SEED = [
  {
    id: 'task-1001',
    name: '每日 AI 资讯简报',
    user: '陈屿',
    username: 'chenyu',
    model: 'GPT-6 Astra',
    frequency: '每天 09:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 126,
    lastStatus: '成功',
    maxRounds: 50,
    lastRunAt: stamp(0, 9, 2),
    createdAt: stamp(62, 11, 10),
    promptTemplate:
      '请汇总 {{date}} AI 行业最重要的三条动态，每条包含标题、关键事实、影响判断和原文链接。'
  },
  {
    id: 'task-1002',
    name: '竞品动态周报',
    user: '周屹',
    username: 'zhouyi',
    model: 'Claude Opus 5.5',
    frequency: '每周一 10:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 18,
    lastStatus: '成功',
    maxRounds: 30,
    lastRunAt: stamp(1, 10, 4),
    createdAt: stamp(90, 9, 30),
    promptTemplate: '检查主要竞品本周的官网、定价页与更新日志，整理变化清单并标注影响程度。'
  },
  {
    id: 'task-1003',
    name: '小红书选题生成',
    user: '林晚',
    username: 'linwan',
    model: 'Doubao Seed 2.0 Pro',
    frequency: '每天 08:30',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 88,
    lastStatus: '成功',
    maxRounds: 20,
    lastRunAt: stamp(0, 8, 31),
    createdAt: stamp(48, 16, 40),
    promptTemplate: '结合当下家居内容趋势，生成 10 条小红书选题，并附上标题与三段式内容大纲。'
  },
  {
    id: 'task-1004',
    name: '长文摘要与归档',
    user: '苏白',
    username: 'subai',
    model: 'Kimi K3.1 Pro',
    frequency: '每 6 小时',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'DISABLED',
    runCount: 34,
    lastStatus: '成功',
    maxRounds: 15,
    lastRunAt: stamp(5, 18, 12),
    createdAt: stamp(32, 10, 5),
    promptTemplate: '读取收件箱中的长文链接，输出 300 字摘要、关键结论与可引用的数据点。'
  },
  {
    id: 'task-1005',
    name: '商品图批量生成',
    user: '沈凉',
    username: 'shenliang',
    model: 'Nano Banana',
    frequency: '每 2 小时',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 210,
    lastStatus: '失败',
    maxRounds: 10,
    lastRunAt: stamp(0, 14, 1),
    createdAt: stamp(41, 13, 30),
    promptTemplate: '根据商品清单生成白底主图与场景图，统一品牌色调，输出 1:1 与 3:4 两种比例。'
  },
  {
    id: 'task-1006',
    name: '短视频脚本拆解',
    user: '许拾',
    username: 'xushi',
    model: 'Sora 2',
    frequency: '每天 14:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 47,
    lastStatus: '成功',
    maxRounds: 25,
    lastRunAt: stamp(0, 14, 3),
    createdAt: stamp(23, 11, 40),
    promptTemplate: '把当日热门短视频拆解为开场钩子、冲突、转折与结尾，并总结可复用的叙事结构。'
  },
  {
    id: 'task-1007',
    name: '客服邮件自动回复',
    user: '陆南舟',
    username: 'lunanzhou',
    model: 'DeepSeek V4 Pro',
    frequency: '每 30 分钟',
    location: '本机',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 512,
    lastStatus: '成功',
    maxRounds: 10,
    lastRunAt: stamp(0, 15, 30),
    createdAt: stamp(120, 10, 20),
    promptTemplate: '读取未读客服邮件，按知识库撰写回复草稿；涉及退款与法律问题的邮件标记为人工处理。'
  },
  {
    id: 'task-1008',
    name: '周报数据整理',
    user: '赵灯',
    username: 'zhaodeng',
    model: 'Qwen 4 Max',
    frequency: '每周五 17:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'DISABLED',
    runCount: 9,
    lastStatus: '成功',
    maxRounds: 20,
    lastRunAt: stamp(6, 17, 5),
    createdAt: stamp(16, 20, 30),
    promptTemplate: '汇总本周订单、活跃用户与用量数据，生成周报并列出三项需要关注的风险。'
  },
  {
    id: 'task-1009',
    name: '行业播客转写摘要',
    user: '温予',
    username: 'wenyu',
    model: 'Gemini 4.0 Pro',
    frequency: '每天 22:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 63,
    lastStatus: '成功',
    maxRounds: 30,
    lastRunAt: stamp(0, 22, 6),
    createdAt: stamp(5, 12, 40),
    promptTemplate: '把当日订阅播客转写为结构化摘要，输出核心观点、金句与延伸阅读建议。'
  },
  {
    id: 'task-1010',
    name: '音乐灵感生成',
    user: '罗曦',
    username: 'luoxi',
    model: 'Suno v6',
    frequency: '每周三 20:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 21,
    lastStatus: '成功',
    maxRounds: 10,
    lastRunAt: stamp(1, 20, 2),
    createdAt: stamp(2, 19, 20),
    promptTemplate: '根据本周热门情绪标签生成 3 首不同风格的歌曲 Demo，附歌词与编曲说明。'
  },
  {
    id: 'task-1011',
    name: '竞品定价监控',
    user: '陈屿',
    username: 'chenyu',
    model: 'GPT-6.1 Sol',
    frequency: '每天 11:00',
    location: '云端',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 74,
    lastStatus: '失败',
    maxRounds: 25,
    lastRunAt: stamp(0, 11, 1),
    createdAt: stamp(30, 9, 50),
    promptTemplate: '抓取主要竞品定价页，对比昨日快照，输出价格变动与套餐权益变化。'
  },
  {
    id: 'task-1012',
    name: '合同条款审阅',
    user: '何月',
    username: 'heyue',
    model: 'Claude Opus 5.5',
    frequency: '每 12 小时',
    location: '本机',
    timezone: 'Asia/Shanghai',
    status: 'ENABLED',
    runCount: 29,
    lastStatus: '成功',
    maxRounds: 40,
    lastRunAt: stamp(0, 13, 20),
    createdAt: stamp(13, 14, 20),
    promptTemplate: '审阅待签合同，标注付款、违约、知识产权与保密条款风险，并给出修改建议。'
  }
]

const ORDER_SEED = [
  {
    id: orderNo(0, 1),
    user: '陈屿',
    username: 'chenyu',
    type: '订阅',
    amount: 199,
    payMethod: '支付宝',
    status: 'PAID',
    createdAt: stamp(0, 8, 32)
  },
  {
    id: orderNo(0, 2),
    user: '温予',
    username: 'wenyu',
    type: '充值',
    amount: 99,
    payMethod: '微信支付',
    status: 'PAID',
    createdAt: stamp(0, 10, 15)
  },
  {
    id: orderNo(1, 3),
    user: '沈凉',
    username: 'shenliang',
    type: '充值',
    amount: 49,
    payMethod: '支付宝',
    status: 'PAID',
    createdAt: stamp(1, 14, 20)
  },
  {
    id: orderNo(2, 4),
    user: '罗曦',
    username: 'luoxi',
    type: '订阅',
    amount: 599,
    payMethod: '微信支付',
    status: 'PAID',
    createdAt: stamp(2, 9, 5)
  },
  {
    id: orderNo(3, 5),
    user: '赵灯',
    username: 'zhaodeng',
    type: '兑换码',
    amount: 19.9,
    payMethod: '兑换码',
    status: 'PAID',
    createdAt: stamp(3, 16, 40)
  },
  {
    id: orderNo(3, 6),
    user: '林晚',
    username: 'linwan',
    type: '充值',
    amount: 199,
    payMethod: '支付宝',
    status: 'PENDING',
    createdAt: stamp(3, 18, 12)
  },
  {
    id: orderNo(5, 7),
    user: '许拾',
    username: 'xushi',
    type: '充值',
    amount: 99,
    payMethod: '微信支付',
    status: 'PAID',
    createdAt: stamp(5, 11, 8)
  },
  {
    id: orderNo(6, 8),
    user: '顾海',
    username: 'guhai',
    type: '充值',
    amount: 19.9,
    payMethod: '支付宝',
    status: 'REFUNDED',
    createdAt: stamp(6, 13, 25)
  },
  {
    id: orderNo(8, 9),
    user: '宋桥',
    username: 'songqiao',
    type: '订阅',
    amount: 49,
    payMethod: '微信支付',
    status: 'PAID',
    createdAt: stamp(8, 20, 10)
  },
  {
    id: orderNo(10, 10),
    user: '陆南舟',
    username: 'lunanzhou',
    type: '充值',
    amount: 999,
    payMethod: '支付宝',
    status: 'PAID',
    createdAt: stamp(10, 10, 30)
  },
  {
    id: orderNo(12, 11),
    user: '苏白',
    username: 'subai',
    type: '充值',
    amount: 19.9,
    payMethod: '微信支付',
    status: 'PENDING',
    createdAt: stamp(12, 15, 44)
  },
  {
    id: orderNo(15, 12),
    user: '周屹',
    username: 'zhouyi',
    type: '订阅',
    amount: 1999,
    payMethod: '支付宝',
    status: 'PAID',
    createdAt: stamp(15, 9, 18)
  },
  {
    id: orderNo(18, 13),
    user: '江柠',
    username: 'jiangning',
    type: '充值',
    amount: 49,
    payMethod: '支付宝',
    status: 'REFUNDED',
    createdAt: stamp(18, 17, 26)
  },
  {
    id: orderNo(22, 14),
    user: '何月',
    username: 'heyue',
    type: '充值',
    amount: 299,
    payMethod: '微信支付',
    status: 'PAID',
    createdAt: stamp(22, 11, 52)
  }
]

export const adminOrders = ORDER_SEED

export const adminUsageRecords = [
  {
    id: 1,
    time: stamp(0, 15, 42),
    user: '陈屿',
    service: '对话',
    operation: 'chat/completions · GPT-6 Astra',
    statusCode: 200,
    duration: '2.4s',
    credits: 0.36
  },
  {
    id: 2,
    time: stamp(0, 15, 20),
    user: '罗曦',
    service: '音乐生成',
    operation: 'music/generations · Suno v6',
    statusCode: 200,
    duration: '18.6s',
    credits: 0.56
  },
  {
    id: 3,
    time: stamp(0, 14, 58),
    user: '沈凉',
    service: '图像生成',
    operation: 'images/generations · Nano Banana',
    statusCode: 200,
    duration: '6.2s',
    credits: 0.14
  },
  {
    id: 4,
    time: stamp(0, 14, 31),
    user: '陆南舟',
    service: '对话',
    operation: 'chat/completions · DeepSeek V4 Pro',
    statusCode: 200,
    duration: '1.8s',
    credits: 0.08
  },
  {
    id: 5,
    time: stamp(0, 14, 6),
    user: '许拾',
    service: '视频生成',
    operation: 'videos/generations · Sora 2',
    statusCode: 500,
    duration: '32.0s',
    credits: 0
  },
  {
    id: 6,
    time: stamp(0, 13, 44),
    user: '温予',
    service: '对话',
    operation: 'chat/completions · Gemini 4.0 Pro',
    statusCode: 200,
    duration: '2.1s',
    credits: 0.12
  },
  {
    id: 7,
    time: stamp(0, 13, 12),
    user: '林晚',
    service: '图像生成',
    operation: 'images/generations · SeeDream 5.0',
    statusCode: 200,
    duration: '7.8s',
    credits: 0.37
  },
  {
    id: 8,
    time: stamp(0, 12, 50),
    user: '何月',
    service: '对话',
    operation: 'chat/completions · Claude Opus 5.5',
    statusCode: 200,
    duration: '3.6s',
    credits: 0.42
  },
  {
    id: 9,
    time: stamp(0, 12, 18),
    user: '赵灯',
    service: '对话',
    operation: 'chat/completions · Qwen 4 Max',
    statusCode: 200,
    duration: '1.4s',
    credits: 0.05
  },
  {
    id: 10,
    time: stamp(0, 11, 52),
    user: '周屹',
    service: '视频生成',
    operation: 'videos/generations · Veo 3.1',
    statusCode: 200,
    duration: '26.4s',
    credits: 0.6
  },
  {
    id: 11,
    time: stamp(1, 22, 14),
    user: '陈屿',
    service: '图像生成',
    operation: 'images/generations · Flux 2 Pro',
    statusCode: 400,
    duration: '1.2s',
    credits: 0
  },
  {
    id: 12,
    time: stamp(1, 21, 36),
    user: '苏白',
    service: '对话',
    operation: 'chat/completions · Kimi K3.1 Pro',
    statusCode: 200,
    duration: '2.8s',
    credits: 0.09
  },
  {
    id: 13,
    time: stamp(1, 20, 18),
    user: '顾海',
    service: '音乐生成',
    operation: 'music/generations · Udio 3',
    statusCode: 200,
    duration: '15.2s',
    credits: 0.5
  },
  {
    id: 14,
    time: stamp(1, 19, 5),
    user: '沈凉',
    service: '图像生成',
    operation: 'images/generations · SeeDream 5.0',
    statusCode: 200,
    duration: '6.9s',
    credits: 0.37
  },
  {
    id: 15,
    time: stamp(2, 16, 48),
    user: '许拾',
    service: '视频生成',
    operation: 'videos/generations · SeeDance 2.0',
    statusCode: 200,
    duration: '41.2s',
    credits: 8.4
  },
  {
    id: 16,
    time: stamp(2, 15, 30),
    user: '罗曦',
    service: '对话',
    operation: 'chat/completions · Grok 4.7',
    statusCode: 200,
    duration: '1.9s',
    credits: 0.1
  }
]

export const serviceShare = [
  { name: '对话', requests: 18620, color: '#3b82f6' },
  { name: '图像生成', requests: 5230, color: '#8b5cf6' },
  { name: '视频生成', requests: 2140, color: '#f59e0b' },
  { name: '音乐生成', requests: 860, color: '#10b981' }
]

const TREND_REQUESTS = [980, 1120, 860, 1240, 1490, 1720, 1380, 1610, 1880, 1520, 2100, 1960, 2380, 2240]
const TREND_CREDITS = [150, 168, 132, 181, 205, 246, 198, 232, 268, 214, 301, 286, 332, 318]

export const usageTrend = TREND_REQUESTS.map((requests, index) => ({
  date: dayjs()
    .subtract(TREND_REQUESTS.length - 1 - index, 'day')
    .format('MM-DD'),
  requests,
  credits: TREND_CREDITS[index]
}))

export const defaultAdminSettings = {
  siteName: '灵犀',
  supportEmail: 'support@lingxi.local',
  announcement: '欢迎使用灵犀管理后台，当前数据为本地演示数据。',
  allowRegister: true,
  defaultCredits: 2,
  maxRounds: 50,
  defaultTimezone: 'Asia/Shanghai',
  maxConcurrent: 3,
  maintenance: false
}

const PAYMENT_SEED = [
  {
    id: 'pay-alipay',
    name: '支付宝',
    code: 'ALIPAY',
    letter: '支',
    color: '#1677ff',
    enabled: true,
    mode: 'PROD',
    merchantId: '2088121044820915',
    feeRate: 0.6,
    settleCycle: 'T+1',
    callbackUrl: 'https://api.lingxi.local/pay/alipay/notify',
    todayAmount: 12860.5,
    todayOrders: 86,
    successRate: 99.6,
    totalAmount: 1268420.8,
    updatedAt: stamp(1, 16, 20)
  },
  {
    id: 'pay-wechat',
    name: '微信支付',
    code: 'WECHAT',
    letter: '微',
    color: '#07c160',
    enabled: true,
    mode: 'PROD',
    merchantId: '1628402931',
    feeRate: 0.6,
    settleCycle: 'T+1',
    callbackUrl: 'https://api.lingxi.local/pay/wechat/notify',
    todayAmount: 9860,
    todayOrders: 64,
    successRate: 99.4,
    totalAmount: 984210.3,
    updatedAt: stamp(2, 10, 5)
  },
  {
    id: 'pay-unionpay',
    name: '银联云闪付',
    code: 'UNIONPAY',
    letter: '银',
    color: '#e22018',
    enabled: true,
    mode: 'PROD',
    merchantId: '898110248160221',
    feeRate: 0.5,
    settleCycle: 'T+1',
    callbackUrl: 'https://api.lingxi.local/pay/unionpay/notify',
    todayAmount: 2140,
    todayOrders: 12,
    successRate: 98.9,
    totalAmount: 186420.6,
    updatedAt: stamp(6, 14, 36)
  },
  {
    id: 'pay-stripe',
    name: 'Stripe',
    code: 'STRIPE',
    letter: 'S',
    color: '#635bff',
    enabled: true,
    mode: 'PROD',
    merchantId: 'acct_1Q8xLingxiDemo',
    feeRate: 2.9,
    settleCycle: 'T+3',
    callbackUrl: 'https://api.lingxi.local/pay/stripe/webhook',
    todayAmount: 5320.4,
    todayOrders: 18,
    successRate: 97.8,
    totalAmount: 342860.2,
    updatedAt: stamp(3, 9, 48)
  },
  {
    id: 'pay-paypal',
    name: 'PayPal',
    code: 'PAYPAL',
    letter: 'P',
    color: '#003087',
    enabled: false,
    mode: 'SANDBOX',
    merchantId: 'LINGXI-DEMO-8891',
    feeRate: 3.4,
    settleCycle: 'T+3',
    callbackUrl: 'https://api.lingxi.local/pay/paypal/notify',
    todayAmount: 0,
    todayOrders: 0,
    successRate: 96.5,
    totalAmount: 84210,
    updatedAt: stamp(28, 11, 15)
  },
  {
    id: 'pay-apple',
    name: 'Apple Pay',
    code: 'APPLEPAY',
    letter: '',
    color: '#a3a3a3',
    enabled: true,
    mode: 'PROD',
    merchantId: 'merchant.ai.lingxi.web',
    feeRate: 0.6,
    settleCycle: 'T+1',
    callbackUrl: 'https://api.lingxi.local/pay/apple/notify',
    todayAmount: 3260,
    todayOrders: 21,
    successRate: 99.2,
    totalAmount: 226480.9,
    updatedAt: stamp(9, 17, 2)
  },
  {
    id: 'pay-redeem',
    name: '兑换码',
    code: 'REDEEM',
    letter: '兑',
    color: '#f59e0b',
    enabled: true,
    mode: 'PROD',
    merchantId: '—',
    feeRate: 0,
    settleCycle: '即时到账',
    callbackUrl: '—',
    todayAmount: 980,
    todayOrders: 35,
    successRate: 100,
    totalAmount: 92160,
    updatedAt: stamp(4, 20, 30)
  }
]

export const providerSpeedTiers = [
  { value: 'slow', label: '降速', rpm: 60, concurrency: 4, desc: '限流保护，适合异常或维护期' },
  { value: 'standard', label: '标准', rpm: 300, concurrency: 20, desc: '默认速率，满足日常调用' },
  { value: 'fast', label: '升速', rpm: 900, concurrency: 60, desc: '高峰期提升吞吐' },
  { value: 'turbo', label: '极速', rpm: 2400, concurrency: 120, desc: '全量放开，需关注成本' }
]

const PROVIDER_SEED = [
  {
    id: 'provider-ark',
    name: '火山引擎方舟',
    vendor: '字节跳动',
    letter: '火',
    color: '#4c8dff',
    enabled: true,
    speedTier: 'fast',
    rpm: 900,
    concurrency: 60,
    latency: 820,
    successRate: 99.7,
    apiBase: 'https://ark.cn-beijing.volces.com/api/v3',
    apiKey: 'sk-ark-demo-9f2c4a7b',
    timeout: 60,
    retries: 2,
    models: ['Doubao Seed 2.0 Pro', 'Doubao Vision 2.0', 'SeeDream 5.0'],
    todayCalls: 12860,
    updatedAt: stamp(0, 9, 12)
  },
  {
    id: 'provider-openai',
    name: 'OpenAI',
    vendor: 'OpenAI',
    letter: 'O',
    color: '#10a37f',
    enabled: true,
    speedTier: 'standard',
    rpm: 300,
    concurrency: 20,
    latency: 1240,
    successRate: 99.2,
    apiBase: 'https://api.openai.com/v1',
    apiKey: 'sk-openai-demo-3d8e1c5a',
    timeout: 90,
    retries: 3,
    models: ['GPT-6 Astra', 'GPT-6.1 Sol', 'o5-mini'],
    todayCalls: 9860,
    updatedAt: stamp(1, 15, 40)
  },
  {
    id: 'provider-anthropic',
    name: 'Anthropic',
    vendor: 'Anthropic',
    letter: 'A',
    color: '#d97757',
    enabled: true,
    speedTier: 'standard',
    rpm: 300,
    concurrency: 20,
    latency: 1310,
    successRate: 99.4,
    apiBase: 'https://api.anthropic.com/v1',
    apiKey: 'sk-ant-demo-6b2f9d0e',
    timeout: 90,
    retries: 2,
    models: ['Claude Opus 5.5', 'Claude Sonnet 5.2', 'Claude Haiku 4.8'],
    todayCalls: 7240,
    updatedAt: stamp(2, 11, 26)
  },
  {
    id: 'provider-google',
    name: 'Google Vertex AI',
    vendor: 'Google Cloud',
    letter: 'G',
    color: '#4285f4',
    enabled: false,
    speedTier: 'slow',
    rpm: 60,
    concurrency: 4,
    latency: 1680,
    successRate: 98.6,
    apiBase: 'https://us-central1-aiplatform.googleapis.com/v1',
    apiKey: 'ya29.demo-vertex-71c4',
    timeout: 120,
    retries: 1,
    models: ['Gemini 4.0 Pro', 'Veo 3.1', 'Imagen 4'],
    todayCalls: 4310,
    updatedAt: stamp(7, 19, 8)
  },
  {
    id: 'provider-bailian',
    name: '阿里云百炼',
    vendor: '阿里云',
    letter: '阿',
    color: '#ff6a00',
    enabled: true,
    speedTier: 'fast',
    rpm: 900,
    concurrency: 60,
    latency: 760,
    successRate: 99.5,
    apiBase: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    apiKey: 'sk-bailian-demo-58a7f2c9',
    timeout: 60,
    retries: 2,
    models: ['Qwen 4 Max', 'Qwen-VL 3', 'Wan 3.0'],
    todayCalls: 6520,
    updatedAt: stamp(0, 8, 55)
  },
  {
    id: 'provider-deepseek',
    name: 'DeepSeek 开放平台',
    vendor: '深度求索',
    letter: 'D',
    color: '#4d6bfe',
    enabled: true,
    speedTier: 'turbo',
    rpm: 2400,
    concurrency: 120,
    latency: 540,
    successRate: 99.8,
    apiBase: 'https://api.deepseek.com/v1',
    apiKey: 'sk-deepseek-demo-2f6b8e4d',
    timeout: 60,
    retries: 3,
    models: ['DeepSeek V4 Pro', 'DeepSeek Coder V4'],
    todayCalls: 15320,
    updatedAt: stamp(0, 7, 30)
  },
  {
    id: 'provider-moonshot',
    name: '月之暗面 Kimi',
    vendor: 'Moonshot AI',
    letter: '月',
    color: '#8b5cf6',
    enabled: true,
    speedTier: 'standard',
    rpm: 300,
    concurrency: 20,
    latency: 1080,
    successRate: 99.1,
    apiBase: 'https://api.moonshot.cn/v1',
    apiKey: 'sk-moonshot-demo-9a3c7f1e',
    timeout: 90,
    retries: 2,
    models: ['Kimi K3.1 Pro', 'Kimi K3.1 Turbo'],
    todayCalls: 3620,
    updatedAt: stamp(3, 13, 12)
  },
  {
    id: 'provider-siliconflow',
    name: '硅基流动',
    vendor: 'SiliconFlow',
    letter: '硅',
    color: '#22c55e',
    enabled: true,
    speedTier: 'standard',
    rpm: 300,
    concurrency: 20,
    latency: 1420,
    successRate: 98.4,
    apiBase: 'https://api.siliconflow.cn/v1',
    apiKey: 'sk-sf-demo-4e1d6b8a',
    timeout: 90,
    retries: 2,
    models: ['Flux 2 Pro', 'Nano Banana', 'Suno v6'],
    todayCalls: 2870,
    updatedAt: stamp(5, 16, 44)
  }
]

const CREDIT_TRANSACTION_SEED = [
  {
    id: 'ctx-1020',
    time: stamp(0, 15, 42),
    user: '陈屿',
    username: 'chenyu',
    type: 'CONSUME',
    amount: -0.36,
    balance: 128.5,
    remark: '对话调用 · GPT-6 Astra',
    operator: '系统'
  },
  {
    id: 'ctx-1019',
    time: stamp(0, 11, 26),
    user: '罗曦',
    username: 'luoxi',
    type: 'REWARD',
    amount: 20,
    balance: 268,
    remark: '邀请好友「温予」注册奖励',
    operator: '系统'
  },
  {
    id: 'ctx-1018',
    time: stamp(0, 10, 15),
    user: '温予',
    username: 'wenyu',
    type: 'CONSUME',
    amount: -0.56,
    balance: 73.5,
    remark: '音乐生成 · Suno v6',
    operator: '系统'
  },
  {
    id: 'ctx-1017',
    time: stamp(0, 9, 5),
    user: '沈凉',
    username: 'shenliang',
    type: 'CONSUME',
    amount: -0.14,
    balance: 52.4,
    remark: '图像生成 · Nano Banana',
    operator: '系统'
  },
  {
    id: 'ctx-1016',
    time: stamp(1, 21, 40),
    user: '陆南舟',
    username: 'lunanzhou',
    type: 'ADJUST',
    amount: 50,
    balance: 310,
    remark: '客服补偿积分',
    operator: '演示管理员'
  },
  {
    id: 'ctx-1015',
    time: stamp(1, 17, 44),
    user: '温予',
    username: 'wenyu',
    type: 'CONSUME',
    amount: -12.8,
    balance: 74.06,
    remark: '视频生成 · Veo 3.1',
    operator: '系统'
  },
  {
    id: 'ctx-1014',
    time: stamp(1, 16, 30),
    user: '林晚',
    username: 'linwan',
    type: 'CONSUME',
    amount: -2.4,
    balance: 86,
    remark: '视频生成 · Sora 2',
    operator: '系统'
  },
  {
    id: 'ctx-1013',
    time: stamp(1, 14, 20),
    user: '周屹',
    username: 'zhouyi',
    type: 'RECHARGE',
    amount: 199,
    balance: 420,
    remark: '支付宝充值 · 团队版续费',
    operator: '系统'
  },
  {
    id: 'ctx-1012',
    time: stamp(2, 20, 30),
    user: '温予',
    username: 'wenyu',
    type: 'CONSUME',
    amount: -12.14,
    balance: 86.86,
    remark: '图像批量生成 · SeeDream 5.0',
    operator: '系统'
  },
  {
    id: 'ctx-1011',
    time: stamp(2, 10, 12),
    user: '温予',
    username: 'wenyu',
    type: 'RECHARGE',
    amount: 99,
    balance: 99,
    remark: '微信支付充值',
    operator: '系统'
  },
  {
    id: 'ctx-1010',
    time: stamp(2, 9, 30),
    user: '何月',
    username: 'heyue',
    type: 'CONSUME',
    amount: -0.42,
    balance: 44.2,
    remark: '对话调用 · Claude Opus 5.5',
    operator: '系统'
  },
  {
    id: 'ctx-1009',
    time: stamp(3, 18, 40),
    user: '沈凉',
    username: 'shenliang',
    type: 'CONSUME',
    amount: -1.8,
    balance: 53.34,
    remark: '视频生成 · SeeDance 2.0',
    operator: '系统'
  },
  {
    id: 'ctx-1008',
    time: stamp(3, 15, 12),
    user: '陈屿',
    username: 'chenyu',
    type: 'RECHARGE',
    amount: 50,
    balance: 130.06,
    remark: '微信支付充值',
    operator: '系统'
  },
  {
    id: 'ctx-1007',
    time: stamp(3, 11, 30),
    user: '罗曦',
    username: 'luoxi',
    type: 'CONSUME',
    amount: -3.5,
    balance: 248,
    remark: '音乐生成 · Suno v6',
    operator: '系统'
  },
  {
    id: 'ctx-1006',
    time: stamp(4, 19, 22),
    user: '顾海',
    username: 'guhai',
    type: 'CONSUME',
    amount: -0.05,
    balance: 3,
    remark: '对话调用 · Qwen 4 Max',
    operator: '系统'
  },
  {
    id: 'ctx-1005',
    time: stamp(5, 21, 40),
    user: '陆南舟',
    username: 'lunanzhou',
    type: 'CONSUME',
    amount: -8.4,
    balance: 260,
    remark: '视频生成 · SeeDance 2.0',
    operator: '系统'
  }
]

const REFERRAL_SEED = [
  {
    id: 'ref-1001',
    inviter: '罗曦',
    inviterUsername: 'luoxi',
    invitee: '温予',
    inviteeUsername: 'wenyu',
    channel: '邀请链接',
    status: 'PENDING',
    reward: 20,
    registeredAt: stamp(0, 11, 26),
    grantedAt: '',
    note: '待被邀请人完成首次充值'
  },
  {
    id: 'ref-1002',
    inviter: '陆南舟',
    inviterUsername: 'lunanzhou',
    invitee: '罗曦',
    inviteeUsername: 'luoxi',
    channel: '邀请链接',
    status: 'GRANTED',
    reward: 20,
    registeredAt: stamp(5, 9, 18),
    grantedAt: stamp(5, 9, 40),
    note: '注册后自动发放'
  },
  {
    id: 'ref-1003',
    inviter: '陈屿',
    inviterUsername: 'chenyu',
    invitee: '何月',
    inviteeUsername: 'heyue',
    channel: '邀请码',
    status: 'GRANTED',
    reward: 30,
    registeredAt: stamp(13, 14, 2),
    grantedAt: stamp(12, 10, 20),
    note: '完成首次付费后发放'
  },
  {
    id: 'ref-1004',
    inviter: '周屹',
    inviterUsername: 'zhouyi',
    invitee: '许拾',
    inviteeUsername: 'xushi',
    channel: '邀请码',
    status: 'GRANTED',
    reward: 20,
    registeredAt: stamp(23, 11, 26),
    grantedAt: stamp(22, 16, 8),
    note: '注册后自动发放'
  },
  {
    id: 'ref-1005',
    inviter: '林晚',
    inviterUsername: 'linwan',
    invitee: '赵灯',
    inviteeUsername: 'zhaodeng',
    channel: '邀请链接',
    status: 'PENDING',
    reward: 20,
    registeredAt: stamp(1, 8, 34),
    grantedAt: '',
    note: '待被邀请人完成首次充值'
  },
  {
    id: 'ref-1006',
    inviter: '沈凉',
    inviterUsername: 'shenliang',
    invitee: '苏白',
    inviteeUsername: 'subai',
    channel: '邀请码',
    status: 'VOID',
    reward: 20,
    registeredAt: stamp(9, 15, 50),
    grantedAt: '',
    note: '被邀请人账号存在异常，奖励作废'
  },
  {
    id: 'ref-1007',
    inviter: '温予',
    inviterUsername: 'wenyu',
    invitee: '顾海',
    inviteeUsername: 'guhai',
    channel: '分享海报',
    status: 'GRANTED',
    reward: 30,
    registeredAt: stamp(8, 17, 12),
    grantedAt: stamp(7, 9, 30),
    note: '完成首次付费后发放'
  },
  {
    id: 'ref-1008',
    inviter: '何月',
    inviterUsername: 'heyue',
    invitee: '宋桥',
    inviteeUsername: 'songqiao',
    channel: '邀请链接',
    status: 'VOID',
    reward: 20,
    registeredAt: stamp(12, 20, 18),
    grantedAt: '',
    note: '超过 30 天未完成有效行为'
  },
  {
    id: 'ref-1009',
    inviter: '陆南舟',
    inviterUsername: 'lunanzhou',
    invitee: '江柠',
    inviteeUsername: 'jiangning',
    channel: '邀请链接',
    status: 'GRANTED',
    reward: 20,
    registeredAt: stamp(20, 15, 30),
    grantedAt: stamp(19, 11, 6),
    note: '注册后自动发放'
  },
  {
    id: 'ref-1010',
    inviter: '周屹',
    inviterUsername: 'zhouyi',
    invitee: '林晚',
    inviteeUsername: 'linwan',
    channel: '分享海报',
    status: 'GRANTED',
    reward: 30,
    registeredAt: stamp(26, 10, 44),
    grantedAt: stamp(25, 14, 20),
    note: '完成首次付费后发放'
  }
]

export const defaultReferralRules = {
  enabled: true,
  inviterReward: 20,
  inviteeReward: 10,
  condition: 'FIRST_PAY',
  validDays: 30,
  maxInvites: 50,
  linkDomain: 'https://lingxi.ai/invite',
  posterEnabled: true,
  remark: '奖励在满足条件后自动发放，也可在推荐记录中手动补发或作废。'
}

export function loadAdminUsers() {
  return readList(STORAGE_KEYS.users, USER_SEED)
}

export function saveAdminUsers(users) {
  write(STORAGE_KEYS.users, users)
}

export function loadAdminTasks() {
  return readList(STORAGE_KEYS.tasks, TASK_SEED)
}

export function saveAdminTasks(tasks) {
  write(STORAGE_KEYS.tasks, tasks)
}

export function loadModelStatus() {
  return readObject(STORAGE_KEYS.models, {})
}

export function saveModelStatus(status) {
  write(STORAGE_KEYS.models, status)
}

export function loadAdminSettings() {
  return { ...defaultAdminSettings, ...readObject(STORAGE_KEYS.settings, {}) }
}

export function saveAdminSettings(settings) {
  write(STORAGE_KEYS.settings, settings)
}

export function loadAdminPayments() {
  return readList(STORAGE_KEYS.payments, PAYMENT_SEED)
}

export function saveAdminPayments(payments) {
  write(STORAGE_KEYS.payments, payments)
}

export function loadAdminProviders() {
  return readList(STORAGE_KEYS.providers, PROVIDER_SEED)
}

export function saveAdminProviders(providers) {
  write(STORAGE_KEYS.providers, providers)
}

export function loadCreditTransactions() {
  return readList(STORAGE_KEYS.credits, CREDIT_TRANSACTION_SEED)
}

export function saveCreditTransactions(records) {
  write(STORAGE_KEYS.credits, records)
}

export function loadAdminReferrals() {
  return readList(STORAGE_KEYS.referrals, REFERRAL_SEED)
}

export function saveAdminReferrals(records) {
  write(STORAGE_KEYS.referrals, records)
}

export function loadReferralRules() {
  return { ...defaultReferralRules, ...readObject(STORAGE_KEYS.referralRules, {}) }
}

export function saveReferralRules(rules) {
  write(STORAGE_KEYS.referralRules, rules)
}

export function resetAdminDemoData() {
  Object.values(STORAGE_KEYS).forEach(key => {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      // 忽略清理失败
    }
  })
}
