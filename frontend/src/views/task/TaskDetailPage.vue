<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchTaskDetail } from '@/api/task'
import { previewReport } from '@/api/report'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref(null)

const loadDetail = async () => {
  loading.value = true
  try {
    detail.value = await fetchTaskDetail(route.params.id)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handlePreview = async () => {
  try {
    const blob = await previewReport(route.params.id)
    const url = window.URL.createObjectURL(new Blob([blob], { type: 'application/pdf' }))
    const win = window.open(url, '_blank')
    if (!win) {
      ElMessage.warning('请允许弹出窗口查看PDF')
    }
  } catch (e) {
    console.error(e)
  }
}

const handleBack = () => {
  router.push('/task/list')
}

onMounted(() => {
  loadDetail()
})
</script>

<template>
  <div class="page-task-detail">
    <el-card v-loading="loading">
      <div class="header">
        <div class="title">检定任务详情</div>
        <div class="header-actions">
          <el-button @click="handleBack">返回任务列表</el-button>
          <el-button type="primary" @click="handlePreview">PDF 预览 / 打印</el-button>
        </div>
      </div>

      <el-descriptions title="任务信息" :column="2" border>
        <el-descriptions-item label="任务ID">{{ detail?.taskInfo?.id }}</el-descriptions-item>
        <el-descriptions-item label="设备ID">{{ detail?.taskInfo?.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="计量点">{{ detail?.taskInfo?.meterPointId }}</el-descriptions-item>
        <el-descriptions-item label="检定员">{{ detail?.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="送检日期">{{ detail?.taskInfo?.deliverDate }}</el-descriptions-item>
        <el-descriptions-item label="测试日期">{{ detail?.taskInfo?.testDate }}</el-descriptions-item>
        <el-descriptions-item label="环境温度">{{ detail?.taskInfo?.temperature }}</el-descriptions-item>
        <el-descriptions-item label="环境湿度">{{ detail?.taskInfo?.humidity }}</el-descriptions-item>
        <el-descriptions-item label="tanφ">{{ detail?.taskInfo?.tanPhi }}</el-descriptions-item>
        <el-descriptions-item label="r%">{{ detail?.taskInfo?.rPercent }}</el-descriptions-item>
      </el-descriptions>

      <el-descriptions class="mt-16" title="设备信息" :column="2" border>
        <el-descriptions-item label="设备编号">{{ detail?.deviceInfo?.productNo }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detail?.deviceInfo?.productName }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ detail?.deviceInfo?.model }}</el-descriptions-item>
        <el-descriptions-item label="制造商">{{ detail?.deviceInfo?.manufacturer }}</el-descriptions-item>
      </el-descriptions>

      <el-table class="mt-16" :data="detail?.resultList || []" border>
        <el-table-column prop="phase" label="相别" width="80" />
        <el-table-column prop="valF" label="f(%)" width="120" />
        <el-table-column prop="valDelta" label="δ(分)" width="120" />
        <el-table-column prop="valDu" label="dU(%)" width="120" />
        <el-table-column prop="valUpt" label="Upt" width="120" />
        <el-table-column prop="valUyb" label="Uyb" width="120" />
        <el-table-column prop="isPass" label="结论" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPass === 1 ? 'success' : 'danger'">
              {{ row.isPass === 1 ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page-task-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.header-actions {
  display: flex;
  gap: 8px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.mt-16 {
  margin-top: 16px;
}
</style>


