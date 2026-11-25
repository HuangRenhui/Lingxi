/** 定时任务页静态配置。 */

export const tabs = [
  { key: 'tasks', label: '定时任务' },
  { key: 'runs', label: '运行记录' }
]

export const modelOptions = [
  'gpt-5.6-sol',
  'gpt-5.6-luna',
  'deepseek-4.0',
  'grok-4',
  'gemini-3.1-pro',
  'claude-opus-5',
  'kimi-k3',
  'glm-5.1'
]

export const frequencyOptions = [
  { label: '每隔', value: 'INTERVAL' },
  { label: '每小时', value: 'HOURLY' },
  { label: '每天', value: 'DAILY' },
  { label: '每周', value: 'WEEKLY' },
  { label: 'Cron 表达式', value: 'CRON' }
]

export const timezoneOptions = [
  { label: 'Asia/Shanghai (UTC+8)', value: 'Asia/Shanghai' },
  { label: 'UTC (UTC)', value: 'UTC' },
  { label: 'Asia/Tokyo (UTC+9)', value: 'Asia/Tokyo' },
  { label: 'Asia/Singapore (UTC+8)', value: 'Asia/Singapore' },
  { label: 'Europe/London (UTC+1)', value: 'Europe/London' },
  { label: 'America/New_York (UTC-4)', value: 'America/New_York' },
  { label: 'America/Los_Angeles (UTC-7)', value: 'America/Los_Angeles' }
]

export const skillOptions = ['/github', '/zhihu', '/web-search', '/suno-music', '/seedance-video', '/skill-creator']
export const mcpOptions = ['browser', 'filesystem', 'notion', 'github', 'postgres']

export const templates = [
  {
    title: '每日资讯简报',
    description: '每天汇总行业动态，输出三条要点、影响判断与原文链接。',
    icon: 'Document',
    model: 'gpt-5.6-sol',
    promptTemplate: '请汇总 {{date}} AI 行业最重要的三条动态，每条包含标题、关键事实、影响判断和原文链接。',
    scheduleType: 'DAILY',
    executeTime: '09:00'
  },
  {
    title: '竞品动态跟踪',
    description: '每周跟踪竞品官网、定价与更新日志，整理变化清单。',
    icon: 'Aim',
    model: 'claude-opus-5',
    promptTemplate: '请检查主要竞品在 {{date}} 当周的官网、定价页与更新日志，整理变化清单并标注影响程度。',
    scheduleType: 'WEEKLY',
    executeTime: '10:00',
    weekDays: '1'
  },
  {
    title: '数据日报',
    description: '每天整理关键指标与异常波动，生成可转发的简报。',
    icon: 'TrendCharts',
    model: 'deepseek-4.0',
    promptTemplate: '请汇总 {{date}} 的关键业务指标，指出环比异常并给出两条可执行的排查建议。',
    scheduleType: 'DAILY',
    executeTime: '18:00'
  },
  {
    title: '社媒内容排期',
    description: '每天生成三条社媒文案，保持账号稳定更新节奏。',
    icon: 'ChatLineSquare',
    model: 'gpt-5.6-luna',
    promptTemplate: '围绕产品卖点生成 {{date}} 的三条社媒文案，语气自然，每条不超过 120 字。',
    scheduleType: 'DAILY',
    executeTime: '10:00'
  }
]

export const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  promptTemplate: [{ required: true, message: '请输入提示词', trigger: 'blur' }],
  model: [{ required: true, message: '请选择模型', trigger: 'change' }]
}
