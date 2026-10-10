<template>
  <div class="stats-grid">
    <el-card shadow="never">
      <div class="metric-value">28</div>
      <div class="metric-label">今日反馈</div>
    </el-card>
    <el-card shadow="never">
      <div class="metric-value">76%</div>
      <div class="metric-label">反馈办结率</div>
    </el-card>
    <el-card shadow="never">
      <div class="metric-value">3.4h</div>
      <div class="metric-label">平均处理时长</div>
    </el-card>
  </div>

  <el-card class="chart-placeholder" shadow="never">
    <template #header>监管统计概览</template>
    <div class="placeholder-content">后续可接入反馈趋势、污染类型占比和区域排行图表。</div>
  </el-card>
</template>
