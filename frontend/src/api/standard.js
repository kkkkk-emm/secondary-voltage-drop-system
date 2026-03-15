// 检定标准配置相关接口
import request from './request'

// 获取标准配置列表（所有项）
export function fetchStandardList() {
  return request({
    url: '/standard/list',
    method: 'get',
  })
}

// 获取分组后的标准列表（用于下拉框选择，去重）
export function fetchStandardGroups() {
  return request({
    url: '/standard/groups',
    method: 'get',
  })
}

// 匹配特定标准（录入实时数据时调用）- 旧接口
export function matchStandard(params) {
  return request({
    url: '/standard/match',
    method: 'get',
    params,
  })
}

// 获取指定组合下的所有阈值项（多项校验）
export function matchAllStandard(params) {
  return request({
    url: '/standard/matchAll',
    method: 'get',
    params,
  })
}

// 修改阈值（仅管理员）
export function updateStandard(data) {
  return request({
    url: '/standard',
    method: 'put',
    data,
  })
}


