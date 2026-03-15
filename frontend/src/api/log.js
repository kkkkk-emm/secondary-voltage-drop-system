// 系统日志相关接口
import request from './request'

// 分页查询系统日志：GET /log/page
export function fetchLogPage(params) {
  return request({
    url: '/log/page',
    method: 'get',
    params,
  })
}

