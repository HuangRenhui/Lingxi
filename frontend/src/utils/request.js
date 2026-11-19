import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { clearInvalidSession, getAdminToken, getToken, removeAdminSession, setToken } from '@/utils/auth'
import { ADMIN_BASE, ADMIN_LOGIN_PATH } from '@/config/admin'

/** 业务码：积分余额不足（与后端 ResultCode.INSUFFICIENT_BALANCE 对齐） */
const CODE_INSUFFICIENT_BALANCE = 8802
const WALLET_PATH = '/console/wallet'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// 并发请求同时 401 时只跳转一次，避免重复重定向打断页面
let redirecting = false
// 过期提示同样去重：并发 5 个请求不应弹 5 条红条
let lastAuthNotice = 0

function notifyAuthExpired() {
  const now = Date.now()
  if (now - lastAuthNotice < 3000) return
  lastAuthNotice = now
  ElMessage.error('登录状态已过期，请重新登录')
}

// 余额不足提示去重：并发扣费失败时只弹一次确认框
let lastInsufficientNotice = 0

/**
 * 余额不足统一处理：弹确认框引导去充值，确认后跳到钱包页并自动打开充值框。
 *
 * <p>用动态 import 取 router，避免 request → router → store → api → request 的循环依赖。</p>
 */
function notifyInsufficientBalance(message) {
  const now = Date.now()
  if (now - lastInsufficientNotice < 3000) return
  lastInsufficientNotice = now
  ElMessageBox.confirm(message || '积分余额不足，是否前往充值？', '积分余额不足', {
    confirmButtonText: '去充值',
    cancelButtonText: '稍后再说',
    type: 'warning'
  })
    .then(() => {
      if (window.location.pathname.startsWith(WALLET_PATH)) {
        // 已在钱包页：直接刷新以打开充值框
        window.location.search = '?recharge=1'
        return
      }
      import('@/router')
        .then(({ default: router }) => router.push(`${WALLET_PATH}?recharge=1`))
        .catch(() => {
          window.location.href = `${WALLET_PATH}?recharge=1`
        })
    })
    .catch(() => {
      // 用户选择「稍后再说」，不做跳转
    })
}

/** 请求是否属于管理后台：接口路径或当前路由在后台布局下。 */
function isAdminRequest(config) {
  const url = String(config?.url || '')
  if (url.startsWith('/admin') || url.includes('/api/admin')) return true
  const path = window.location.pathname
  return path === ADMIN_BASE || path.startsWith(`${ADMIN_BASE}/`)
}

function redirectToLogin(path = '/login') {
  if (redirecting || window.location.pathname === path) return
  redirecting = true
  const current = `${window.location.pathname}${window.location.search}`
  const target = current && current !== '/' ? `?redirect=${encodeURIComponent(current)}` : ''
  window.location.href = `${path}${target}`
  window.setTimeout(() => {
    redirecting = false
  }, 3000)
}

function handleUnauthorized(config) {
  // 注销/刷新等主动请求失败时不跳转，避免打断本地清理
  if (config?.skipAuthRedirect) return
  // 后台会话失效只清后台令牌，不影响用户端登录态
  if (isAdminRequest(config)) {
    removeAdminSession()
    redirectToLogin(ADMIN_LOGIN_PATH)
    return
  }
  clearInvalidSession()
  redirectToLogin()
}

// 并发 401 时只发起一次刷新，其余请求复用同一个 Promise
let refreshPromise = null

function refreshToken() {
  const token = getToken()
  if (!token) return Promise.reject(new Error('no token'))
  if (!refreshPromise) {
    const base = service.defaults.baseURL || '/api'
    refreshPromise = axios
      .post(`${base}/auth/refresh`, null, {
        headers: { Authorization: `Bearer ${token}` }
      })
      .then(res => {
        const body = res.data
        if (body?.code === 200 && body.data?.token) {
          setToken(body.data.token)
          return body.data.token
        }
        throw new Error(body?.message || '刷新登录态失败')
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

// 用户端 401：先尝试刷新并重放原请求，失败再清理并跳转登录
function handle401(config) {
  const url = String(config?.url || '')
  const isAuthCall = url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/logout')
  if (!config || config._retried || config.skipAuthRefresh || isAdminRequest(config) || isAuthCall) {
    const silent = Boolean(config?.skipAuthRedirect)
    handleUnauthorized(config)
    // 统一走 notifyAuthExpired 去重：后台一次加载会并发多个请求，裸 ElMessage 会弹出一串红条
    if (!silent) notifyAuthExpired()
    return Promise.reject(new Error('登录状态已过期'))
  }
  config._retried = true
  return refreshToken()
    .then(token => {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
      return service(config)
    })
    .catch(error => {
      handleUnauthorized(config)
      notifyAuthExpired()
      return Promise.reject(error)
    })
}

service.interceptors.request.use(config => {
  const admin = isAdminRequest(config)
  const token = admin ? getAdminToken() || getToken() : getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res && typeof res.code !== 'undefined') {
      if (res.code === 200) {
        return res.data
      }
      if (res.code === 401) {
        return handle401(response.config)
      }
      // 余额不足不是普通错误：弹确认框引导充值，而不是只弹一条红条
      if (res.code === CODE_INSUFFICIENT_BALANCE) {
        notifyInsufficientBalance(res.message)
        return Promise.reject(new Error(res.message || '积分余额不足'))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络异常'

    // HTTP 层 401 与业务码 401 必须同等处理，否则令牌过期后页面会停留在旧状态
    if (status === 401) {
      return handle401(error.config)
    }

    // 部分网关会把业务码透传到 HTTP 层，这里同样兜住余额不足
    if (error.response?.data?.code === CODE_INSUFFICIENT_BALANCE) {
      notifyInsufficientBalance(message)
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error('没有访问权限')
      return Promise.reject(error)
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
