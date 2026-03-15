<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchTaskPage, fetchTaskDetail } from '@/api/task'
import { correctResult } from '@/api/correct'

// 任务列表
const taskList = ref([])
const taskLoading = ref(false)
const taskTotal = ref(0)
const taskQuery = reactive({
  page: 1,
  size: 10,
  taskId: '',
  deviceProductNo: '',
  operatorName: '',
  isPass: undefined,
})

// 当前选中的任务详情
const currentTask = ref(null)
const detailLoading = ref(false)

// 修正弹窗
const correctDialogVisible = ref(false)
const correctLoading = ref(false)
const correctForm = reactive({
  resultId: null,
  phase: '',
  valF: null,
  valDelta: null,
  valDu: null,
  valUpt: null,
  valUyb: null,
  reason: '',
})

// 加载任务列表
const loadTaskList = async () => {
  taskLoading.value = true
  try {
    const data = await fetchTaskPage({
      page: taskQuery.page,
      size: taskQuery.size,
      taskId: taskQuery.taskId || undefined,
      deviceProductNo: taskQuery.deviceProductNo || undefined,
      operatorName: taskQuery.operatorName || undefined,
      isPass: taskQuery.isPass,
    })
    taskList.value = data.records || []
    taskTotal.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    taskLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  taskQuery.page = 1
  loadTaskList()
}

// 重置搜索
const handleReset = () => {
  taskQuery.taskId = ''
  taskQuery.deviceProductNo = ''
  taskQuery.operatorName = ''
  taskQuery.isPass = undefined
  taskQuery.page = 1
  loadTaskList()
}

// 查看任务详情
const handleViewDetail = async (task) => {
  detailLoading.value = true
  try {
    currentTask.value = await fetchTaskDetail(task.id)
  } catch (e) {
    console.error(e)
  } finally {
    detailLoading.value = false
  }
}

// 打开修正弹窗
const handleOpenCorrect = (result) => {
  correctForm.resultId = result.id
  correctForm.phase = result.phase
  correctForm.valF = result.valF
  correctForm.valDelta = result.valDelta
  correctForm.valDu = result.valDu
  correctForm.valUpt = result.valUpt
  correctForm.valUyb = result.valUyb
  correctForm.reason = ''
  correctDialogVisible.value = true
}

// 提交修正
const handleSubmitCorrect = async () => {
  if (!correctForm.reason) {
    ElMessage.warning('请填写修正原因')
    return
  }

  try {
    await ElMessageBox.confirm('确认提交数据修正？修正后系统将重新计算合格状态。', '确认修正', {
      type: 'warning',
    })
  } catch {
    return
  }

  correctLoading.value = true
  try {
    await correctResult({
      resultId: correctForm.resultId,
      valF: correctForm.valF,
      valDelta: correctForm.valDelta,
      valDu: correctForm.valDu,
      valUpt: correctForm.valUpt,
      valUyb: correctForm.valUyb,
      reason: correctForm.reason,
    })
    ElMessage.success('修正成功')
    correctDialogVisible.value = false
    // 刷新详情
    if (currentTask.value) {
      handleViewDetail({ id: currentTask.value.taskInfo.id })
    }
  } catch (e) {
    console.error(e)
  } finally {
    correctLoading.value = false
  }
}

// 分页切换
const handlePageChange = (page) => {
  taskQuery.page = page
  loadTaskList()
}

// 返回列表
const handleBack = () => {
  currentTask.value = null
}

onMounted(() => {
  loadTaskList()
})
</script>

<template>
  <div class="page-correct">
    <!-- 任务列表视图 -->
    <el-card v-if="!currentTask">
      <div class="header">
        <span class="title">数据修正（管理员）</span>
      </div>
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        <template #title>
          仅管理员可进行数据修正操作，修正后系统将自动重新计算合格状态并记录操作日志。
        </template>
      </el-alert>

      <!-- 搜索表单 -->
      <el-form :inline="true" :model="taskQuery" style="margin-bottom: 16px">
        <el-form-item label="任务ID">
          <el-input v-model="taskQuery.taskId" placeholder="精确查询" clearable style="width: 100px" />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="taskQuery.deviceProductNo" placeholder="模糊查询" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="检定员">
          <el-input v-model="taskQuery.operatorName" placeholder="模糊查询" clearable style="width: 100px" />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="taskQuery.isPass" clearable placeholder="全部" style="width: 100px">
            <el-option :value="1" label="合格" />
            <el-option :value="0" label="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="taskLoading" :data="taskList" border>
        <el-table-column prop="id" label="任务ID" width="100" />
        <el-table-column prop="deviceProductNo" label="设备编号" width="140" />
        <el-table-column prop="deviceProductName" label="设备名称" min-width="150" />
        <el-table-column prop="operatorName" label="检定员" width="100" />
        <el-table-column prop="testDate" label="检定日期" width="180" />
        <el-table-column prop="result" label="结果" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.result === 1" type="success" size="small">合格</el-tag>
            <el-tag v-else type="danger" size="small">不合格</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="taskQuery.page"
          :page-size="taskQuery.size"
          layout="total, prev, pager, next"
          :total="taskTotal"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 任务详情视图 -->
    <el-card v-else v-loading="detailLoading">
      <div class="header">
        <el-button @click="handleBack">返回列表</el-button>
        <span class="title" style="margin-left: 16px">任务详情 #{{ currentTask.taskInfo?.id }}</span>
      </div>

      <!-- 任务基本信息 -->
      <el-descriptions title="任务信息" :column="3" border style="margin-bottom: 20px">
        <el-descriptions-item label="计量点编号">{{ currentTask.taskInfo?.meterPointId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="送检日期">{{ currentTask.taskInfo?.deliverDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检定日期">{{ currentTask.taskInfo?.testDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="检定员">{{ currentTask.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="环境温度">{{ currentTask.taskInfo?.temperature ?? '-' }} ℃</el-descriptions-item>
        <el-descriptions-item label="环境湿度">{{ currentTask.taskInfo?.humidity ?? '-' }} %</el-descriptions-item>
      </el-descriptions>

      <!-- 设备信息 -->
      <el-descriptions title="设备信息" :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="产品编号">{{ currentTask.deviceInfo?.productNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentTask.deviceInfo?.productName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentTask.deviceInfo?.model || '-' }}</el-descriptions-item>
        <el-descriptions-item label="制造商">{{ currentTask.deviceInfo?.manufacturer || '-' }}</el-descriptions-item>
      </el-descriptions>

      <!-- 检测明细 -->
      <div class="section-title">检测明细数据</div>
      <el-table :data="currentTask.resultList" border>
        <el-table-column prop="phase" label="相别" width="80">
          <template #default="{ row }">
            <span style="text-transform: uppercase; font-weight: bold">{{ row.phase }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="valF" label="f(%)" width="120" />
        <el-table-column prop="valDelta" label="δ(分)" width="120" />
        <el-table-column prop="valDu" label="dU(%)" width="120" />
        <el-table-column prop="valUpt" label="Upt" width="120" />
        <el-table-column prop="valUyb" label="Uyb" width="120" />
        <el-table-column prop="isPass" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isPass === 1" type="success" size="small">合格</el-tag>
            <el-tag v-else type="danger" size="small">不合格</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="warning" link @click="handleOpenCorrect(row)">修正</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 修正弹窗 -->
    <el-dialog v-model="correctDialogVisible" title="数据修正" width="500px">
      <el-form label-width="100px">
        <el-form-item label="相别">
          <span style="text-transform: uppercase; font-weight: bold">{{ correctForm.phase }}</span>
        </el-form-item>
        <el-form-item label="f(%)">
          <el-input-number v-model="correctForm.valF" :precision="4" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="δ(分)">
          <el-input-number v-model="correctForm.valDelta" :precision="3" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="dU(%)">
          <el-input-number v-model="correctForm.valDu" :precision="4" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Upt">
          <el-input-number v-model="correctForm.valUpt" :precision="3" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Uyb">
          <el-input-number v-model="correctForm.valUyb" :precision="3" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="修正原因" required>
          <el-input
            v-model="correctForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请填写修正原因（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="correctDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="correctLoading" @click="handleSubmitCorrect">确认修正</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-correct {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
