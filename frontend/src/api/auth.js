import request from '@/utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

/** 管理后台登录：独立端点，避免与用户端登录态互相覆盖。 */
export function adminLogin(data) {
  return request.post('/auth/admin-login', data, {
    skipAuthRefresh: true,
    skipAuthRedirect: true
  })
}

export function sendEmailCode(data) {
  return request.post('/auth/email-code', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getProfile() {
  return request.get('/auth/profile')
}

export function logout(token) {
  return request.post('/auth/logout', null, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    skipAuthRefresh: true,
    skipAuthRedirect: true
  })
}
