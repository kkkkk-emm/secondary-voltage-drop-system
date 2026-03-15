// 认证相关接口封装
import request from './request'

// 用户登录：POST /auth/login
export function loginApi(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

// 用户登出：POST /auth/logout
export function logoutApi() {
  return request({
    url: '/auth/logout',
    method: 'post',
  })
}


