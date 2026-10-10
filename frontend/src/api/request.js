import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useGridAuthStore } from '../stores/gridAuth'
import { usePublicAuthStore } from '../stores/publicAuth'
import {
  AUTH_EXPIRED_EVENT,
  AUTH_IDENTITY,
  IDENTITY_UNKNOWN,
  isSharedDictionaryPath,
  resolveIdentity,
} from './auth'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
})

// 同时到达多个 401 时只提示、只跳转一次，避免重复弹窗与连续跳转
let handlingExpired = false

function readIdentity() {
  return {
    [AUTH_IDENTITY.ADMIN]: useAuthStore(),
    [AUTH_IDENTITY.GRID]: useGridAuthStore(),
    [AUTH_IDENTITY.PUBLIC]: usePublicAuthStore(),
  }
}

/** 当前有效身份：三种身份同时登录的可能性已被登录逻辑排除，因此任取其一即可 */
function activeIdentity() {
  const stores = readIdentity()

  if (stores[AUTH_IDENTITY.ADMIN]?.token) {
    return AUTH_IDENTITY.ADMIN
  }

  if (stores[AUTH_IDENTITY.GRID]?.token) {
    return AUTH_IDENTITY.GRID
  }

  if (stores[AUTH_IDENTITY.PUBLIC]?.token) {
    return AUTH_IDENTITY.PUBLIC
  }

  return AUTH_IDENTITY.ADMIN
}

/**
 * 令牌只从 store 读取：store 初始化时已从 localStorage 恢复，是唯一真相来源。
 * 调用方可通过 config.authIdentity 显式声明身份，否则按请求路径推断；
 * 未登记的路径不带令牌；角色共用的字典接口改用当前有效身份，否则公众监督员会因缺少网格员令牌而 401。
 */
function resolveToken(config) {
  const identity = config.authIdentity || resolveIdentity(config.url)

  if (identity === IDENTITY_UNKNOWN) {
    if (isSharedDictionaryPath(config.url)) {
      const active = activeIdentity()
      return { identity: active, token: readIdentity()[active]?.token || '' }
    }

    return { identity, token: '' }
  }

  return { identity, token: readIdentity()[identity]?.token || '' }
}

/** 是否存在任何已登录身份 */
function hasActiveSession() {
  const stores = readIdentity()

  return Object.values(stores).some((store) => Boolean(store?.token))
}

/**
 * 登录态失效：清理对应身份的内存状态与本地缓存，并广播事件交给界面层跳转。
 *
 * 仅在"原本有登录态"时才提示与跳转：未登录时发出的请求拿到 401 属于正常情况
 * （例如直接打开登录页），此时若也广播跳转，会把用户从当前页面莫名踢到另一个角色的登录页。
 *
 * 这里不直接操作 router，是因为与 request.js 同级导入 router 会形成
 * request → router → store → api → request 的循环依赖，历史上正是它导致令牌被改为
 * 直接读写 localStorage、进而出现"内存与缓存两份真相"。跳转由 App.vue 收口。
 */
function handleUnauthorized(identity) {
  if (handlingExpired || !hasActiveSession()) {
    return
  }

  handlingExpired = true

  readIdentity()[identity]?.logout()
  ElMessage.error('登录状态已失效，请重新登录')
  window.dispatchEvent(new CustomEvent(AUTH_EXPIRED_EVENT, { detail: { identity } }))
}

/** 登录成功后重置标记，使下一次失效仍能正常提示与跳转 */
export function resetAuthExpiredFlag() {
  handlingExpired = false
}

request.interceptors.request.use((config) => {
  const { identity, token } = resolveToken(config)

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  config.authIdentity = identity

  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    const message =
      error.response?.data?.message ||
      error.response?.data?.msg ||
      error.message ||
      '请求失败'

    if (status === 401 && !error.config?.skipAuthRedirect) {
      // 未登记路径没有归属身份，回退到当前有效身份，保证跳回正确的登录页
      const identity =
        error.config?.authIdentity && error.config.authIdentity !== IDENTITY_UNKNOWN
          ? error.config.authIdentity
          : activeIdentity()

      handleUnauthorized(identity)
      return Promise.reject(error)
    }

    if (status !== 401) {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  },
)

export default request
