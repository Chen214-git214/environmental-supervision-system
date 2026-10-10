<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { BarChart } from 'echarts/charts'
import { GridComponent, GraphicComponent, LegendComponent, TitleComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, GridComponent, GraphicComponent, LegendComponent, TitleComponent, TooltipComponent, CanvasRenderer])

const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  pollutantName: {
    type: String,
    required: true,
  },
  color: {
    type: String,
    default: '#2f80ed',
  },
  data: {
    type: Array,
    default: () => [],
  },
  dark: {
    type: Boolean,
    default: false,
  },
})

const chartRef = ref(null)
let chartInstance = null

function buildOption() {
  const provinces = props.data.map((item) => item.provinceShortName || item.provinceName || '-')
  const values = props.data.map((item) => Number(item.value || 0))

  return {
    title: {
      text: props.title,
      left: 12,
      top: 8,
      textStyle: {
        color: props.dark ? '#ebf8ff' : '#111827',
        fontSize: 15,
        fontWeight: 700,
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      backgroundColor: props.dark ? 'rgba(8, 18, 45, 0.96)' : 'rgba(255, 255, 255, 0.96)',
      borderColor: props.dark ? 'rgba(86, 214, 255, 0.35)' : 'rgba(209, 213, 219, 0.9)',
      textStyle: {
        color: props.dark ? '#d9f2ff' : '#111827',
      },
      formatter(params) {
        const item = params?.[0]
        return item ? `${item.name}<br/>${props.pollutantName}：${item.value} 次` : ''
      },
    },
    legend: {
      top: 8,
      right: 12,
      data: [props.pollutantName],
      textStyle: {
        color: props.dark ? '#8eb8db' : '#4b5563',
      },
    },
    grid: {
      top: 58,
      right: 18,
      bottom: 32,
      left: 42,
    },
    xAxis: {
      type: 'category',
      data: provinces,
      axisTick: {
        alignWithLabel: true,
      },
      axisLabel: {
        color: props.dark ? '#8eb8db' : '#4b5563',
        interval: 0,
      },
      axisLine: {
        lineStyle: {
          color: props.dark ? 'rgba(120, 188, 255, 0.35)' : '#d1d5db',
        },
      },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      name: '次数',
      nameTextStyle: {
        color: props.dark ? '#8eb8db' : '#6b7280',
      },
      axisLabel: {
        color: props.dark ? '#8eb8db' : '#4b5563',
      },
      splitLine: {
        lineStyle: {
          color: props.dark ? 'rgba(70, 119, 182, 0.22)' : '#edf0f5',
        },
      },
    },
    series: [
      {
        name: props.pollutantName,
        type: 'bar',
        barMaxWidth: 34,
        data: values,
        itemStyle: {
          color: props.color,
          borderRadius: [4, 4, 0, 0],
          shadowBlur: props.dark ? 12 : 0,
          shadowColor: props.dark ? `${props.color}66` : 'transparent',
        },
        label: {
          show: true,
          position: 'top',
          color: props.dark ? '#d9f2ff' : '#374151',
          fontSize: 12,
        },
      },
    ],
    graphic: values.length
      ? undefined
      : {
          type: 'text',
          left: 'center',
          top: 'middle',
          style: {
            text: '暂无统计数据',
            fill: props.dark ? '#7fa8c9' : '#9ca3af',
            fontSize: 14,
          },
        },
  }
}

function renderChart() {
  if (!chartRef.value) {
    return
  }

  if (!chartInstance) {
    chartInstance = init(chartRef.value)
  }

  chartInstance.setOption(buildOption(), true)
}

function resizeChart() {
  chartInstance?.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
  chartInstance = null
})

watch(
  () => [props.title, props.pollutantName, props.color, props.data],
  () => {
    nextTick(renderChart)
  },
  { deep: true },
)
</script>

<template>
  <div ref="chartRef" class="pollution-bar-chart"></div>
</template>
