<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { BarChart, EffectScatterChart, GaugeChart, LineChart, MapChart, PieChart } from 'echarts/charts'
import {
  GeoComponent,
  GraphicComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent,
} from 'echarts/components'
import { init, registerMap, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import chinaGeoJson from '../assets/china.json'

use([
  BarChart,
  EffectScatterChart,
  GaugeChart,
  LineChart,
  MapChart,
  PieChart,
  GeoComponent,
  GraphicComponent,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  VisualMapComponent,
  CanvasRenderer,
])

registerMap('china-screen', chinaGeoJson)

const props = defineProps({
  option: {
    type: Object,
    required: true,
  },
  themeClass: {
    type: String,
    default: '',
  },
})

const chartRef = ref(null)
const chartClass = computed(() => ['big-screen-chart', props.themeClass].filter(Boolean).join(' '))
let chartInstance = null

function renderChart() {
  if (!chartRef.value) {
    return
  }

  if (!chartInstance) {
    chartInstance = init(chartRef.value)
  }

  chartInstance.setOption(props.option, true)
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
  () => props.option,
  () => {
    nextTick(renderChart)
  },
  { deep: true },
)
</script>

<template>
  <div ref="chartRef" :class="chartClass"></div>
</template>
