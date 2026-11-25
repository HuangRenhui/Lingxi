/**
 * 单页最大记录数，需与后端分页上限保持一致。
 */
export const MAX_PAGE_SIZE = 100

/** 兜底页数上限：接口不返回 total 时防止无限拉取。 */
const FALLBACK_MAX_PAGES = 50

function isPageResult(value) {
  return value && !Array.isArray(value) && Array.isArray(value.list)
}

/**
 * 拉取列表数据。
 *
 * <p>首页请求会带上 total（后端已改为返回 PageResult），据此精确计算需要拉取的页数，
 * 并发取完剩余页；不再盲目串行拉取 50 页，也不再依赖「拉满一页才继续」的猜测。</p>
 *
 * <p>兼容旧的数组返回（未接后端的 Mock 路由）：逐页拉取直到不足一页。</p>
 *
 * @param {(params: { page: number, size: number }) => Promise<any>} fetcher 单页请求函数
 * @param {{ maxPages?: number }} options 兜底页数上限
 * @returns {Promise<{ list: any[], total: number, truncated: boolean } | null>}
 */
export async function fetchAllPages(fetcher, { maxPages = FALLBACK_MAX_PAGES } = {}) {
  const first = await fetcher({ page: 1, size: MAX_PAGE_SIZE })
  if (first == null) return null

  // 新格式：{ list, total } —— 用 total 精确计算页数，并发拉取剩余页
  if (isPageResult(first)) {
    const total = Number(first.total || 0)
    const neededPages = Math.ceil(total / MAX_PAGE_SIZE)
    const pagesToFetch = Math.min(neededPages, maxPages)
    const tasks = []
    for (let p = 2; p <= pagesToFetch; p++) {
      tasks.push(fetcher({ page: p, size: MAX_PAGE_SIZE }))
    }
    const rest = await Promise.all(tasks)
    const collected = [...first.list]
    for (const res of rest) {
      if (isPageResult(res)) collected.push(...res.list)
    }
    const truncated = pagesToFetch < neededPages
    if (truncated) {
      console.warn(`[pagination] total=${total}，仅拉取前 ${pagesToFetch} 页（上限 ${maxPages}），列表可能被截断`)
    }
    return { list: collected, total, truncated }
  }

  // 旧格式：数组返回，逐页拉取直到不足一页或达到上限
  if (!Array.isArray(first)) return null
  const collected = [...first]
  if (first.length < MAX_PAGE_SIZE) {
    return { list: collected, total: collected.length, truncated: false }
  }
  const tasks = []
  for (let p = 2; p <= maxPages; p++) {
    tasks.push(fetcher({ page: p, size: MAX_PAGE_SIZE }))
  }
  const rest = await Promise.all(tasks)
  let truncated = false
  for (const records of rest) {
    if (!Array.isArray(records)) {
      truncated = true
      break
    }
    collected.push(...records)
    if (records.length < MAX_PAGE_SIZE) break
  }
  return { list: collected, total: collected.length, truncated }
}
