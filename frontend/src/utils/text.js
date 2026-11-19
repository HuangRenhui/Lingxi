/**
 * 文本处理的安全封装。
 *
 * 演示数据与接口返回都可能缺字段（null / undefined / 数字 / 嵌套对象），
 * 直接调用 `value.toLowerCase()` 会抛 TypeError 并让整个列表白屏，
 * 因此列表筛选与状态样式统一走这里的工具函数。
 */

/** 转成可安全参与字符串运算的文本；对象与数组按空串处理，避免渲染出 [object Object]。 */
export function textOf(value) {
  if (value == null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)
  return ''
}

/** 状态/枚举转 class 名（空值返回空串）。 */
export function classOf(value) {
  return textOf(value).toLowerCase()
}

/** 关键词匹配：任一字段包含关键字即命中；关键字为空时视为全部命中。 */
export function matchKeyword(keyword, ...fields) {
  const key = textOf(keyword).trim().toLowerCase()
  if (!key) return true
  return fields.some(field => textOf(field).toLowerCase().includes(key))
}
