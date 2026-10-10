<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFeedbackPage } from '../api/admin'
import { getCityOptions } from '../api/dictionary'

const router = useRouter()
const loading = ref(false)
const feedbackRows = ref([])
const total = ref(0)
// 城市筛选项来自后端字典，前端不再硬编码
const cityOptions = ref([])

const pageQuery = reactive({
  page: 1,
  size: 10,
  cityId: null,
  status: null,
})

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已驳回', value: 3 },
]

function statusTagType(status) {
  if (Number(status) === 2) {
    return 'success'
  }

  if (Number(status) === 3) {
    return 'danger'
  }

  if (Number(status) === 1) {
    return 'primary'
  }

  return 'warning'
}

async function fetchCityOptions() {
  const payload = unwrapPayload(await getCityOptions())
  cityOptions.value = (Array.isArray(payload) ? payload : []).map((item) => ({
    label: item.cityName,
    value: item.cityId,
  }))
}

function getRowId(row) {
  return row.afId ?? row.feedbackId ?? row.id
}

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

async function fetchFeedbackPage() {
  loading.value = true

  try {
    const params = {
      page: pageQuery.page,
      size: pageQuery.size,
    }

    if (pageQuery.cityId !== null && pageQuery.cityId !== '') {
      params.cityId = pageQuery.cityId
    }

    if (pageQuery.status !== null && pageQuery.status !== '') {
      params.status = pageQuery.status
    }

    const payload = unwrapPayload(await getFeedbackPage(params))
    const records = payload.records || payload.list || payload.rows || []

    feedbackRows.value = Array.isArray(records) ? records : []
    total.value = Number(payload.total ?? payload.totalElements ?? feedbackRows.value.length)
  } finally {
    loading.value = false
  }
}

function handleFilterChange() {
  pageQuery.page = 1
  fetchFeedbackPage()
}

function resetFilters() {
  pageQuery.cityId = null
  pageQuery.status = null
  handleFilterChange()
}

function handleSizeChange(size) {
  pageQuery.size = size
  pageQuery.page = 1
  fetchFeedbackPage()
}

function handleCurrentChange(page) {
  pageQuery.page = page
  fetchFeedbackPage()
}

function handleDetail(row) {
  const afId = getRowId(row)

  if (!afId) {
    ElMessage.warning('当前反馈缺少编号，无法查看详情')
    return
  }

  router.push(`/feedback/${afId}`)
}

onMounted(() => {
  fetchCityOptions()
  fetchFeedbackPage()
})
</script>

<template>
  <section>
    <div class="page-title">
      <h2>反馈列表</h2>
      <el-button type="primary" :loading="loading" @click="fetchFeedbackPage">刷新</el-button>
    </div>

    <div class="filter-bar">
      <el-select
        v-model="pageQuery.cityId"
        class="filter-item"
        placeholder="全部城市"
        clearable
        data-testid="filter-city"
        @change="handleFilterChange"
      >
        <el-option
          v-for="item in cityOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="pageQuery.status"
        class="filter-item"
        placeholder="全部状态"
        clearable
        data-testid="filter-status"
        @change="handleFilterChange"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-button plain @click="resetFilters">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="feedbackRows" :row-key="getRowId" border>
      <el-table-column label="编号" width="110">
        <template #default="{ row }">{{ displayValue(getRowId(row)) }}</template>
      </el-table-column>
      <el-table-column prop="supervisorName" label="监督员" width="140">
        <template #default="{ row }">{{ displayValue(row.supervisorName) }}</template>
      </el-table-column>
      <el-table-column prop="cityName" label="城市" width="140">
        <template #default="{ row }">{{ displayValue(row.cityName) }}</template>
      </el-table-column>
      <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">{{ displayValue(row.address) }}</template>
      </el-table-column>
      <el-table-column prop="estimatedLevel" label="预估等级" width="130">
        <template #default="{ row }">
          <el-tag v-if="row.estimatedLevel" type="warning">{{ row.estimatedLevel }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ displayValue(row.statusText) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="反馈日期" width="180">
        <template #default="{ row }">{{ formatDate(row.feedbackDate || row.submittedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="pageQuery.page"
        v-model:page-size="pageQuery.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </section>
</template>
