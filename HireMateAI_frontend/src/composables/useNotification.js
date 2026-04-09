/**
 * 全局 SSE 通知（简历异步优化完成等）
 *
 * 注意：原生 EventSource 无法设置 Authorization 头，会导致 403。
 * 这里使用 fetch + ReadableStream 手动解析 SSE，与 axios 一样携带 Bearer Token。
 */
import { ref, readonly } from 'vue'
import { ElNotification } from 'element-plus'
import router from '@/router'

const isConnected = ref(false)

/** @type {AbortController | null} */
let abortController = null
/** @type {ReturnType<typeof setTimeout> | null} */
let reconnectTimer = null
let reconnectAttempt = 0
const callbacks = new Set()

function getToken() {
  return localStorage.getItem('token')
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

function scheduleReconnect() {
  clearReconnectTimer()
  const token = getToken()
  if (!token || !abortController) return

  reconnectAttempt++
  const delay = Math.min(30000, 2000 * Math.pow(2, Math.min(reconnectAttempt, 4)))
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    connectNotificationSSE()
  }, delay)
}

function dispatchToCallbacks(data) {
  callbacks.forEach((cb) => {
    try {
      cb(data)
    } catch (e) {
      console.warn('[通知] 回调异常:', e)
    }
  })
}

function flushSseEvent(eventName, dataLines) {
  if (dataLines.length === 0) return
  const dataStr = dataLines.join('\n')
  let data
  try {
    data = JSON.parse(dataStr)
  } catch {
    return
  }
  const effectiveEvent = eventName || 'message'
  if (effectiveEvent === 'notification' || data.type === 'RESUME_OPTIMIZED') {
    handleNotification(data)
  }
}

/**
 * 使用 fetch 建立 SSE，携带 JWT
 */
export async function connectNotificationSSE() {
  const token = getToken()
  if (!token) return

  if (abortController) return

  clearReconnectTimer()
  abortController = new AbortController()
  const { signal } = abortController

  try {
    const response = await fetch('/api/resume/notifications/subscribe', {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'text/event-stream'
      },
      signal
    })

    if (response.status === 401 || response.status === 403) {
      isConnected.value = false
      abortController = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElNotification({
        title: '登录状态无效',
        message: '通知通道无法连接，请重新登录',
        type: 'warning',
        duration: 5000
      })
      if (router.currentRoute.value.meta?.requiresAuth !== false) {
        router.push('/login').catch(() => {})
      }
      return
    }

    if (!response.ok || !response.body) {
      throw new Error(`SSE ${response.status}`)
    }

    isConnected.value = true
    reconnectAttempt = 0

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let currentEvent = 'message'
    let dataLines = []

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() ?? ''

      for (const raw of lines) {
        const line = raw.replace(/\r$/, '')
        if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trimStart())
        } else if (line === '') {
          flushSseEvent(currentEvent, dataLines)
          currentEvent = 'message'
          dataLines = []
        }
      }
    }
  } catch (e) {
    if (e.name === 'AbortError') {
      return
    }
    console.warn('[通知] SSE 读取异常:', e.message)
  } finally {
    isConnected.value = false
    abortController = null
    const t = getToken()
    if (t) {
      scheduleReconnect()
    }
  }
}

export function disconnectNotificationSSE() {
  clearReconnectTimer()
  reconnectAttempt = 0
  if (abortController) {
    abortController.abort()
    abortController = null
  }
  isConnected.value = false
}

function handleNotification(data) {
  if (data.type === 'RESUME_OPTIMIZED') {
    handleResumeOptimized(data)
  }
  dispatchToCallbacks(data)
}

function handleResumeOptimized(data) {
  const { status, targetPosition, recordId } = data

  if (status === 'COMPLETED') {
    ElNotification({
      title: '简历 AI 优化已完成',
      message: `「${targetPosition || '目标岗位'}」的分析结果已生成，点击前往简历管理查看`,
      type: 'success',
      duration: 0,
      onClick: () => {
        router.push('/resume').catch(() => {})
      }
    })
  } else if (status === 'FAILED') {
    ElNotification({
      title: '简历 AI 优化失败',
      message: `「${targetPosition || '目标岗位'}」未能完成分析，请重试`,
      type: 'error',
      duration: 8000,
      onClick: () => {
        router.push('/resume').catch(() => {})
      }
    })
  }
}

export function onNotification(callback) {
  if (typeof callback === 'function') {
    callbacks.add(callback)
  }
}

export function offNotification(callback) {
  if (callback) {
    callbacks.delete(callback)
  } else {
    callbacks.clear()
  }
}

export function useNotification() {
  return {
    isConnected: readonly(isConnected),
    connect: connectNotificationSSE,
    disconnect: disconnectNotificationSSE,
    on: onNotification,
    off: offNotification
  }
}
