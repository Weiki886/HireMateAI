import request from '@/utils/request'

/**
 * @param {{ targetPosition?: string, jdText?: string, resumePdf?: File | null }} payload
 */
export function createGroupSession(payload) {
  const formData = new FormData()
  formData.append('targetPosition', payload.targetPosition ?? '')
  if (payload.jdText) {
    formData.append('jdText', payload.jdText)
  }
  if (payload.resumePdf) {
    formData.append('resumePdf', payload.resumePdf)
  }
  return request({
    url: '/group-chat/session',
    method: 'POST',
    data: formData
  })
}

const GROUP_CHAT_MESSAGE_TIMEOUT_MS = 600000

export function sendGroupMessage(message) {
  return request({
    url: '/group-chat/message',
    method: 'POST',
    data: { message },
    timeout: GROUP_CHAT_MESSAGE_TIMEOUT_MS,
    suppressTimeoutToast: true
  })
}

export function getGroupHistory() {
  return request({
    url: '/group-chat/history',
    method: 'GET'
  })
}

export function destroyGroupSession() {
  return request({
    url: '/group-chat/session',
    method: 'DELETE'
  })
}
