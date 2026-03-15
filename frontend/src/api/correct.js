// 数据修正相关接口（管理员专用）
import request from './request'

// 修正检测结果数据：POST /correct/result
export function correctResult(data) {
    return request({
        url: '/correct/result',
        method: 'post',
        data,
    })
}
