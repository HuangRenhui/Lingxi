import request from '@/utils/request'

/**
 * 管理端请求封装：统一注入 admin 标记。
 *
 * <p>请求层据此选择管理端令牌、401 后跳管理端登录页，不再依赖 URL 前缀判断。
 * 管理端专用的 api 模块应统一从本模块导入 request。</p>
 */
function withAdmin(config = {}) {
  return { ...config, admin: true }
}

const adminRequest = {
  get(url, config) {
    return request.get(url, withAdmin(config))
  },
  post(url, data, config) {
    return request.post(url, data, withAdmin(config))
  },
  put(url, data, config) {
    return request.put(url, data, withAdmin(config))
  },
  patch(url, data, config) {
    return request.patch(url, data, withAdmin(config))
  },
  delete(url, config) {
    return request.delete(url, withAdmin(config))
  }
}

export default adminRequest
