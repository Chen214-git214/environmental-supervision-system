import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { publicSupervisorLogin, publicSupervisorRegister } from '../api/publicSupervisor'
import { resetAuthExpiredFlag } from '../api/request'

const TOKEN_KEY = 'publicSupervisorToken'
const INFO_KEY = 'publicSupervisorInfo'
const ADMIN_TOKEN_KEY = 'adminToken'
const ADMIN_INFO_KEY = 'adminInfo'
const GRID_TOKEN_KEY = 'gridToken'
const GRID_INFO_KEY = 'gridMemberInfo'

function readSupervisorInfo() {
  const value = localStorage.getItem(INFO_KEY)

  if (!value) {
    return null
  }

  try {
    return JSON.parse(value)
  } catch {
    localStorage.removeItem(INFO_KEY)
    return null
  }
}

/**
 * 后端返回的是 realName，界面统一用 displayName 展示，这里做一次映射。
 */
function normalizeSupervisorInfo(info) {
  if (!info) {
    return null
  }

  return {
    supervisorId: info.supervisorId,
    username: info.username,
    displayName: info.realName || info.username,
    realName: info.realName,
    phone: info.phone,
    status: info.status,
  }
}

function parseAuthResult(result) {
  const payload = result?.data ?? result ?? {}
  return {
    token: payload.token || '',
    info: normalizeSupervisorInfo(payload.supervisorInfo),
  }
}

export const usePublicAuthStore = defineStore('publicAuth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const supervisorInfo = ref(readSupervisorInfo())

  const isLogin = computed(() => Boolean(token.value && supervisorInfo.value?.supervisorId))

  function persist(tokenValue, info) {
    token.value = tokenValue
    supervisorInfo.value = info
    localStorage.setItem(TOKEN_KEY, tokenValue)
    localStorage.setItem(INFO_KEY, JSON.stringify(info))

    // 清除可能残留的管理员/网格员令牌，避免跨角色串用
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    localStorage.removeItem(ADMIN_INFO_KEY)
    localStorage.removeItem(GRID_TOKEN_KEY)
    localStorage.removeItem(GRID_INFO_KEY)
    resetAuthExpiredFlag()
  }

  async function login(form) {
    const result = await publicSupervisorLogin({
      username: form?.username?.trim(),
      password: form?.password,
    })
    const { token: issuedToken, info } = parseAuthResult(result)

    if (!issuedToken || !info?.supervisorId) {
      throw new Error('登录响应缺少令牌或监督员信息')
    }

    persist(issuedToken, info)
    return info
  }

  async function register(form) {
    const result = await publicSupervisorRegister({
      username: form?.username?.trim(),
      password: form?.password,
      realName: form?.realName?.trim(),
      phone: form?.phone?.trim(),
      idCard: form?.idCard?.trim() || null,
    })
    const { token: issuedToken, info } = parseAuthResult(result)

    if (!issuedToken || !info?.supervisorId) {
      throw new Error('注册响应缺少令牌或监督员信息')
    }

    persist(issuedToken, info)
    return info
  }

  function logout() {
    token.value = ''
    supervisorInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(INFO_KEY)
  }

  return {
    token,
    supervisorInfo,
    isLogin,
    login,
    register,
    logout,
  }
})
