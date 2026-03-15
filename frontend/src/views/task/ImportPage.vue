<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download, Document, SuccessFilled, CircleCloseFilled, View, Check, Close } from '@element-plus/icons-vue'
import { previewTasks, previewDevices, importTasks, importDevices } from '@/api/import'

const activeTab = ref('task')
const uploading = ref(false)
const importing = ref(false)

// 预览相关状态
const previewVisible = ref(false)
const previewData = ref(null)
const currentFile = ref(null)

// 导入结果
const importResult = ref(null)

// 文件上传前校验
const beforeUpload = (file) => {
  const isExcel = file.name.endsWith('.xlsx')
  if (!isExcel) {
    ElMessage.error('仅支持 .xlsx 格式的 Excel 文件')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }

  return true
}

// 处理任务文件选择 - 先预览
const handleTaskFileSelect = async (options) => {
  uploading.value = true
  previewData.value = null
  importResult.value = null
  currentFile.value = options.file

  try {
    const result = await previewTasks(options.file)
    previewData.value = result
    previewVisible.value = true
  } catch (e) {
    console.error(e)
  } finally {
    uploading.value = false
  }
}

// 处理设备文件选择 - 先预览
const handleDeviceFileSelect = async (options) => {
  uploading.value = true
  previewData.value = null
  importResult.value = null
  currentFile.value = options.file

  try {
    const result = await previewDevices(options.file)
    previewData.value = result
    previewVisible.value = true
  } catch (e) {
    console.error(e)
  } finally {
    uploading.value = false
  }
}

// 确认导入
const handleConfirmImport = async () => {
  if (!currentFile.value) return

  importing.value = true
  try {
    let result
    if (activeTab.value === 'task') {
      result = await importTasks(currentFile.value)
    } else {
      result = await importDevices(currentFile.value)
    }

    importResult.value = result
    previewVisible.value = false

    if (result.successRows > 0) {
      ElMessage.success(`成功导入 ${result.successRows} 条数据`)
    }
    if (result.failedRows > 0) {
      ElMessage.warning(`${result.failedRows} 条数据导入失败，请查看详情`)
    }
  } catch (e) {
    console.error(e)
  } finally {
    importing.value = false
  }
}

// 取消预览
const handleCancelPreview = () => {
  previewVisible.value = false
  previewData.value = null
  currentFile.value = null
}

// 下载任务导入模板
const downloadTaskTemplate = () => {
  const headers = [
    '设备编号', '计量点编号', '送检日期', '测试日期', '温度', '湿度', 'tanφ', 'r%',
    '相别', '项目类型', '档位', '负载%', 'f(%)', 'δ(分)', 'dU(%)', 'Upt', 'Uyb'
  ]
  const exampleRow = [
    'YJ-2023-A001', 'MP-001', '2025-12-19', '2025-12-19 10:00:00', '23.0', '45.0', '0.5', '1.0',
    'ao', 'PT1', '100V', '100%', '0.02', '1.5', '0.01', '55', '60'
  ]
  downloadTemplate(headers, exampleRow, '检定任务导入模板.xlsx')
}

// 下载设备导入模板
const downloadDeviceTemplate = () => {
  const headers = ['产品编号', '产品名称', '型号', '制造商', '产地', '生产日期']
  const exampleRow = ['YJ-2024-B001', '三相二次压降测试仪', 'HG-9800', '武汉高压研究所', '湖北武汉', '2024-01-15']
  downloadTemplate(headers, exampleRow, '设备导入模板.xlsx')
}

// 生成并下载 Excel 模板
const downloadTemplate = (headers, exampleRow, filename) => {
  ElMessageBox.alert(
    `<div style="line-height: 1.8">
      <p><strong>Excel 模板格式说明：</strong></p>
      <p>第一行（表头）：</p>
      <code style="background: #f5f5f5; padding: 4px 8px; border-radius: 4px; display: block; margin: 8px 0; overflow-x: auto;">
        ${headers.join(' | ')}
      </code>
      <p>数据行示例：</p>
      <code style="background: #f5f5f5; padding: 4px 8px; border-radius: 4px; display: block; margin: 8px 0; overflow-x: auto;">
        ${exampleRow.join(' | ')}
      </code>
      <p style="color: #909399; margin-top: 12px;">
        <i class="el-icon-info"></i> 请在 Excel 中创建 .xlsx 文件，表头必须完全一致
      </p>
    </div>`,
    '模板格式',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '我知道了',
      customClass: 'template-dialog',
    }
  )
}

// 清除结果
const clearResult = () => {
  importResult.value = null
  previewData.value = null
  currentFile.value = null
}

// 获取行的错误信息
const getRowErrors = (rowIndex) => {
  if (!previewData.value || !previewData.value.validationErrors) return []
  const actualRowNum = rowIndex + 2 // 因为 Excel 从 1 开始，且第 1 行是表头
  return previewData.value.validationErrors.filter(e => e.rowNum === actualRowNum)
}

// 判断单元格是否有错误
const hasCellError = (rowIndex, columnName) => {
  const errors = getRowErrors(rowIndex)
  return errors.some(e => e.columnName === columnName)
}

// 获取单元格错误信息
const getCellErrorMessage = (rowIndex, columnName) => {
  const errors = getRowErrors(rowIndex)
  const error = errors.find(e => e.columnName === columnName)
  return error ? error.message : ''
}
</script>

<template>
  <div class="page-import">
    <el-card>
      <div class="title">Excel 数据导入</div>

      <el-tabs v-model="activeTab" @tab-change="clearResult">
        <!-- 检定任务导入 -->
        <el-tab-pane label="导入检定任务" name="task">
          <div class="import-section">
            <div class="tips">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  <span>导入检定任务数据，Excel 必须包含以下列：</span>
                </template>
                <template #default>
                  <div class="tip-content">
                    <p><strong>基本信息：</strong>设备编号、计量点编号、送检日期、测试日期、温度、湿度、tanφ、r%</p>
                    <p><strong>检测数据：</strong>相别(ao/bo/co)、项目类型(PT1/PT2/CT1/CT2)、档位、负载%、f(%)、δ(分)、dU(%)、Upt、Uyb</p>
                    <p class="note">注意：每行对应一个相别的检测数据，同一任务的三个相别需要分三行录入</p>
                  </div>
                </template>
              </el-alert>
            </div>

            <div class="actions">
              <el-button :icon="Download" @click="downloadTaskTemplate">查看模板格式</el-button>
            </div>

            <el-upload
              class="upload-area"
              drag
              :auto-upload="true"
              :show-file-list="false"
              :http-request="handleTaskFileSelect"
              :before-upload="beforeUpload"
              accept=".xlsx"
              :disabled="uploading"
            >
              <el-icon class="el-icon--upload" :size="60"><Upload /></el-icon>
              <div class="el-upload__text">
                将 Excel 文件拖到此处，或 <em>点击选择文件</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">仅支持 .xlsx 格式，文件大小不超过 10MB，选择后将先预览数据</div>
              </template>
            </el-upload>
          </div>
        </el-tab-pane>

        <!-- 设备导入 -->
        <el-tab-pane label="导入设备" name="device">
          <div class="import-section">
            <div class="tips">
              <el-alert type="info" :closable="false" show-icon>
                <template #title>
                  <span>导入设备数据，Excel 必须包含以下列：</span>
                </template>
                <template #default>
                  <div class="tip-content">
                    <p><strong>必填：</strong>产品编号（唯一）、产品名称</p>
                    <p><strong>选填：</strong>型号、制造商、产地、生产日期</p>
                  </div>
                </template>
              </el-alert>
            </div>

            <div class="actions">
              <el-button :icon="Download" @click="downloadDeviceTemplate">查看模板格式</el-button>
            </div>

            <el-upload
              class="upload-area"
              drag
              :auto-upload="true"
              :show-file-list="false"
              :http-request="handleDeviceFileSelect"
              :before-upload="beforeUpload"
              accept=".xlsx"
              :disabled="uploading"
            >
              <el-icon class="el-icon--upload" :size="60"><Upload /></el-icon>
              <div class="el-upload__text">
                将 Excel 文件拖到此处，或 <em>点击选择文件</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">仅支持 .xlsx 格式，文件大小不超过 10MB，选择后将先预览数据</div>
              </template>
            </el-upload>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 导入结果展示 -->
      <div v-if="importResult" class="import-result">
        <el-divider>导入结果</el-divider>

        <el-row :gutter="20" class="stats">
          <el-col :span="8">
            <el-statistic title="总行数" :value="importResult.totalRows">
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="成功" :value="importResult.successRows" class="success-stat">
              <template #prefix>
                <el-icon color="#67C23A"><SuccessFilled /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="失败" :value="importResult.failedRows" class="fail-stat">
              <template #prefix>
                <el-icon color="#F56C6C"><CircleCloseFilled /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>

        <!-- 错误详情 -->
        <div v-if="importResult.errors && importResult.errors.length > 0" class="error-list">
          <el-alert type="error" title="错误详情" :closable="false" show-icon>
            <template #default>
              <el-scrollbar max-height="300px">
                <ul>
                  <li v-for="(err, index) in importResult.errors" :key="index">
                    <strong>第 {{ err.rowNum }} 行：</strong>{{ err.message }}
                  </li>
                </ul>
              </el-scrollbar>
            </template>
          </el-alert>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="uploading" class="loading-mask">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在解析 Excel 文件，请稍候...</p>
      </div>
    </el-card>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      :title="activeTab === 'task' ? '检定任务数据预览' : '设备数据预览'"
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <template v-if="previewData">
        <!-- 预览统计 -->
        <div class="preview-stats">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-tag type="info" size="large">
                共 {{ previewData.totalRows }} 行数据
              </el-tag>
            </el-col>
            <el-col :span="8">
              <el-tag :type="previewData.valid ? 'success' : 'danger'" size="large">
                {{ previewData.valid ? '校验通过，可以导入' : `发现 ${previewData.validationErrors.length} 个错误` }}
              </el-tag>
            </el-col>
            <el-col :span="8">
              <el-tag type="warning" size="large" v-if="previewData.totalRows > 100">
                仅展示前 100 行
              </el-tag>
            </el-col>
          </el-row>
        </div>

        <!-- 错误提示 -->
        <div v-if="!previewData.valid && previewData.validationErrors.length > 0" class="preview-errors">
          <el-alert type="error" :closable="false" show-icon>
            <template #title>数据校验未通过，请修正以下错误后重新上传：</template>
            <template #default>
              <el-scrollbar max-height="150px">
                <ul>
                  <li v-for="(err, index) in previewData.validationErrors" :key="index">
                    <strong>第 {{ err.rowNum }} 行 {{ err.columnName ? `[${err.columnName}]` : '' }}：</strong>
                    {{ err.message }}
                  </li>
                </ul>
              </el-scrollbar>
            </template>
          </el-alert>
        </div>

        <!-- 数据表格 -->
        <div class="preview-table">
          <el-table
            :data="previewData.rows"
            border
            stripe
            max-height="400"
            size="small"
            :row-class-name="({ rowIndex }) => getRowErrors(rowIndex).length > 0 ? 'error-row' : ''"
          >
            <el-table-column type="index" label="行号" width="60" :index="(i) => i + 2" />
            <el-table-column
              v-for="header in previewData.headers"
              :key="header"
              :prop="header"
              :label="header"
              min-width="100"
            >
              <template #default="{ row, $index }">
                <el-tooltip
                  v-if="hasCellError($index, header)"
                  :content="getCellErrorMessage($index, header)"
                  placement="top"
                  effect="dark"
                >
                  <span class="error-cell">{{ row[header] || '-' }}</span>
                </el-tooltip>
                <span v-else>{{ row[header] || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template #footer>
        <el-button @click="handleCancelPreview" :icon="Close">取消</el-button>
        <el-button
          type="primary"
          @click="handleConfirmImport"
          :loading="importing"
          :disabled="!previewData?.valid"
          :icon="Check"
        >
          确认导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading }
}
</script>

<style scoped>
.page-import {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #303133;
}

.import-section {
  padding: 20px 0;
}

.tips {
  margin-bottom: 20px;
}

.tip-content {
  line-height: 1.8;
  margin-top: 8px;
}

.tip-content p {
  margin: 4px 0;
}

.tip-content .note {
  color: #E6A23C;
  font-size: 13px;
  margin-top: 8px;
}

.actions {
  margin-bottom: 20px;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  padding: 40px 20px;
}

.import-result {
  margin-top: 20px;
}

.stats {
  text-align: center;
  margin-bottom: 20px;
}

.success-stat :deep(.el-statistic__number) {
  color: #67C23A;
}

.fail-stat :deep(.el-statistic__number) {
  color: #F56C6C;
}

.error-list {
  margin-top: 20px;
}

.error-list ul {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
}

.error-list li {
  padding: 6px 0;
  border-bottom: 1px dashed #eee;
  font-size: 13px;
}

.error-list li:last-child {
  border-bottom: none;
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.loading-mask p {
  margin-top: 16px;
  color: #606266;
}

:deep(.template-dialog) {
  max-width: 600px;
}

/* 预览样式 */
.preview-stats {
  margin-bottom: 16px;
}

.preview-errors {
  margin-bottom: 16px;
}

.preview-errors ul {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
}

.preview-errors li {
  padding: 4px 0;
  font-size: 13px;
}

.preview-table {
  margin-top: 16px;
}

:deep(.error-row) {
  background-color: #fef0f0 !important;
}

.error-cell {
  color: #F56C6C;
  font-weight: 500;
  border-bottom: 2px dashed #F56C6C;
  cursor: help;
}
</style>
