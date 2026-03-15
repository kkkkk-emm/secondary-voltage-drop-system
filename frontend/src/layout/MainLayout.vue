<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getUserInfo, clearAuth } from '@/utils/auth'
import { logoutApi } from '@/api/auth'
import { changePasswordApi } from '@/api/user'
import {
  Folder,
  Plus,
  List,
  Upload,
  Picture,
  Setting,
  User,
  DataLine,
  Document,
  Edit,
  ArrowDown,
  Key,
  SwitchButton,
  TrendCharts,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 当前登录用户信息
const userInfo = ref(getUserInfo())
const isAdmin = computed(() => userInfo.value && userInfo.value.role === 0)
const isUser = computed(() => userInfo.value && userInfo.value.role === 1)

// 当前时间
const currentTime = ref('')
let timeTimer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
}

onMounted(() => {
  updateTime()
  timeTimer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeTimer) clearInterval(timeTimer)
})

// 根据路由高亮菜单
const activeMenu = computed(() => {
  if (route.path.startsWith('/device')) return '/device'
  if (route.path.startsWith('/task/create')) return '/task/create'
  if (route.path.startsWith('/task/list')) return '/task/list'
  if (route.path.startsWith('/task/import')) return '/task/import'
  if (route.path.startsWith('/task/ocr')) return '/task/ocr'
  if (route.path.startsWith('/admin/dashboard')) return '/admin/dashboard'
  if (route.path.startsWith('/admin')) return route.path
  return route.path
})

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确认退出当前登录吗？', '提示', {
    type: 'warning',
  })
    .then(async () => {
      try {
        await logoutApi()
      } catch (e) {
        // 忽略接口错误，前端仍然清理状态
        console.warn('logout error:', e)
      }
      clearAuth()
      ElMessage.success('已退出登录')
      router.replace('/login')
    })
    .catch(() => {})
}

// 修改密码弹窗
const pwdDialogVisible = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback(new Error('请再次输入新密码'))
        if (value !== pwdForm.value.newPassword) {
          return callback(new Error('两次输入的新密码不一致'))
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

const handleOpenChangePwd = () => {
  pwdForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
  pwdDialogVisible.value = true
}

const handleChangePwd = () => {
  pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await changePasswordApi({
        oldPassword: pwdForm.value.oldPassword,
        newPassword: pwdForm.value.newPassword,
      })
      ElMessage.success('密码修改成功，请使用新密码重新登录')
      pwdDialogVisible.value = false
      // 修改密码后强制重新登录
      clearAuth()
      router.replace('/login')
    } catch (e) {
      console.error(e)
    }
  })
}
</script>

<template>
  <el-container class="layout-root">
    <!-- 侧边菜单 -->
    <el-aside width="240px" class="layout-aside">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor" width="28" height="28">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <span class="logo-text">互感器检定系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="menu"
        :collapse="false"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#fff"
      >
        <!-- 检测员专属菜单：角色为1时显示 -->
        <el-menu-item v-if="isUser" index="/device">
          <el-icon><Folder /></el-icon>
          <span>被检设备管理</span>
        </el-menu-item>
        <el-menu-item v-if="isUser" index="/task/create">
          <el-icon><Plus /></el-icon>
          <span>新建检定任务</span>
        </el-menu-item>
        <el-menu-item v-if="isUser" index="/task/list">
          <el-icon><List /></el-icon>
          <span>检定任务列表</span>
        </el-menu-item>
        <el-menu-item v-if="isUser" index="/task/import">
          <el-icon><Upload /></el-icon>
          <span>Excel数据导入</span>
        </el-menu-item>
        <el-menu-item v-if="isUser" index="/task/ocr">
          <el-icon><Picture /></el-icon>
          <span>图片OCR提取</span>
        </el-menu-item>
        <!-- 管理员专属菜单：角色为0时显示 -->
        <el-sub-menu v-if="isAdmin" index="admin">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            用户账号管理
          </el-menu-item>
          <el-menu-item index="/admin/standard">
            <el-icon><DataLine /></el-icon>
            标准阈值配置
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><Document /></el-icon>
            日志审计
          </el-menu-item>
          <el-menu-item index="/admin/correct">
            <el-icon><Edit /></el-icon>
            数据修正
          </el-menu-item>
        </el-sub-menu>
        <!-- 数据可视化：与系统管理平行 -->
        <el-menu-item v-if="isAdmin" index="/admin/dashboard">
          <el-icon><TrendCharts /></el-icon>
          <span>数据可视化</span>
        </el-menu-item>
      </el-menu>
      <!-- 侧边栏底部版权 -->
      <div class="aside-footer">
        <span>© 2025 检测管理平台</span>
      </div>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="main-container">
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '首页' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="header-actions">
            <el-tooltip content="当前时间" placement="bottom">
              <span class="current-time">{{ currentTime }}</span>
            </el-tooltip>
          </div>
          <el-dropdown trigger="click">
            <div class="user-dropdown">
              <el-avatar :size="32" class="user-avatar">
                {{ (userInfo?.realName || userInfo?.username || 'U').charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="user-name">
                {{ userInfo?.realName || userInfo?.username || '未登录' }}
              </span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  <el-icon><User /></el-icon>
                  角色：{{ userInfo?.role === 0 ? '管理员' : '检测员' }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleOpenChangePwd">
                  <el-icon><Key /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <div class="page-container">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </el-main>

      <!-- 修改密码弹窗 -->
      <el-dialog
        v-model="pwdDialogVisible"
        title="修改密码"
        width="450px"
        destroy-on-close
        :close-on-click-modal="false"
      >
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="pwdDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="handleChangePwd">确 定</el-button>
          </span>
        </template>
      </el-dialog>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-root {
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  background: linear-gradient(180deg, #001529 0%, #002140 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.menu::-webkit-scrollbar {
  width: 4px;
}

.menu::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

/* 菜单项样式优化 */
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: 4px 8px;
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #1890ff 0%, #096dd9 100%) !important;
  color: #fff !important;
}

:deep(.el-sub-menu .el-menu-item) {
  padding-left: 48px !important;
  min-width: auto;
}

.aside-footer {
  padding: 16px;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-time {
  font-size: 13px;
  color: #666;
  font-family: 'SF Mono', 'Consolas', monospace;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-dropdown:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  color: #fff;
  font-weight: 600;
}

.user-name {
  font-size: 14px;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

.layout-main {
  flex: 1;
  overflow: auto;
  background: #f0f2f5;
  padding: 0;
}

.page-container {
  padding: 20px;
  min-height: calc(100vh - 64px);
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 全局卡片样式优化 */
:deep(.el-card) {
  border-radius: 8px;
  border: none;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 弹窗样式优化 */
:deep(.el-dialog) {
  border-radius: 12px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-right: 0;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px 20px;
  border-top: 1px solid #f0f0f0;
}
</style>


