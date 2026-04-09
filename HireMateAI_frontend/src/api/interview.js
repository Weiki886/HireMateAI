import request from '@/utils/request'

export function createSession(data) {
  return request({
    url: '/interview/session',
    method: 'POST',
    data
  })
}

export function getSession(id) {
  return request({
    url: `/interview/session/${id}`,
    method: 'GET'
  })
}

export function sendMessage(id, content) {
  return fetch(`/api/interview/session/${id}/message`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    },
    body: JSON.stringify({ content })
  })
}

export function closeSession(id) {
  return request({
    url: `/interview/session/${id}`,
    method: 'DELETE'
  })
}
