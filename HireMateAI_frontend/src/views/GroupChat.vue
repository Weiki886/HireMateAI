<template>
  <div class="group-chat-page">
    <!-- ══════════════════════════════════════
         页面标题区
    ══════════════════════════════════════ -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">AI 专家群聊</h1>
        <p class="page-desc">三位 AI 专家同时为你提供多维度职业建议</p>
      </div>
      <div class="header-actions">
        <el-button v-if="sessionId" type="danger" plain @click="handleLeaveSession">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          结束会话
        </el-button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         专家团队介绍（未开启会话时）
    ══════════════════════════════════════ -->
    <div v-if="!sessionId" class="intro-section">
      <div class="expert-cards">
        <div class="expert-intro-card" v-for="expert in experts" :key="expert.role" :style="{ '--accent': expert.color }">
          <div class="expert-icon" v-html="expert.icon"></div>
          <div class="expert-body">
            <h3>{{ expert.name }}</h3>
            <p>{{ expert.desc }}</p>
          </div>
          <div class="expert-tags">
            <span v-for="tag in expert.tags" :key="tag" class="expert-tag">{{ tag }}</span>
          </div>
        </div>
      </div>

      <div class="start-session-card">
        <h2>开启多专家会话</h2>
        <p>配置会话信息，三位专家将同时为你服务</p>
        <div class="session-form">
          <div class="form-field">
            <label>目标岗位</label>
            <el-input v-model="sessionForm.targetPosition" placeholder="如：Java高级开发工程师" size="large" />
          </div>
          <div class="form-field">
            <label>简历 PDF（可选）</label>
            <el-upload
              ref="resumeUploadRef"
              class="gc-resume-upload"
              :auto-upload="false"
              :limit="1"
              accept=".pdf,application/pdf"
              :on-change="handleResumeFileChange"
              :on-remove="handleResumeFileRemove"
            >
              <template #trigger>
                <el-button type="primary" plain>选择 PDF 文件</el-button>
              </template>
              <template #tip>
                <div class="upload-tip">仅支持 PDF，最大 10MB；上传后专家将结合简历内容给建议</div>
              </template>
            </el-upload>
          </div>
          <el-button type="primary" size="large" :loading="creating" class="start-session-btn" @click="handleCreateSession">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            发起群聊
          </el-button>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         群聊会话界面（统一聊天流）
    ══════════════════════════════════════ -->
    <div v-else class="chat-wrapper">
      <!-- 左侧：统一聊天流 -->
      <div class="chat-main-panel">
        <div class="chat-container">
          <div class="messages-list" ref="messagesContainer">
            <!-- 欢迎消息 -->
            <div v-if="messages.length === 0" class="welcome-msg">
              <div class="welcome-icons">
                <div class="mini-avatar" style="background: #F59E0B; z-index: 3">HR</div>
                <div class="mini-avatar" style="background: #6366F1; z-index: 2">TECH</div>
                <div class="mini-avatar" style="background: #10B981; z-index: 1">CP</div>
              </div>
              <h3>三位专家已就位</h3>
              <p>输入你的问题，三位专家将同时从不同角度为你解答</p>
            </div>

            <!-- 消息列表 -->
            <div v-for="(msg, index) in messages" :key="index" class="msg-block">
              <!-- 用户消息 -->
              <div v-if="msg.role === 'USER'" class="msg-user">
                <div class="msg-avatar avatar-user">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <div class="msg-bubble msg-bubble--user">{{ msg.content }}</div>
              </div>

              <!-- AI 专家回复（三人卡片组） -->
              <div v-else-if="msg.responses && msg.responses.length > 0" class="msg-experts">
                <div class="expert-response-card" v-for="resp in msg.responses" :key="resp.role"
                  :style="{ '--accent': getExpertColor(resp.role) }">
                  <div class="erc-header">
                    <div class="erc-avatar">{{ getExpertShort(resp.role) }}</div>
                    <span class="erc-name">{{ getExpertName(resp.role) }}</span>
                    <span class="erc-badge">{{ getExpertBadge(resp.role) }}</span>
                  </div>
                  <div class="erc-content" v-html="formatContent(resp.content)"></div>
                </div>
              </div>
            </div>

            <!-- 加载中状态（仅在非流式响应时显示骨架屏） -->
            <div v-if="loading && !messages.some(m => m._streamId)" class="loading-experts">
              <div class="expert-response-card loading" v-for="i in 3" :key="i">
                <div class="erc-header">
                  <div class="erc-avatar skeleton" :style="{ width: '32px', height: '32px' }"></div>
                  <div class="skeleton" :style="{ width: '80px', height: '14px' }"></div>
                </div>
                <div class="erc-content">
                  <div class="skeleton" style="width: 100%; height: 14px; margin-bottom: 6px"></div>
                  <div class="skeleton" style="width: 75%; height: 14px; margin-bottom: 6px"></div>
                  <div class="skeleton" style="width: 60%; height: 14px"></div>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入框 -->
          <div class="input-area">
            <ChatInput
              v-model="inputMessage"
              :streaming="loading"
              placeholder="输入你的问题，三位专家将同时为你解答..."
              :rows="3"
              @send="handleSend"
            />
          </div>
        </div>
      </div>

      <!-- 右侧：专家概览 -->
      <div class="experts-sidebar">
        <div class="sidebar-header">
          <h3>专家团队</h3>
          <span class="session-badge">会话进行中</span>
        </div>
        <div class="expert-list">
          <div v-for="expert in experts" :key="expert.role" class="sidebar-expert" :style="{ '--accent': expert.color }">
            <div class="sidebar-avatar">{{ expert.short }}</div>
            <div class="sidebar-info">
              <div class="sidebar-name">{{ expert.name }}</div>
              <div class="sidebar-desc">{{ expert.descShort }}</div>
            </div>
          </div>
        </div>
        <div class="session-meta">
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
            <span>{{ currentSession.targetPosition || '未指定岗位' }}</span>
          </div>
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>{{ messages.filter(m => m.role === 'USER').length }} 轮对话</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  createGroupSession,
  sendGroupMessage,
  getGroupHistory,
  destroyGroupSession
} from '@/api/groupChat'
import ChatInput from '@/components/ui/ChatInput.vue'

const sessionId = ref(null)
const creating = ref(false)
const loading = ref(false)
const inputMessage = ref('')
const messagesContainer = ref()
const messages = ref([])

const currentSession = ref({ targetPosition: '' })
const streamingStates = ref({})

const sessionForm = ref({
  targetPosition: '',
  jdText: ''
})

const resumeUploadRef = ref()
const resumePdfFile = ref(null)

const experts = [
  {
    role: 'HR总监',
    name: 'HR 总监',
    short: 'HR',
    desc: '15年招聘经验，擅长简历优化、面试表现指导',
    descShort: '简历优化 · 面试指导',
    color: '#F59E0B',
    icon: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`,
    tags: ['简历优化', '薪资谈判', '团队适配']
  },
  {
    role: '技术专家',
    name: '技术专家',
    short: 'TECH',
    desc: '12年技术经验，精通Java、分布式系统架构',
    descShort: '技术评估 · 系统设计',
    color: '#6366F1',
    icon: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#6366F1" stroke-width="1.8"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`,
    tags: ['技术深度', '架构设计', '学习潜力']
  },
  {
    role: '职业规划师',
    name: '职业规划师',
    short: 'CP',
    desc: '专注科技行业，帮你规划3-5年成长路径',
    descShort: '职业规划 · 成长路径',
    color: '#10B981',
    icon: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
    tags: ['成长路径', '行业选择', '转型建议']
  }
]

const getExpertColor = (role) => {
  return experts.find(e => e.role === role)?.color || '#3B82F6'
}

const getExpertShort = (role) => {
  return experts.find(e => e.role === role)?.short || '?'
}

const getExpertName = (role) => {
  return experts.find(e => e.role === role)?.name || role
}

const getExpertBadge = (role) => {
  return experts.find(e => e.role === role)?.descShort || ''
}

const getExpertDesc = (role) => {
  return experts.find(e => e.role === role)?.desc || ''
}

/** 后端 history 为扁平列表：USER → 三位专家各一条；界面需要 USER + { role:'AI', responses:[] } */
const EXPERT_ROLES = ['HR总监', '技术专家', '职业规划师']

function foldGroupChatHistory(flat) {
  if (!flat?.length) return []
  const result = []
  let i = 0
  const isExpert = (role) => EXPERT_ROLES.includes(role)
  while (i < flat.length) {
    const row = flat[i]
    if (row.role === 'USER') {
      result.push({ role: 'USER', content: row.content || '', timestamp: row.timestamp })
      i++
      const responses = []
      while (i < flat.length && isExpert(flat[i].role)) {
        responses.push({ role: flat[i].role, content: flat[i].content || '' })
        i++
      }
      if (responses.length > 0) {
        result.push({ role: 'AI', responses, timestamp: row.timestamp })
      }
    } else {
      i++
    }
  }
  return result
}

const handleResumeFileChange = (uploadFile) => {
  const raw = uploadFile.raw
  if (!raw) return
  const name = (raw.name || '').toLowerCase()
  const okType = !raw.type || raw.type === 'application/pdf'
  const okExt = name.endsWith('.pdf')
  if (!okType || !okExt) {
    ElMessage.warning('请上传 PDF 文件')
    resumeUploadRef.value?.clearFiles()
    resumePdfFile.value = null
    return
  }
  resumePdfFile.value = raw
}

const handleResumeFileRemove = () => {
  resumePdfFile.value = null
}

const handleCreateSession = async () => {
  creating.value = true
  try {
    const res = await createGroupSession({
      targetPosition: sessionForm.value.targetPosition,
      jdText: sessionForm.value.jdText,
      resumePdf: resumePdfFile.value
    })
    sessionId.value = res.data.sessionId
    currentSession.value.targetPosition = res.data.targetPosition
    messages.value = []
    resumePdfFile.value = null
    resumeUploadRef.value?.clearFiles()
    ElMessage.success('群聊会话已创建，三位专家已就位')
  } catch (error) {
    ElMessage.error('创建会话失败')
  } finally {
    creating.value = false
  }
}

const handleLeaveSession = async () => {
  try {
    await destroyGroupSession()
    sessionId.value = null
    messages.value = []
    resumePdfFile.value = null
    resumeUploadRef.value?.clearFiles()
    ElMessage.success('已离开群聊')
  } catch (error) {
    ElMessage.error('离开失败')
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleSend = async (text) => {
  if (!text || loading.value) return

  messages.value.push({
    role: 'USER',
    content: text,
    timestamp: new Date().toISOString()
  })
  scrollToBottom()
  loading.value = true

  const tmpId = 'tmp-' + Date.now()
  messages.value.push({
    role: 'AI',
    responses: [
      { role: 'HR总监', content: '' },
      { role: '技术专家', content: '' },
      { role: '职业规划师', content: '' }
    ],
    timestamp: new Date().toISOString(),
    _streamId: tmpId
  })

  const token = localStorage.getItem('token')

  try {
    const response = await fetch('/api/group-chat/message/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify({ message: text })
    })

    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.error('登录状态无效，请重新登录')
        window.location.href = '/login'
        return
      }
      throw new Error('Stream request failed')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.startsWith('data:')) continue
        const jsonStr = line.slice(5).trim()
        if (!jsonStr) continue

        try {
          const event = JSON.parse(jsonStr)
          const aiMsg = messages.value.find(m => m._streamId === tmpId)
          if (!aiMsg) continue

          if (event.type === 'chunk' && event.role && event.content !== undefined) {
            const resp = aiMsg.responses.find(r => r.role === event.role)
            if (resp) resp.content = event.content
            scrollToBottom()
          } else if (event.type === 'done' && event.results) {
            aiMsg.responses = event.results.map(r => ({
              role: r.role,
              content: r.content || ''
            }))
            delete aiMsg._streamId
          }
        } catch {
          // ignore parse error
        }
      }
    }

    scrollToBottom()
  } catch {
    const aiIdx = messages.value.findIndex(m => m._streamId === tmpId)
    if (aiIdx !== -1) messages.value.splice(aiIdx, 1)
    messages.value.pop()
    ElMessage.error('发送消息失败，请重试')
  } finally {
    loading.value = false
  }
}

const formatContent = (content) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
}

onMounted(async () => {
  try {
    const res = await getGroupHistory()
    if (res.data && res.data.length > 0) {
      messages.value = foldGroupChatHistory(res.data)
      sessionId.value = 'active'
    }
  } catch {
    // ignore
  }
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.group-chat-page {
  max-width: 1300px;
  margin: 0 auto;
}

// ── 页面标题区 ───────────────────────────────
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: $spacing-6;

  .header-left {
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
}

// ── 专家介绍（未开启时） ──────────────────────
.intro-section {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: $spacing-6;
  align-items: start;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
  }
}

.expert-cards {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.expert-intro-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-5;
  border: 1px solid $color-border-light;
  display: flex;
  align-items: center;
  gap: $spacing-4;
  transition: all $duration-normal;

  &:hover {
    transform: translateX(4px);
    border-color: var(--accent);
    box-shadow: 0 4px 16px color-mix(in srgb, var(--accent) 15%, transparent);
  }

  .expert-icon {
    width: 56px;
    height: 56px;
    border-radius: $radius-xl;
    background: color-mix(in srgb, var(--accent) 10%, transparent);
    border: 1.5px solid color-mix(in srgb, var(--accent) 25%, transparent);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .expert-body {
    flex: 1;
    h3 {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin: 0 0 4px;
    }
    p {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      margin: 0;
      line-height: $line-height-base;
    }
  }

  .expert-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }
}

.expert-tag {
  font-size: $font-size-xs;
  padding: 3px 10px;
  border-radius: $radius-full;
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  color: var(--accent);
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

// ── 开启会话卡片 ──────────────────────────────
.start-session-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-6;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-md;

  h2 {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 8px;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin: 0 0 24px;
  }
}

.session-form {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .form-field {
    display: flex;
    flex-direction: column;
    gap: 6px;

    label {
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $color-text-regular;
    }

    .upload-tip {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
      margin-top: 4px;
      line-height: $line-height-base;
    }

    :deep(.gc-resume-upload .el-upload-list) {
      margin-top: $spacing-2;
    }
  }
}

.start-session-btn {
  width: 100%;
  height: 48px;
  border-radius: $radius-xl;
  font-size: $font-size-base;
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

// ── 群聊界面 ──────────────────────────────
.chat-wrapper {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: $spacing-5;
  height: calc(100vh - 56px - 48px - 80px);
  min-height: 500px;

  @media (max-width: 900px) {
    grid-template-columns: 1fr;
    .experts-sidebar { display: none; }
  }
}

.chat-container {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: $shadow-md;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-msg {
  text-align: center;
  padding: 60px 20px;
  animation: fade-in 0.4s ease;

  .welcome-icons {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
    .mini-avatar {
      width: 40px;
      height: 40px;
      border-radius: $radius-full;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: $font-size-xs;
      font-weight: $font-weight-bold;
      margin-left: -8px;
      border: 2px solid #fff;
      &:first-child { margin-left: 0; }
    }
  }

  h3 {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 8px;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin: 0;
  }
}

// 用户消息
.msg-user {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  animation: msg-in 0.25s ease;

  .msg-avatar {
    width: 36px;
    height: 36px;
    border-radius: $radius-full;
    background: $color-primary;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    align-self: flex-end;
  }

  .msg-bubble {
    max-width: 72%;
    padding: 12px 16px;
    border-radius: 18px 18px 4px 18px;
    font-size: $font-size-base;
    line-height: 1.7;

    &--user {
      background: $color-primary;
      color: #fff;
    }
  }
}

// 专家回复
.msg-experts {
  display: flex;
  flex-direction: column;
  gap: 10px;
  animation: msg-in 0.25s ease;
}

.expert-response-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: 16px 18px;
  border: 1px solid $color-border-light;
  border-left: 3px solid var(--accent);
  box-shadow: $shadow-xs;
  transition: all $duration-fast;

  &:hover {
    box-shadow: $shadow-sm;
  }

  &.loading {
    opacity: 0.7;
  }

  .erc-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
  }

  .erc-avatar {
    width: 32px;
    height: 32px;
    border-radius: $radius-lg;
    background: var(--accent);
    color: #fff;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .erc-name {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: var(--accent);
  }

  .erc-badge {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  .erc-content {
    font-size: $font-size-sm;
    color: $color-text-regular;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;

    :deep(.inline-code) {
      background: rgba(102, 126, 234, 0.08);
      color: $color-primary;
      padding: 1px 6px;
      border-radius: 4px;
      font-family: monospace;
    }
  }
}

// 加载动画
.loading-experts {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

// 输入区
.input-area {
  border-top: 1px solid $color-border-light;
}

// ── 专家侧边栏 ──────────────────────────────
.experts-sidebar {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: $shadow-sm;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid $color-border-light;
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0;
  }

  .session-badge {
    font-size: $font-size-xs;
    padding: 2px 8px;
    border-radius: $radius-full;
    background: #DCFCE7;
    color: $color-success;
    font-weight: $font-weight-medium;
  }
}

.expert-list {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-expert {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: $radius-xl;
  transition: all $duration-fast;

  &:hover {
    background: $color-bg-hover;
  }

  .sidebar-avatar {
    width: 36px;
    height: 36px;
    border-radius: $radius-lg;
    background: var(--accent);
    color: #fff;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .sidebar-name {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  .sidebar-desc {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.session-meta {
  padding: 16px;
  border-top: 1px solid $color-border-light;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

// 动画
@keyframes fade-in {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes msg-in {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
