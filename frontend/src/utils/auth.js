const TOKEN_KEY = 'lingxi-token'
const USER_KEY = 'lingxi-user'
const ACCOUNTS_KEY = 'lingxi-accounts'
const ADMIN_SESSION_KEY = 'lingxi-admin-session'
const ADMIN_TOKEN_KEY = 'lingxi_admin_token'

// 管理员会话有效期与账号记忆有效期
const ADMIN_SESSION_TTL = 8 * 60 * 60 * 1000
const ACCOUNT_TTL = 7 * 24 * 60 * 60 * 1000

/**
 * 本地存储访问统一走安全封装：
 * 隐私模式、存储配额耗尽或浏览器禁用存储时，读写不应让页面直接白屏。
 */
function safeGet(key) {
  try {
    return localStorage.getItem(key)
  } catch (error) {
    return null
  }
}

function safeSet(key, value) {
  try {
    localStorage.setItem(key, value)
    return true
  } catch (error) {
    return false
  }
}

function safeRemove(key) {
  try {
    localStorage.removeItem(key)
  } catch (error) {
    // 忽略清理失败
  }
}

function readJson(key, fallback) {
  const raw = safeGet(key)
  if (!raw) return fallback
  try {
    const value = JSON.parse(raw)
    return value ?? fallback
  } catch (error) {
    return fallback
  }
}

export function getToken() {
  return safeGet(TOKEN_KEY)
}

export function setToken(token) {
  return safeSet(TOKEN_KEY, token)
}

export function removeToken() {
  safeRemove(TOKEN_KEY)
}

export function getUser() {
  const user = readJson(USER_KEY, null)
  return user && typeof user === 'object' ? user : null
}

export function setUser(user) {
  return safeSet(USER_KEY, JSON.stringify(user))
}

export function removeUser() {
  safeRemove(USER_KEY)
}

/* ---------- JWT 有效性 ---------- */

/**
 * 解析 JWT 载荷（不校验签名，签名只能由后端验证）。
 * 前端解析仅用于判断令牌是否过期，避免过期后仍停留在页面反复报错。
 */
export function parseJwtPayload(token) {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length !== 3 || !parts[1]) return null
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
    const binary = atob(padded)
    let json = ''
    for (let i = 0; i < binary.length; i += 1) {
      json += `%${`00${binary.charCodeAt(i).toString(16)}`.slice(-2)}`
    }
    const payload = JSON.parse(decodeURIComponent(json))
    return payload && typeof payload === 'object' ? payload : null
  } catch (error) {
    return null
  }
}

export function isTokenValid(token) {
  const payload = parseJwtPayload(token)
  if (!payload) return false
  if (payload.exp && Date.now() >= Number(payload.exp) * 1000) return false
  return true
}

export function hasValidToken() {
  return isTokenValid(getToken())
}

/** 令牌过期时清理登录态，避免守卫反复重定向。 */
export function clearInvalidSession() {
  removeToken()
  removeUser()
}

/* ---------- 本机账号列表 ---------- */

export function getAccounts() {
  const list = readJson(ACCOUNTS_KEY, [])
  if (!Array.isArray(list)) return []
  const now = Date.now()
  // 过期账号不再返回：避免本机长期堆积历史 JWT 与完整用户对象
  return list.filter(item => item && item.key && (!item.expiresAt || now < Number(item.expiresAt)))
}

export function rememberAccount(user) {
  if (!user || typeof user !== 'object') return getAccounts()
  const key = String(user.id ?? user.username ?? user.email ?? '')
  if (!key) return getAccounts()
  const token = getToken()
  const payload = parseJwtPayload(token)
  const record = {
    key,
    username: user.username || '',
    nickname: user.nickname || user.username || '',
    email: user.email || '',
    token,
    user,
    expiresAt: payload?.exp ? Number(payload.exp) * 1000 : Date.now() + ACCOUNT_TTL
  }
  const next = [record, ...getAccounts().filter(item => item.key !== key)]
  safeSet(ACCOUNTS_KEY, JSON.stringify(next))
  return next
}

export function switchAccount(key) {
  const account = getAccounts().find(item => item.key === String(key))
  if (!account?.token) return false
  if (account.expiresAt && Date.now() >= Number(account.expiresAt)) return false
  if (!isTokenValid(account.token)) return false
  if (!setToken(account.token)) return false
  setUser(account.user)
  return true
}

/* ---------- 管理后台会话 ---------- */

/** 后台令牌独立存储，避免与用户端登录态互相覆盖。 */
export function getAdminToken() {
  return safeGet(ADMIN_TOKEN_KEY)
}

export function setAdminToken(token) {
  return safeSet(ADMIN_TOKEN_KEY, token)
}

export function removeAdminToken() {
  safeRemove(ADMIN_TOKEN_KEY)
}

export function getAdminSession() {
  const session = readJson(ADMIN_SESSION_KEY, null)
  if (!session || typeof session !== 'object') return null
  // 会话必须带管理员标识，空对象不应被当成有效登录
  if (!session.username) return null
  // 读操作不做删除，清理交给守卫与登出流程，避免一次读到过期令牌就静默登出
  const token = getAdminToken()
  if (!token || !isTokenValid(token)) return null
  if (session.expiresAt && Date.now() >= Number(session.expiresAt)) return null
  return { ...session, token }
}

export function setAdminSession(session) {
  const { token, ...rest } = session || {}
  if (token) setAdminToken(token)
  const next = {
    ...rest,
    expiresAt: Number(rest?.expiresAt) || Date.now() + ADMIN_SESSION_TTL
  }
  safeSet(ADMIN_SESSION_KEY, JSON.stringify(next))
  return next
}

export function removeAdminSession() {
  safeRemove(ADMIN_SESSION_KEY)
  removeAdminToken()
}
