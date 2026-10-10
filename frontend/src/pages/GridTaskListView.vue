<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getGridTasks } from '../api/grid'
import { useGridAuthStore } from '../stores/gridAuth'

const router = useRouter()
const gridAuthStore = useGridAuthStore()
const loading = ref(false)
const refreshing = ref(false)
const tasks = ref([])

const gridMember = computed(() => gridAuthStore.gridMemberInfo || {})

function unwrapPayload(result) {
  return result?.data ?? result ?? []
}

function taskId(task) {
  return task.afId ?? task.feedbackId ?? task.id
}

// 实测提交后状态仍是"处理中"（办结权在管理员），因此用 measuredAt 区分"待检测/已录入"
function isMeasured(task) {
  return Boolean(task.measuredAt)
}

function statusLabel(task) {
  return isMeasured(task) ? '已录入实测' : task.statusText || '待检测'
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return String(value).replace('T', ' ').slice(0, 16)
}

async function fetchTasks() {
  if (!gridMember.value.gridMemberId) {
    return
  }

  loading.value = true

  try {
    const payload = unwrapPayload(
      await getGridTasks({
        status: 1,
      }),
    )
    tasks.value = Array.isArray(payload) ? payload : []
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function handleRefresh() {
  refreshing.value = true
  fetchTasks()
}

function openTask(task) {
  const id = taskId(task)

  if (!id) {
    showToast('任务编号缺失')
    return
  }

  router.push(`/grid/tasks/${id}`)
}

function logout() {
  gridAuthStore.logout()
  router.replace('/grid/login')
}

onMounted(fetchTasks)
</script>

<template>
  <main class="grid-page">
    <van-nav-bar title="待检测任务" fixed placeholder>
      <template #right>
        <van-icon name="revoke" size="20" @click="logout" />
      </template>
    </van-nav-bar>

    <section class="grid-member-strip">
      <div>
        <strong>{{ gridMember.realName }}</strong>
        <span>{{ gridMember.employeeNo }} · {{ gridMember.gridName }}</span>
      </div>
      <van-tag type="primary" plain>{{ tasks.length }} 项在办</van-tag>
    </section>

    <van-pull-refresh v-model="refreshing" @refresh="handleRefresh">
      <section class="grid-task-list">
        <van-loading v-if="loading && !refreshing" class="grid-centered" />

        <van-empty v-else-if="tasks.length === 0" description="暂无待检测任务" />

        <article v-for="task in tasks" v-else :key="taskId(task)" class="grid-task-card" @click="openTask(task)">
          <div class="task-card-header">
            <span>#{{ taskId(task) }}</span>
            <van-tag :type="isMeasured(task) ? 'success' : 'warning'">{{ statusLabel(task) }}</van-tag>
          </div>
          <h2>{{ task.pollutionType || 'AQI 反馈任务' }}</h2>
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
              <dt>指派时间</dt>
              <dd>{{ formatDate(task.assignedAt || task.feedbackDate) }}</dd>
            </div>
          </dl>
        </article>
      </section>
    </van-pull-refresh>
  </main>
</template>
