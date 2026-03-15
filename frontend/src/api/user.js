// 用户相关接口
import request from './request'

// 修改当前登录用户密码：PUT /user/password
export function changePasswordApi(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data,
  })
}

// 分页查询用户：GET /user/page (管理员)
export function fetchUserPage(params) {
  return request({
    url: '/user/page',
    method: 'get',
    params,
  })
}

// 新增用户：POST /user (管理员)
export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data,
  })
}

// 修改用户状态：PUT /user/status (管理员)
export function updateUserStatus(data) {
  return request({
    url: '/user/status',
    method: 'put',
    data,
  })
}

// 重置密码：PUT /user/reset-password (管理员)
export function resetUserPassword(data) {
  return request({
    url: '/user/reset-password',
    method: 'put',
    data,
  })
}

