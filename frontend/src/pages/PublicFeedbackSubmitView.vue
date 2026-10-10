<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getAqiLevelOptions, getCityOptions } from '../api/dictionary'
import { createPublicFeedback, getMyFeedback } from '../api/publicSupervisor'
import { usePublicAuthStore } from '../stores/publicAuth'

const router = useRouter()
const publicAuthStore = usePublicAuthStore()

// 城市与 AQI 级别均来自后端字典接口，前端不再维护副本
const cityOptions = ref([])
const levelOptions = ref([])

const pollutionOptions = ['扬尘', '噪声', '水污染', '空气污染', '工业废气', '交通污染']

const formRef = ref()
const loading = ref(false)
const submitResult = ref(null)
const myFeedback = ref([])
const myLoading = ref(false)

const form = reactive({
  cityId: null,
  levelId: null,
  pollutionType: '扬尘',
  address: '沈阳市和平区南京北街100号',
  longitude: 123.414662,
  latitude: 41.799921,
  description: '施工现场未覆盖防尘网，扬尘明显，影响周边通行与空气质量。',
})

async function fetchDictionaries() {
  // 先取级别，再取城市：表单在 onMounted 时就已绑定，若先设城市会因 cityOptions 尚未填充而匹配不到，
  // 导致 el-select 显示不出已选中的城市名。
  const [levelPayload, cityPayload] = await Promise.all([
    getAqiLevelOptions(),
    getCityOptions(),
  ])

  const levels = unwrapPayload(levelPayload)
  const cities = unwrapPayload(cityPayload)

  levelOptions.value = (Array.isArray(levels) ? levels : []).map((item) => ({
    label: item.levelName,
    value: item.levelId,
  }))

  cityOptions.value = (Array.isArray(cities) ? cities : []).map((item) => ({
    label: item.cityName,
    value: item.cityId,
  }))

  if (!form.levelId && levelOptions.value.length) {
    form.levelId = levelOptions.value[Math.min(1, levelOptions.value.length - 1)].value
  }

  if (!form.cityId && cityOptions.value.length) {
    form.cityId = cityOptions.value[0].value
  }
}

const rules = {
  cityId: [{ required: true, message: '请选择反馈城市', trigger: 'change' }],
  pollutionType: [{ required: true, message: '请选择污染类型', trigger: 'change' }],
  address: [{ required: true, message: '请输入事发地址', trigger: 'blur' }],
  longitude: [{ required: true, message: '请输入经度', trigger: 'blur' }],
  latitude: [{ required: true, message: '请输入纬度', trigger: 'blur' }],
  description: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }],
}

const selectedCityName = computed(() => {
  return cityOptions.value.find((item) => item.value === form.cityId)?.label || '-'
})

const supervisorLabel = computed(() => {
  const info = publicAuthStore.supervisorInfo
  return info?.displayName || info?.username || `监督员${info?.supervisorId ?? ''}`
})

const supervisorAccount = computed(() => publicAuthStore.supervisorInfo?.username || '-')

function unwrapPayload(result) {
  return result?.data ?? result ?? {}
}

function resetForm() {
  formRef.value?.resetFields()
  submitResult.value = null
}

function handleLogout() {
  publicAuthStore.logout()
  router.replace('/public/login')
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

function statusTagType(status) {
  if (Number(status) === 2) {
    return 'success'
  }

  if (Number(status) === 3) {
    return 'danger'
  }

  return 'warning'
}

async function fetchMyFeedback() {
  myLoading.value = true

  try {
    const payload = unwrapPayload(await getMyFeedback())
    myFeedback.value = Array.isArray(payload) ? payload : []
  } finally {
    myLoading.value = false
  }
}

onMounted(() => {
  // 本页受路由守卫保护，进入时必定已登录，直接拉取字典与自身反馈
  fetchDictionaries()
  fetchMyFeedback()
})

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true

  try {
    submitResult.value = unwrapPayload(
      await createPublicFeedback({
        cityId: Number(form.cityId),
        levelId: form.levelId ? Number(form.levelId) : null,
        pollutionType: form.pollutionType,
        address: form.address,
        longitude: Number(form.longitude),
        latitude: Number(form.latitude),
        description: form.description,
      }),
    )
    ElMessage.success('反馈提交成功')
    await fetchMyFeedback()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="public-page">
    <section class="public-hero">
      <p>公众监督反馈处理流程</p>
      <h1>公众监督员反馈提交</h1>
      <span>用于演示公众监督员提交污染反馈信息，并触发后续管理员指派与网格员处理流程。</span>
    </section>

    <section class="public-card">
      <div class="public-card-title">
        <div>
          <h2>反馈提交页面</h2>
          <span>{{ supervisorLabel }} | 当前城市：{{ selectedCityName }}</span>
        </div>
        <el-button plain @click="handleLogout">退出登录</el-button>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="public-form"
        @submit.prevent="handleSubmit"
      >
        <div class="public-form-grid">
          <el-form-item label="监督员账号">
            <el-input :model-value="supervisorAccount" disabled />
          </el-form-item>
          <el-form-item label="反馈城市" prop="cityId">
            <el-select v-model="form.cityId" class="full-width" data-testid="city-select">
              <el-option
                v-for="item in cityOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="预计等级" prop="levelId">
            <el-select v-model="form.levelId" class="full-width" clearable>
              <el-option
                v-for="item in levelOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="污染类型" prop="pollutionType">
            <el-select v-model="form.pollutionType" class="full-width">
              <el-option v-for="item in pollutionOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item label="经度" prop="longitude">
            <el-input-number
              v-model="form.longitude"
              class="full-width"
              :precision="6"
              :step="0.000001"
            />
          </el-form-item>
          <el-form-item label="纬度" prop="latitude">
            <el-input-number
              v-model="form.latitude"
              class="full-width"
              :precision="6"
              :step="0.000001"
            />
          </el-form-item>
        </div>

        <el-form-item label="事发地址" prop="address">
          <el-input v-model="form.address" />
        </el-form-item>

        <el-form-item label="反馈内容" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>

        <div class="public-actions">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" native-type="submit" :loading="loading">提交反馈</el-button>
        </div>
      </el-form>

      <el-alert
        v-if="submitResult"
        class="public-result"
        type="success"
        show-icon
        :closable="false"
        title="反馈已提交到后端"
      >
        <template #default>
          <div>反馈编号：{{ submitResult.feedbackId }}</div>
          <div>处理状态：{{ submitResult.statusText || submitResult.status }}</div>
          <div>提交时间：{{ submitResult.submittedAt }}</div>
        </template>
      </el-alert>
    </section>

    <section class="public-card">
      <div class="public-card-title">
        <div>
          <h2>我的反馈</h2>
          <span>共 {{ myFeedback.length }} 条，含管理员处理回复</span>
        </div>
        <el-button plain :loading="myLoading" @click="fetchMyFeedback">刷新</el-button>
      </div>

      <el-empty v-if="!myLoading && myFeedback.length === 0" description="暂无提交记录" />

      <div v-else v-loading="myLoading" class="my-feedback-list">
        <article v-for="item in myFeedback" :key="item.feedbackId" class="my-feedback-item">
          <header>
            <strong>#{{ item.feedbackId }} {{ item.pollutionType }}</strong>
            <el-tag :type="statusTagType(item.status)" size="small">
              {{ item.statusText }}
            </el-tag>
          </header>
          <p class="my-feedback-address">{{ item.address }}</p>
          <dl>
            <div>
              <dt>所属城市</dt>
              <dd>{{ item.cityName || '-' }}</dd>
            </div>
            <div>
              <dt>提交时间</dt>
              <dd>{{ formatDate(item.submittedAt) }}</dd>
            </div>
            <div>
              <dt>实测 AQI</dt>
              <dd>
                {{ item.aqiLevel || '未检测' }}
                <template v-if="item.aqiValue">（{{ item.aqiValue }}）</template>
              </dd>
            </div>
            <div>
              <dt>处理时间</dt>
              <dd>{{ formatDate(item.handledAt) }}</dd>
            </div>
          </dl>
          <div v-if="item.replyContent" class="my-feedback-reply">
            <span>管理员回复</span>
            <p>{{ item.replyContent }}</p>
          </div>
          <div v-else class="my-feedback-reply my-feedback-reply--pending">
            <span>暂无处理回复</span>
          </div>
        </article>
      </div>
    </section>
  </main>
</template>
