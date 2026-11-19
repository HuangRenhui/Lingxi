/**
 * 本地存储统一封装。
 *
 * 隐私模式、存储配额耗尽或浏览器禁用存储时，裸调用 localStorage 会抛异常并中断整个交互流程。
 * 这里统一返回布尔结果，由调用方决定是否提示，页面不会因为写失败而白屏。
 */

export function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return fallback
    const value = JSON.parse(raw)
    return value ?? fallback
  } catch (error) {
    return fallback
  }
}

export function writeJson(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (error) {
    return false
  }
}

export function readText(key, fallback = '') {
  try {
    return localStorage.getItem(key) ?? fallback
  } catch (error) {
    return fallback
  }
}

export function writeText(key, value) {
  try {
    localStorage.setItem(key, String(value))
    return true
  } catch (error) {
    return false
  }
}

export function removeItem(key) {
  try {
    localStorage.removeItem(key)
    return true
  } catch (error) {
    return false
  }
}
