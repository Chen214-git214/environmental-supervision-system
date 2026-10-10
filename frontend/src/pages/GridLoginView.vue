<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useGridAuthStore } from '../stores/gridAuth'

const route = useRoute()
const router = useRouter()
const gridAuthStore = useGridAuthStore()
const loading = ref(false)

const form = reactive({
  employeeNo: 'SY-001',
  password: '123',
})

function getRedirectPath() {
  return typeof route.query.redirect === 'string' && route.query.redirect
    ? route.query.redirect
    : '/grid/tasks'
}

async function handleLogin() {
  if (!form.employeeNo.trim()) {
    showToast('请输入网格员账号')
    return
  }

  if (!form.password) {
    showToast('请输入密码')
    return
  }

  loading.value = true

  try {
    await gridAuthStore.login(form)
    showToast('登录成功')
    router.replace(getRedirectPath())
  } catch (error) {
    showToast(error.response?.data?.message || error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="mobile-auth-page mobile-auth-page--grid">
    <section class="mobile-auth-frame">
      <div class="mobile-auth-status-bar">
        <span>网格员终端</span>
        <span>NEPM</span>
      </div>

      <section class="mobile-auth-hero">
        <p>GRID APP</p>
        <h1>任务登录</h1>
        <span>以手机竖屏工作台形式进入待检测任务，适配现场处理场景。</span>
      </section>

      <van-form class="mobile-auth-form" @submit="handleLogin">
        <van-cell-group inset class="mobile-auth-group">
          <van-field
            v-model="form.employeeNo"
            name="employeeNo"
            label="账号"
            placeholder="请输入网格员账号"
            clearable
          />
          <van-field
            v-model="form.password"
            name="password"
            label="密码"
            type="password"
            placeholder="请输入密码"
            clearable
          />
        </van-cell-group>

        <div class="mobile-auth-actions">
          <van-button block round type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>

        <div class="mobile-auth-footer">
          <button type="button" class="mobile-auth-link" @click="router.push('/public/login')">
            公众监督员入口
          </button>
          <button type="button" class="mobile-auth-link" @click="router.push('/login')">
            管理员入口
          </button>
        </div>
      </van-form>
    </section>
  </main>
</template>
