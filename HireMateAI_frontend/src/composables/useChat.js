/**
 * useChat — AI 聊天核心逻辑
 * 封装：消息列表、SSE 流式消费、自动滚动、停止生成
 */
import { ref, nextTick, computed } from 'vue'
import { consumeSseJson } from '@/utils/sse'

export function useChat() {
  // ── State ────────────────────────────────────────────────
  const messages = ref([])
  const streaming = ref(false)
  const currentStreamingContent = ref('')
  let abortController = null

  // ── Computed ─────────────────────────────────────────────
  const lastMessage = computed(() => messages.value[messages.value.length - 1] ?? null)
  const isAILastMessage = computed(() => lastMessage.value?.role === 'ai')

  // ── 发送用户消息并获取 AI 流式回复 ──────────────────────
  const sendMessage = async (sessionId, userMessage, onError) => {
    if (streaming.value) return

    // 1. 添加用户消息
    messages.value.push({ role: 'user', content: userMessage })
    scrollToBottom()

    // 2. 占位 AI 消息（空内容，打字机效果）
    const aiIndex = messages.value.length
    messages.value.push({ role: 'ai', content: '', streaming: true })
    scrollToBottom()

    streaming.value = true
    currentStreamingContent.value = ''

    try {
      abortController = new AbortController()

      const response = await fetch(`/api/interview/session/${sessionId}/message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ content: userMessage }),
        signal: abortController.signal
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
            currentStreamingContent.value = aiContent
          }
          scrollToBottom()
        } else if (event.type === 'error') {
          console.error('SSE error:', event.data.error)
          onError?.(event.data.error || 'AI服务错误')
        }
      })

      const row = messages.value[aiIndex]
      if (row?.role === 'ai' && !row.content) {
        row.content = '（未收到回复，请重试）'
      }
    } catch (error) {
      if (error.name === 'AbortError') {
        const row = messages.value[aiIndex]
        if (row?.role === 'ai' && !row.content) {
          row.content = '（生成已停止）'
        }
      } else {
        onError?.(error.message || '发送消息失败，请重试')
        const row = messages.value[aiIndex]
        if (row?.role === 'ai' && !row.content) {
          row.content = '发送失败，请检查网络后重试。'
        }
      }
    } finally {
      const row = messages.value[aiIndex]
      if (row?.role === 'ai') {
        row.streaming = false
      }
      streaming.value = false
      currentStreamingContent.value = ''
      abortController = null
    }
  }

  // ── 停止生成 ──────────────────────────────────────────────
  const stopGenerating = () => {
    if (abortController) {
      abortController.abort()
    }
  }

  // ── 清空对话 ──────────────────────────────────────────────
  const clearMessages = () => {
    messages.value = []
  }

  // ── 加载历史消息 ──────────────────────────────────────────
  const loadMessages = (historyMessages) => {
    messages.value = historyMessages.map(m => ({
      role: m.role === 'user' ? 'user' : 'ai',
      content: m.content,
      streaming: false
    }))
  }

  // ── 内部：滚动到底部 ─────────────────────────────────────
  const scrollToBottom = (container) => {
    nextTick(() => {
      const el = container || document.querySelector('.messages')
      if (el) {
        el.scrollTop = el.scrollHeight
      }
    })
  }

  return {
    messages,
    streaming,
    currentStreamingContent,
    lastMessage,
    isAILastMessage,
    sendMessage,
    stopGenerating,
    clearMessages,
    loadMessages,
    scrollToBottom
  }
}