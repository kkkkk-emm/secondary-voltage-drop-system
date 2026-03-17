const TOKEN_KEY = 'svd_token'
const USER_KEY = 'svd_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    console.error('解析用户信息失败', e)
    return null
  }
}

export function setUserInfo(info) {
  if (info) {
    localStorage.setItem(USER_KEY, JSON.stringify(info))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export function clearAuth() {
  clearToken()
  setUserInfo(null)
}

function decodeJwtPayload(token) {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length < 2) return null
  try {
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(''),
    )
    return JSON.parse(json)
  } catch (e) {
    return null
  }
}

// 非法 token 也按过期处理，避免停留在受保护页面
export function isTokenExpired(token = getToken()) {
  const payload = decodeJwtPayload(token)
  if (!payload || typeof payload.exp !== 'number') return true
  const nowSeconds = Math.floor(Date.now() / 1000)
  return payload.exp <= nowSeconds
}
