// Excel 导入相关接口
import request from './request'

/**
 * 预览检定任务数据（不入库）
 */
export function previewTasks(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/import/tasks/preview',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
        timeout: 60000,
    })
}

/**
 * 预览设备数据（不入库）
 */
export function previewDevices(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/import/devices/preview',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
        timeout: 60000,
    })
}

/**
 * 导入检定任务数据
 */
export function importTasks(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/import/tasks',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
        timeout: 60000, // 导入可能需要较长时间
    })
}

/**
 * 导入设备数据
 */
export function importDevices(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/import/devices',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data',
        },
        timeout: 60000,
    })
}
