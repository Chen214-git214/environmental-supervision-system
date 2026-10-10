import request from './request'

export function getDashboardData() {
  return request.get('/statistics/dashboard')
}
