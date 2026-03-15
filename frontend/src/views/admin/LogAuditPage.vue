<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchLogPage } from '@/api/log'
import { Search, Refresh } from '@element-plus/icons-vue'

// 查询参数
const query = reactive({
  page: 1,
  size: 10,
  username: '',
  startDate: '',
  endDate: '',
})

// 分页数据
const total = ref(0)
const loading = ref(false)
const tableData = ref([])

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const data = await fetchLogPage({
      page: query.page,
      size: query.size,
      username: query.username || undefined,
      startDate: query.startDate || undefined,
      endDate: query.endDate || undefined,
    })
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 错误已在拦截器中提示
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  query.page = 1
  getList()
}

// 重置
const handleReset = () => {
  query.username = ''
  query.startDate = ''
  query.endDate = ''
  handleSearch()
}

// 分页切换
const handlePageChange = (page) => {
  query.page = page
  getList()
}

const handleSizeChange = (size) => {
  query.size = size
  query.page = 1
  getList()
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-log">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">日志审计</h2>
      <p class="page-desc">查看系统操作日志记录</p>
    </div>

    <!-- 筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="操作人">
          <el-input
            v-model="query.username"
            placeholder="模糊查询"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="操作日期">
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
          <span class="card-title">日志列表</span>
          <span class="record-count">共 {{ total }} 条记录</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="username" label="操作人" width="100" align="center" />
        <el-table-column prop="operation" label="操作描述" width="140" />
        <el-table-column prop="method" label="方法路径" min-width="280" show-overflow-tooltip />
        <el-table-column prop="params" label="请求参数" min-width="180" show-overflow-tooltip />
        <el-table-column prop="time" label="耗时(ms)" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.time > 1000 ? 'warning' : 'success'" size="small">
              {{ row.time }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="createTime" label="操作时间" width="170" />
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
.page-log {
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
  gap: 16px;
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
