// 数据可视化统计相关接口
import request from './request'

// 获取仪表盘统计数据
export function fetchDashboardStats() {
    return request({
        url: '/dashboard/stats',
        method: 'get',
    })
}
