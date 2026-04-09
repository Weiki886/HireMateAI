import request from '@/utils/request'

export function generateReviewReport(sessionId) {
  return request({
    url: `/interview/review/${sessionId}`,
    method: 'POST',
    timeout: 120000
  })
}

export function getReviewReport(sessionId) {
  return request({
    url: `/interview/review/${sessionId}`,
    method: 'GET'
  })
}

export function getReviewReportDetail(sessionId) {
  return request({
    url: `/interview/review/${sessionId}/detail`,
    method: 'GET'
  })
}
