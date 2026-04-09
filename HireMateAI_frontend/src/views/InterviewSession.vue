<template>
  <div class="interview-session-page">
    <!-- 返回栏 -->
    <div class="session-topbar">
      <el-button text @click="handleBack" class="back-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        返回
      </el-button>
      <div class="session-meta">
        <div class="session-tag" :style="{ background: getTypeColor(sessionInfo?.interviewType) }">
          {{ sessionInfo?.interviewType || '面试' }}
        </div>
        <span class="session-position">{{ sessionInfo?.jobPosition || '面试会话' }}</span>
      </div>
      <el-button type="danger" plain size="small" @click="handleEnd">
        结束面试
      </el-button>
    </div>

    <!-- 聊天主体 -->
    <div class="session-body">
      <div class="chat-panel">
        <MessageList :messages="messages" />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSession, closeSession } from '@/api/interview'
import { getSessionDetail } from '@/api/history'
import { consumeSseJson } from '@/utils/sse'
import MessageList from '@/components/ui/MessageList.vue'
import ChatInput from '@/components/ui/ChatInput.vue'

const route = useRoute()
const router = useRouter()

const sessionId = ref(route.params.id)
const sessionInfo = ref(null)
const messages = ref([])
const inputText = ref('')
const streaming = ref(false)

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

const loadSession = async () => {
  try {
    const [infoRes, detailRes] = await Promise.all([
      getSession(sessionId.value),
      getSessionDetail(sessionId.value)
    ])
    sessionInfo.value = infoRes.data
    messages.value = (detailRes.data?.messages || []).map(m => ({
      role: m.role === 'user' ? 'user' : 'ai',
      content: m.content,
      streaming: false
    }))
  } catch (error) {
    console.error('Failed to load session:', error)
    ElMessage.error('加载会话失败')
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
        // aiIndex 指向最后一条 AI 气泡；上一条是用户消息，不能用 aiIndex - 1
        const row = messages.value[aiIndex]
        if (row?.role === 'ai') row.content = aiContent
      } else if (event.type === 'error') {
        ElMessage.error(event.data.error || 'AI服务错误')
      }
    })

    const row = messages.value[aiIndex]
    if (row?.role === 'ai' && !row.content) row.content = '（未收到回复，请重试）'
  } catch (error) {
    console.error('Failed to send message:', error)
    ElMessage.error('发送消息失败')
    const row = messages.value[aiIndex]
    if (row?.role === 'ai' && !row.content) row.content = '发送失败，请检查网络后重试。'
  } finally {
    const row = messages.value[aiIndex]
    if (row?.role === 'ai') row.streaming = false
    streaming.value = false
  }
}

const handleStop = () => {
  streaming.value = false
}

const handleBack = () => {
  router.push({ path: '/interview', query: { tab: 'records' } })
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

onMounted(() => {
  loadSession()
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.interview-session-page {
  height: calc(100vh - 56px - 48px);
  display: flex;
  flex-direction: column;
  gap: 0;
}

.session-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  margin-bottom: 0;

  .back-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: $color-text-secondary;
    font-size: $font-size-sm;
    padding: 6px 12px;
    border-radius: $radius-lg;
    &:hover {
      background: $color-bg-hover;
      color: $color-text-primary;
    }
  }

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
}

.session-body {
  flex: 1;
  overflow: hidden;
}

.chat-panel {
  height: 100%;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  overflow: hidden;
  box-shadow: $shadow-md;
  border: 1px solid $color-border-light;
  display: flex;
  flex-direction: column;
}
</style>
