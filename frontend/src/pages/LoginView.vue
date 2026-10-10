<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  adminCode: 'admin',
  password: '123',
})

const rules = {
  adminCode: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function getRedirectPath() {
  return typeof route.query.redirect === 'string' && route.query.redirect
    ? route.query.redirect
    : '/feedback'
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true

  try {
    await authStore.login({
      adminCode: form.adminCode,
      password: form.password,
    })
    ElMessage.success('登录成功')
    router.replace(getRedirectPath())
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-shell">
      <aside class="login-brand">
        <p>NEPM 环保公众监督系统</p>
        <h1>管理员登录入口</h1>
        <span>登录后进入反馈列表与环境监测大屏，处理公众监督反馈、分派网格员并查看统计信息。</span>
      </aside>

      <section class="login-panel">
        <div class="login-panel-header">
          <h2>NEPM 管理端</h2>
          <p>保留原有桌面端登录样式与视觉风格，作为系统管理员独立入口。</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item label="管理员账号" prop="adminCode">
            <el-input v-model="form.adminCode" placeholder="请输入管理员账号" clearable />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              placeholder="请输入密码"
              show-password
              type="password"
            />
          </el-form-item>

          <el-button class="login-button" type="primary" native-type="submit" :loading="loading">
            登录并进入
          </el-button>

          <div class="login-panel-links">
            <button type="button" class="login-inline-link" @click="router.push('/public/login')">
              公众监督员登录
            </button>
            <button type="button" class="login-inline-link" @click="router.push('/grid/login')">
              网格员登录
            </button>
          </div>
        </el-form>
      </section>
    </section>
  </main>
</template>
