import service from '@/utils/request'
import { getUser } from '@/utils/auth'

/**
 * 前端 Mock 模式
 *
 * 默认在开发环境下启用（`npm run dev`），无需启动后端即可浏览全部页面。
 * 如需连接真实后端，在 `.env.development` 或启动命令中设置 `VITE_MOCK=false`。
 * 如需发布纯前端演示版，可在构建时显式设置 `VITE_MOCK=true`，此时数据保存在浏览器本地。
 */
export const MOCK_ENABLED =
  import.meta.env.VITE_MOCK === 'true' ||
  (import.meta.env.DEV && import.meta.env.VITE_MOCK !== 'false')

export const MOCK_USER = {
  id: 1,
  username: 'demo',
  nickname: '体验用户',
  email: 'demo@lingxi.local',
  role: 'USER',
  balance: 2,
  credits: 2,
  plan: '体验版'
}

function toBase64Url(value) {
  const bytes = new TextEncoder().encode(JSON.stringify(value))
  let binary = ''
  bytes.forEach(byte => {
    binary += String.fromCharCode(byte)
  })
  return btoa(binary).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_')
}

/**
 * 演示令牌：结构与 JWT 一致（alg=none，不参与签名校验），并带 exp，
 * 让前端可以用统一的过期判断逻辑，而不是「有字符串就算已登录」。
 */
export const MOCK_TOKEN = `${toBase64Url({ alg: 'none', typ: 'JWT' })}.${toBase64Url({
  sub: MOCK_USER.id,
  username: MOCK_USER.username,
  exp: Math.floor(Date.now() / 1000) + 7 * 24 * 60 * 60
})}.mock`

// 本地账号库：注册与登录都落在浏览器，仅用于演示。
const ACCOUNTS_KEY = 'lingxi-local-accounts'
const EMAIL_CODES_KEY = 'lingxi-email-codes'

// 验证码有效期；回传验证码只用于本地开发与显式开启的演示构建，避免随响应泄露
const CODE_TTL = 5 * 60 * 1000
const EXPOSE_MOCK_CODE = Boolean(import.meta.env.DEV) || import.meta.env.VITE_MOCK_EXPOSE_CODE === 'true'

export const DEMO_ACCOUNT = {
  email: 'demo@lingxi.local',
  password: 'lingxi-demo-2026',
  user: MOCK_USER
}

/**
 * 演示管理员账号：口令不写进页面，可用环境变量覆盖。
 * 仅用于本地 Mock，真实后端由 /auth/login 校验数据库中的 BCrypt 哈希。
 */
const ADMIN_USERNAME = import.meta.env.VITE_MOCK_ADMIN_USER || 'admin'
const ADMIN_PASSWORD = import.meta.env.VITE_MOCK_ADMIN_PASSWORD || 'Admin@123'

export const MOCK_ADMIN_USER = {
  id: 0,
  username: ADMIN_USERNAME,
  nickname: '平台管理员',
  email: 'admin@lingxi.local',
  role: 'ADMIN',
  plan: '平台'
}

function readAccounts() {
  try {
    const raw = localStorage.getItem(ACCOUNTS_KEY)
    if (raw) {
      const list = JSON.parse(raw)
      if (Array.isArray(list) && list.length) return list
    }
  } catch (error) {
    // 本地存储异常时退回内置演示账号
  }
  return [DEMO_ACCOUNT]
}

function writeAccounts(accounts) {
  try {
    localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts))
  } catch (error) {
    // 忽略写入失败，保证流程不中断
  }
}

function readEmailCodes() {
  try {
    const value = JSON.parse(localStorage.getItem(EMAIL_CODES_KEY) || '{}')
    // 缓存被写成数组/原始值时不能直接当对象用，否则 delete/write 会静默失效
    if (!value || typeof value !== 'object' || Array.isArray(value)) return {}
    return value
  } catch (error) {
    return {}
  }
}

function writeEmailCodes(codes) {
  try {
    localStorage.setItem(EMAIL_CODES_KEY, JSON.stringify(codes))
  } catch (error) {
    // 忽略写入失败，保证流程不中断
  }
}

function normalizeEmail(value) {
  return String(value || '').trim().toLowerCase()
}

const TASK_STORAGE_KEY = 'lingxi-mock-tasks'

function buildSeedTasks() {
  const now = Date.now()
  return [
    {
      id: 'mock-task-1',
      name: '每日资讯简报',
      promptTemplate: '请汇总 {{date}} AI 行业最重要的三条动态，每条包含标题、关键事实、影响判断和原文链接。',
      model: 'gpt-5.6-sol',
      runLocation: 'CLOUD',
      timezone: 'Asia/Shanghai',
      scheduleType: 'DAILY',
      cronExpr: null,
      intervalMinutes: null,
      executeTime: '09:00',
      weekDays: null,
      jitterSeconds: 0,
      maxRounds: 50,
      status: 'ENABLED',
      runCount: 12,
      createdAt: new Date(now - 6 * 86400000).toISOString(),
      lastRunAt: new Date(now - 18 * 3600000).toISOString()
    },
    {
      id: 'mock-task-2',
      name: '竞品动态跟踪',
      promptTemplate: '请检查主要竞品在本周的官网、定价页与更新日志，整理变化清单并标注影响程度。',
      model: 'claude-opus-5',
      runLocation: 'CLOUD',
      timezone: 'Asia/Shanghai',
      scheduleType: 'WEEKLY',
      cronExpr: null,
      intervalMinutes: null,
      executeTime: '10:00',
      weekDays: '1',
      jitterSeconds: 30,
      maxRounds: 30,
      status: 'DISABLED',
      runCount: 4,
      createdAt: new Date(now - 4 * 86400000).toISOString(),
      lastRunAt: new Date(now - 3 * 86400000).toISOString()
    }
  ]
}

function readTasks() {
  try {
    const raw = localStorage.getItem(TASK_STORAGE_KEY)
    if (raw) {
      const list = JSON.parse(raw)
      // 缓存被写成 null/对象时不能直接返回，否则后续 unshift/find 会抛 TypeError
      if (Array.isArray(list)) return list
    }
  } catch (error) {
    // 本地存储不可用或内容损坏时退回种子数据
  }
  return buildSeedTasks()
}

function writeTasks(tasks) {
  try {
    localStorage.setItem(TASK_STORAGE_KEY, JSON.stringify(tasks))
  } catch (error) {
    // 忽略写入失败，保证交互不中断
  }
}

function ok(data, config) {
  return {
    data: { code: 200, message: 'ok', data },
    status: 200,
    statusText: 'OK',
    headers: {},
    config
  }
}

function fail(message, config) {
  return {
    data: { code: 400, message, data: null },
    status: 200,
    statusText: 'OK',
    headers: {},
    config
  }
}

// 与后端 RestAuthenticationEntryPoint 对齐：HTTP 200 + 业务码 401，由前端拦截器统一清理登录态
function unauthorized(message, config) {
  return {
    data: { code: 401, message, data: null },
    status: 200,
    statusText: 'OK',
    headers: {},
    config
  }
}

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function parseBody(data) {
  if (!data) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch (error) {
      return {}
    }
  }
  return data
}

// 用自定义 adapter 接管请求：命中 Mock 路由时直接返回本地数据，未命中时返回空数据，
// 避免未启动后端时页面出现网络错误提示。
if (MOCK_ENABLED) {
  // adapter 内部任何未预期异常都转换为业务错误，避免整个请求 Promise 直接 reject 造成白屏
  service.defaults.adapter = async config => {
    try {
      return await handleMockRequest(config)
    } catch (error) {
      return fail('演示数据读取失败，请刷新后重试', config)
    }
  }
}

async function handleMockRequest(config) {
    await delay(180)
    const method = (config.method || 'get').toUpperCase()
    const path = (config.url || '').split('?')[0]
    const body = parseBody(config.data)

    if (method === 'POST' && path === '/auth/admin-login') {
      const username = String(body.username || '').trim()
      const password = String(body.password || '')
      // 只在本地演示适配器内比对，视图层不再持有明文口令
      if (username !== ADMIN_USERNAME || password !== ADMIN_PASSWORD) {
        return fail('管理员账号或密码不正确', config)
      }
      return ok({ token: MOCK_TOKEN, user: MOCK_ADMIN_USER }, config)
    }

    if (method === 'POST' && path === '/auth/login') {
      const email = normalizeEmail(body.email || body.username)
      const account = readAccounts().find(item => normalizeEmail(item.email) === email)
      if (!account) return fail('该邮箱尚未注册，请先注册', config)
      if (account.password !== String(body.password || '')) {
        return fail('邮箱或密码不正确', config)
      }
      return ok({ token: MOCK_TOKEN, user: account.user }, config)
    }

    if (method === 'POST' && path === '/auth/email-code') {
      const email = normalizeEmail(body.email)
      if (!email) return fail('请输入电子邮件', config)
      const codes = readEmailCodes()
      const existing = codes[email]
      // 同一邮箱 60 秒内只允许发送一次，避免接口被刷；老缓存缺 sentAt 时按可发送处理
      const lastSentAt = Number(existing?.sentAt) || 0
      if (lastSentAt && Date.now() - lastSentAt < 60 * 1000) {
        return fail('验证码发送过于频繁，请稍后再试', config)
      }
      const code = String(Math.floor(100000 + Math.random() * 900000))
      const now = Date.now()
      codes[email] = { code, sentAt: now, attempts: 0, expiresAt: now + CODE_TTL }
      writeEmailCodes(codes)
      // 仅本地开发（或显式开启的演示构建）回传验证码，生产模拟包不回传
      return ok(EXPOSE_MOCK_CODE ? { code } : null, config)
    }

    if (method === 'POST' && path === '/auth/register') {
      const email = normalizeEmail(body.email)
      const code = String(body.code || '')
      const password = String(body.password || '')
      const records = readEmailCodes()
      const record = records[email]
      if (!record || record.code !== code) {
        // 连续错误 5 次作废验证码，避免 6 位码被穷举
        if (record) {
          record.attempts = (Number(record.attempts) || 0) + 1
          if (record.attempts >= 5) delete records[email]
          writeEmailCodes(records)
        }
        return fail('验证码不正确', config)
      }
      // 老缓存可能没有 expiresAt：此时必须按过期处理，否则「Date.now() > undefined」恒为 false，验证码永不过期
      const expiresAt = Number(record.expiresAt) || 0
      if (!expiresAt || Date.now() > expiresAt) {
        delete records[email]
        writeEmailCodes(records)
        return fail('验证码已过期，请重新获取', config)
      }
      if (password.length < 12 || /^\d+$/.test(password)) {
        return fail('密码不符合安全要求', config)
      }
      const accounts = readAccounts()
      if (accounts.some(item => normalizeEmail(item.email) === email)) {
        return fail('该邮箱已注册，请直接登录', config)
      }
      const name = email.split('@')[0]
      const user = {
        id: Date.now(),
        username: name,
        nickname: name,
        email,
        role: 'USER',
        balance: 0,
        credits: 0,
        plan: '免费版'
      }
      accounts.push({ email, password, user })
      writeAccounts(accounts)
      const codes = readEmailCodes()
      delete codes[email]
      writeEmailCodes(codes)
      return ok({ token: MOCK_TOKEN, user }, config)
    }

    if (method === 'GET' && path === '/auth/profile') {
      // 未登录时不能兜底返回演示用户：那样清掉令牌后仍能读到「已登录」资料，鉴权形同虚设
      const user = getUser()
      if (!user) return unauthorized('登录状态已失效，请重新登录', config)
      return ok(user, config)
    }

    if (path === '/agent/tasks' && method === 'GET') {
      return ok(readTasks(), config)
    }

    if (path === '/agent/tasks' && method === 'POST') {
      const tasks = readTasks()
      const task = {
        ...body,
        id: `mock-task-${Date.now()}`,
        status: 'ENABLED',
        runCount: 0,
        createdAt: new Date().toISOString(),
        lastRunAt: null
      }
      tasks.unshift(task)
      writeTasks(tasks)
      return ok(task, config)
    }

    const statusMatch = path.match(/^\/agent\/tasks\/([^/]+)\/status$/)
    if (statusMatch && method === 'PATCH') {
      const tasks = readTasks()
      const task = tasks.find(item => item.id === statusMatch[1])
      if (task) {
        task.status = body.status
        writeTasks(tasks)
      }
      return ok(task || null, config)
    }

    const taskMatch = path.match(/^\/agent\/tasks\/([^/]+)$/)
    if (taskMatch) {
      const tasks = readTasks()
      const index = tasks.findIndex(item => item.id === taskMatch[1])
      if (method === 'PUT' && index >= 0) {
        tasks[index] = { ...tasks[index], ...body, id: tasks[index].id }
        writeTasks(tasks)
        return ok(tasks[index], config)
      }
      if (method === 'DELETE' && index >= 0) {
        tasks.splice(index, 1)
        writeTasks(tasks)
        return ok(true, config)
      }
    }

  return ok(null, config)
}
