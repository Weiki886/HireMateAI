<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="bg-orb orb-1"></div>
      <div class="bg-orb orb-2"></div>
      <div class="bg-orb orb-3"></div>
    </div>

    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="brand-panel">
        <div class="brand-content">
          <div class="brand-logo">
            <svg width="44" height="44" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#3B82F6"/>
              <path d="M2 17L12 22L22 17" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="#60A5FA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <h1 class="brand-title">HireMate AI</h1>
          <p class="brand-desc">你的智能 AI 面试助手</p>
          <div class="brand-features">
            <div class="feature-item" v-for="f in features" :key="f">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.8)" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              {{ f }}
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-panel">
        <div class="form-card">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录你的账户开始 AI 面试之旅</p>
          </div>

          <!-- Tab 切换 -->
          <div class="tab-nav">
            <button class="tab-btn" :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">登录</button>
            <button class="tab-btn" :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">注册</button>
          </div>

          <!-- 登录表单 -->
          <transition name="tab" mode="out-in">
            <div v-if="activeTab === 'login'" key="login" class="form-body">
              <div class="field-group">
                <label>用户名</label>
                <div class="input-wrap">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input
                    v-model="loginForm.username"
                    type="text"
                    placeholder="请输入用户名"
                    autocomplete="username"
                    @keyup.enter="handleLogin"
                  />
                </div>
              </div>
              <div class="field-group">
                <label>密码</label>
                <div class="input-wrap">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input
                    v-model="loginForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                    @keyup.enter="handleLogin"
                  />
                  <button class="toggle-pwd" @click="showPassword = !showPassword" type="button">
                    <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                  </button>
                </div>
              </div>
              <button class="submit-btn" :class="{ loading }" @click="handleLogin" :disabled="loading">
                <span v-if="!loading">登 录</span>
                <span v-else class="loading-dots">登录中<span>.</span><span>.</span><span>.</span></span>
              </button>
            </div>

            <div v-else key="register" class="form-body">
              <div class="field-group">
                <label>用户名</label>
                <div class="input-wrap">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input v-model="registerForm.username" type="text" placeholder="3-50位字母数字" autocomplete="username" />
                </div>
              </div>
              <div class="field-group">
                <label>邮箱</label>
                <div class="input-wrap">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                  <input v-model="registerForm.email" type="email" placeholder="请输入邮箱" autocomplete="email" />
                </div>
              </div>
              <div class="field-group">
                <label>密码</label>
                <div class="input-wrap">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input v-model="registerForm.password" :type="showPassword ? 'text' : 'password'" placeholder="至少 6 位" autocomplete="new-password" />
                </div>
              </div>
              <div class="field-group">
                <label>确认密码</label>
                <div class="input-wrap">
                  <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input v-model="registerForm.confirmPassword" :type="showPassword ? 'text' : 'password'" placeholder="再次输入密码" autocomplete="new-password" @keyup.enter="handleRegister" />
                </div>
              </div>
              <button class="submit-btn" :class="{ loading }" @click="handleRegister" :disabled="loading">
                <span v-if="!loading">注 册</span>
                <span v-else class="loading-dots">注册中<span>.</span><span>.</span><span>.</span></span>
              </button>
            </div>
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)
const showPassword = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const features = [
  'AI 模拟面试 — 对标真实面试场景',
  '多角色简历优化 — HR + 技术 + 职业规划',
  '岗位智能匹配 — 简历与 JD 自动分析',
  '多专家群聊 — 三位 AI 同时服务'
]

const validateRegister = () => {
  if (!registerForm.value.username || registerForm.value.username.length < 3) {
    ElMessage.warning('用户名至少 3 位')
    return false
  }
  if (!registerForm.value.email || !registerForm.value.email.includes('@')) {
    ElMessage.warning('请输入正确的邮箱')
    return false
  }
  if (registerForm.value.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return false
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return false
  }
  return true
}

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(loginForm.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!validateRegister()) return
  loading.value = true
  try {
    await userStore.register({
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password
    })
    ElMessage.success('注册成功，请登录')
    loginForm.value.username = registerForm.value.username
    loginForm.value.password = ''
    registerForm.value = { username: '', email: '', password: '', confirmPassword: '' }
    activeTab.value = 'login'
  } catch (error) {
    console.error('Register failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.login-page {
  min-height: 100vh;
  background: #EFF6FF;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

// 背景装饰
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;

  &.orb-1 {
    width: 400px;
    height: 400px;
    background: $color-primary;
    top: -100px;
    right: -100px;
    animation: float-orb 8s ease-in-out infinite;
  }

  &.orb-2 {
    width: 300px;
    height: 300px;
    background: $color-secondary;
    bottom: -80px;
    left: -80px;
    animation: float-orb 10s ease-in-out 2s infinite;
  }

  &.orb-3 {
    width: 200px;
    height: 200px;
    background: #10B981;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    animation: float-orb 6s ease-in-out 4s infinite;
  }
}

@keyframes float-orb {
  0%, 100% { transform: translateY(0px) scale(1); }
  50%       { transform: translateY(-30px) scale(1.05); }
}

// 登录容器
.login-container {
  display: flex;
  width: 900px;
  max-width: 100%;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 32px 80px rgba(59, 130, 246, 0.12);
  position: relative;
  z-index: 1;
}

// 左侧品牌区
.brand-panel {
  width: 380px;
  min-width: 380px;
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 50%, #BFDBFE 100%);
  padding: 48px 40px;
  display: flex;
  align-items: center;
  border-right: 1px solid #BFDBFE;

  @media (max-width: 768px) {
    display: none;
  }
}

.brand-content {
  .brand-logo {
    width: 72px;
    height: 72px;
    border-radius: 20px;
    background: #EFF6FF;
    border: 1px solid #BFDBFE;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
  }

  .brand-title {
    font-size: 32px;
    font-weight: 700;
    color: $color-text-primary;
    margin: 0 0 8px;
    letter-spacing: -0.5px;
  }

  .brand-desc {
    font-size: 15px;
    color: $color-text-secondary;
    margin: 0 0 40px;
  }

  .brand-features {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .feature-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    color: $color-text-secondary;
    line-height: 1.4;
  }
}

// 右侧表单区
.form-panel {
  flex: 1;
  background: #fff;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-card {
  width: 100%;
  max-width: 360px;
}

.form-header {
  margin-bottom: 32px;
  h2 {
    font-size: 26px;
    font-weight: 700;
    color: $color-text-primary;
    margin: 0 0 8px;
  }
  p {
    font-size: 14px;
    color: $color-text-secondary;
    margin: 0;
  }
}

// Tab 切换
.tab-nav {
  display: flex;
  background: $color-bg-page;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;
}

.tab-btn {
  flex: 1;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: $color-text-secondary;
  cursor: pointer;
  transition: all 200ms;

  &.active {
    background: #fff;
    color: $color-text-primary;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  &:hover:not(.active) {
    color: $color-text-primary;
  }
}

// 表单字段
.form-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-size: 13px;
    font-weight: 600;
    color: $color-text-regular;
  }
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  background: $color-bg-page;
  border: 1.5px solid $color-border;
  border-radius: 12px;
  padding: 0 14px;
  height: 48px;
  transition: all 200ms;

  &:focus-within {
    border-color: $color-primary;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }

  .input-icon {
    color: $color-text-placeholder;
    flex-shrink: 0;
  }

  input {
    flex: 1;
    border: none;
    outline: none;
    background: transparent;
    font-family: $font-family-base;
    font-size: 14px;
    color: $color-text-primary;

    &::placeholder {
      color: $color-text-placeholder;
    }
  }

  .toggle-pwd {
    border: none;
    background: transparent;
    padding: 4px;
    cursor: pointer;
    color: $color-text-placeholder;
    display: flex;
    align-items: center;
    transition: color 150ms;
    &:hover { color: $color-text-secondary; }
  }
}

// 提交按钮
.submit-btn {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 12px;
  background: $color-primary;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 200ms;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 1px;
  margin-top: 4px;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.3);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.loading-dots {
  span {
    animation: blink 1.4s infinite;
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40%            { opacity: 1; }
}

// Tab 切换动画
.tab-enter-active, .tab-leave-active {
  transition: opacity 200ms, transform 200ms;
}
.tab-enter-from {
  opacity: 0;
  transform: translateX(16px);
}
.tab-leave-to {
  opacity: 0;
  transform: translateX(-16px);
}
</style>
