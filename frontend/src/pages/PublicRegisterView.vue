<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { usePublicAuthStore } from '../stores/publicAuth'
const router = useRouter()
const publicAuthStore = usePublicAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  idCard: '',
  password: '',
  confirmPassword: '',
})

const rules = {
  username: [
    { required: true, message: '请输入登录账号', trigger: 'blur' },
    { min: 4, max: 50, message: '登录账号长度需在 4-50 之间', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  idCard: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback()
          return
        }

        if (!/^(\d{17}[\dXx]|\d{15})$/.test(String(value).trim())) {
          callback(new Error('身份证号格式不正确'))
          return
        }

        callback()
      },
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6-32 之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }

        callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true

  try {
    await publicAuthStore.register(form)
    ElMessage.success('注册成功，已自动登录')
    router.replace('/public/feedback/submit')
  } catch (error) {
    // 具体错误信息已由 axios 拦截器统一提示（如账号/手机号已被注册）
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
        <h1>公众监督员注册</h1>
        <span>填写基础信息后即可创建监督员账号，注册成功后自动进入公众反馈提交流程。</span>
      </aside>

      <section class="login-panel">
        <div class="login-panel-header">
          <h2>创建公众监督员账号</h2>
          <p>账号与手机号均不可重复，密码将加密存储，注册成功后直接进入反馈流程。</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          @submit.prevent="handleRegister"
        >
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" placeholder="请输入登录账号（4-50 位）" clearable />
          </el-form-item>

          <el-form-item label="姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入姓名" clearable />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入 11 位手机号" clearable />
          </el-form-item>

          <el-form-item label="身份证号（选填）" prop="idCard">
            <el-input v-model="form.idCard" placeholder="选填，用于实名核验" clearable />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码（6-32 位）"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入密码"
            />
          </el-form-item>

          <div class="login-extra-actions">
            <el-button @click="router.push('/public/login')">返回登录</el-button>
            <el-button
              class="login-button login-button--inline"
              type="primary"
              native-type="submit"
              :loading="loading"
            >
              注册并进入
            </el-button>
          </div>
        </el-form>
      </section>
    </section>
  </main>
</template>
