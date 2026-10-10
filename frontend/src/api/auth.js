/**
 * 令牌归属与登录态失效事件的统一约定。
 *
 * 三种身份各自持有令牌（管理员、网格员、公众监督员）。axios 拦截器只负责"取哪个令牌、
 * 放到哪个请求头上"，令牌本身的读写一律交给 Pinia store，避免出现内存状态与 localStorage 两份真相。
 */

export const AUTH_IDENTITY = {
  PUBLIC: 'public',
  GRID: 'grid',
  ADMIN: 'admin',
}

/** 登录态失效（后端返回 401）时广播的事件名 */
export const AUTH_EXPIRED_EVENT = 'auth:expired'

/** 各身份的登录页，用于失效后跳转 */
export const LOGIN_PATH = {
  [AUTH_IDENTITY.ADMIN]: '/login',
  [AUTH_IDENTITY.GRID]: '/grid/login',
  [AUTH_IDENTITY.PUBLIC]: '/public/login',
}

/** 请求路径未归属任何身份时的标记：不带令牌，由拦截器改用"当前有效身份" */
export const IDENTITY_UNKNOWN = 'UNKNOWN'

/**
 * 三种角色共用的字典接口。
 *
 * 必须在 {@link resolveIdentity} 之前判定：`/grid-cities` 也以 `/grid` 开头，
 * 若先走网格员规则，公众监督员就会因为拿不到网格员令牌而 401。
 */
const SHARED_DICTIONARY_PREFIXES = ['/grid-cities', '/aqi-levels']

export function isSharedDictionaryPath(url) {
  const path = url || ''
  return SHARED_DICTIONARY_PREFIXES.some((prefix) => path.startsWith(prefix))
}

/**
 * 按请求路径判断应携带哪种身份的令牌。
 *
 * 未登记的路径与共享字典接口一律归为 UNKNOWN（不带令牌），避免把 A 角色的令牌误发给 B 角色的接口。
 */
export function resolveIdentity(url) {
  const path = url || ''

  // 共享字典接口必须最先判定，否则会被下面的 /grid 规则截走
  if (isSharedDictionaryPath(path)) {
    return IDENTITY_UNKNOWN
  }

  if (path.startsWith('/grid')) {
    return AUTH_IDENTITY.GRID
  }

  if (path.startsWith('/feedback') || path.startsWith('/statistics')) {
    return AUTH_IDENTITY.ADMIN
  }

  if (path.startsWith('/public-feedback') || path.startsWith('/public-supervisors')) {
    return AUTH_IDENTITY.PUBLIC
  }

  if (path.startsWith('/auth/grid-member')) {
    return AUTH_IDENTITY.GRID
  }

  if (path.startsWith('/auth/admin')) {
    return AUTH_IDENTITY.ADMIN
  }

  if (path.startsWith('/auth/public-supervisor')) {
    return AUTH_IDENTITY.PUBLIC
  }

  return IDENTITY_UNKNOWN
}

export function isUnauthenticated(url) {
  return resolveIdentity(url) === IDENTITY_UNKNOWN
}
