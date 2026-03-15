// 被检设备管理相关接口封装（对应 DeviceController）
import request from './request'

// 分页查询设备列表：GET /device/page
export function fetchDevicePage(params) {
  return request({
    url: '/device/page',
    method: 'get',
    params,
  })
}

// 获取所有设备列表（用于下拉选择）：GET /device/list
export function fetchDeviceList() {
  return request({
    url: '/device/list',
    method: 'get',
  })
}

// 新增设备档案：POST /device
export function createDevice(data) {
  return request({
    url: '/device',
    method: 'post',
    data,
  })
}

// 修改设备档案：PUT /device
export function updateDevice(data) {
  return request({
    url: '/device',
    method: 'put',
    data,
  })
}

// 删除设备：DELETE /device/{id}
export function deleteDevice(id) {
  return request({
    url: `/device/${id}`,
    method: 'delete',
  })
}


