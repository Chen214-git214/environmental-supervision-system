<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  assignFeedback,
  getCrossProvinceAssignmentCandidates,
  getFeedbackDetail,
  getLocalAssignmentCandidates,
  handleFeedback,
} from '../api/admin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const assigning = ref(false)
const candidateLoading = ref(false)
const assignDialogVisible = ref(false)
const handleDialogVisible = ref(false)
const handling = ref(false)
const feedback = ref({})
const candidateInfo = ref({})
const candidates = ref([])
const selectedGridMemberId = ref()
const crossProvince = ref(false)

const handleForm = reactive({
  status: 2,
  reply: '',
})

const afId = computed(() => route.params.afId)
const isAssigned = computed(() => Boolean(feedback.value.assignedGridMemberId))
// 状态 2/3 为终态，不能再办结或驳回
const isTerminal = computed(() => [2, 3].includes(Number(feedback.value.status)))
const canHandle = computed(() => !isTerminal.value)

function displayValue(value) {
  return value === null || value === undefined || value === '' ? '-' : value
}

function formatDate(value) {
  if (!value) {
    return '-'
  }

  return String(value).replace('T', ' ').slice(0, 19)
}

function unwrapPayload(result) {
  return result?.data ?? result ?? {}
}

async function fetchFeedbackDetail() {
  if (!afId.value) {
    return
  }

  loading.value = true

  try {
    feedback.value = unwrapPayload(await getFeedbackDetail(afId.value))
  } finally {
    loading.value = false
  }
}

async function loadCandidates(loader, useCrossProvince) {
  candidateLoading.value = true
  selectedGridMemberId.value = undefined
  crossProvince.value = useCrossProvince

  try {
    const payload = unwrapPayload(await loader(afId.value))
    candidateInfo.value = payload
    candidates.value = Array.isArray(payload.members) ? payload.members : []
  } finally {
    candidateLoading.value = false
  }
}

async function openAssignDialog() {
  assignDialogVisible.value = true
  await loadCandidates(getLocalAssignmentCandidates, false)
}

async function switchCandidateSource(value) {
  await loadCandidates(
    value ? getCrossProvinceAssignmentCandidates : getLocalAssignmentCandidates,
    value,
  )
}

async function handleAssign() {
  if (!selectedGridMemberId.value) {
    ElMessage.warning('请选择网格员')
    return
  }

  assigning.value = true

  try {
    feedback.value = unwrapPayload(
      await assignFeedback(afId.value, {
        gridMemberId: selectedGridMemberId.value,
        crossProvince: crossProvince.value,
      }),
    )
    ElMessage.success('指派成功')
    assignDialogVisible.value = false
  } finally {
    assigning.value = false
  }
}

function openHandleDialog(status) {
  handleForm.status = status
  handleForm.reply = feedback.value.replyContent || ''
  handleDialogVisible.value = true
}

async function submitHandle() {
  if (!handleForm.reply.trim()) {
    ElMessage.warning('请填写处理回复')
    return
  }

  handling.value = true

  try {
    await handleFeedback(afId.value, {
      status: handleForm.status,
      reply: handleForm.reply.trim(),
    })
    ElMessage.success(handleForm.status === 2 ? '已办结' : '已驳回')
    handleDialogVisible.value = false
    await fetchFeedbackDetail()
  } finally {
    handling.value = false
  }
}

onMounted(fetchFeedbackDetail)
</script>

<template>
  <section v-loading="loading">
    <div class="page-title">
      <h2>反馈详情 #{{ afId }}</h2>
      <div class="header-actions">
        <el-button :disabled="isAssigned" @click="openAssignDialog">
          {{ isAssigned ? '已指派网格员' : '指派网格员' }}
        </el-button>
        <el-button type="success" :disabled="!canHandle" @click="openHandleDialog(2)">
          {{ isTerminal ? '已处理' : '办结反馈' }}
        </el-button>
        <el-button type="danger" plain :disabled="!canHandle" @click="openHandleDialog(3)">
          驳回反馈
        </el-button>
        <el-button type="primary" @click="router.back()">返回</el-button>
      </div>
    </div>

    <div class="detail-grid">
      <el-card shadow="never">
        <template #header>基础信息</template>
        <el-descriptions border :column="2">
          <el-descriptions-item label="编号">
            {{ displayValue(feedback.afId || feedback.feedbackId) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            {{ displayValue(feedback.statusText) }}
          </el-descriptions-item>
          <el-descriptions-item label="监督员">
            {{ displayValue(feedback.supervisorName) }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ displayValue(feedback.supervisorPhone) }}
          </el-descriptions-item>
          <el-descriptions-item label="城市">
            {{ displayValue(feedback.cityName) }}
          </el-descriptions-item>
          <el-descriptions-item label="预计等级">
            {{ displayValue(feedback.estimatedLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="污染类型">
            {{ displayValue(feedback.pollutionType) }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈日期">
            {{ formatDate(feedback.feedbackDate || feedback.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="经度">
            {{ displayValue(feedback.longitude) }}
          </el-descriptions-item>
          <el-descriptions-item label="纬度">
            {{ displayValue(feedback.latitude) }}
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            {{ displayValue(feedback.address) }}
          </el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            {{ displayValue(feedback.description) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never">
        <template #header>处置与检测信息</template>
        <el-descriptions border :column="2">
          <el-descriptions-item label="指派网格员">
            {{ displayValue(feedback.assignedGridMemberName) }}
          </el-descriptions-item>
          <el-descriptions-item label="指派方式">
            {{ displayValue(feedback.assignmentTypeText) }}
          </el-descriptions-item>
          <el-descriptions-item label="SO2">
            {{ displayValue(feedback.so2Value) }} / {{ displayValue(feedback.so2Level) }}
          </el-descriptions-item>
          <el-descriptions-item label="CO">
            {{ displayValue(feedback.coValue) }} / {{ displayValue(feedback.coLevel) }}
          </el-descriptions-item>
          <el-descriptions-item label="PM2.5">
            {{ displayValue(feedback.pm25Value) }} / {{ displayValue(feedback.pm25Level) }}
          </el-descriptions-item>
          <el-descriptions-item label="AQI">
            {{ displayValue(feedback.aqiLevel) }}
            <template v-if="feedback.aqiValue">（AQI 数值 {{ feedback.aqiValue }}）</template>
          </el-descriptions-item>
          <el-descriptions-item label="实测提交时间" :span="2">
            {{ formatDate(feedback.measuredAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-status-strip">
          <div class="detail-status-strip__item">
            <span>反馈提交</span>
            <strong>{{ formatDate(feedback.feedbackDate || feedback.submittedAt) }}</strong>
          </div>
          <div class="detail-status-strip__item">
            <span>网格员指派</span>
            <strong>{{ formatDate(feedback.assignedAt) }}</strong>
          </div>
          <div class="detail-status-strip__item">
            <span>现场检测</span>
            <strong>{{ formatDate(feedback.measuredAt) }}</strong>
          </div>
          <div class="detail-status-strip__item">
            <span>管理员处置</span>
            <strong>{{ formatDate(feedback.handledAt) }}</strong>
          </div>
        </div>
      </el-card>

      <el-card shadow="never">
        <template #header>处理结果</template>
        <el-descriptions border :column="2">
          <el-descriptions-item label="处理状态">
            {{ displayValue(feedback.statusText) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理人">
            {{ displayValue(feedback.handlerName) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间">
            {{ formatDate(feedback.handledAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理回复" :span="2">
            {{ displayValue(feedback.replyContent) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-button
          v-if="canHandle"
          class="handle-inline-action"
          type="success"
          @click="openHandleDialog(2)"
        >
          填写处理回复并办结
        </el-button>
      </el-card>
    </div>

    <el-dialog v-model="assignDialogVisible" title="指派网格员" width="560px">
      <el-alert
        v-if="candidateInfo.message"
        class="assign-alert"
        :title="candidateInfo.message"
        type="info"
        show-icon
        :closable="false"
      />

      <el-switch
        v-model="crossProvince"
        active-text="外省调派"
        inactive-text="本地指派"
        :disabled="candidateLoading"
        @change="switchCandidateSource"
      />

      <el-table
        v-loading="candidateLoading"
        class="candidate-table"
        :data="candidates"
        border
        empty-text="暂无可指派网格员"
        @row-click="(row) => (selectedGridMemberId = row.gridMemberId)"
      >
        <el-table-column width="56">
          <template #default="{ row }">
            <el-radio v-model="selectedGridMemberId" :label="row.gridMemberId">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="employeeNo" label="账号" width="110" />
        <el-table-column prop="cityName" label="城市" width="110" />
        <el-table-column prop="gridName" label="网格" min-width="140" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="handleAssign">确认指派</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="handleDialogVisible"
      :title="handleForm.status === 2 ? '办结反馈' : '驳回反馈'"
      width="560px"
    >
      <el-alert
        class="assign-alert"
        :title="
          handleForm.status === 2
            ? '办结后反馈进入终态，公众监督员可看到本条回复。'
            : '驳回后反馈进入终态，请在回复中说明驳回理由。'
        "
        :type="handleForm.status === 2 ? 'success' : 'warning'"
        show-icon
        :closable="false"
      />

      <el-form label-position="top">
        <el-form-item label="处理回复" required>
          <el-input
            v-model="handleForm.reply"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请填写处理过程与结论，将展示给公众监督员"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button
          :type="handleForm.status === 2 ? 'success' : 'danger'"
          :loading="handling"
          @click="submitHandle"
        >
          {{ handleForm.status === 2 ? '确认办结' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </section>
</template>
