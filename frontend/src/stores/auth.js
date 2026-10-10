import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { adminLogin } from '../api/admin'
import { useGridAuthStore } from './gridAuth'
import { usePublicAuthStore } from './publicAuth'
import { resetAuthExpiredFlag } from '../api/request'

const TOKEN_KEY = 'adminToken'
const ADMIN_INFO_KEY = 'adminInfo'

function readAdminInfo() {
  const value = localStorage.getItem(ADMIN_INFO_KEY)

  if (!value) {
    return null
  }

  try {
    return JSON.parse(value)
  } catch {
    localStorage.removeItem(ADMIN_INFO_KEY)
    return null
  }
}

function parseLoginResult(result) {
  const payload = result?.data ?? result ?? {}
  const token = payload.token || payload.adminToken || payload.accessToken
  const adminInfo = payload.adminInfo || payload.admin || payload.user || {
    adminCode: payload.adminCode,
    name: payload.name,
  }

  return { token, adminInfo }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const adminInfo = ref(readAdminInfo())

  const isLogin = computed(() => Boolean(token.value))

  async function login(form) {
    const result = await adminLogin(form)
    const loginResult = parseLoginResult(result)

    if (!loginResult.token) {
      throw new Error('登录响应缺少 token')
    }

    token.value = loginResult.token
    adminInfo.value = loginResult.adminInfo || null
    localStorage.setItem(TOKEN_KEY, token.value)
    localStorage.setItem(ADMIN_INFO_KEY, JSON.stringify(adminInfo.value))

    // 同一浏览器只保留一种身份，避免残留的网格员/监督员令牌被误发到管理端接口
    useGridAuthStore().logout()
    usePublicAuthStore().logout()
    resetAuthExpiredFlag()

    return loginResult
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(ADMIN_INFO_KEY)
  }

  return {
    token,
    adminInfo,
    isLogin,
    login,
    logout,
  }
})
