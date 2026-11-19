// 管理后台使用隐藏路径，普通用户访问 /admin 会落到 404 页面。
// 可通过环境变量 VITE_ADMIN_PATH 自定义入口，需以 / 开头。
const raw = import.meta.env.VITE_ADMIN_PATH || '/lingxi-console-7c3f'

export const ADMIN_BASE = raw.replace(/\/+$/, '')
export const ADMIN_HOME_PATH = `${ADMIN_BASE}/dashboard`
export const ADMIN_LOGIN_PATH = `${ADMIN_BASE}/login`

export function adminPath(sub = '') {
  return sub ? `${ADMIN_BASE}/${sub}` : ADMIN_BASE
}
