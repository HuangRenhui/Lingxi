import { loadModelCatalog } from '@/mock/model-catalog'

/**
 * 用户端读取后台「模型管理」的模型目录。
 *
 * 规则：
 * - 只返回已上架的模型，按排序值升序
 * - 目录中缺少该应用时回退到 config/tools.js 的静态配置，避免页面空掉
 * - 价格按「单价 × 倍率」折算，供创作页定价弹窗展示
 */

export const MODEL_FIELD_KEYS = ['model', 'version']

export function catalogModels(appKey) {
  if (!appKey) return []
  return loadModelCatalog()
    .filter(item => item.enabled && item.appKey === appKey)
    .sort((a, b) => a.sort - b.sort)
}

export function resolveModelOptions(appKey, fallback = []) {
  const list = catalogModels(appKey)
  if (!list.length) return fallback
  return list.map(item => ({
    name: item.name,
    description: item.description,
    badge: item.recommended ? '推荐' : ''
  }))
}

export function optionNames(appKey, fallbackOptions = []) {
  const list = catalogModels(appKey)
  if (!list.length) return fallbackOptions
  return list.map(item => item.name)
}

export function actualPrice(item) {
  return Number((Number(item.price || 0) * Number(item.multiplier || 1)).toFixed(4))
}

export function unitPrice(appKey, modelName, fallbackPrice = 0) {
  const target = catalogModels(appKey).find(item => item.name === modelName)
  if (!target) return Number(fallbackPrice) || 0
  return actualPrice(target)
}

export function formatPrice(value) {
  const num = Number(value)
  if (!num) return '0.00'
  return num < 1 ? num.toFixed(2) : num.toFixed(2)
}
