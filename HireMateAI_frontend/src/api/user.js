import request from '@/utils/request'

export function getProfile() {
  return request({
    url: '/user/profile',
    method: 'GET'
  })
}

export function updateProfile(data) {
  return request({
    url: '/user/profile',
    method: 'PUT',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'PUT',
    data
  })
}
