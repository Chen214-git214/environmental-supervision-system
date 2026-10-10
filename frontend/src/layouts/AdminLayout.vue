<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => {
  return route.path.startsWith('/statistics') ? '/statistics' : '/feedback'
})

const isDashboardRoute = computed(() => route.path.startsWith('/statistics'))

const adminName = computed(() => {
  return authStore.adminInfo?.name || authStore.adminInfo?.adminCode || '管理员'
})

function handleLogout() {
  authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container :class="['admin-shell', { 'admin-shell--dashboard': isDashboardRoute }]">
    <el-aside v-if="!isDashboardRoute" class="admin-aside" width="220px">
      <div class="brand">NEPM 管理端</div>
      <el-menu router :default-active="activeMenu" class="admin-menu">
        <el-menu-item index="/feedback">反馈列表</el-menu-item>
        <el-menu-item index="/statistics">环境监测大屏</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header v-if="!isDashboardRoute" class="admin-header">
        <span>{{ adminName }}</span>
        <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main :class="['admin-main', { 'admin-main--dashboard': isDashboardRoute }]">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>
