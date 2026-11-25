/**
 * 创作工具与对话应用注册表（门面）。
 *
 * <p>数据按用途拆到 ./tools/ 子模块，本文件统一导出，调用方 import 路径不变。</p>
 */
import { chatApps } from './tools/chatApps'
import { tools, toolCategories, showcases } from './tools/catalog'

export { chatApps, tools, toolCategories, showcases }

/**
 * 读取应用可选模型。
 * 对话应用直接使用 models 对象数组，工具应用从字段里的模型下拉推导。
 */
export function modelOptionsOf(item) {
  if (!item) return []
  if (Array.isArray(item.models)) {
    return item.models.map(entry => (typeof entry === 'string' ? { name: entry, description: '' } : entry))
  }
  const field = (item.fields || []).find(entry => entry.key === 'model' || entry.key === 'version')
  const options = field && Array.isArray(field.options) ? field.options : []
  return options.map(option => (typeof option === 'string' ? { name: option, description: '' } : option))
}

export function getTool(key) {
  return tools.find(item => item.key === key) || tools[0]
}

export function getChatApp(key) {
  return chatApps.find(item => item.key === key) || chatApps[0]
}

export function getToolsByKeys(keys = []) {
  return keys.map(key => tools.find(item => item.key === key)).filter(Boolean)
}
