/**
 * 单页最大记录数，需与后端分页上限保持一致。
 */
export const MAX_PAGE_SIZE = 100

/**
 * 逐页拉取，直到取完或达到页数上限。
 *
 * <p>管理端配置类列表的总量有限，但可能超过单页 100 条（例如批量生成 200 个兑换码）。
 * 在列表接口尚未返回 total 的情况下，用该函数保证超出单页的数据仍然可见、可操作。</p>
 *
 * <p>首屏返回非数组（接口不可用）时返回 null，调用方据此沿用原有的演示数据回退逻辑。</p>
 *
 * @param {(params: { page: number, size: number }) => Promise<any>} fetcher 单页请求函数
 * @param {{ maxPages?: number }} options 页数上限，默认 50 页（最多 5000 条）
 * @returns {Promise<{ list: any[], truncated: boolean } | null>}
 */
export async function fetchAllPages(fetcher, { maxPages = 50 } = {}) {
  const collected = []
  for (let page = 1; page <= maxPages; page++) {
    const records = await fetcher({ page, size: MAX_PAGE_SIZE })
    if (!Array.isArray(records)) {
      return page === 1 ? null : { list: collected, truncated: true }
    }
    if (records.length === 0) {
      return { list: collected, truncated: false }
    }
    collected.push(...records)
    if (records.length < MAX_PAGE_SIZE) {
      return { list: collected, truncated: false }
    }
  }
  console.warn(`[pagination] 已取满 ${maxPages} 页，列表可能被截断`)
  return { list: collected, truncated: true }
}
