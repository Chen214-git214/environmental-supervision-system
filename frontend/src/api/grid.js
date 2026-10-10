import request from './request'

export function gridMemberLogin(data) {
  return request.post('/auth/grid-member/login', data)
}

// 任务列表与详情不再传 gridMemberId：身份由后端从 Authorization 令牌解析
export function getGridTasks(params) {
  return request.get('/grid/tasks', { params })
}

export function getGridTaskDetail(afId, params) {
  return request.get(`/grid/tasks/${afId}`, { params })
}

export function submitGridMeasurement(afId, data) {
  return request.post(`/grid/tasks/${afId}/measurements`, data)
}
