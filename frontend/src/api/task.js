// 检定任务相关接口
import request, { API_BASE_URL } from './request'

// 提交检定任务：POST /task/submit
export function submitTask(data) {
  return request({
    url: '/task/submit',
    method: 'post',
    data,
  })
}

// 分页查询检定记录：GET /task/page
export function fetchTaskPage(params) {
  return request({
    url: '/task/page',
    method: 'get',
    params,
  })
}

// 获取检定任务详情：GET /task/{id}
export function fetchTaskDetail(id) {
  return request({
    url: `/task/${id}`,
    method: 'get',
  })
}

// 获取报告预览URL
export function getReportPreviewUrl(taskId) {
  return `${API_BASE_URL}/report/preview/${taskId}`
}

// 获取报告下载URL
export function getReportDownloadUrl(taskId) {
  return `${API_BASE_URL}/report/download/${taskId}`
}


