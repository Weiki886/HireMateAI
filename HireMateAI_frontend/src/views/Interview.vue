<template>
  <div class="interview-page">
    <!-- 子导航：开始面试 / 面试记录（进行中会话时隐藏） -->
    <div v-if="!sessionId" class="interview-subnav">
      <div class="interview-tabs" role="tablist">
        <button
          type="button"
          role="tab"
          class="interview-tab"
          :class="{ 'interview-tab--active': mainTab === 'start' }"
          :aria-selected="mainTab === 'start'"
          @click="setMainTab('start')"
        >
          开始面试
        </button>
        <button
          type="button"
          role="tab"
          class="interview-tab"
          :class="{ 'interview-tab--active': mainTab === 'records' }"
          :aria-selected="mainTab === 'records'"
          @click="setMainTab('records')"
        >
          面试记录
        </button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         阶段一：配置面试参数
    ══════════════════════════════════════ -->
    <div class="interview-setup" v-if="!sessionId && mainTab === 'start'">

      <!-- 页面标题 -->
      <div class="page-title-bar">
        <h1 class="page-title">AI 模拟面试</h1>
        <p class="page-desc">选择岗位和面试类型，开启专业 AI 面试体验</p>
      </div>

      <div class="setup-grid">
        <!-- 左侧：配置表单 -->
        <div class="setup-form-card">
          <div class="form-header">
            <h2>面试配置</h2>
          </div>

          <div class="form-body">
            <!-- 岗位 -->
            <div class="field-group">
              <label class="field-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                目标岗位
              </label>
              <el-input
                v-model="form.jobPosition"
                placeholder="如：前端开发工程师、Java 后端工程师"
                size="large"
                clearable
                class="hm-input"
              />
            </div>

            <!-- 面试类型 -->
            <div class="field-group">
              <label class="field-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                面试类型
              </label>
              <div class="type-grid">
                <div
                  v-for="type in interviewTypes"
                  :key="type.value"
                  class="type-card"
                  :class="{ 'type-card--active': form.interviewType === type.value }"
                  @click="form.interviewType = type.value"
                >
                  <div class="type-icon" v-html="type.icon"></div>
                  <div class="type-label">{{ type.label }}</div>
                </div>
              </div>
            </div>

            <!-- 简历上传 -->
            <div class="field-group">
              <label class="field-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                上传简历（可选）
              </label>
              <el-upload
                ref="uploadRef"
                :auto-upload="false"
                :limit="1"
                accept=".pdf,.doc,.docx,.txt"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                class="hm-upload"
                drag
              >
                <div class="upload-content">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.5">
                    <polyline points="16 16 12 12 8 16"/>
                    <line x1="12" y1="12" x2="12" y2="21"/>
                    <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/>
                  </svg>
                  <p>拖拽文件或<span class="upload-link">点击上传</span></p>
                  <span class="upload-hint">支持 PDF、Word、TXT 格式</span>
                </div>
              </el-upload>
            </div>

            <!-- 开始按钮 -->
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="start-btn"
              @click="handleStart"
            >
              <svg v-if="!loading" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <polygon points="5 3 19 12 5 21 5 3"/>
              </svg>
              {{ loading ? '创建中...' : '开始面试' }}
            </el-button>
          </div>
        </div>

        <!-- 右侧：说明卡片 -->
        <div class="setup-info-card">
          <div class="info-card">
            <div class="info-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="16" x2="12" y2="12"/>
                <line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
            </div>
            <h3>面试说明</h3>
            <ul>
              <li>AI 面试官将根据你选择的岗位和类型进行提问</li>
              <li>上传简历可让 AI 更有针对性地评估</li>
              <li>回答完毕后，AI 会给出专业的反馈和建议</li>
              <li>面试记录会自动保存，随时可回顾</li>
            </ul>
          </div>

          <div class="info-card">
            <div class="info-icon info-icon--success">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <h3>面试技巧</h3>
            <ul>
              <li>回答时条理清晰，使用 STAR 法则</li>
              <li>遇到不会的问题，诚实说明并展示学习意愿</li>
              <li>保持自信，眼神交流（如果是视频面试）</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <InterviewHistoryPanel
      v-else-if="!sessionId && mainTab === 'records'"
      @start-interview="setMainTab('start')"
    />

    <!-- ══════════════════════════════════════
         阶段二：聊天界面
    ══════════════════════════════════════ -->
    <div v-else-if="sessionId" class="interview-session">
      <!-- 顶部信息栏 -->
      <div class="session-header-bar">
        <div class="session-meta">
          <div class="session-tag" :style="{ background: getTypeColor(form.interviewType) }">
            {{ form.interviewType }}
          </div>
          <span class="session-position">{{ form.jobPosition || '未指定岗位' }}</span>
        </div>
        <el-button size="small" plain @click="handleEnd" class="end-btn">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/></svg>
          结束面试
        </el-button>
      </div>

      <!-- 消息列表 -->
      <div class="chat-main">
        <MessageList :messages="messages" />
      </div>

      <!-- 输入框 -->
      <ChatInput
        v-model="inputText"
        :streaming="streaming"
        placeholder="输入你的回答，AI 面试官将为你点评..."
        :rows="3"
        @send="handleSend"
        @stop="handleStop"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createSession, closeSession } from '@/api/interview'
import { consumeSseJson } from '@/utils/sse'
import MessageList from '@/components/ui/MessageList.vue'
import ChatInput from '@/components/ui/ChatInput.vue'
import InterviewHistoryPanel from '@/components/interview/InterviewHistoryPanel.vue'

const route = useRoute()
const router = useRouter()

const mainTab = ref('start')

const syncTabFromRoute = () => {
  mainTab.value = route.query.tab === 'records' ? 'records' : 'start'
}

watch(() => [route.path, route.query.tab], () => {
  if (route.path === '/interview') syncTabFromRoute()
}, { immediate: true })

const setMainTab = (tab) => {
  mainTab.value = tab
  if (tab === 'records') {
    router.replace({ path: '/interview', query: { tab: 'records' } })
  } else {
    router.replace({ path: '/interview' })
  }
}
const uploadRef = ref()
const loading = ref(false)
const streaming = ref(false)
const sessionId = ref(null)
const inputText = ref('')
const resumeText = ref('')
const messages = ref([])

const form = ref({
  jobPosition: '',
  interviewType: '综合面试'
})

const interviewTypes = [
  {
    value: '综合面试',
    label: '综合面试',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`
  },
  {
    value: '技术面试',
    label: '技术面试',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`
  },
  {
    value: 'HR面试',
    label: 'HR 面试',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`
  },
  {
    value: '行为面试',
    label: '行为面试',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`
  },
  {
    value: '终面',
    label: '终面',
    icon: `<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`
  }
]

const getTypeColor = (type) => {
  const map = {
    '技术面试': 'rgba(102, 126, 234, 0.12)',
    'HR面试': 'rgba(16, 185, 129, 0.12)',
    '综合面试': 'rgba(99, 102, 241, 0.12)',
    '行为面试': 'rgba(245, 158, 11, 0.12)',
    '终面': 'rgba(236, 72, 153, 0.12)',
  }
  return map[type] || 'rgba(102, 126, 234, 0.12)'
}

const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    resumeText.value = e.target.result
  }
  if (file.raw) {
    reader.readAsText(file.raw)
  }
}

const handleFileRemove = () => {
  resumeText.value = ''
}

const handleStart = async () => {
  if (!form.value.interviewType) {
    ElMessage.warning('请选择面试类型')
    return
  }

  loading.value = true
  try {
    const res = await createSession({
      jobPosition: form.value.jobPosition,
      interviewType: form.value.interviewType,
      resumeText: resumeText.value
    })

    sessionId.value = res.data.sessionId

    messages.value.push({
      role: 'ai',
      content: `你好！我是 HireMate AI 面试助手。我将为你进行一场${form.value.interviewType}，${form.value.jobPosition ? `针对【${form.value.jobPosition}】岗位` : ''}。\n\n请准备好后，我会开始向你提问。准备好了吗？`,
      streaming: false
    })
  } catch (error) {
    console.error('Failed to create session:', error)
    ElMessage.error('创建面试会话失败')
  } finally {
    loading.value = false
  }
}

const handleSend = async (userMessage) => {
  if (!userMessage || streaming.value) return

  messages.value.push({ role: 'user', content: userMessage, streaming: false })
  messages.value.push({ role: 'ai', content: '', streaming: true })
  const aiIndex = messages.value.length - 1

  streaming.value = true

  try {
    const response = await fetch(`/api/interview/session/${sessionId.value}/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ content: userMessage })
    })

    if (!response.ok) {
      const errText = await response.text().catch(() => '')
      throw new Error(errText || `HTTP ${response.status}`)
    }

    let aiContent = ''
    await consumeSseJson(response, (event) => {
      if (event.type === 'message' && typeof event.data.content === 'string') {
        aiContent += event.data.content
        const row = messages.value[aiIndex]
        if (row?.role === 'ai') {
          row.content = aiContent
        }
      } else if (event.type === 'error') {
        ElMessage.error(event.data.error || 'AI服务错误')
      }
    })

    const row = messages.value[aiIndex]
    if (row?.role === 'ai' && !row.content) {
      row.content = '（未收到回复，请重试）'
    }
  } catch (error) {
    console.error('Failed to send message:', error)
    ElMessage.error('发送消息失败，请重试')
    const row = messages.value[aiIndex]
    if (row?.role === 'ai' && !row.content) {
      row.content = '发送失败，请检查网络后重试。'
    }
  } finally {
    const row = messages.value[aiIndex]
    if (row?.role === 'ai') {
      row.streaming = false
    }
    streaming.value = false
  }
}

const handleStop = () => {
  streaming.value = false
}

const handleEnd = async () => {
  try {
    await closeSession(sessionId.value)
    ElMessage.success('面试已结束')
    router.push({ path: '/interview', query: { tab: 'records' } })
  } catch (error) {
    console.error('Failed to close session:', error)
  }
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.interview-page {
  max-width: 1100px;
  margin: 0 auto;
}

.interview-subnav {
  margin-bottom: $spacing-5;
}

.interview-tabs {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  background: $color-bg-card;
  border: 1px solid $color-border-light;
  border-radius: $radius-xl;
}

.interview-tab {
  border: none;
  background: transparent;
  padding: 10px 20px;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: color $duration-fast, background $duration-fast;

  &:hover {
    color: $color-text-primary;
    background: $color-bg-hover;
  }

  &--active {
    color: $color-primary;
    font-weight: $font-weight-semibold;
    background: $color-bg-active;
    box-shadow: $shadow-xs;
  }
}

// ══════════════════════════════════════
// 阶段一：配置页
// ══════════════════════════════════════
.page-title-bar {
  margin-bottom: $spacing-6;

  .page-title {
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 8px;
  }

  .page-desc {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 0;
  }
}

.setup-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: $spacing-6;
  align-items: start;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.setup-form-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  box-shadow: $shadow-md;
  overflow: hidden;
  border: 1px solid $color-border-light;

  .form-header {
    padding: 24px 28px;
    border-bottom: 1px solid $color-border-light;

    h2 {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin: 0;
    }
  }

  .form-body {
    padding: 28px;
    display: flex;
    flex-direction: column;
    gap: 24px;
  }
}

.field-group {
  .field-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-regular;
    margin-bottom: 10px;

    svg { color: $color-primary; }
  }
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;

  @media (max-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.type-card {
  padding: 14px 12px;
  border-radius: $radius-xl;
  border: 1.5px solid $color-border;
  cursor: pointer;
  text-align: center;
  transition: all $duration-normal $ease-default;
  background: $color-bg-card;

  .type-icon {
    color: $color-text-secondary;
    margin-bottom: 6px;
    display: flex;
    justify-content: center;
  }

  .type-label {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
  }

  &:hover {
    border-color: $color-primary;
    background: $color-bg-active;

    .type-icon, .type-label { color: $color-primary; }
  }

  &--active {
    border-color: $color-primary;
    background: rgba(102, 126, 234, 0.06);

    .type-icon { color: $color-primary; }
    .type-label {
      color: $color-primary;
      font-weight: $font-weight-semibold;
    }
  }
}

.hm-upload {
  width: 100%;

  :deep(.el-upload) { width: 100%; }

  :deep(.el-upload-dragger) {
    width: 100%;
    height: 120px;
    border-radius: $radius-xl;
    border: 1.5px dashed $color-border;
    background: $color-bg-page;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all $duration-normal;

    &:hover {
      border-color: $color-primary;
      background: $color-bg-active;
    }
  }
}

.upload-content {
  text-align: center;

  p {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 8px 0 4px;
  }

  .upload-link {
    color: $color-primary;
    font-weight: $font-weight-medium;
  }

  .upload-hint {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

.start-btn {
  width: 100%;
  height: 52px;
  border-radius: $radius-xl;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  background: $color-primary;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all $duration-normal $ease-spring;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-primary-lg;
  }
}

// 右侧说明卡片
.setup-info-card {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.info-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: 24px;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-sm;

  .info-icon {
    width: 40px;
    height: 40px;
    border-radius: $radius-lg;
    background: rgba(102, 126, 234, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 12px;

    &--success {
      background: rgba(16, 185, 129, 0.08);
    }
  }

  h3 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 12px;
  }

  ul {
    margin: 0;
    padding-left: 16px;

    li {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      line-height: $line-height-loose;
      margin-bottom: 4px;

      &:last-child { margin-bottom: 0; }
    }
  }
}

// ══════════════════════════════════════
// 阶段二：聊天界面
// ══════════════════════════════════════
.interview-session {
  height: calc(100vh - 56px - 48px);
  display: flex;
  flex-direction: column;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  overflow: hidden;
  box-shadow: $shadow-md;
  border: 1px solid $color-border-light;
}

.session-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-bottom: 1px solid $color-border-light;
  background: $color-bg-card;
  flex-shrink: 0;

  .session-meta {
    display: flex;
    align-items: center;
    gap: 10px;

    .session-tag {
      padding: 4px 12px;
      border-radius: $radius-full;
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $color-primary;
    }

    .session-position {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
    }
  }

  .end-btn {
    border-radius: $radius-lg;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.chat-main {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
