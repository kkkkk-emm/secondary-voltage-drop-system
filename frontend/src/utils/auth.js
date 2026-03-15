// 简单的 Token 与用户信息本地存储工具
// 为保证前后端解耦，不关心 Token 内容，只负责存取

const TOKEN_KEY = 'svd_token'
const USER_KEY = 'svd_user'

// 获取 Token
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

// 设置 Token
export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

// 清除 Token
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

// 获取用户信息
export function getUserInfo() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    console.error('解析用户信息失败', e)
    return null
  }
}

// 设置用户信息
export function setUserInfo(info) {
  if (info) {
    localStorage.setItem(USER_KEY, JSON.stringify(info))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

// 清除登录状态（Token + 用户信息）
export function clearAuth() {
  clearToken()
  setUserInfo(null)
}


