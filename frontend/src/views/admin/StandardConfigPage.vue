<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchStandardList, updateStandard } from '@/api/standard'

const loading = ref(false)
const tableData = ref([])

const getList = async () => {
  loading.value = true
  try {
    const data = await fetchStandardList()
    tableData.value = data || []
  } finally {
    loading.value = false
  }
}

const handleSave = async (row) => {
  try {
    await updateStandard({
      id: row.id,
      limitMin: row.limitMin,
      limitMax: row.limitMax,
    })
    ElMessage.success('保存成功')
    getList()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  getList()
})
</script>

<template>
  <div class="page-standard">
    <el-card>
      <div class="title">标准阈值配置</div>
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="projectType" label="项目类型" width="120" />
        <el-table-column prop="gearLevel" label="档位" width="120" />
        <el-table-column prop="loadPercent" label="负载百分比" width="120" />
        <el-table-column prop="thresholdItem" label="阈值项目" width="120" />
        <el-table-column prop="limitMin" label="下限">
          <template #default="{ row }">
            <el-input-number v-model="row.limitMin" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column prop="limitMax" label="上限">
          <template #default="{ row }">
            <el-input-number v-model="row.limitMax" :controls="false" :precision="2" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleSave(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page-standard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
}
</style>


