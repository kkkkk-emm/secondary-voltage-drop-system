<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchDevicePage, createDevice, updateDevice, deleteDevice } from '@/api/device'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'

// 查询参数（分页 + 筛选）
const query = reactive({
  page: 1,
  size: 10,
  productNo: '',
  productName: '',
  manufacturer: '',
})

// 分页数据
const total = ref(0)
const loading = ref(false)
const tableData = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')

// 表单数据
const form = reactive({
  id: null,
  productNo: '',
  productName: '',
  model: '',
  manufacturer: '',
  placeOrigin: '',
  productionDate: '',
})

const formRef = ref()

// 表单校验规则（根据接口文档必填项）
const rules = {
  productNo: [{ required: true, message: '请输入产品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.productNo = ''
  form.productName = ''
  form.model = ''
  form.manufacturer = ''
  form.placeOrigin = ''
  form.productionDate = ''
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const data = await fetchDevicePage({
      page: query.page,
      size: query.size,
      productNo: query.productNo || undefined,
      productName: query.productName || undefined,
      manufacturer: query.manufacturer || undefined,
    })
    // 按接口文档：data -> { total, size, current, pages, records }
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

// 重置搜索
const handleReset = () => {
  query.productNo = ''
  query.productName = ''
  query.manufacturer = ''
  handleSearch()
}

// 分页切换
const handlePageChange = (page) => {
  query.page = page
  getList()
}

// 每页数量变化
const handleSizeChange = (size) => {
  query.size = size
  query.page = 1
  getList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增设备'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑设备'
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除设备【${row.productName}】吗？`, '提示', {
    type: 'warning',
  })
    .then(async () => {
      await deleteDevice(row.id)
      ElMessage.success('删除成功')
      getList()
    })
    .catch(() => {})
}

// 提交表单（新增 / 编辑）
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (form.id) {
        await updateDevice(form)
        ElMessage.success('修改成功')
      } else {
        await createDevice(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getList()
    } catch (e) {
      // 错误已在拦截器中提示
    }
  })
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-device">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">被检设备管理</h2>
      <p class="page-desc">管理和维护所有被检设备信息</p>
    </div>

    <!-- 筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="产品编号">
          <el-input
            v-model="query.productNo"
            placeholder="精确查询"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input
            v-model="query.productName"
            placeholder="模糊查询"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="制造商">
          <el-input
            v-model="query.manufacturer"
            placeholder="模糊查询"
            clearable
            style="width: 180px"
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
          <span class="card-title">设备列表</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增设备</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="productNo" label="产品编号" width="140" />
        <el-table-column prop="productName" label="产品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="manufacturer" label="制造商" min-width="140" show-overflow-tooltip />
        <el-table-column prop="placeOrigin" label="产地" width="100" />
        <el-table-column prop="productionDate" label="生产日期" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" :icon="Edit" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" :icon="Delete" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="产品编号" prop="productNo">
              <el-input v-model="form.productNo" placeholder="请输入唯一产品编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="产品名称" prop="productName">
              <el-input v-model="form.productName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号">
              <el-input v-model="form.model" placeholder="请输入型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产地">
              <el-input v-model="form.placeOrigin" placeholder="请输入产地" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="制造商">
              <el-input v-model="form.manufacturer" placeholder="请输入制造商" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生产日期">
              <el-date-picker
                v-model="form.productionDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择生产日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-device {
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


