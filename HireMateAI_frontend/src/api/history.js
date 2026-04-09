import request from '@/utils/request'

export function getSessionList(page = 1, size = 10) {
  return request({
    url: '/history/sessions',
    method: 'GET',
    params: { page, size }
  })
}

export function getSessionDetail(id) {
  return request({
    url: `/history/sessions/${id}`,
    method: 'GET'
  })
}

export function deleteSession(id) {
  return request({
    url: `/history/sessions/${id}`,
    method: 'DELETE'
  })
}
