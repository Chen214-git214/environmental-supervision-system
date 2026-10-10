import request from './request'

export function adminLogin(data) {
  return request.post('/auth/admin/login', data)
}

export function getFeedbackPage(params) {
  return request.get('/feedback', { params })
}

export function getFeedbackDetail(afId) {
  return request.get(`/feedback/${afId}`)
}

export function getLocalAssignmentCandidates(afId) {
  return request.get(`/feedback/${afId}/assignment-candidates/local`)
}

export function getCrossProvinceAssignmentCandidates(afId) {
  return request.get(`/feedback/${afId}/assignment-candidates/cross-province`)
}

export function assignFeedback(afId, data) {
  return request.put(`/feedback/${afId}/assign`, data)
}

/** 处理反馈：status 2 办结、3 驳回，reply 为处理回复（处理人由后端从令牌取得） */
export function handleFeedback(afId, data) {
  return request.put(`/feedback/${afId}/handle`, data)
}
