<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { fetchDashboardStats } from '@/api/dashboard'
import { Odometer, User, Document, CircleCheck, CircleClose } from '@element-plus/icons-vue'

// 统计数据
const stats = ref({
  deviceCount: 0,
  taskCount: 0,
  userCount: 0,
  passRate: 0,
  passFailDistribution: { passCount: 0, failCount: 0 },
  recentTasks: [],
  taskTrend: [],
})

const loading = ref(true)

// 图表实例
let taskTrendChart = null
let passRateChart = null
let projectTypeChart = null

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  try {
    const data = await fetchDashboardStats()
    stats.value = data
    // 数据加载完成后渲染图表
    setTimeout(() => {
      initCharts()
    }, 100)
  } catch (e) {
    console.error('加载统计数据失败:', e)
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  initTaskTrendChart()
  initPassRateChart()
  initProjectTypeChart()
}

// 任务趋势图
const initTaskTrendChart = () => {
  const chartDom = document.getElementById('taskTrendChart')
  if (!chartDom) return
  
  if (taskTrendChart) {
    taskTrendChart.dispose()
  }
  taskTrendChart = echarts.init(chartDom)
  
  const trend = stats.value.taskTrend || []
  const option = {
    title: {
      text: '近7天检定任务趋势',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 600,
        color: '#1a1a2e',
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: trend.map(item => item.date),
      axisLine: {
        lineStyle: { color: '#e0e0e0' },
      },
      axisLabel: {
        color: '#666',
      },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: {
        show: false,
      },
      splitLine: {
        lineStyle: { color: '#f0f0f0' },
      },
      axisLabel: {
        color: '#666',
      },
    },
    series: [
      {
        name: '任务数',
        type: 'bar',
        data: trend.map(item => item.count),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
        barWidth: '40%',
      },
    ],
  }
  taskTrendChart.setOption(option)
}

// 合格率饼图
const initPassRateChart = () => {
  const chartDom = document.getElementById('passRateChart')
  if (!chartDom) return
  
  if (passRateChart) {
    passRateChart.dispose()
  }
  passRateChart = echarts.init(chartDom)
  
  const option = {
    title: {
      text: '检定结果分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 600,
        color: '#1a1a2e',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'horizontal',
      bottom: '5%',
    },
    series: [
      {
        name: '检定结果',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
        },
        data: [
          {
            value: stats.value.passFailDistribution?.passCount || 0,
            name: '合格',
            itemStyle: { color: '#52c41a' },
          },
          {
            value: stats.value.passFailDistribution?.failCount || 0,
            name: '不合格',
            itemStyle: { color: '#ff4d4f' },
          },
        ],
      },
    ],
  }
  passRateChart.setOption(option)
}

// 项目类型分布图 - 改为显示月度趋势或占位
const initProjectTypeChart = () => {
  const chartDom = document.getElementById('projectTypeChart')
  if (!chartDom) return
  
  if (projectTypeChart) {
    projectTypeChart.dispose()
  }
  projectTypeChart = echarts.init(chartDom)
  
  // 从最近任务数据提取设备分布
  const recentTasks = stats.value.recentTasks || []
  const deviceMap = {}
  recentTasks.forEach(task => {
    const name = task.deviceProductNo || '未知设备'
    deviceMap[name] = (deviceMap[name] || 0) + 1
  })
  const deviceStats = Object.entries(deviceMap).map(([name, count]) => ({ name, count }))
  
  const option = {
    title: {
      text: '检定设备分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 600,
        color: '#1a1a2e',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'horizontal',
      bottom: '5%',
    },
    series: [
      {
        name: '设备分布',
        type: 'pie',
        radius: '60%',
        center: ['50%', '45%'],
        data: deviceStats.length > 0 ? deviceStats.map((item, index) => ({
          value: item.count,
          name: item.name,
          itemStyle: {
            color: ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe'][index % 5],
          },
        })) : [{ value: 1, name: '暂无数据', itemStyle: { color: '#e0e0e0' } }],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }
  projectTypeChart.setOption(option)
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  taskTrendChart?.resize()
  passRateChart?.resize()
  projectTypeChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

// 清理
import { onUnmounted } from 'vue'
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  taskTrendChart?.dispose()
  passRateChart?.dispose()
  projectTypeChart?.dispose()
})
</script>

<template>
  <div class="dashboard-page" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">数据可视化</h2>
      <p class="page-desc">系统运行数据统计与分析</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
          <el-icon :size="28"><Odometer /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.deviceCount }}</div>
          <div class="stat-label">设备总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.taskCount }}</div>
          <div class="stat-label">检定任务数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%)">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.passRate }}%</div>
          <div class="stat-label">检定合格率</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card class="chart-card" shadow="never">
        <div id="taskTrendChart" class="chart-container"></div>
      </el-card>
      
      <el-card class="chart-card" shadow="never">
        <div id="passRateChart" class="chart-container"></div>
      </el-card>
    </div>

    <div class="charts-row">
      <el-card class="chart-card" shadow="never">
        <div id="projectTypeChart" class="chart-container"></div>
      </el-card>
      
      <el-card class="chart-card recent-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span class="card-title">最近检定任务</span>
          </div>
        </template>
        <el-table :data="stats.recentTasks" stripe size="small" max-height="280">
          <el-table-column prop="id" label="ID" width="60" align="center" />
          <el-table-column prop="deviceProductNo" label="设备名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="检定员" width="80" align="center" />
          <el-table-column prop="result" label="结果" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">
                {{ row.result === 1 ? '合格' : '不合格' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="testDate" label="检定时间" width="100">
            <template #default="{ row }">
              {{ row.testDate ? row.testDate.substring(5, 16) : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  margin-bottom: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 4px 0;
}

.page-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  border-radius: 12px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.recent-card {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

/* 响应式 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
