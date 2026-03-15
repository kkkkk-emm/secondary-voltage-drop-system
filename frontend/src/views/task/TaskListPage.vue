<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchTaskPage } from '@/api/task'
import { useRouter } from 'vue-router'
import { Search, Refresh, View } from '@element-plus/icons-vue'

const router = useRouter()
const query = reactive({
  page: 1,
  size: 10,
  taskId: '',
  deviceProductNo: '',
  operatorName: '',
  startDate: '',
  endDate: '',
  isPass: undefined,
})

const total = ref(0)
const loading = ref(false)
const tableData = ref([])

const getList = async () => {
  loading.value = true
  try {
    const data = await fetchTaskPage({
      page: query.page,
      size: query.size,
      taskId: query.taskId || undefined,
      deviceProductNo: query.deviceProductNo || undefined,
      operatorName: query.operatorName || undefined,
      startDate: query.startDate || undefined,
      endDate: query.endDate || undefined,
      isPass: query.isPass,
    })
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  getList()
}

const handleReset = () => {
  query.taskId = ''
  query.deviceProductNo = ''
  query.operatorName = ''
  query.startDate = ''
  query.endDate = ''
  query.isPass = undefined
  handleSearch()
}

const handlePageChange = (page) => {
  query.page = page
  getList()
}

const handleSizeChange = (size) => {
  query.size = size
  query.page = 1
  getList()
}

const goDetail = (row) => {
  router.push(`/task/detail/${row.id}`)
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-task-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">检定任务列表</h2>
      <p class="page-desc">查看和管理所有检定任务记录</p>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="任务ID">
          <el-input v-model="query.taskId" placeholder="精确查询" clearable style="width: 100px" />
        </el-form-item>
        <el-form-item label="设备编号">
          <el-input v-model="query.deviceProductNo" placeholder="模糊查询" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="检定员">
          <el-input v-model="query.operatorName" placeholder="模糊查询" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="结论">
          <el-select v-model="query.isPass" clearable placeholder="全部" style="width: 100px">
            <el-option :value="1" label="合格" />
            <el-option :value="0" label="不合格" />
          </el-select>
        </el-form-item>
        <el-form-item label="检定日期">
          <el-date-picker
            v-model="query.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width: 140px"
          />
          <span style="margin: 0 8px; color: #999">至</span>
          <el-date-picker
            v-model="query.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item class="search-buttons">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">任务记录</span>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="任务ID" width="90" align="center" />
        <el-table-column prop="deviceProductNo" label="设备编号" width="140" />
        <el-table-column prop="deviceProductName" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="检定员" width="100" align="center" />
        <el-table-column prop="meterPointId" label="计量点" width="140" />
        <el-table-column prop="testDate" label="检定时间" width="170" />
        <el-table-column prop="result" label="检定结论" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'" effect="light">
              {{ row.result === 1 ? '合格' : '不合格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" :icon="View" link @click="goDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-task-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  margin-bottom: 8px;
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

.search-card {
  background: #fff;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 0;
}

.search-buttons {
  margin-left: auto;
}

.table-card {
  background: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.record-count {
  font-size: 13px;
  color: #999;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>


