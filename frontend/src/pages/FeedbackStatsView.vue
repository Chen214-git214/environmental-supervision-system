<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import BigScreenChart from '../components/BigScreenChart.vue'
import PollutionBarChart from '../components/PollutionBarChart.vue'
import { getDashboardData } from '../api/statistics'

const POLL_INTERVAL = 5000

const router = useRouter()
const loading = ref(false)
const currentTime = ref('')
const dashboard = ref({
  summary: {},
  provincePollutantStats: [],
  aqiDistribution: [],
  monthlyTrend: [],
  refreshedAt: '',
})

let refreshTimer = null
let clockTimer = null

const panelColors = ['#56d6ff', '#8cff7a', '#ffbc57']
const aqiPalette = ['#69f0ae', '#d4ff4f', '#ffcd4b', '#ff8a47', '#ff5c7f', '#c255ff']

const summaryCards = computed(() => {
  const summary = dashboard.value.summary || {}
  const completionRate = Number(summary.completionRate || 0)

  return [
    {
      label: '累计反馈总数',
      value: Number(summary.totalFeedback || 0),
      suffix: '件',
      accent: '#56d6ff',
      note: '系统累计接入公众环境反馈',
    },
    {
      label: '已完成处置',
      value: Number(summary.completedFeedback || 0),
      suffix: '件',
      accent: '#8cff7a',
      note: '已形成闭环处置记录',
    },
    {
      label: '今日新增反馈',
      value: Number(summary.todayFeedback || 0),
      suffix: '件',
      accent: '#ffbc57',
      note: '当前自然日新增任务量',
    },
    {
      label: '办结率',
      value: completionRate.toFixed(completionRate % 1 === 0 ? 0 : 1),
      suffix: '%',
      accent: '#7f8cff',
      note: '已完成任务 / 累计任务',
    },
  ]
})

const provinceStats = computed(() => dashboard.value.provincePollutantStats || [])

const rankedProvinces = computed(() => {
  return provinceStats.value
    .map((item) => {
      const score =
        Number(item.pm25ExceedCount || 0) * 1.2 +
        Number(item.so2ExceedCount || 0) * 1.05 +
        Number(item.coExceedCount || 0) * 0.9

      return {
        name: item.provinceShortName || item.provinceName || '-',
        value: Math.round(score),
      }
    })
    .sort((a, b) => b.value - a.value)
    .slice(0, 6)
})

const mapData = computed(() => {
  return provinceStats.value.map((item) => {
    const value =
      Number(item.pm25ExceedCount || 0) +
      Number(item.so2ExceedCount || 0) +
      Number(item.coExceedCount || 0)

    return {
      name: item.provinceName,
      value,
    }
  })
})

const monthlyTrend = computed(() => dashboard.value.monthlyTrend || [])

const totalAlerts = computed(() => {
  return mapData.value.reduce((sum, item) => sum + Number(item.value || 0), 0)
})

const activeProvinceCount = computed(() => mapData.value.filter((item) => Number(item.value || 0) > 0).length)

const peakMonth = computed(() => {
  const sorted = [...monthlyTrend.value].sort((a, b) => Number(b.feedbackCount || 0) - Number(a.feedbackCount || 0))
  return sorted[0]?.month || '--'
})

const lineOption = computed(() => ({
  grid: { left: 36, right: 16, top: 26, bottom: 28 },
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(8, 18, 45, 0.96)',
    borderColor: 'rgba(86, 214, 255, 0.35)',
    textStyle: { color: '#d9f2ff' },
  },
  xAxis: {
    type: 'category',
    data: monthlyTrend.value.map((item) => item.month),
    axisLine: { lineStyle: { color: 'rgba(120, 188, 255, 0.35)' } },
    axisLabel: { color: '#8eb8db', fontSize: 11 },
    axisTick: { show: false },
  },
  yAxis: {
    type: 'value',
    splitNumber: 4,
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#8eb8db', fontSize: 11 },
    splitLine: { lineStyle: { color: 'rgba(70, 119, 182, 0.22)' } },
  },
  series: [
    {
      name: '反馈量',
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      data: monthlyTrend.value.map((item) => Number(item.feedbackCount || 0)),
      lineStyle: { width: 3, color: '#79d8ff' },
      itemStyle: { color: '#79d8ff', borderColor: '#ffffff', borderWidth: 1.5 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(121, 216, 255, 0.34)' },
            { offset: 1, color: 'rgba(121, 216, 255, 0.02)' },
          ],
        },
      },
    },
  ],
}))

const pieOption = computed(() => ({
  color: aqiPalette,
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(8, 18, 45, 0.96)',
    borderColor: 'rgba(86, 214, 255, 0.35)',
    textStyle: { color: '#d9f2ff' },
  },
  legend: {
    orient: 'vertical',
    left: 8,
    top: 'center',
    icon: 'rect',
    itemWidth: 12,
    itemHeight: 8,
    textStyle: { color: '#8eb8db', fontSize: 11 },
  },
  series: [
    {
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['66%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderColor: 'rgba(4, 18, 45, 0.95)',
        borderWidth: 3,
      },
      label: {
        color: '#d9f2ff',
        formatter: '{b}\n{c}',
        fontSize: 10,
      },
      labelLine: {
        lineStyle: { color: 'rgba(142, 184, 219, 0.5)' },
        length: 10,
        length2: 8,
      },
      data: (dashboard.value.aqiDistribution || []).map((item) => ({
        name: item.level,
        value: Number(item.count || 0),
      })),
    },
  ],
}))

function buildGaugeOption(name, value, color) {
  const safeValue = Math.max(0, Math.min(100, Number(value || 0)))

  return {
    series: [
      {
        type: 'gauge',
        center: ['50%', '58%'],
        radius: '92%',
        startAngle: 210,
        endAngle: -30,
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          lineStyle: {
            width: 12,
            color: [
              [safeValue / 100, color],
              [1, 'rgba(74, 112, 168, 0.24)'],
            ],
          },
        },
        pointer: {
          icon: 'path://M5.1,0.6L-1.4,70.1h2.8L8,0.6z',
          length: '54%',
          width: 6,
          itemStyle: { color },
        },
        progress: { show: false },
        axisTick: {
          distance: -18,
          splitNumber: 4,
          lineStyle: { color: '#8eb8db', width: 1 },
        },
        splitLine: {
          distance: -18,
          length: 10,
          lineStyle: { color: '#bfe5ff', width: 2 },
        },
        axisLabel: {
          distance: -34,
          color: '#81accd',
          fontSize: 9,
        },
        detail: {
          valueAnimation: true,
          offsetCenter: [0, '52%'],
          color: color,
          fontSize: 26,
          fontWeight: 700,
          formatter: '{value}',
        },
        title: {
          offsetCenter: [0, '78%'],
          color: '#a6c8e5',
          fontSize: 11,
        },
        data: [{ value: safeValue, name }],
      },
    ],
  }
}

const gaugeOptions = computed(() => {
  const summary = dashboard.value.summary || {}

  return [
    buildGaugeOption('办结效率', Number(summary.completionRate || 0), '#8cff7a'),
    buildGaugeOption('日新增压力', Math.min(Number(summary.todayFeedback || 0) * 4, 100), '#ff8a72'),
  ]
})

const pollutantCharts = computed(() => [
  {
    key: 'pm25',
    title: '超标热度值（PM2.5）',
    pollutantName: 'PM2.5',
    field: 'pm25ExceedCount',
    color: panelColors[0],
  },
  {
    key: 'so2',
    title: '二氧化硫（SO2）超标',
    pollutantName: 'SO2',
    field: 'so2ExceedCount',
    color: panelColors[1],
  },
  {
    key: 'co',
    title: '一氧化碳（CO）超标',
    pollutantName: 'CO',
    field: 'coExceedCount',
    color: panelColors[2],
  },
])

function pollutantChartData(field) {
  return provinceStats.value.map((item) => ({
    provinceName: item.provinceName,
    provinceShortName: item.provinceShortName,
    value: Number(item[field] || 0),
  }))
}

const mapOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(8, 18, 45, 0.96)',
    borderColor: 'rgba(86, 214, 255, 0.35)',
    textStyle: { color: '#d9f2ff' },
    formatter(params) {
      const value = Array.isArray(params.value) ? params.value[0] : params.value
      return `${params.name}<br/>综合预警值：${Number(value || 0)}`
    },
  },
  visualMap: {
    min: 0,
    max: Math.max(...mapData.value.map((item) => Number(item.value || 0)), 1),
    show: true,
    orient: 'vertical',
    left: 20,
    bottom: 12,
    itemWidth: 12,
    itemHeight: 74,
    text: ['高', '低'],
    textStyle: { color: '#9ec7e8', fontSize: 11 },
    inRange: {
      color: ['#dbff51', '#ffbd4b', '#ff7f50', '#ff4f8b'],
    },
    calculable: false,
    backgroundColor: 'rgba(3, 15, 38, 0.3)',
  },
  series: [
    {
      type: 'map',
      map: 'china-screen',
      roam: false,
      zoom: 1.08,
      selectedMode: false,
      label: {
        show: true,
        color: '#d8eeff',
        fontSize: 9,
      },
      itemStyle: {
        areaColor: '#163765',
        borderColor: '#78bcff',
        borderWidth: 1,
        shadowColor: 'rgba(41, 173, 255, 0.18)',
        shadowBlur: 8,
      },
      emphasis: {
        label: { color: '#ffffff' },
        itemStyle: {
          areaColor: '#1e4b84',
          borderColor: '#bbf2ff',
        },
      },
      data: mapData.value,
    },
    {
      type: 'effectScatter',
      coordinateSystem: 'geo',
      geoIndex: 0,
      showEffectOn: 'render',
      rippleEffect: { scale: 4, brushType: 'stroke' },
      itemStyle: {
        color: '#56d6ff',
        shadowBlur: 14,
        shadowColor: 'rgba(86, 214, 255, 0.8)',
      },
      data: [],
    },
  ],
  geo: {
    map: 'china-screen',
    roam: false,
    show: false,
  },
}))

const centerStats = computed(() => [
  { label: '活跃预警省份', value: activeProvinceCount.value, accent: '#56d6ff' },
  { label: '本期峰值月份', value: peakMonth.value, accent: '#8cff7a' },
  { label: '累计超标次数', value: totalAlerts.value, accent: '#ff7f72' },
])

function unwrapPayload(result) {
  return result?.data ?? result ?? {}
}

function formatDate(value) {
  if (!value) {
    return '--'
  }

  return String(value).replace('T', ' ').slice(0, 19)
}

function updateClock() {
  const now = new Date()
  const pad = (value) => String(value).padStart(2, '0')
  currentTime.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

async function fetchDashboard(showLoading = false) {
  if (showLoading) {
    loading.value = true
  }

  try {
    dashboard.value = unwrapPayload(await getDashboardData())
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push('/feedback')
}

onMounted(() => {
  updateClock()
  fetchDashboard(true)
  refreshTimer = window.setInterval(() => fetchDashboard(false), POLL_INTERVAL)
  clockTimer = window.setInterval(updateClock, 1000)
})

onBeforeUnmount(() => {
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }

  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
})
</script>

<template>
  <section class="monitor-screen" v-loading="loading">
    <div class="monitor-screen__bg"></div>

    <header class="monitor-screen__header">
      <div class="monitor-screen__header-side">
        <button class="screen-action" type="button" @click="goBack">返回反馈列表</button>
      </div>
      <div class="monitor-screen__title-wrap">
        <p>NEPM ENVIRONMENT COMMAND CENTER</p>
        <h1>环境监测可视化监管大屏</h1>
      </div>
      <div class="monitor-screen__header-side monitor-screen__header-side--right">
        <span>数据刷新：{{ formatDate(dashboard.refreshedAt) }}</span>
        <strong>{{ currentTime }}</strong>
      </div>
    </header>

    <div class="monitor-summary">
      <article
        v-for="card in summaryCards"
        :key="card.label"
        class="monitor-summary__card"
        :style="{ '--card-accent': card.accent }"
      >
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}<small>{{ card.suffix }}</small></strong>
        <p>{{ card.note }}</p>
      </article>
    </div>

    <div class="monitor-layout">
      <aside class="monitor-column monitor-column--left">
        <section class="screen-panel screen-panel--small">
          <header class="screen-panel__header">
            <h3>运行态势仪表</h3>
          </header>
          <div class="monitor-gauges">
            <div v-for="(option, index) in gaugeOptions" :key="index" class="monitor-gauge">
              <BigScreenChart :option="option" />
            </div>
          </div>
        </section>

        <section class="screen-panel">
          <header class="screen-panel__header">
            <h3>AQI 等级分布分析</h3>
          </header>
          <BigScreenChart :option="pieOption" theme-class="big-screen-chart--pie" />
        </section>

        <section class="screen-panel">
          <header class="screen-panel__header">
            <h3>近月反馈趋势曲线</h3>
          </header>
          <BigScreenChart :option="lineOption" theme-class="big-screen-chart--line" />
        </section>
      </aside>

      <main class="monitor-center">
        <section class="screen-panel screen-panel--map">
          <header class="screen-panel__header">
            <h3>全国环境风险热力分布</h3>
            <span>按 PM2.5、SO2、CO 综合超标次数计算</span>
          </header>
          <div class="monitor-map-wrap">
            <BigScreenChart :option="mapOption" theme-class="big-screen-chart--map" />
            <div class="monitor-hot-list">
              <h4>重点区域预警</h4>
              <div v-for="(item, index) in rankedProvinces" :key="item.name" class="hot-row">
                <span>TOP {{ index + 1 }}</span>
                <strong>{{ item.name }}</strong>
                <em>{{ item.value }}</em>
              </div>
            </div>
          </div>
          <div class="monitor-map-stats">
            <article v-for="item in centerStats" :key="item.label" :style="{ '--stat-accent': item.accent }">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </article>
          </div>
        </section>
      </main>

      <aside class="monitor-column monitor-column--right">
        <section v-for="item in pollutantCharts" :key="item.key" class="screen-panel">
          <header class="screen-panel__header">
            <h3>{{ item.title }}</h3>
          </header>
          <PollutionBarChart
            :title="item.title"
            :pollutant-name="item.pollutantName"
            :color="item.color"
            :data="pollutantChartData(item.field)"
            :dark="true"
          />
        </section>
      </aside>
    </div>
  </section>
</template>
