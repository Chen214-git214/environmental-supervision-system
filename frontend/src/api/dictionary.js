import request from './request'

/**
 * 字典数据（城市、AQI 级别）统一从后端拉取，前端不再维护第二份列表。
 * 这两个接口三角色共用，令牌由 request 拦截器按当前有效身份自动附带。
 */

/** 系统网格覆盖城市，可按省筛选 */
export function getCityOptions(provinceId) {
  return request.get('/grid-cities', {
    params: provinceId ? { provinceId } : undefined,
  })
}

/** AQI 级别字典（含指数区间、健康影响与防护建议） */
export function getAqiLevelOptions() {
  return request.get('/aqi-levels')
}
