import request from '@/utils/request'

export function getResumeList() {
  return request({
    url: '/resume/list',
    method: 'GET'
  })
}

export function getResume(id) {
  return request({
    url: `/resume/${id}`,
    method: 'GET'
  })
}

export function createResume(data) {
  return request({
    url: '/resume',
    method: 'POST',
    data
  })
}

export function updateResume(id, data) {
  return request({
    url: `/resume/${id}`,
    method: 'PUT',
    data
  })
}

export function deleteResume(id) {
  return request({
    url: `/resume/${id}`,
    method: 'DELETE'
  })
}

export function getResumeVersions(id) {
  return request({
    url: `/resume/${id}/versions`,
    method: 'GET'
  })
}

export function optimizeResume(data) {
  const formData = new FormData()
  if (data.pdfFile) {
    formData.append('pdfFile', data.pdfFile)
  }
  if (data.resumeText) {
    formData.append('resumeText', data.resumeText)
  }
  formData.append('targetPosition', data.targetPosition)
  if (data.jdText) {
    formData.append('jdText', data.jdText)
  }
  if (data.discussionRounds) {
    formData.append('discussionRounds', data.discussionRounds)
  }

  // 不要手动设 Content-Type，否则缺少 boundary，Spring 无法解析 multipart
  return request({
    url: '/resume/optimize-suggestions',
    method: 'POST',
    data: formData,
    timeout: 600000
  })
}

export function optimizeResumeAsync(data) {
  const formData = new FormData()
  if (data.pdfFile) {
    formData.append('pdfFile', data.pdfFile)
  }
  if (data.resumeText) {
    formData.append('resumeText', data.resumeText)
  }
  formData.append('targetPosition', data.targetPosition)
  if (data.jdText) {
    formData.append('jdText', data.jdText)
  }
  if (data.discussionRounds) {
    formData.append('discussionRounds', data.discussionRounds)
  }

  return request({
    url: '/resume/optimize-async',
    method: 'POST',
    data: formData,
    timeout: 30000
  })
}

export function getAnalysisRecords() {
  return request({
    url: '/resume/analysis-records',
    method: 'GET'
  })
}

export function getAnalysisRecord(recordId) {
  return request({
    url: `/resume/analysis-records/${recordId}`,
    method: 'GET'
  })
}

export function deleteAnalysisRecord(recordId) {
  return request({
    url: `/resume/analysis-records/${recordId}`,
    method: 'DELETE'
  })
}
