// 报表相关接口
import request from './request'

// PDF 预览：GET /report/preview/{taskId}，返回 blob
export function previewReport(taskId) {
  return request({
    url: `/report/preview/${taskId}`,
    method: 'get',
    responseType: 'blob',
  })
}


