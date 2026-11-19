import dayjs from 'dayjs'

/**
 * 模型目录（对用户暴露的模型条目）。
 *
 * 与「平台接入」是两个层级：
 * - 平台接入：上游通道、密钥、限速、超时（决定能不能调）
 * - 模型目录：用户看到什么名称/图标/价格/能力（决定怎么卖）
 *
 * 演示数据，接入后端后替换 loadModelCatalog / saveModelCatalog 即可。
 */

const STORAGE_KEY = 'lingxi-admin-model-catalog'

function stamp(daysAgo, hour = 10, minute = 0) {
  return dayjs().subtract(daysAgo, 'day').hour(hour).minute(minute).second(0).format('YYYY-MM-DD HH:mm')
}

export const modelCategories = [
  { key: 'chat', label: '对话' },
  { key: 'image', label: '图片' },
  { key: 'video', label: '视频' },
  { key: 'music', label: '音乐' }
]

export const iconChoices = [
  { key: 'chatgpt', label: 'OpenAI' },
  { key: 'claude', label: 'Claude' },
  { key: 'gemini', label: 'Gemini' },
  { key: 'deepseek', label: 'DeepSeek' },
  { key: 'kimi', label: 'Kimi' },
  { key: 'qwen', label: '通义千问' },
  { key: 'grok', label: 'Grok' },
  { key: 'doubao', label: '豆包' },
  { key: 'nanobanana', label: 'Nano Banana' },
  { key: 'seedream', label: 'SeeDream' },
  { key: 'flux', label: 'Flux' },
  { key: 'midjourney', label: 'Midjourney' },
  { key: 'ideogram', label: 'Ideogram' },
  { key: 'seedance', label: 'SeeDance' },
  { key: 'kling', label: 'Kling' },
  { key: 'sora', label: 'Sora' },
  { key: 'veo', label: 'Veo' },
  { key: 'pika', label: 'Pika' },
  { key: 'suno', label: 'Suno' },
  { key: 'producer', label: 'Producer' },
  { key: 'udio', label: 'Udio' }
]

const CATALOG_SEED = [
  {
    id: 'mc-1',
    key: 'gpt-6-astra',
    name: 'GPT-6 Astra',
    appKey: 'chatgpt',
    vendor: 'OpenAI',
    provider: 'OpenAI',
    category: 'chat',
    capabilities: ['推理', '长上下文', '视觉'],
    unit: '1K tokens',
    price: 0.02,
    multiplier: 1,
    context: '200K',
    description: '复杂项目深度推理，适合长文档与多步骤任务。',
    recommended: true,
    enabled: true,
    sort: 1,
    updatedAt: stamp(3, 11, 0)
  },
  {
    id: 'mc-2',
    key: 'gpt-6-luna',
    name: 'GPT-6 Luna',
    appKey: 'chatgpt',
    vendor: 'OpenAI',
    provider: 'OpenAI',
    category: 'chat',
    capabilities: ['快速', '通用'],
    unit: '1K tokens',
    price: 0.004,
    multiplier: 1,
    context: '128K',
    description: '快速对话与日常编程，成本低。',
    recommended: false,
    enabled: true,
    sort: 2,
    updatedAt: stamp(3, 11, 0)
  },
  {
    id: 'mc-3',
    key: 'claude-opus-5',
    name: 'Claude Opus 5.5',
    appKey: 'claude',
    vendor: 'Anthropic',
    provider: 'Anthropic',
    category: 'chat',
    capabilities: ['推理', '代码', '长上下文'],
    unit: '1K tokens',
    price: 0.03,
    multiplier: 1.2,
    context: '200K',
    description: '长程智能体与复杂代码任务表现稳定。',
    recommended: true,
    enabled: true,
    sort: 3,
    updatedAt: stamp(4, 9, 30)
  },
  {
    id: 'mc-4',
    key: 'gemini-3-pro',
    name: 'Gemini 3.1 Pro',
    appKey: 'gemini',
    vendor: 'Google',
    provider: 'Google',
    category: 'chat',
    capabilities: ['视觉', '长上下文', '多模态'],
    unit: '1K tokens',
    price: 0.012,
    multiplier: 1,
    context: '1M',
    description: '超长上下文与多模态理解。',
    recommended: false,
    enabled: true,
    sort: 4,
    updatedAt: stamp(6, 14, 20)
  },
  {
    id: 'mc-5',
    key: 'deepseek-v4-pro',
    name: 'DeepSeek V4 Pro',
    appKey: 'deepseek',
    vendor: 'DeepSeek',
    provider: 'DeepSeek',
    category: 'chat',
    capabilities: ['推理', '中文', '低成本'],
    unit: '1K tokens',
    price: 0.002,
    multiplier: 1,
    context: '128K',
    description: '中文理解与数学推理，性价比高。',
    recommended: true,
    enabled: true,
    sort: 5,
    updatedAt: stamp(4, 9, 30)
  },
  {
    id: 'mc-6',
    key: 'kimi-k3',
    name: 'Kimi K3',
    appKey: 'kimi',
    vendor: 'Moonshot',
    provider: 'Moonshot',
    category: 'chat',
    capabilities: ['长上下文', '中文', '文档'],
    unit: '1K tokens',
    price: 0.003,
    multiplier: 1,
    context: '256K',
    description: '长文档解析与资料整理。',
    recommended: false,
    enabled: true,
    sort: 6,
    updatedAt: stamp(11, 10, 0)
  },
  {
    id: 'mc-7',
    key: 'qwen-4-max',
    name: 'Qwen 4 Max',
    appKey: 'qwen',
    vendor: '阿里云',
    provider: '阿里云百炼',
    category: 'chat',
    capabilities: ['中文', '工具调用'],
    unit: '1K tokens',
    price: 0.004,
    multiplier: 1,
    context: '128K',
    description: '中文场景与函数调用。',
    recommended: false,
    enabled: true,
    sort: 7,
    updatedAt: stamp(11, 10, 0)
  },
  {
    id: 'mc-8',
    key: 'grok-4',
    name: 'Grok 4',
    appKey: 'grok',
    vendor: 'xAI',
    provider: 'OpenAI',
    category: 'chat',
    capabilities: ['推理', '实时信息'],
    unit: '1K tokens',
    price: 0.01,
    multiplier: 1,
    context: '128K',
    description: '结合实时信息的推理与问答。',
    recommended: false,
    enabled: false,
    sort: 8,
    updatedAt: stamp(20, 16, 40)
  },
  {
    id: 'mc-9',
    key: 'nano-banana',
    name: 'nano-banana',
    appKey: 'nanobanana',
    vendor: 'Google',
    provider: 'Google',
    category: 'image',
    capabilities: ['图像生成', '文字渲染', '参考图'],
    unit: '张',
    price: 0.14,
    multiplier: 1,
    context: '—',
    description: '文字排版准确，适合海报与商品图。',
    recommended: true,
    enabled: true,
    sort: 9,
    updatedAt: stamp(3, 14, 0)
  },
  {
    id: 'mc-10',
    key: 'seedream-5',
    name: 'doubao-seedream-5.0-lite',
    appKey: 'seedream',
    vendor: '字节跳动',
    provider: '火山引擎',
    category: 'image',
    capabilities: ['图像生成', '中文提示词'],
    unit: '张',
    price: 0.37,
    multiplier: 1,
    context: '—',
    description: '中文提示词理解好，出图稳定。',
    recommended: false,
    enabled: true,
    sort: 10,
    updatedAt: stamp(3, 14, 0)
  },
  {
    id: 'mc-11',
    key: 'flux-1-1-pro',
    name: 'Flux 1.1 Pro',
    appKey: 'flux',
    vendor: 'Black Forest Labs',
    provider: '硅基流动',
    category: 'image',
    capabilities: ['图像生成', '写实'],
    unit: '张',
    price: 0.08,
    multiplier: 1,
    context: '—',
    description: '写实风格与摄影质感。',
    recommended: false,
    enabled: true,
    sort: 11,
    updatedAt: stamp(9, 15, 10)
  },
  {
    id: 'mc-12',
    key: 'midjourney-v7',
    name: 'Midjourney v7',
    appKey: 'midjourney',
    vendor: 'Midjourney',
    provider: 'Midjourney',
    category: 'image',
    capabilities: ['图像生成', '风格化'],
    unit: '张',
    price: 0.22,
    multiplier: 1,
    context: '—',
    description: '强风格化插画与概念图。',
    recommended: false,
    enabled: false,
    sort: 12,
    updatedAt: stamp(25, 10, 0)
  },
  {
    id: 'mc-13',
    key: 'seedance-2',
    name: 'doubao-seedance-2.0',
    appKey: 'seedance',
    vendor: '字节跳动',
    provider: '火山引擎',
    category: 'video',
    capabilities: ['文生视频', '图生视频'],
    unit: '5s',
    price: 8.4,
    multiplier: 1,
    context: '—',
    description: '画面稳定的短视频生成。',
    recommended: true,
    enabled: true,
    sort: 13,
    updatedAt: stamp(2, 10, 20)
  },
  {
    id: 'mc-14',
    key: 'kling-2-5',
    name: 'Kling v2.5-Turbo',
    appKey: 'kling',
    vendor: '快手',
    provider: '火山引擎',
    category: 'video',
    capabilities: ['文生视频', '运动控制'],
    unit: '5s',
    price: 2.1,
    multiplier: 1,
    context: '—',
    description: '运动幅度大，出片快。',
    recommended: false,
    enabled: true,
    sort: 14,
    updatedAt: stamp(2, 10, 20)
  },
  {
    id: 'mc-15',
    key: 'sora-2',
    name: 'Sora 2',
    appKey: 'sora',
    vendor: 'OpenAI',
    provider: 'OpenAI',
    category: 'video',
    capabilities: ['文生视频', '长镜头'],
    unit: '10s',
    price: 4.2,
    multiplier: 1,
    context: '—',
    description: '长镜头与物理一致性更好。',
    recommended: false,
    enabled: false,
    sort: 15,
    updatedAt: stamp(9, 16, 0)
  },
  {
    id: 'mc-16',
    key: 'suno-v6',
    name: 'Suno v6',
    appKey: 'suno',
    vendor: 'Suno',
    provider: 'Suno',
    category: 'music',
    capabilities: ['音乐生成', '人声'],
    unit: '首',
    price: 0.56,
    multiplier: 1,
    context: '—',
    description: '从提示词生成完整歌曲与人声。',
    recommended: true,
    enabled: true,
    sort: 16,
    updatedAt: stamp(5, 15, 40)
  },
  {
    id: 'mc-17',
    key: 'producer-2',
    name: 'FUZZ-2.0 Pro',
    appKey: 'producer',
    vendor: 'Producer',
    provider: 'Suno',
    category: 'music',
    capabilities: ['编曲', '分轨'],
    unit: '首',
    price: 0.58,
    multiplier: 1,
    context: '—',
    description: '编曲与分轨导出。',
    recommended: false,
    enabled: true,
    sort: 17,
    updatedAt: stamp(5, 15, 40)
  }
]

function read() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (error) {
    return null
  }
}

function write(value) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
  } catch (error) {
    // 忽略本地存储异常
  }
}

export function loadModelCatalog() {
  const cached = read()
  if (Array.isArray(cached)) return cached
  write(CATALOG_SEED)
  return JSON.parse(JSON.stringify(CATALOG_SEED))
}

export function saveModelCatalog(list) {
  write(list)
  // 通知已挂载的用户端页面重新读取目录（同标签页不触发 storage 事件）
  try {
    window.dispatchEvent(new CustomEvent('lingxi:model-catalog-changed'))
  } catch (error) {
    // 非浏览器环境忽略
  }
}
