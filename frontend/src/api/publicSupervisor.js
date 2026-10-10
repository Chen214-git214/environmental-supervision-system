import request from './request'

/** 注册接口公开（尚无账号）；成功即返回令牌 */
export function publicSupervisorRegister(data) {
  return request.post('/public-supervisors', data)
}

export function publicSupervisorLogin(data) {
  return request.post('/auth/public-supervisor/login', data)
}

export function getCurrentSupervisor() {
  return request.get('/public-supervisors/me')
}

/** 提交反馈：提交人身份由令牌决定，请求体不传 supervisorId */
export function createPublicFeedback(data) {
  return request.post('/public-feedback', data)
}

/** 查看自己提交的反馈及处理进展 */
export function getMyFeedback() {
  return request.get('/public-feedback/mine')
}
