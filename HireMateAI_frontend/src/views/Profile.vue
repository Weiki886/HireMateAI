<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">个人中心</h1>
        <p class="page-desc">查看账户信息，管理邮箱与登录密码</p>
      </div>
    </div>

    <div v-loading="loading" class="profile-layout">
      <!-- 账户概览 -->
      <section class="card overview-card">
        <div class="overview-avatar">
          {{ profile?.username?.charAt(0)?.toUpperCase() || 'U' }}
        </div>
        <div class="overview-body">
          <h2 class="overview-name">{{ profile?.username || '—' }}</h2>
          <p class="overview-email">{{ profile?.email || '—' }}</p>
          <div class="overview-meta">
            <span>用户 ID：{{ profile?.userId ?? '—' }}</span>
            <span class="sep">·</span>
            <span>注册时间：{{ formatDate(profile?.createdAt) }}</span>
          </div>
        </div>
      </section>

      <div class="profile-columns">
        <!-- 修改邮箱 -->
        <section class="card form-card">
          <h3 class="card-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
              <polyline points="22,6 12,13 2,6"/>
            </svg>
            联系邮箱
          </h3>
          <p class="card-hint">用于账号通知与找回，修改后即时生效。</p>
          <el-form ref="emailFormRef" :model="emailForm" :rules="emailRules" label-width="88px" class="hm-form">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="emailForm.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingEmail" @click="submitEmail">保存邮箱</el-button>
            </el-form-item>
          </el-form>
        </section>

        <!-- 修改密码 -->
        <section class="card form-card">
          <h3 class="card-title">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            登录密码
          </h3>
          <p class="card-hint">建议定期更换密码，不要使用过于简单的组合。</p>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="hm-form">
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" autocomplete="current-password" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" autocomplete="new-password" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" autocomplete="new-password" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPwd" @click="submitPassword">更新密码</el-button>
            </el-form-item>
          </el-form>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile, changePassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(true)
const profile = ref(null)
const savingEmail = ref(false)
const savingPwd = ref(false)

const emailFormRef = ref()
const pwdFormRef = ref()

const emailForm = reactive({ email: '' })
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const formatDate = (d) => {
  if (!d) return '—'
  const date = typeof d === 'string' ? new Date(d) : d
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadProfile = async () => {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data
    emailForm.email = res.data?.email || ''
    userStore.syncUserFromProfile(res.data)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const submitEmail = async () => {
  await emailFormRef.value?.validate()
  savingEmail.value = true
  try {
    const res = await updateProfile({ email: emailForm.email.trim() })
    profile.value = res.data
    userStore.syncUserFromProfile(res.data)
    ElMessage.success('邮箱已更新')
  } catch (e) {
    console.error(e)
  } finally {
    savingEmail.value = false
  }
}

const submitPassword = async () => {
  await pwdFormRef.value?.validate()
  savingPwd.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码已更新，请使用新密码登录')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdFormRef.value?.resetFields()
  } catch (e) {
    console.error(e)
  } finally {
    savingPwd.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.profile-page {
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: $spacing-6;

  .page-title {
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 6px;
  }

  .page-desc {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 0;
  }
}

.profile-layout {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

.card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
  padding: $spacing-6;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: $spacing-6;
}

.overview-avatar {
  width: 72px;
  height: 72px;
  border-radius: $radius-full;
  background: $color-primary;
  color: #fff;
  font-size: 28px;
  font-weight: $font-weight-bold;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.overview-body {
  min-width: 0;

  .overview-name {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 4px;
  }

  .overview-email {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 0 0 10px;
    word-break: break-all;
  }

  .overview-meta {
    font-size: $font-size-sm;
    color: $color-text-placeholder;

    .sep {
      margin: 0 6px;
      color: $color-border;
    }
  }
}

.profile-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-5;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.form-card {
  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 8px;

    svg {
      color: $color-primary;
    }
  }

  .card-hint {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin: 0 0 $spacing-5;
    line-height: $line-height-base;
  }
}

.hm-form {
  max-width: 100%;
}
</style>
