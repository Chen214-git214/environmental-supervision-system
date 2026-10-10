<script setup>
import { onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { AUTH_EXPIRED_EVENT, AUTH_IDENTITY, LOGIN_PATH } from './api/auth'

const router = useRouter()

/**
 * 后端返回 401 时，request.js 已清理登录态并广播事件，这里统一收口做路由跳转：
 * 走 router 而不是整页刷新，既保留 redirect 期望路径，也不丢前端状态。
 */
function handleAuthExpired(event) {
  const identity = event.detail?.identity || AUTH_IDENTITY.ADMIN
  const loginPath = LOGIN_PATH[identity] || LOGIN_PATH[AUTH_IDENTITY.ADMIN]

  if (router.currentRoute.value.path === loginPath) {
    return
  }

  router.replace(loginPath)
}

onMounted(() => {
  window.addEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
})

onBeforeUnmount(() => {
  window.removeEventListener(AUTH_EXPIRED_EVENT, handleAuthExpired)
})
</script>

<template>
  <RouterView />
</template>
