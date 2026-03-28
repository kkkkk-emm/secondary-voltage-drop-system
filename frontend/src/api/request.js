import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getToken, clearAuth } from '@/utils/auth'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
})

const redirectToLogin = () => {
  const currentPath = router.currentRoute.value?.path || ''
  if (currentPath === '/login') return
  const redirect = router.currentRoute.value?.fullPath || '/'
  router.replace({ path: '/login', query: { redirect } })
}

const handleUnauthorized = (message = '登录已失效，请重新登录') => {
  clearAuth()
  ElMessage.error(message)
  redirectToLogin()
}

service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

service.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      if (response.data.type === 'application/json') {
        return new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => {
            try {
              const errorData = JSON.parse(reader.result)
              const code = errorData?.code
              const message = errorData?.message || '请求失败'
              if (code === 401) {
                handleUnauthorized(message)
              } else if (code === 403) {
                ElMessage.error(message || '无访问权限')
              } else {
                ElMessage.error(message)
              }
              reject(new Error(message))
            } catch (_e) {
              reject(new Error('请求失败'))
            }
          }
          reader.readAsText(response.data)
        })
      }
      return response.data
    }

    const res = response.data
    if (res.code && res.code !== 200) {
      const message = res.message || '请求失败'
      if (res.code === 401) {
        handleUnauthorized(message)
      } else if (res.code === 403) {
        ElMessage.error(message || '无访问权限')
      } else {
        ElMessage.error(message)
      }
      return Promise.reject(new Error(message))
    }
    return res.data
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      handleUnauthorized()
      return Promise.reject(error)
    }
    if (status === 403) {
      const message = error?.response?.data?.message || '无访问权限'
      ElMessage.error(message)
      return Promise.reject(error)
    }

    console.error('请求异常:', error)
    ElMessage.error(error.message || '网络错误，请稍后重试')
    return Promise.reject(error)
  },
)

export default service
