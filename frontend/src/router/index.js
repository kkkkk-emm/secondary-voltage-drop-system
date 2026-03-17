import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUserInfo, clearAuth, isTokenExpired } from '@/utils/auth'

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
          redirect: () => {
            const userInfo = getUserInfo()
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
          meta: { title: '被检设备管理', roles: [1] },
        },
        {
          path: 'admin/standard',
          name: 'AdminStandard',
          component: StandardConfigPage,
          meta: { title: '标准阈值配置', roles: [0] },
        },
        {
          path: 'admin/users',
          name: 'AdminUsers',
          component: UserManagePage,
          meta: { title: '用户账号管理', roles: [0] },
        },
        {
          path: 'admin/logs',
          name: 'AdminLogs',
          component: LogAuditPage,
          meta: { title: '日志审计', roles: [0] },
        },
        {
          path: 'admin/correct',
          name: 'AdminCorrect',
          component: DataCorrectPage,
          meta: { title: '数据修正', roles: [0] },
        },
        {
          path: 'admin/dashboard',
          name: 'AdminDashboard',
          component: DashboardPage,
          meta: { title: '数据可视化', roles: [0] },
        },
        {
          path: 'task/create',
          name: 'TaskCreate',
          component: TaskCreatePage,
          meta: { title: '新建检定任务', roles: [1] },
        },
        {
          path: 'task/list',
          name: 'TaskList',
          component: TaskListPage,
          meta: { title: '检定任务列表', roles: [1] },
        },
        {
          path: 'task/detail/:id',
          name: 'TaskDetail',
          component: TaskDetailPage,
          meta: { title: '检定任务详情', roles: [0, 1] },
        },
        {
          path: 'task/import',
          name: 'TaskImport',
          component: ImportPage,
          meta: { title: 'Excel数据导入', roles: [1] },
        },
        {
          // 兼容旧入口：图片OCR提取页已下线，统一跳转到新建任务中的OCR回填
          path: 'task/ocr',
          redirect: '/task/create',
          meta: { roles: [1] },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  const userInfo = getUserInfo()

  if (to.meta.public) {
    next()
    return
  }

  if (!token || isTokenExpired(token) || !userInfo) {
    clearAuth()
    next({ path: '/login', replace: true })
    return
  }

  const roles = to.meta.roles
  if (roles && roles.length > 0 && !roles.includes(userInfo.role)) {
    if (userInfo.role === 0) {
      next({ path: '/admin/users', replace: true })
    } else {
      next({ path: '/device', replace: true })
    }
    return
  }

  next()
})

export default router
