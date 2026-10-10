import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { gridMemberLogin } from '../api/grid'
import { resetAuthExpiredFlag } from '../api/request'

const TOKEN_KEY = 'gridToken'
const INFO_KEY = 'gridMemberInfo'
const ADMIN_TOKEN_KEY = 'adminToken'
const ADMIN_INFO_KEY = 'adminInfo'
const PUBLIC_TOKEN_KEY = 'publicSupervisorToken'
const PUBLIC_INFO_KEY = 'publicSupervisorInfo'
const PUBLIC_ACCOUNT_KEY = 'publicSupervisorAccounts'

function readGridMemberInfo() {
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

function parseLoginResult(result) {
  const payload = result?.data ?? result ?? {}
  const token = payload.token || payload.gridToken || payload.accessToken
  const gridMemberInfo = payload.gridMemberInfo || payload.gridMember || payload.user || null

  return { token, gridMemberInfo }
}

export const useGridAuthStore = defineStore('gridAuth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const gridMemberInfo = ref(readGridMemberInfo())

  const isLogin = computed(() => Boolean(token.value && gridMemberInfo.value?.gridMemberId))

  async function login(form) {
    const result = await gridMemberLogin(form)
    const loginResult = parseLoginResult(result)

    if (!loginResult.token || !loginResult.gridMemberInfo?.gridMemberId) {
      throw new Error('登录响应缺少网格员信息')
    }

    token.value = loginResult.token
    gridMemberInfo.value = loginResult.gridMemberInfo
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(INFO_KEY, JSON.stringify(gridMemberInfo.value))

    // 清除可能残留的其他身份令牌，避免跨角色串用
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    localStorage.removeItem(ADMIN_INFO_KEY)
    localStorage.removeItem(PUBLIC_TOKEN_KEY)
    localStorage.removeItem(PUBLIC_INFO_KEY)
    localStorage.removeItem(PUBLIC_ACCOUNT_KEY)
    resetAuthExpiredFlag()

    return loginResult
  }

  function logout() {
    token.value = ''
    gridMemberInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(INFO_KEY)
  }

  return {
    token,
    gridMemberInfo,
    isLogin,
    login,
    logout,
  }
})
