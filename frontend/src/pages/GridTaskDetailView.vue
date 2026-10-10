<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getGridTaskDetail, submitGridMeasurement } from '../api/grid'
import { useGridAuthStore } from '../stores/gridAuth'

const LEVELS = ['优', '良', '轻度污染', '中度污染', '重度污染', '严重污染']

const route = useRoute()
const router = useRouter()
const gridAuthStore = useGridAuthStore()
const loading = ref(false)
const submitting = ref(false)
const task = ref({})

const form = reactive({
  so2Value: '',
  coValue: '',
  pm25Value: '',
})

const afId = computed(() => route.params.afId)
// 实测提交后状态仍是"处理中"（需管理员办结），因此以 measuredAt 判断是否已录入
const isMeasured = computed(() => Boolean(task.value.measuredAt))
// 已提交时展示服务端算出的等级，未提交时展示按同一阈值本地预估的结果
const displayLevels = computed(() => {
  if (isMeasured.value) {
    return {
      so2: task.value.so2Level || '-',
      co: task.value.coLevel || '-',
      pm25: task.value.pm25Level || '-',
      aqi: task.value.aqiLevel || '-',
      aqiValue: task.value.aqiValue,
    }
  }

  return {
    so2: so2Level.value,
    co: coLevel.value,
    pm25: pm25Level.value,
    aqi: aqiLevel.value,
    aqiValue: null,
  }
})

function unwrapPayload(result) {
  return result?.data ?? result ?? {}
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

function levelByThreshold(value, thresholds) {
  const numericValue = Number(value)

  if (!Number.isFinite(numericValue)) {
    return '-'
  }

  const index = thresholds.findIndex((threshold) => numericValue <= threshold)
  return LEVELS[index === -1 ? LEVELS.length - 1 : index]
}

function levelIndex(level) {
  const index = LEVELS.indexOf(level)
  return index === -1 ? 0 : index
}

const so2Level = computed(() => levelByThreshold(form.so2Value, [50, 150, 475, 800, 1600]))
const coLevel = computed(() => levelByThreshold(form.coValue, [2, 4, 14, 24, 36]))
const pm25Level = computed(() => levelByThreshold(form.pm25Value, [35, 75, 115, 150, 250]))
const aqiLevel = computed(() => {
  const levels = [so2Level.value, coLevel.value, pm25Level.value].filter((level) => level !== '-')

  if (levels.length < 3) {
    return '-'
  }

  return levels.sort((left, right) => levelIndex(right) - levelIndex(left))[0]
})

async function fetchTask() {
  loading.value = true

  try {
    task.value = unwrapPayload(await getGridTaskDetail(afId.value))
    form.so2Value = task.value.so2Value ?? ''
    form.coValue = task.value.coValue ?? ''
    form.pm25Value = task.value.pm25Value ?? ''
  } finally {
    loading.value = false
  }
}

function validateForm() {
  const fields = [
    ['SO2', form.so2Value],
    ['CO', form.coValue],
    ['PM2.5', form.pm25Value],
  ]

  const invalid = fields.find(([, value]) => value === '' || Number(value) < 0 || !Number.isFinite(Number(value)))

  if (invalid) {
    showToast(`请输入有效的 ${invalid[0]} 实测值`)
    return false
  }

  return true
}

async function handleSubmit() {
  if (!validateForm()) {
    return
  }

  await showConfirmDialog({
    title: '提交实测数据',
    message: `提交后由服务端按国标计算 AQI 等级，并交由管理员处置。`,
  })

  submitting.value = true

  try {
    // 只提交浓度值：单项等级与 AQI 由服务端计算后返回，客户端不再参与判定
    task.value = unwrapPayload(
      await submitGridMeasurement(afId.value, {
        so2Value: Number(form.so2Value),
        coValue: Number(form.coValue),
        pm25Value: Number(form.pm25Value),
      }),
    )
    showToast('提交成功，等待管理员处置')
  } catch (error) {
    if (error?.message !== 'cancel') {
      showToast(error.response?.data?.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(fetchTask)
</script>

<template>
  <main class="grid-page grid-detail-page">
    <van-nav-bar title="任务详情" left-arrow fixed placeholder @click-left="router.back()" />

    <van-loading v-if="loading" class="grid-centered" />

    <template v-else>
      <section class="grid-task-summary">
        <div class="task-card-header">
          <span>#{{ task.afId || task.feedbackId }}</span>
          <van-tag :type="isMeasured ? 'success' : 'warning'">
            {{ isMeasured ? '已录入实测' : task.statusText || '待检测' }}
          </van-tag>
        </div>
        <h1>{{ task.pollutionType || 'AQI 反馈任务' }}</h1>
        <p>{{ task.address }}</p>
        <dl>
          <div>
            <dt>城市</dt>
            <dd>{{ task.cityName || '-' }}</dd>
          </div>
          <div>
            <dt>预计等级</dt>
            <dd>{{ task.estimatedLevel || '-' }}</dd>
          </div>
          <div>
            <dt>反馈时间</dt>
            <dd>{{ formatDate(task.feedbackDate) }}</dd>
          </div>
          <div>
            <dt>指派时间</dt>
            <dd>{{ formatDate(task.assignedAt) }}</dd>
          </div>
        </dl>
        <van-divider />
        <p class="task-description">{{ task.description }}</p>
      </section>

      <section class="measurement-panel">
        <div class="section-heading">
          <h2>实测数据</h2>
          <van-tag v-if="displayLevels.aqi !== '-'" type="danger" size="large">
            AQI {{ displayLevels.aqiValue ? `${displayLevels.aqiValue} · ` : '' }}{{ displayLevels.aqi }}
          </van-tag>
        </div>

        <van-cell-group inset>
          <van-field
            v-model="form.so2Value"
            label="SO2"
            type="number"
            input-align="right"
            placeholder="μg/m³"
            :readonly="isMeasured"
          >
            <template #button>
              <van-tag plain>{{ displayLevels.so2 }}</van-tag>
            </template>
          </van-field>
          <van-field
            v-model="form.coValue"
            label="CO"
            type="number"
            input-align="right"
            placeholder="mg/m³"
            :readonly="isMeasured"
          >
            <template #button>
              <van-tag plain>{{ displayLevels.co }}</van-tag>
            </template>
          </van-field>
          <van-field
            v-model="form.pm25Value"
            label="PM2.5"
            type="number"
            input-align="right"
            placeholder="μg/m³"
            :readonly="isMeasured"
          >
            <template #button>
              <van-tag plain>{{ displayLevels.pm25 }}</van-tag>
            </template>
          </van-field>
        </van-cell-group>

        <div v-if="isMeasured" class="done-note">
          已于 {{ formatDate(task.measuredAt) }} 提交，服务端计算结果：AQI
          {{ displayLevels.aqiValue || '-' }}（{{ displayLevels.aqi }}）。等待管理员处置后办结。
        </div>

        <van-button
          v-else
          class="submit-measurement"
          block
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          提交实测数据
        </van-button>
      </section>
    </template>
  </main>
</template>
