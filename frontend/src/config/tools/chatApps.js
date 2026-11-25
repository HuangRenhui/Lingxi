/**
 * 创作工具与对话应用注册表。
 *
 * 首页、左侧应用栏、路由和通用创作页都从这里读取配置，
 * 新增一个工具时只需要补一条配置，不需要再复制一套页面。
 */

export const chatApps = [
  {
    key: 'chatgpt',
    path: '/chat/chatgpt',
    title: 'ChatGPT',
    model: 'GPT-6 Astra',
    models: [
      { name: 'GPT-6 Astra', description: '复杂项目深度推理' },
      { name: 'GPT-6.1 Sol', description: '高性能多模态推理' },
      { name: 'GPT-6.1 Terra', description: '高效多模态推理' },
      { name: 'GPT-6 Sol', description: '复杂编程与长上下文推理' },
      { name: 'GPT-6 Luna', description: '快速对话与日常编程' },
      { name: 'GPT-5.6 Luna', description: '免费快速问答', badge: '免费' },
      { name: 'GPT-5.6 Sol', description: '进阶推理与编程' },
      {
        name: 'GPT-5.6 Sol Fast',
        description: '面向高强度推理和编程任务的快速档位',
        badge: 'ACE 4'
      },
      { name: 'GPT-5.6 Terra', description: '均衡写作与分析' }
    ],
    icon: 'ChatDotRound',
    description: '旗舰通用模型，兼顾复杂推理与日常问答，支持语音通话。'
  },
  {
    key: 'deepseek',
    path: '/chat/deepseek',
    title: 'DeepSeek',
    model: 'DeepSeek V4 Pro',
    models: [
      { name: 'DeepSeek V4 Pro', description: '百万上下文旗舰，Agent 与推理能力全面增强' },
      { name: 'DeepSeek V4 Flash', description: '高效快速版，低延迟与高性价比' },
      { name: 'DeepSeek V3.2', description: '通用对话与工具调用，稳定可靠' },
      { name: 'DeepSeek R1', description: '深度思考，擅长数学与逻辑推理' },
      { name: 'DeepSeek V3', description: '经典版本，适合兼容旧工作流' }
    ],
    icon: 'Cpu',
    description: '性价比出众，深度思考版本擅长数学与逻辑推理。'
  },
  {
    key: 'grok',
    path: '/chat/grok',
    title: 'Grok',
    model: 'Grok 4.7',
    models: [
      { name: 'Grok 4.7', description: '推理、视觉与工具调用' },
      { name: 'Grok 4', description: '视觉分析与研究' },
      { name: 'Grok 3', description: '日常对话与写作' }
    ],
    icon: 'Lightning',
    description: '回答风格直接，实时性强，支持图片理解。'
  },
  {
    key: 'gemini',
    path: '/chat/gemini',
    title: 'Gemini',
    model: 'Gemini 4.0 Pro',
    models: [
      { name: 'Gemini 4.0 Pro', description: '多模态旗舰，复杂推理与长上下文' },
      { name: 'Gemini 4.0 Flash', description: '高速多模态，适合实时应用' },
      { name: 'Gemini 3.1 Pro', description: '稳定旗舰，图文音视频理解全面' },
      { name: 'Gemini 3.1 Flash', description: '轻量快速，适合长文档处理' },
      { name: 'Gemini 2.5 Flash', description: '经典轻量模型，成本更低' }
    ],
    icon: 'Star',
    description: '多模态理解能力强，适合图文混合与复杂推理场景。'
  },
  {
    key: 'claude',
    path: '/chat/claude',
    title: 'Claude',
    model: 'Claude Opus 5.5',
    models: [
      { name: 'Claude Opus 5.5', description: '复杂智能体任务旗舰' },
      { name: 'Claude Fable 5.2', description: '复杂推理与长程任务' },
      { name: 'Claude Opus 5', description: '深度推理与复杂编程' },
      { name: 'Claude Opus 4.8', description: '严谨分析与规划' },
      { name: 'Claude Sonnet 4.6', description: '均衡编程与文档处理' },
      { name: 'Claude Haiku 4.5', description: '快速问答与信息提取' }
    ],
    icon: 'Document',
    description: '长文写作与代码任务表现出色，擅长严谨的分析与推理。'
  },
  {
    key: 'kimi',
    path: '/chat/kimi',
    title: 'Kimi',
    model: 'Kimi K3.1 Pro',
    models: [
      { name: 'Kimi K3.1 Pro', description: '1M 上下文旗舰，Agent 与工具调用增强' },
      { name: 'Kimi K3', description: '超长上下文，深度研究与资料整理' },
      { name: 'Kimi K2.5', description: '稳定版本，知识问答与写作' },
      { name: 'Kimi K2', description: '经典开源模型，中文表达自然' },
      { name: 'Kimi K1.5', description: '轻量快速，成本优先' }
    ],
    icon: 'Reading',
    description: '长上下文阅读与资料整理，适合文档分析与知识问答。'
  },
  {
    key: 'qwen',
    path: '/chat/qwen',
    title: '通义千问',
    model: 'Qwen 4 Max',
    models: [
      { name: 'Qwen 4 Max', description: '旗舰版本，中文理解与复杂推理领先' },
      { name: 'Qwen 4 Plus', description: '均衡版本，适合日常对话与内容创作' },
      { name: 'Qwen 4 Flash', description: '低延迟版本，适合高频与批量任务' },
      { name: 'Qwen3.5 Coder', description: '代码专用模型，擅长工程与重构任务' },
      { name: 'Qwen3 235B', description: '开源大模型，适合私有化与定制场景' }
    ],
    icon: 'Sunrise',
    description: '中文语义与工具调用能力强，覆盖对话、代码与多模态场景。'
  },
  {
    key: 'doubao',
    path: '/chat/doubao',
    title: '豆包',
    model: 'Doubao Seed 2.0 Pro',
    models: [
      { name: 'Doubao Seed 2.0 Pro', description: '旗舰版本，中文对话与内容创作表现稳定' },
      { name: 'Doubao Seed 2.0 Vision', description: '视觉理解增强，适合图片问答与文档识别' },
      { name: 'Doubao Seed 1.6', description: '通用版本，响应快、适合高频场景' },
      { name: 'Doubao Seed 1.6 Flash', description: '轻量高速版，成本优先' },
      { name: 'Doubao Lite', description: '轻量版本，适合批量任务' }
    ],
    icon: 'ChatLineRound',
    description: '中文对话自然，视觉与语音场景覆盖完整。'
  }
]

const imageRatios = [
  { value: '1:1', w: 22, h: 22 },
  { value: '3:2', w: 26, h: 18 },
  { value: '2:3', w: 18, h: 26 },
  { value: '16:9', w: 28, h: 16 },
  { value: '9:16', w: 16, h: 28 },
  { value: '4:3', w: 26, h: 20 },
  { value: '3:4', w: 20, h: 26 }
]

const videoRatios = [
  { value: '16:9', w: 28, h: 16 },
  { value: '9:16', w: 16, h: 28 },
  { value: '1:1', w: 22, h: 22 },
  { value: '4:3', w: 26, h: 20 },
  { value: '3:4', w: 20, h: 26 },
  { value: '21:9', w: 30, h: 13 },
  { value: '3:2', w: 26, h: 18 },
  { value: '2:3', w: 18, h: 26 }
]

const qrStyles = [
  '日落',
  '花卉',
  '雪花',
  '羽毛',
  '雨滴',
  '超现实',
  '史诗王国',
  '复杂工作室',
  '对称杰作',
  '发光之路',
  '天体之旅',
  '霓虹机械',
  '空灵',
  '低多边形',
  '金色景观',
  '电影般的广阔',
  '电影般的温暖',
  '荒凉荒野',
  '充满活力的调色板',
  '神秘之旅',
  '永恒的电影',
  '华丽星系',
  '杰出画布',
  '富有表现力的壁画',
  '宁静薄雾'
]
