// Axios 实例封装：统一处理 baseURL、Token、错误提示等

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'

// 基础API地址，支持通过环境变量配置
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const service = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
})

// 请求拦截器：自动附加 Token
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

// 响应拦截器：统一处理业务状态码与错误
service.interceptors.response.use(
  (response) => {
    // 如果是 blob 类型（文件下载/预览），直接返回
    if (response.config.responseType === 'blob') {
      // 检查是否是错误响应（后端返回的 JSON 错误信息）
      if (response.data.type === 'application/json') {
        return new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => {
            try {
              const errorData = JSON.parse(reader.result)
              ElMessage.error(errorData.message || '文件获取失败')
              reject(new Error(errorData.message || '文件获取失败'))
            } catch (e) {
              reject(new Error('文件获取失败'))
            }
          }
          reader.readAsText(response.data)
        })
      }
      return response.data
    }

    const res = response.data
    // 接口文档统一结构：{ code, message, data }
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401/403 处理：清除登录状态
      if (res.code === 401 || res.code === 403) {
        clearAuth()
        // 这里不直接跳转路由，避免在纯工具模块中耦合 router
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  (error) => {
    console.error('请求异常：', error)
    ElMessage.error(error.message || '网络异常，请稍后重试')
    return Promise.reject(error)
  },
)

export default service


