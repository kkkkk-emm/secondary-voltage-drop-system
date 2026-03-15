import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUserInfo, clearAuth, isTokenExpired } from '@/utils/auth'

// 路由懒加载
const Login = () => import('@/views/Login.vue')
const Layout = () => import('@/layout/MainLayout.vue')
const DevicePage = () => import('@/views/device/DevicePage.vue')
const StandardConfigPage = () => import('@/views/admin/StandardConfigPage.vue')
const UserManagePage = () => import('@/views/admin/UserManagePage.vue')
const LogAuditPage = () => import('@/views/admin/LogAuditPage.vue')
const DataCorrectPage = () => import('@/views/admin/DataCorrectPage.vue')
const DashboardPage = () => import('@/views/admin/DashboardPage.vue')
const TaskCreatePage = () => import('@/views/task/TaskCreatePage.vue')
const TaskListPage = () => import('@/views/task/TaskListPage.vue')
const TaskDetailPage = () => import('@/views/task/TaskDetailPage.vue')
const ImportPage = () => import('@/views/task/ImportPage.vue')
const OcrExtractPage = () => import('@/views/task/OcrExtractPage.vue')

// 预留其它业务页面（检定标准、检定任务、日志、用户管理等）

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: { public: true, title: '登录' },
    },
    {
      path: '/',
      component: Layout,
      children: [
        {
          path: '',
          name: 'Home',
          // 根据角色动态重定向，在路由守卫中处理
          redirect: (to) => {
            const userInfo = getUserInfo()
            // role=0 管理员，role=1 检测员
            if (userInfo && userInfo.role === 0) {
              return '/admin/users'
            }
            return '/device'
          },
        },
        {
          path: 'device',
          name: 'Device',
          component: DevicePage,
          meta: { title: '被检设备管理', roles: [1] }, // 仅检测员
        },
        {
          path: 'admin/standard',
          name: 'AdminStandard',
          component: StandardConfigPage,
          meta: { title: '标准阈值配置', roles: [0] }, // 仅管理员
        },
        {
          path: 'admin/users',
          name: 'AdminUsers',
          component: UserManagePage,
          meta: { title: '用户账号管理', roles: [0] }, // 仅管理员
        },
        {
          path: 'admin/logs',
          name: 'AdminLogs',
          component: LogAuditPage,
          meta: { title: '日志审计', roles: [0] }, // 仅管理员
        },
        {
          path: 'admin/correct',
          name: 'AdminCorrect',
          component: DataCorrectPage,
          meta: { title: '数据修正', roles: [0] }, // 仅管理员
        },
        {
          path: 'admin/dashboard',
          name: 'AdminDashboard',
          component: DashboardPage,
          meta: { title: '数据可视化', roles: [0] }, // 仅管理员
        },
        {
          path: 'task/create',
          name: 'TaskCreate',
          component: TaskCreatePage,
          meta: { title: '新建检定任务', roles: [1] }, // 仅检测员
        },
        {
          path: 'task/list',
          name: 'TaskList',
          component: TaskListPage,
          meta: { title: '检定任务列表', roles: [1] }, // 仅检测员
        },
        {
          path: 'task/detail/:id',
          name: 'TaskDetail',
          component: TaskDetailPage,
          meta: { title: '检定任务详情', roles: [0, 1] }, // 管理员和检测员都可查看
        },
        {
          path: 'task/import',
          name: 'TaskImport',
          component: ImportPage,
          meta: { title: 'Excel数据导入', roles: [1] }, // 仅检测员
        },
        {
          path: 'task/ocr',
          name: 'TaskOcr',
          component: OcrExtractPage,
          meta: { title: '图片OCR提取', roles: [1] }, // 仅检测员
        },
        // TODO: 后续在此处追加其它模块路由
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

// 路由守卫：登录拦截 + 角色权限校验
router.beforeEach((to, from, next) => {
  const token = getToken()
  const userInfo = getUserInfo()

  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 未登录跳转登录页
  if (!token || isTokenExpired(token)) {
    clearAuth()
    next({ path: '/login', replace: true })
    return
  }

  // 角色权限校验
  const roles = to.meta.roles
  if (roles && roles.length > 0 && userInfo) {
    if (!roles.includes(userInfo.role)) {
      // 无权限，重定向到对应角色的首页
      if (userInfo.role === 0) {
        next({ path: '/admin/users', replace: true })
      } else {
        next({ path: '/device', replace: true })
      }
      return
    }
  }

  next()
})

export default router


