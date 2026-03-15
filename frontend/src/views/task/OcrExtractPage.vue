<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { extractOcr } from '@/api/ocr'

const loading = ref(false)
const result = ref(null)

const BASIC_KEYS = [
  '一次电压',
  'tanφ',
  '计量点编号',
  '温度',
  '测试日期',
  '湿度',
  'r%',
]

const MATRIX_ITEMS = ['f', 'd', 'dU', 'Upt:U', 'Uyb:U']
const PHASES = ['AO', 'BO', 'CO']

const ALL_DETAILED_KEYS = [
  ...BASIC_KEYS,
  'AO相别f',
  'BO相别f',
  'CO相别f',
  'AO相别d',
  'BO相别d',
  'CO相别d',
  'AO相别dU',
  'BO相别dU',
  'CO相别dU',
  'AO相别Upt:U',
  'BO相别Upt:U',
  'CO相别Upt:U',
  'AO相别Uyb:U',
  'BO相别Uyb:U',
  'CO相别Uyb:U',
]

const LEGACY_FALLBACK = {
  计量点编号: '设备编号',
  测试日期: '试验日期',
  AO相别dU: 'A相压降',
  BO相别dU: 'B相压降',
  CO相别dU: 'C相压降',
}

const detailedPairs = computed(() => {
  const detailed = result.value?.detailedPairs || {}
  const legacy = result.value?.pairs || {}
  const merged = {}
  ALL_DETAILED_KEYS.forEach((key) => {
    const detailedValue = detailed[key]
    if (hasValue(detailedValue)) {
      merged[key] = String(detailedValue).trim()
      return
    }
    const fallbackKey = LEGACY_FALLBACK[key]
    if (fallbackKey && hasValue(legacy[fallbackKey])) {
      merged[key] = String(legacy[fallbackKey]).trim()
      return
    }
    merged[key] = ''
  })
  return merged
})

const basicRows = computed(() =>
  BASIC_KEYS.map((key) => ({
    key,
    value: detailedPairs.value[key] || '',
  })),
)

const matrixRows = computed(() =>
  MATRIX_ITEMS.map((item) => ({
    item,
    ao: detailedPairs.value[buildPhaseKey(PHASES[0], item)] || '',
    bo: detailedPairs.value[buildPhaseKey(PHASES[1], item)] || '',
    co: detailedPairs.value[buildPhaseKey(PHASES[2], item)] || '',
  })),
)

const hasValue = (value) => value !== undefined && value !== null && String(value).trim() !== ''

const buildPhaseKey = (phase, item) => `${phase}相别${item}`

const rawOcrText = computed(() => {
  if (!result.value?.rawOcr) return ''
  try {
    return JSON.stringify(result.value.rawOcr, null, 2)
  } catch (e) {
    return String(result.value.rawOcr)
  }
})

const beforeUpload = (file) => {
  const name = (file.name || '').toLowerCase()
  const isImage = name.endsWith('.jpg') || name.endsWith('.jpeg') || name.endsWith('.png')
  if (!isImage) {
    ElMessage.error('仅支持 jpg/jpeg/png 格式图片')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 <= 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  loading.value = true
  result.value = null
  try {
    result.value = await extractOcr(options.file)
    ElMessage.success('识别成功')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page-ocr">
    <el-card v-loading="loading">
      <div class="title">图片 OCR 提取</div>

      <el-alert type="info" :closable="false" show-icon class="mb-16">
        <template #title>
          上传 jpg/jpeg/png 图片，系统将执行：百度 OCR -> 规则提取 + 大模型兜底补全
        </template>
      </el-alert>

      <el-upload
        class="upload-area"
        drag
        :auto-upload="true"
        :show-file-list="false"
        :http-request="handleUpload"
        :before-upload="beforeUpload"
        accept=".jpg,.jpeg,.png"
        :disabled="loading"
      >
        <el-icon class="el-icon--upload" :size="56"><Upload /></el-icon>
        <div class="el-upload__text">
          将图片拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">支持 jpg / jpeg / png，单文件不超过 5MB</div>
        </template>
      </el-upload>

      <div v-if="result" class="result-wrapper">
        <el-divider>抽取结果</el-divider>

        <el-row :gutter="16">
          <el-col :span="24">
            <div class="section-title">OCR 原文</div>
            <el-input
              :model-value="result.ocrText || ''"
              type="textarea"
              :rows="10"
              resize="none"
              readonly
            />
          </el-col>
        </el-row>

        <el-row :gutter="16" class="mt-16">
          <el-col :span="24">
            <div class="section-title">基础信息</div>
            <el-table :data="basicRows" border>
              <el-table-column prop="key" label="字段" width="160" />
              <el-table-column prop="value" label="识别值" min-width="280" show-overflow-tooltip />
            </el-table>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="mt-16">
          <el-col :span="24">
            <div class="section-title">三相矩阵</div>
            <el-table :data="matrixRows" border>
              <el-table-column prop="item" label="项目" width="140" />
              <el-table-column prop="ao" label="AO" min-width="180" show-overflow-tooltip />
              <el-table-column prop="bo" label="BO" min-width="180" show-overflow-tooltip />
              <el-table-column prop="co" label="CO" min-width="180" show-overflow-tooltip />
            </el-table>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="mt-16">
          <el-col :span="24">
            <el-collapse>
              <el-collapse-item title="查看百度 OCR 原始返回（调试）" name="raw">
                <pre class="raw-json">{{ rawOcrText }}</pre>
              </el-collapse-item>
            </el-collapse>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-ocr {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
}

.mb-16 {
  margin-bottom: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  width: 100%;
  padding: 36px 20px;
}

.result-wrapper {
  margin-top: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.raw-json {
  margin: 0;
  padding: 12px;
  max-height: 320px;
  overflow: auto;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
