<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi } from '@/api/auth'
import { setToken, setUserInfo } from '@/utils/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const formRef = ref()
const loading = ref(false)

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const data = await loginApi({
        username: form.username,
        password: form.password,
      })
      setToken(data.token)
      setUserInfo(data.userInfo)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      router.replace(typeof redirect === 'string' && redirect ? redirect : '/')
    } catch (_e) {
      // 错误消息已在请求拦截器统一提示
    } finally {
      loading.value = false
    }
  })
}

const handleKeyup = (e) => {
  if (e.key === 'Enter') {
    handleSubmit()
  }
}
</script>

<template>
  <div class="login-page">
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-container">
      <div class="login-banner">
        <div class="banner-content">
          <div class="banner-icon">
            <svg viewBox="0 0 24 24" fill="currentColor" width="80" height="80">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
            </svg>
          </div>
          <h1 class="banner-title">互感器二次压降检测仪</h1>
          <h2 class="banner-subtitle">检定管理系统</h2>
          <p class="banner-desc">专业 · 高效 · 精准</p>
          <div class="banner-features">
            <div class="feature-item">
              <span class="feature-icon">✓</span>
              <span>设备全生命周期管理</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">✓</span>
              <span>自动化检定流程</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">✓</span>
              <span>标准化数据分析</span>
            </div>
          </div>
        </div>
      </div>

      <div class="login-form-wrapper">
        <div class="login-card">
          <div class="login-header">
            <h2 class="login-title">欢迎登录</h2>
            <p class="login-hint">请输入您的账号密码</p>
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup="handleKeyup">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入账号"
                autocomplete="username"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入密码"
                autocomplete="current-password"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" class="login-btn" @click="handleSubmit">
                {{ loading ? '登录中...' : '登录' }}
              </el-button>
            </el-form-item>
          </el-form>
          <div class="login-footer">
            <span>© 2025 检测管理平台 · 技术支持</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: float 10s ease-in-out infinite;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

.login-container {
  display: flex;
  width: 900px;
  min-height: 520px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  z-index: 1;
}

.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-banner::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 50%);
  top: -50%;
  left: -50%;
}

.banner-content {
  text-align: center;
  color: #fff;
  position: relative;
  z-index: 1;
}

.banner-icon {
  color: #667eea;
  margin-bottom: 24px;
  filter: drop-shadow(0 0 20px rgba(102, 126, 234, 0.5));
}

.banner-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.banner-subtitle {
  font-size: 28px;
  font-weight: 300;
  margin-bottom: 16px;
  color: #667eea;
}

.banner-desc {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 32px;
}

.banner-features {
  text-align: left;
  max-width: 280px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 15px;
}

.feature-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: #f8f9fa;
}

.login-card {
  width: 100%;
  max-width: 360px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 34px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.login-hint {
  color: #7f8c9a;
  font-size: 16px;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border: none;
  box-shadow: 0 8px 20px rgba(118, 75, 162, 0.25);
}

.login-btn:hover {
  filter: brightness(1.05);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #9aa0a6;
  font-size: 12px;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

@media (max-width: 960px) {
  .login-container {
    width: 94%;
    min-height: 0;
    flex-direction: column;
  }

  .login-banner {
    padding: 28px;
  }

  .banner-title {
    font-size: 20px;
  }

  .banner-subtitle {
    font-size: 24px;
  }

  .login-form-wrapper {
    padding: 28px 20px;
  }
}
</style>
