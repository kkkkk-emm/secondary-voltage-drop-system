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
              ElMessage.error(errorData.message || '请求失败')
              reject(new Error(errorData.message || '请求失败'))
            } catch (e) {
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
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401 || res.code === 403) {
        clearAuth()
        redirectToLogin()
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401 || status === 403) {
      clearAuth()
      ElMessage.error('登录已失效，请重新登录')
      redirectToLogin()
      return Promise.reject(error)
    }
    console.error('请求异常:', error)
    ElMessage.error(error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  },
)

export default service
