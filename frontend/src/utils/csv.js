/**
 * 把二维数组导出为 CSV 文件。
 * 使用带 BOM 的 UTF-8 编码，Excel 直接打开不会出现中文乱码。
 */
// Excel/WPS 会把以 = + - @ 制表符开头的单元格当公式执行，导出前必须加前缀转义
const FORMULA_PREFIX = /^[=+\-@\t\r]/
// 正负数、千分位、小数字符串都不是公式，加前缀会让金额变成不可计算的文本
const NUMERIC_TEXT = /^[+-]?(?:\d{1,3}(?:,\d{3})+|\d+)(?:\.\d+)?$/

export function downloadCsv(filename, headers, rows) {
  const escape = value => {
    if (value == null) return ''
    let text = String(value)
    // 先去掉前导空白再判断，避免 " =1+1" 绕过；数字字符串保持原样
    const trimmed = text.trimStart()
    if (typeof value === 'string' && !NUMERIC_TEXT.test(trimmed) && FORMULA_PREFIX.test(trimmed)) {
      text = `'${text}`
    }
    return /[",\n\r]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text
  }

  const lines = [headers, ...rows].map(row => row.map(escape).join(','))
  const blob = new Blob([`\ufeff${lines.join('\r\n')}`], {
    type: 'text/csv;charset=utf-8;'
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  URL.revokeObjectURL(url)
}
