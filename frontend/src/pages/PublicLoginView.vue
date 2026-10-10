<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { usePublicAuthStore } from '../stores/publicAuth'

const route = useRoute()
const router = useRouter()
const publicAuthStore = usePublicAuthStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
})

function getRedirectPath() {
  return typeof route.query.redirect === 'string' && route.query.redirect
    ? route.query.redirect
    : '/public/feedback/submit'
}

function goToRegister() {
  router.push('/public/register')
}

async function handleLogin() {
  if (!form.username.trim()) {
    showToast('请输入登录账号')
    return
  }

  if (!form.password) {
    showToast('请输入密码')
    return
  }

  loading.value = true

  try {
    await publicAuthStore.login(form)
    showToast('登录成功')
    router.replace(getRedirectPath())
  } catch (error) {
    showToast(error.response?.data?.message || error?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="mobile-auth-page mobile-auth-page--public">
    <section class="mobile-auth-frame">
      <div class="mobile-auth-status-bar">
        <span>公众监督员</span>
        <span>NEPM</span>
      </div>

      <section class="mobile-auth-hero">
        <p>PUBLIC APP</p>
        <h1>监督员登录</h1>
        <span>进入移动端反馈入口，提交污染问题并跟踪处理状态。</span>
      </section>

      <van-form class="mobile-auth-form" @submit="handleLogin">
        <van-cell-group inset class="mobile-auth-group">
          <van-field
            v-model="form.username"
            name="username"
            label="账号"
            placeholder="请输入登录账号"
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
          <button type="button" class="mobile-auth-link" @click="goToRegister">立即注册</button>
          <button type="button" class="mobile-auth-link" @click="router.push('/login')">
            管理员入口
          </button>
        </div>
      </van-form>
    </section>
  </main>
</template>
