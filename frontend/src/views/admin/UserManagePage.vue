<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUserPage, createUser, updateUserStatus, resetUserPassword } from '@/api/user'
import { Search, Refresh, Plus, Key } from '@element-plus/icons-vue'

const query = reactive({
  page: 1,
  size: 10,
  username: '',
  role: undefined,
  status: undefined,
})

const total = ref(0)
const loading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref()
const form = reactive({
  username: '',
  realName: '',
  role: 1,
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const getList = async () => {
  loading.value = true
  try {
    const data = await fetchUserPage({
      page: query.page,
      size: query.size,
      username: query.username || undefined,
      role: query.role,
      status: query.status,
    })
    tableData.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  getList()
}

const handleReset = () => {
  query.username = ''
  query.role = undefined
  query.status = undefined
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

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.username = ''
  form.realName = ''
  form.role = 1
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await createUser(form)
      ElMessage.success('新增用户成功，默认密码为 Cug@2025')
      dialogVisible.value = false
      getList()
    } catch (e) {
      console.error(e)
    }
  })
}

const handleStatusChange = async (row) => {
  try {
    await updateUserStatus({ id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (e) {
    console.error(e)
  }
}

const handleResetPwd = (row) => {
  ElMessageBox.confirm(`确认重置用户【${row.username}】的密码为默认值吗？`, '提示', {
    type: 'warning',
  })
    .then(async () => {
      await resetUserPassword({ id: row.id })
      ElMessage.success('密码已重置为默认值 Cug@2025')
    })
    .catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-user">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">用户账号管理</h2>
      <p class="page-desc">管理系统用户账号和权限</p>
    </div>

    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="账号">
          <el-input v-model="query.username" clearable placeholder="请输入账号" style="width: 150px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" clearable placeholder="全部" style="width: 120px">
            <el-option :value="0" label="管理员" />
            <el-option :value="1" label="检测员" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-buttons">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">用户列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="账号" width="160" />
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="role" label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.role === 0" type="danger" effect="light">管理员</el-tag>
            <el-tag v-else type="info" effect="light">检测员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="140" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
              @change="() => handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="warning" :icon="Key" link @click="handleResetPwd(row)">重置密码</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" autocomplete="off" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" autocomplete="off" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option :value="0" label="管理员" />
            <el-option :value="1" label="检测员" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-alert
            title="新增用户默认密码为：Cug@2025"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-user {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>


