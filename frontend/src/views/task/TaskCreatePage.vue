<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submitTask } from '@/api/task'
import { fetchStandardGroups, matchAllStandard } from '@/api/standard'
import { fetchDeviceList } from '@/api/device'
import { extractOcr } from '@/api/ocr'

const formRef = ref()
const loading = ref(false)
const ocrLoading = ref(false)
const ocrPreviewVisible = ref(false)
const pendingOcrData = ref(null)

// 设备下拉列表
const deviceList = ref([])
const deviceLoading = ref(false)

// 分组后的标准配置列表（去重后）
const standardGroups = ref([])
const standardLoading = ref(false)
const standardTypeLock = ref('')

// 每行选中的标准组的完整阈值信息
const rowThresholds = reactive({
  ao: null,
  bo: null,
  co: null,
})

const form = reactive({
  deviceId: null,
  meterPointId: '',
  deliverDate: '',
  testDate: '',
  temperature: null,
  humidity: null,
  tanPhi: null,
  rPercent: null,
  resultList: [
    { phase: 'ao', standardGroupKey: null, valF: null, valDelta: null, valDu: null, valUpt: null, valUyb: null },
    { phase: 'bo', standardGroupKey: null, valF: null, valDelta: null, valDu: null, valUpt: null, valUyb: null },
    { phase: 'co', standardGroupKey: null, valF: null, valDelta: null, valDu: null, valUpt: null, valUyb: null },
  ],
})

const rules = {
  deviceId: [{ required: true, message: '请选择被检设备', trigger: 'change' }],
  meterPointId: [{ required: true, message: '请输入计量点编号', trigger: 'blur' }],
  deliverDate: [{ required: true, message: '请选择送检日期', trigger: 'change' }],
  testDate: [{ required: true, message: '请选择测试日期', trigger: 'change' }],
  temperature: [{ required: true, message: '请输入环境温度', trigger: 'blur' }],
  humidity: [{ required: true, message: '请输入环境湿度', trigger: 'blur' }],
  tanPhi: [{ required: true, message: '请输入 tanφ', trigger: 'blur' }],
  rPercent: [{ required: true, message: '请输入 r%', trigger: 'blur' }],
}

// 加载设备列表
const loadDeviceList = async () => {
  deviceLoading.value = true
  try {
    deviceList.value = await fetchDeviceList()
  } catch (e) {
    console.error('加载设备列表失败:', e)
  } finally {
    deviceLoading.value = false
  }
}

// 加载分组后的标准列表
const loadStandardGroups = async () => {
  standardLoading.value = true
  try {
    standardGroups.value = await fetchStandardGroups()
  } catch (e) {
    console.error('加载标准列表失败:', e)
  } finally {
    standardLoading.value = false
  }
}

// 根据 groupKey 获取标准组
const getGroupByKey = (groupKey) => {
  if (!groupKey) return null
  return standardGroups.value.find((g) => g.groupKey === groupKey)
}

const normalizeProjectType = (projectType) => String(projectType || '').toUpperCase()
const isPtProjectType = (projectType) => ['PT1', 'PT2'].includes(normalizeProjectType(projectType))
const isCtProjectType = (projectType) => ['CT1', 'CT2'].includes(normalizeProjectType(projectType))

const isStandardGroupDisabled = (group) => {
  if (!group) return false
  if (standardTypeLock.value === 'PT') {
    return isCtProjectType(group.projectType)
  }
  return false
}

// 当选择标准变化时，加载完整的阈值信息
const onStandardChange = async (row) => {
  const group = getGroupByKey(row.standardGroupKey)
  if (!group) {
    rowThresholds[row.phase] = null
    return
  }
  try {
    const detail = await matchAllStandard({
      projectType: group.projectType,
      gearLevel: group.gearLevel,
      loadPercent: group.loadPercent,
    })
    rowThresholds[row.phase] = detail
  } catch (e) {
    console.error('加载阈值失败:', e)
    rowThresholds[row.phase] = null
  }
}

// 判断该行是否是 PT 项目（PT 有5项，CT只有2项）
const isPTProject = (row) => {
  const thresholds = rowThresholds[row.phase]
  return thresholds?.isPT === true
}

// 获取某项的阈值范围
const getThresholdRange = (row, itemKey) => {
  const thresholds = rowThresholds[row.phase]
  if (!thresholds || !thresholds.thresholds) return null
  return thresholds.thresholds[itemKey.toLowerCase()]
}

// 校验单个数据项是否合格
const checkItemPass = (row, itemKey, value) => {
  const range = getThresholdRange(row, itemKey)
  if (!range || range.min == null || range.max == null) return null
  if (value == null || value === '') return null
  const val = parseFloat(value)
  const min = parseFloat(range.min)
  const max = parseFloat(range.max)
  return val >= min && val <= max
}

// 综合校验整行是否合格（所有数据项都必须在各自阈值范围内）
const checkRowPass = (row) => {
  const thresholds = rowThresholds[row.phase]
  if (!thresholds || !thresholds.thresholds) return null

  const isPT = thresholds.isPT

  // PT项目需要校验5项：f, delta, du, upt, uyb
  // CT项目只需校验2项：f, delta
  const itemsToCheck = isPT
    ? ['f', 'delta', 'du', 'upt', 'uyb']
    : ['f', 'delta']

  const fieldMap = {
    f: 'valF',
    delta: 'valDelta',
    du: 'valDu',
    upt: 'valUpt',
    uyb: 'valUyb',
  }

  let hasValue = false
  let allPass = true

  for (const item of itemsToCheck) {
    const field = fieldMap[item]
    const value = row[field]
    const range = thresholds.thresholds[item]

    if (!range || range.min == null || range.max == null) {
      // 没有配置该项阈值，跳过
      continue
    }

    if (value == null || value === '') {
      // 数据未填写，暂时不判断
      continue
    }

    hasValue = true
    const val = parseFloat(value)
    const min = parseFloat(range.min)
    const max = parseFloat(range.max)

    if (val < min || val > max) {
      allPass = false
    }
  }

  if (!hasValue) return null
  return allPass
}

// 获取行的样式类名
const getRowClassName = ({ row }) => {
  const pass = checkRowPass(row)
  if (pass === null) return ''
  return pass ? 'row-pass' : 'row-fail'
}

// 获取单元格配色样式（根据各项阈值分别判断）
const getCellStyle = (row, field) => {
  const itemMap = {
    valF: 'f',
    valDelta: 'delta',
    valDu: 'du',
    valUpt: 'upt',
    valUyb: 'uyb',
  }
  const itemKey = itemMap[field]
  if (!itemKey) return {}

  const pass = checkItemPass(row, itemKey, row[field])
  if (pass === null) return {}

  if (pass) {
    return { color: '#67C23A', fontWeight: 'bold' }
  } else {
    return { color: '#F56C6C', fontWeight: 'bold' }
  }
}

// 格式化显示阈值范围
const formatThresholdRange = (row, itemKey) => {
  const range = getThresholdRange(row, itemKey)
  if (!range || range.min == null || range.max == null) return '-'
  return `[${range.min}, ${range.max}]`
}

// 获取选中的设备信息
const selectedDevice = computed(() => {
  if (!form.deviceId) return null
  return deviceList.value.find((d) => d.id === form.deviceId)
})

const previewOcrText = computed(() => String(pendingOcrData.value?.ocrText || '').trim())

const previewExtractedPairsText = computed(() => {
  const ocrData = pendingOcrData.value || {}
  const detailedPairs = ocrData.detailedPairs || {}
  const legacyPairs = ocrData.pairs || {}
  const pairs = Object.keys(detailedPairs).length > 0 ? detailedPairs : legacyPairs
  try {
    return JSON.stringify(pairs, null, 2)
  } catch (e) {
    return '{}'
  }
})

const beforeOcrUpload = (file) => {
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

const hasValue = (value) => value !== undefined && value !== null && String(value).trim() !== ''

const getDetailedValue = (ocrData, key) => {
  const detailedPairs = ocrData?.detailedPairs || {}
  const value = detailedPairs[key]
  return hasValue(value) ? String(value).trim() : ''
}

const getLegacyValue = (ocrData, key) => {
  const pairs = ocrData?.pairs || {}
  const value = pairs[key]
  return hasValue(value) ? String(value).trim() : ''
}

const parseNumber = (rawValue) => {
  if (!hasValue(rawValue)) return null
  const normalized = String(rawValue).replace(/\s+/g, '')
  const matched = normalized.match(/[-+]?\d+(\.\d+)?/)
  if (!matched) return null
  const parsed = Number(matched[0])
  return Number.isFinite(parsed) ? parsed : null
}

const pad2 = (n) => String(n).padStart(2, '0')

const formatTodayDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = pad2(now.getMonth() + 1)
  const day = pad2(now.getDate())
  return `${year}-${month}-${day}`
}

const normalizeTestDateTime = (rawValue) => {
  if (!hasValue(rawValue)) return ''
  const text = String(rawValue).trim()

  let match = text.match(/^(\d{4})(\d{2})(\d{2})$/)
  if (match) {
    return `${match[1]}-${match[2]}-${match[3]} 00:00:00`
  }

  match = text.match(/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})$/)
  if (match) {
    return `${match[1]}-${match[2]}-${match[3]} ${match[4]}:${match[5]}:${match[6]}`
  }

  match = text.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})$/)
  if (match) {
    return `${match[1]}-${pad2(match[2])}-${pad2(match[3])} 00:00:00`
  }

  match = text.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})\s+(\d{1,2}):(\d{1,2})(?::(\d{1,2}))?$/)
  if (match) {
    const second = match[6] || '00'
    return `${match[1]}-${pad2(match[2])}-${pad2(match[3])} ${pad2(match[4])}:${pad2(match[5])}:${pad2(second)}`
  }

  return ''
}

const hasExistingFormDataForOcrFill = () => {
  if (hasValue(form.meterPointId) || hasValue(form.deliverDate) || hasValue(form.testDate)) return true
  if (form.temperature !== null || form.humidity !== null || form.tanPhi !== null || form.rPercent !== null) return true

  return form.resultList.some((row) =>
    hasValue(row.standardGroupKey) ||
    row.valF !== null ||
    row.valDelta !== null ||
    row.valDu !== null ||
    row.valUpt !== null ||
    row.valUyb !== null,
  )
}

const detectSideFromOcrText = (ocrText) => {
  const text = String(ocrText || '').replace(/\s+/g, '')
  if (/PT侧/i.test(text)) return 'PT'
  if (/CT侧/i.test(text)) return 'CT'
  return ''
}

const fillResultRowsFromOcr = (ocrData) => {
  const legacyDuKeyMap = {
    ao: 'A相压降',
    bo: 'B相压降',
    co: 'C相压降',
  }

  form.resultList.forEach((row) => {
    const phasePrefix = row.phase.toUpperCase()
    const valF = parseNumber(getDetailedValue(ocrData, `${phasePrefix}相别f`))
    const valDelta = parseNumber(getDetailedValue(ocrData, `${phasePrefix}相别d`))
    const duRaw = getDetailedValue(ocrData, `${phasePrefix}相别dU`) || getLegacyValue(ocrData, legacyDuKeyMap[row.phase])
    const valDu = parseNumber(duRaw)
    const valUpt = parseNumber(getDetailedValue(ocrData, `${phasePrefix}相别Upt:U`))
    const valUyb = parseNumber(getDetailedValue(ocrData, `${phasePrefix}相别Uyb:U`))

    row.valF = valF
    row.valDelta = valDelta
    row.valDu = valDu
    row.valUpt = valUpt
    row.valUyb = valUyb
  })
}

const detectFiveItemsFromResultRows = () => {
  const hasDu = form.resultList.some((row) => row.valDu !== null)
  const hasUpt = form.resultList.some((row) => row.valUpt !== null)
  const hasUyb = form.resultList.some((row) => row.valUyb !== null)
  return hasDu && hasUpt && hasUyb
}

const applyPtLockAndSelectDefault = async () => {
  if (!standardGroups.value.length) {
    await loadStandardGroups()
  }

  standardTypeLock.value = 'PT'

  const pt1Group = standardGroups.value.find((g) => normalizeProjectType(g.projectType) === 'PT1')
  const fallbackPtGroup = standardGroups.value.find((g) => isPtProjectType(g.projectType))
  const targetGroup = pt1Group || fallbackPtGroup

  if (!targetGroup) {
    ElMessage.warning('检测到5项数据，但未找到PT标准，请检查标准组配置')
    return
  }

  for (const row of form.resultList) {
    row.standardGroupKey = targetGroup.groupKey
  }
  for (const row of form.resultList) {
    await onStandardChange(row)
  }
  ElMessage.success('已检测到5项，标准已默认选择PT1，CT1/CT2已禁用')
}

const tryAutoSelectUniqueStandard = async (ocrText) => {
  if (!standardGroups.value.length) {
    await loadStandardGroups()
  }

  let candidates = standardGroups.value
  const side = detectSideFromOcrText(ocrText)

  if (side === 'PT') {
    candidates = candidates.filter((g) => ['PT1', 'PT2'].includes((g.projectType || '').toUpperCase()))
  } else if (side === 'CT') {
    candidates = candidates.filter((g) => ['CT1', 'CT2'].includes((g.projectType || '').toUpperCase()))
  }

  if (candidates.length !== 1) {
    ElMessage.info('未能唯一匹配检定标准，请手动选择')
    return
  }

  const uniqueGroup = candidates[0]
  for (const row of form.resultList) {
    row.standardGroupKey = uniqueGroup.groupKey
  }
  for (const row of form.resultList) {
    await onStandardChange(row)
  }
  ElMessage.success(`已自动匹配唯一标准：${uniqueGroup.groupKey}`)
}

const applyOcrToForm = async (ocrData) => {
  const meterPointId = getDetailedValue(ocrData, '计量点编号') || getLegacyValue(ocrData, '设备编号')
  const testDateRaw = getDetailedValue(ocrData, '测试日期') || getLegacyValue(ocrData, '试验日期')

  form.meterPointId = meterPointId
  form.testDate = normalizeTestDateTime(testDateRaw)
  form.deliverDate = formatTodayDate()
  form.temperature = parseNumber(getDetailedValue(ocrData, '温度'))
  form.humidity = parseNumber(getDetailedValue(ocrData, '湿度'))
  form.tanPhi = parseNumber(getDetailedValue(ocrData, 'tanφ'))
  form.rPercent = parseNumber(getDetailedValue(ocrData, 'r%'))

  fillResultRowsFromOcr(ocrData)
  if (detectFiveItemsFromResultRows()) {
    await applyPtLockAndSelectDefault()
  } else {
    standardTypeLock.value = ''
    await tryAutoSelectUniqueStandard(ocrData?.ocrText || '')
  }
  formRef.value?.clearValidate?.()
}

const openOcrPreview = (ocrData) => {
  pendingOcrData.value = ocrData || null
  ocrPreviewVisible.value = true
}

const closeOcrPreview = () => {
  ocrPreviewVisible.value = false
}

const handleConfirmOcrFill = async () => {
  if (!pendingOcrData.value) {
    ElMessage.warning('暂无可回填的OCR结果')
    return
  }
  try {
    await applyOcrToForm(pendingOcrData.value)
    closeOcrPreview()
    pendingOcrData.value = null
    ElMessage.success('OCR回填完成，请核对后提交')
  } catch (e) {
    console.error(e)
  }
}

const handleCancelOcrFill = () => {
  closeOcrPreview()
  pendingOcrData.value = null
  ElMessage.info('已取消本次OCR回填')
}

const handleOcrUpload = async (options) => {
  try {
    if (hasExistingFormDataForOcrFill()) {
      await ElMessageBox.confirm('检测到当前表单已有数据，OCR回填将覆盖相关字段，是否继续？', '提示', {
        type: 'warning',
        confirmButtonText: '继续覆盖',
        cancelButtonText: '取消',
      })
    }
  } catch (e) {
    return
  }

  ocrLoading.value = true
  try {
    const ocrData = await extractOcr(options.file)
    openOcrPreview(ocrData)
    options.onSuccess?.(ocrData, options.file)
    ElMessage.success('OCR识别完成，请确认后回填')
  } catch (e) {
    options.onError?.(e)
    console.error(e)
  } finally {
    ocrLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.deviceId = null
  form.meterPointId = ''
  form.deliverDate = ''
  form.testDate = ''
  form.temperature = null
  form.humidity = null
  form.tanPhi = null
  form.rPercent = null
  form.resultList = [
    { phase: 'ao', standardGroupKey: null, valF: null, valDelta: null, valDu: null, valUpt: null, valUyb: null },
    { phase: 'bo', standardGroupKey: null, valF: null, valDelta: null, valDu: null, valUpt: null, valUyb: null },
    { phase: 'co', standardGroupKey: null, valF: null, valDelta: null, valDu: null, valUpt: null, valUyb: null },
  ]
  standardTypeLock.value = ''
  rowThresholds.ao = null
  rowThresholds.bo = null
  rowThresholds.co = null
}

// 提交表单
const handleSubmit = () => {
  // 先校验明细数据
  for (const item of form.resultList) {
    if (!item.standardGroupKey) {
      ElMessage.warning(`请为【${item.phase.toUpperCase()}】相选择检定标准`)
      return
    }
  }

  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      // 构建提交数据，将 groupKey 拆分为 projectType, gearLevel, loadPercent
      const payload = {
        ...form,
        resultList: form.resultList.map((row) => {
          const group = getGroupByKey(row.standardGroupKey)
          return {
            phase: row.phase,
            projectType: group?.projectType || '',
            gearLevel: group?.gearLevel || '',
            loadPercent: group?.loadPercent || '',
            valF: row.valF,
            valDelta: row.valDelta,
            valDu: row.valDu,
            valUpt: row.valUpt,
            valUyb: row.valUyb,
          }
        }),
      }
      await submitTask(payload)
      ElMessage.success('提交成功')
      resetForm()
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  loadDeviceList()
  loadStandardGroups()
})
</script>

<template>
  <div class="page-task-create">
    <el-card>
      <div class="title">新建检定任务</div>
      <el-alert type="info" :closable="false" show-icon class="ocr-tip">
        <template #title>
          可先上传检测图片自动回填字段，设备选择仍需手动确认
        </template>
      </el-alert>
      <el-upload
        class="ocr-upload"
        :show-file-list="false"
        :auto-upload="true"
        :http-request="handleOcrUpload"
        :before-upload="beforeOcrUpload"
        accept=".jpg,.jpeg,.png"
        :disabled="ocrLoading || loading"
      >
        <el-button type="primary" plain :loading="ocrLoading">图片OCR一键回填</el-button>
      </el-upload>
      <el-dialog
        v-model="ocrPreviewVisible"
        title="OCR识别结果确认"
        width="900px"
        top="8vh"
        append-to-body
        destroy-on-close
        @closed="pendingOcrData = null"
      >
        <el-alert type="warning" :closable="false" show-icon class="ocr-preview-alert">
          <template #title>请先确认识别结果，点击“确认回填”后才会写入表单</template>
        </el-alert>
        <el-row :gutter="12">
          <el-col :span="12">
            <div class="preview-title">OCR原文</div>
            <el-input
              type="textarea"
              :rows="16"
              readonly
              resize="none"
              :model-value="previewOcrText || '-'"
            />
          </el-col>
          <el-col :span="12">
            <div class="preview-title">抽取结果（JSON）</div>
            <el-input
              type="textarea"
              :rows="16"
              readonly
              resize="none"
              :model-value="previewExtractedPairsText"
            />
          </el-col>
        </el-row>
        <template #footer>
          <el-button @click="handleCancelOcrFill">取消</el-button>
          <el-button type="primary" @click="handleConfirmOcrFill">确认回填</el-button>
        </template>
      </el-dialog>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <!-- 设备选择下拉 -->
        <el-form-item label="被检设备" prop="deviceId">
          <el-select
            v-model="form.deviceId"
            placeholder="请选择被检设备"
            filterable
            :loading="deviceLoading"
            style="width: 100%"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.id"
              :label="`${device.productNo} - ${device.productName}`"
              :value="device.id"
            />
          </el-select>
        </el-form-item>

        <!-- 选中设备信息展示 -->
        <el-form-item v-if="selectedDevice" label="设备信息">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="产品编号">{{ selectedDevice.productNo }}</el-descriptions-item>
            <el-descriptions-item label="产品名称">{{ selectedDevice.productName }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ selectedDevice.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="制造商">{{ selectedDevice.manufacturer || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item label="计量点编号" prop="meterPointId">
          <el-input v-model="form.meterPointId" placeholder="请输入计量点编号" />
        </el-form-item>
        <el-form-item label="送检日期" prop="deliverDate">
          <el-date-picker
            v-model="form.deliverDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="测试日期" prop="testDate">
          <el-date-picker
            v-model="form.testDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择日期时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="环境温度(℃)" prop="temperature">
          <el-input-number v-model="form.temperature" :precision="1" :controls="false" style="width: 200px" />
        </el-form-item>
        <el-form-item label="环境湿度(%)" prop="humidity">
          <el-input-number v-model="form.humidity" :precision="1" :controls="false" style="width: 200px" />
        </el-form-item>
        <el-form-item label="tanφ" prop="tanPhi">
          <el-input-number v-model="form.tanPhi" :precision="4" :controls="false" style="width: 200px" />
        </el-form-item>
        <el-form-item label="r%" prop="rPercent">
          <el-input-number v-model="form.rPercent" :precision="3" :controls="false" style="width: 200px" />
        </el-form-item>

        <el-divider>明细数据（ao / bo / co）</el-divider>

        <!-- 配色提示说明 -->
        <div class="color-legend">
          <span class="legend-item"><span class="dot pass"></span> 合格（数值在阈值范围内）</span>
          <span class="legend-item"><span class="dot fail"></span> 不合格（数值超出阈值范围）</span>
          <span class="legend-item"><el-tag size="small" type="info">提示：PT项目需校验5项，CT项目仅校验f和δ</el-tag></span>
        </div>

        <el-table :data="form.resultList" border stripe :row-class-name="getRowClassName" style="width: 100%">
          <el-table-column prop="phase" label="相别" width="86">
            <template #default="{ row }">
              <span style="text-transform: uppercase; font-weight: bold">{{ row.phase }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="standardGroupKey" label="检定标准" min-width="220">
            <template #default="{ row }">
              <el-select
                class="standard-select"
                v-model="row.standardGroupKey"
                placeholder="选择标准"
                filterable
                :loading="standardLoading"
                size="small"
                @change="() => onStandardChange(row)"
              >
                <el-option
                  v-for="group in standardGroups"
                  :key="group.groupKey"
                  :label="group.groupKey"
                  :value="group.groupKey"
                  :disabled="isStandardGroupDisabled(group)"
                >
                  <span>{{ group.groupKey }}</span>
                  <el-tag v-if="group.isPT" type="primary" size="small" style="margin-left: 8px">PT</el-tag>
                  <el-tag v-else type="warning" size="small" style="margin-left: 8px">CT</el-tag>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="f(%)" min-width="170">
            <template #header>
              <span>f(%)</span>
            </template>
            <template #default="{ row }">
              <el-input-number
                class="measure-input"
                v-model="row.valF"
                :precision="4"
                :controls="false"
                size="small"
                :style="getCellStyle(row, 'valF')"
              />
              <div class="threshold-hint">{{ formatThresholdRange(row, 'f') }}</div>
            </template>
          </el-table-column>
          <el-table-column label="δ(分)" min-width="170">
            <template #default="{ row }">
              <el-input-number
                class="measure-input"
                v-model="row.valDelta"
                :precision="3"
                :controls="false"
                size="small"
                :style="getCellStyle(row, 'valDelta')"
              />
              <div class="threshold-hint">{{ formatThresholdRange(row, 'delta') }}</div>
            </template>
          </el-table-column>
          <el-table-column label="dU(%)" min-width="170">
            <template #default="{ row }">
              <el-input-number
                class="measure-input"
                v-model="row.valDu"
                :precision="4"
                :controls="false"
                size="small"
                :disabled="!isPTProject(row)"
                :style="getCellStyle(row, 'valDu')"
              />
              <div class="threshold-hint">{{ isPTProject(row) ? formatThresholdRange(row, 'du') : '(CT无)' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="Upt" min-width="170">
            <template #default="{ row }">
              <el-input-number
                class="measure-input"
                v-model="row.valUpt"
                :precision="3"
                :controls="false"
                size="small"
                :disabled="!isPTProject(row)"
                :style="getCellStyle(row, 'valUpt')"
              />
              <div class="threshold-hint">{{ isPTProject(row) ? formatThresholdRange(row, 'upt') : '(CT无)' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="Uyb" min-width="170">
            <template #default="{ row }">
              <el-input-number
                class="measure-input"
                v-model="row.valUyb"
                :precision="3"
                :controls="false"
                size="small"
                :disabled="!isPTProject(row)"
                :style="getCellStyle(row, 'valUyb')"
              />
              <div class="threshold-hint">{{ isPTProject(row) ? formatThresholdRange(row, 'uyb') : '(CT无)' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="实时校验" min-width="120">
            <template #default="{ row }">
              <el-tag v-if="checkRowPass(row) === true" type="success" size="small">合格</el-tag>
              <el-tag v-else-if="checkRowPass(row) === false" type="danger" size="small">不合格</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <el-form-item style="margin-top: 20px">
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交任务</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.page-task-create {
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

.ocr-tip {
  margin-bottom: 12px;
}

.ocr-upload {
  margin-bottom: 16px;
}

.ocr-preview-alert {
  margin-bottom: 12px;
}

.preview-title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.color-legend {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
  align-items: center;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.pass {
  background-color: #67C23A;
}

.dot.fail {
  background-color: #F56C6C;
}

.threshold-hint {
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
  text-align: center;
}

:deep(.row-pass) {
  background-color: #f0f9eb !important;
}

:deep(.row-fail) {
  background-color: #fef0f0 !important;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}

:deep(.el-input-number.is-disabled .el-input__inner) {
  background-color: #f5f7fa;
}

.standard-select {
  width: 100%;
}

:deep(.standard-select .el-select__wrapper),
:deep(.measure-input .el-input__wrapper) {
  min-height: 30px;
}
</style>


