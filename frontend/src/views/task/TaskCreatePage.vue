<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchTaskDetail, submitTask, updateTask } from '@/api/task'
import { fetchStandardGroups, matchAllStandard } from '@/api/standard'
import { fetchDeviceList } from '@/api/device'
import { extractOcr } from '@/api/ocr'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const ocrLoading = ref(false)
const ocrPreviewVisible = ref(false)
const pendingOcrData = ref(null)

const deviceList = ref([])
const deviceLoading = ref(false)
const standardGroups = ref([])
const standardLoading = ref(false)
const standardTypeLock = ref('')

const isEditMode = computed(() => route.name === 'TaskEdit')
const editingTaskId = computed(() => {
  const id = Number(route.params.id)
  return Number.isFinite(id) && id > 0 ? id : null
})
const pageTitle = computed(() => (isEditMode.value ? '编辑实验任务' : '新建实验任务'))
const submitButtonText = computed(() => (isEditMode.value ? '保存修改' : '提交任务'))

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
  tanPhi: [{ required: true, message: '请输入 tanPhi', trigger: 'blur' }],
  rPercent: [{ required: true, message: '请输入 rPercent', trigger: 'blur' }],
}

const hasValue = (value) => value !== undefined && value !== null && String(value).trim() !== ''

const buildGroupKey = (projectType, gearLevel, loadPercent) => {
  if (!hasValue(projectType) || !hasValue(gearLevel) || !hasValue(loadPercent)) {
    return null
  }
  return `${projectType}-${gearLevel}-${loadPercent}`
}

const getGroupByKey = (groupKey) => {
  if (!groupKey) return null
  return standardGroups.value.find((g) => g.groupKey === groupKey)
}

const getGroupByParts = (projectType, gearLevel, loadPercent) => {
  return standardGroups.value.find(
    (g) => g.projectType === projectType && g.gearLevel === gearLevel && g.loadPercent === loadPercent,
  )
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

const loadDeviceList = async () => {
  deviceLoading.value = true
  try {
    deviceList.value = await fetchDeviceList()
  } catch (e) {
    console.error('loadDeviceList failed', e)
  } finally {
    deviceLoading.value = false
  }
}

const loadStandardGroups = async () => {
  standardLoading.value = true
  try {
    standardGroups.value = await fetchStandardGroups()
  } catch (e) {
    console.error('loadStandardGroups failed', e)
  } finally {
    standardLoading.value = false
  }
}

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
    console.error('onStandardChange failed', e)
    rowThresholds[row.phase] = null
  }
}

const isPTProject = (row) => {
  const thresholds = rowThresholds[row.phase]
  return thresholds?.isPT === true
}

const getThresholdRange = (row, itemKey) => {
  const thresholds = rowThresholds[row.phase]
  if (!thresholds || !thresholds.thresholds) return null
  return thresholds.thresholds[itemKey.toLowerCase()]
}

const checkItemPass = (row, itemKey, value) => {
  const range = getThresholdRange(row, itemKey)
  if (!range || range.min == null || range.max == null) return null
  if (value == null || value === '') return null
  const val = parseFloat(value)
  const min = parseFloat(range.min)
  const max = parseFloat(range.max)
  return val >= min && val <= max
}

const checkRowPass = (row) => {
  const thresholds = rowThresholds[row.phase]
  if (!thresholds || !thresholds.thresholds) return null

  const itemsToCheck = thresholds.isPT ? ['f', 'delta', 'du', 'upt', 'uyb'] : ['f', 'delta']
  const fieldMap = {
    f: 'valF',
    delta: 'valDelta',
    du: 'valDu',
    upt: 'valUpt',
    uyb: 'valUyb',
  }

  let hasMeasuredValue = false
  let allPass = true

  for (const item of itemsToCheck) {
    const field = fieldMap[item]
    const value = row[field]
    const range = thresholds.thresholds[item]

    if (!range || range.min == null || range.max == null) continue
    if (value == null || value === '') continue

    hasMeasuredValue = true
    const val = parseFloat(value)
    const min = parseFloat(range.min)
    const max = parseFloat(range.max)
    if (val < min || val > max) {
      allPass = false
    }
  }

  if (!hasMeasuredValue) return null
  return allPass
}

const getRowClassName = ({ row }) => {
  const pass = checkRowPass(row)
  if (pass === null) return ''
  return pass ? 'row-pass' : 'row-fail'
}

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
  return pass ? { color: '#67C23A', fontWeight: 'bold' } : { color: '#F56C6C', fontWeight: 'bold' }
}

const formatThresholdRange = (row, itemKey) => {
  const range = getThresholdRange(row, itemKey)
  if (!range || range.min == null || range.max == null) return '-'
  return `[${range.min}, ${range.max}]`
}

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
  } catch {
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
  if (/PT侧/i.test(text) || /PT/i.test(text)) return 'PT'
  if (/CT侧/i.test(text) || /CT/i.test(text)) return 'CT'
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
    ElMessage.warning('检测到 5 项数据，但未找到 PT 标准，请检查标准组配置')
    return
  }

  for (const row of form.resultList) {
    row.standardGroupKey = targetGroup.groupKey
  }
  for (const row of form.resultList) {
    await onStandardChange(row)
  }
  ElMessage.success('已检测到 5 项，默认选择 PT 标准，并禁用 CT 标准')
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
    ElMessage.info('未能唯一匹配标准组，请手动选择')
    return
  }

  const uniqueGroup = candidates[0]
  for (const row of form.resultList) {
    row.standardGroupKey = uniqueGroup.groupKey
  }
  for (const row of form.resultList) {
    await onStandardChange(row)
  }
  ElMessage.success(`已自动匹配标准组：${uniqueGroup.groupKey}`)
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
    ElMessage.warning('暂无可回填的 OCR 结果')
    return
  }
  try {
    await applyOcrToForm(pendingOcrData.value)
    closeOcrPreview()
    pendingOcrData.value = null
    ElMessage.success('OCR 回填完成，请核对后提交')
  } catch (e) {
    console.error(e)
  }
}

const handleCancelOcrFill = () => {
  closeOcrPreview()
  pendingOcrData.value = null
  ElMessage.info('已取消本次 OCR 回填')
}

const handleOcrUpload = async (options) => {
  try {
    if (hasExistingFormDataForOcrFill()) {
      await ElMessageBox.confirm('检测到当前表单已有数据，OCR 回填将覆盖相关字段，是否继续？', '提示', {
        type: 'warning',
        confirmButtonText: '继续覆盖',
        cancelButtonText: '取消',
      })
    }
  } catch {
    return
  }

  ocrLoading.value = true
  try {
    const ocrData = await extractOcr(options.file)
    openOcrPreview(ocrData)
    options.onSuccess?.(ocrData, options.file)
    ElMessage.success('OCR 识别完成，请确认后回填')
  } catch (e) {
    options.onError?.(e)
    console.error(e)
  } finally {
    ocrLoading.value = false
  }
}

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
  pendingOcrData.value = null
  ocrPreviewVisible.value = false
  formRef.value?.clearValidate?.()
}

const fillFormFromTaskDetail = async (detail) => {
  if (!detail) return

  form.deviceId = detail.taskInfo?.deviceId ?? null
  form.meterPointId = detail.taskInfo?.meterPointId || ''
  form.deliverDate = detail.taskInfo?.deliverDate || ''
  form.testDate = detail.taskInfo?.testDate || ''
  form.temperature = detail.taskInfo?.temperature ?? null
  form.humidity = detail.taskInfo?.humidity ?? null
  form.tanPhi = detail.taskInfo?.tanPhi ?? null
  form.rPercent = detail.taskInfo?.rPercent ?? null

  const resultMap = new Map((detail.resultList || []).map((item) => [String(item.phase || '').toLowerCase(), item]))

  for (const row of form.resultList) {
    const item = resultMap.get(row.phase)
    if (!item) continue

    row.valF = item.valF
    row.valDelta = item.valDelta
    row.valDu = item.valDu
    row.valUpt = item.valUpt
    row.valUyb = item.valUyb

    const matchedGroup = getGroupByParts(item.projectType, item.gearLevel, item.loadPercent)
    row.standardGroupKey = matchedGroup?.groupKey || buildGroupKey(item.projectType, item.gearLevel, item.loadPercent)
  }

  for (const row of form.resultList) {
    await onStandardChange(row)
  }
}

const loadTaskForEdit = async () => {
  if (!isEditMode.value) return
  if (!editingTaskId.value) {
    ElMessage.error('任务 ID 无效')
    router.replace('/task/list')
    return
  }

  loading.value = true
  try {
    const detail = await fetchTaskDetail(editingTaskId.value)
    if (!detail) {
      ElMessage.error('任务不存在或已删除')
      router.replace('/task/list')
      return
    }
    await fillFormFromTaskDetail(detail)
  } catch (e) {
    console.error(e)
    router.replace('/task/list')
  } finally {
    loading.value = false
  }
}

const buildPayload = () => ({
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
})

const handleSubmit = () => {
  for (const item of form.resultList) {
    if (!item.standardGroupKey) {
      ElMessage.warning(`请为 ${item.phase.toUpperCase()} 相选择检定标准`)
      return
    }
  }

  formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const payload = buildPayload()
      if (isEditMode.value && editingTaskId.value) {
        await updateTask(editingTaskId.value, payload)
        ElMessage.success('任务修改成功')
        router.push('/task/list')
      } else {
        await submitTask(payload)
        ElMessage.success('任务提交成功')
        resetForm()
      }
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  })
}

const initializePage = async () => {
  resetForm()
  await Promise.all([loadDeviceList(), loadStandardGroups()])
  if (isEditMode.value) {
    await loadTaskForEdit()
  }
}

const handleBack = () => {
  router.push('/task/list')
}

onMounted(() => {
  initializePage()
})

watch(
  () => route.fullPath,
  () => {
    initializePage()
  },
)
</script>

<template>
  <div class="page-task-create">
    <el-card v-loading="loading">
      <div class="title-wrap">
        <div class="title">{{ pageTitle }}</div>
        <el-button v-if="isEditMode" @click="handleBack">返回任务列表</el-button>
      </div>

      <el-alert type="info" :closable="false" show-icon class="ocr-tip">
        <template #title>
          可先上传检测图片自动回填字段，设备选择仍需手动确认。
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
        <el-button type="primary" plain :loading="ocrLoading">OCR 图片识别并回填</el-button>
      </el-upload>

      <el-dialog
        v-model="ocrPreviewVisible"
        title="OCR 识别结果预览"
        width="900px"
        top="8vh"
        append-to-body
        destroy-on-close
        @closed="pendingOcrData = null"
      >
        <el-alert type="warning" :closable="false" show-icon class="ocr-preview-alert">
          <template #title>请先核对 OCR 识别结果，确认无误后再回填到表单。</template>
        </el-alert>
        <el-row :gutter="12">
          <el-col :span="12">
            <div class="preview-title">OCR 原始文本</div>
            <el-input
              type="textarea"
              :rows="16"
              readonly
              resize="none"
              :model-value="previewOcrText || '-'"
            />
          </el-col>
          <el-col :span="12">
            <div class="preview-title">提取后的键值对（JSON）</div>
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

        <el-form-item v-if="selectedDevice" label="设备信息">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="设备编号">{{ selectedDevice.productNo }}</el-descriptions-item>
            <el-descriptions-item label="设备名称">{{ selectedDevice.productName }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ selectedDevice.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="生产厂家">{{ selectedDevice.manufacturer || '-' }}</el-descriptions-item>
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
            placeholder="请选择送检日期"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="测试日期" prop="testDate">
          <el-date-picker
            v-model="form.testDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择测试日期时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="温度(℃)" prop="temperature">
          <el-input-number v-model="form.temperature" :precision="1" :controls="false" style="width: 220px" />
        </el-form-item>

        <el-form-item label="湿度(%)" prop="humidity">
          <el-input-number v-model="form.humidity" :precision="1" :controls="false" style="width: 220px" />
        </el-form-item>

        <el-form-item label="tanPhi" prop="tanPhi">
          <el-input-number v-model="form.tanPhi" :precision="4" :controls="false" style="width: 220px" />
        </el-form-item>

        <el-form-item label="rPercent" prop="rPercent">
          <el-input-number v-model="form.rPercent" :precision="3" :controls="false" style="width: 220px" />
        </el-form-item>

        <el-divider>三相测量明细（ao / bo / co）</el-divider>

        <div class="color-legend">
          <span class="legend-item"><span class="dot pass"></span>绿色数值表示该项落在阈值范围内</span>
          <span class="legend-item"><span class="dot fail"></span>红色数值表示该项超出阈值范围</span>
          <span class="legend-item">
            <el-tag size="small" type="info">PT 项目有 5 项，CT 项目仅校验 f/delta</el-tag>
          </span>
        </div>

        <el-table :data="form.resultList" border stripe :row-class-name="getRowClassName" style="width: 100%">
          <el-table-column prop="phase" label="相别" width="86">
            <template #default="{ row }">
              <span style="text-transform: uppercase; font-weight: bold">{{ row.phase }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="standardGroupKey" label="检定标准组" min-width="220">
            <template #default="{ row }">
              <el-select
                class="standard-select"
                v-model="row.standardGroupKey"
                placeholder="请选择检定标准组"
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

          <el-table-column label="delta" min-width="170">
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
              <div class="threshold-hint">
                {{ isPTProject(row) ? formatThresholdRange(row, 'du') : '仅 PT 项目需要填写' }}
              </div>
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
              <div class="threshold-hint">
                {{ isPTProject(row) ? formatThresholdRange(row, 'upt') : '仅 PT 项目需要填写' }}
              </div>
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
              <div class="threshold-hint">
                {{ isPTProject(row) ? formatThresholdRange(row, 'uyb') : '仅 PT 项目需要填写' }}
              </div>
            </template>
          </el-table-column>

          <el-table-column label="本相结论" min-width="120">
            <template #default="{ row }">
              <el-tag v-if="checkRowPass(row) === true" type="success" size="small">合格</el-tag>
              <el-tag v-else-if="checkRowPass(row) === false" type="danger" size="small">不合格</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <el-form-item style="margin-top: 20px">
          <el-button type="primary" :loading="loading" @click="handleSubmit">{{ submitButtonText }}</el-button>
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

.title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.title {
  font-size: 18px;
  font-weight: 600;
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
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot.pass {
  background: #67c23a;
}

.dot.fail {
  background: #f56c6c;
}

.standard-select {
  width: 100%;
}

.measure-input {
  width: 100%;
}

.threshold-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

:deep(.row-pass td) {
  background: rgba(103, 194, 58, 0.07) !important;
}

:deep(.row-fail td) {
  background: rgba(245, 108, 108, 0.07) !important;
}
</style>
