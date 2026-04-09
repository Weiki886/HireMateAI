import request from '@/utils/request'
import uploadRequest from '@/utils/uploadRequest'

export function listJobDescriptions() {
  return request({
    url: '/job-match/job-description',
    method: 'GET'
  })
}

export function getJobDescription(id) {
  return request({
    url: `/job-match/job-description/${id}`,
    method: 'GET'
  })
}

export function saveJobDescription(data) {
  return request({
    url: '/job-match/job-description',
    method: 'POST',
    data
  })
}

export function deleteJobDescription(id) {
  return request({
    url: `/job-match/job-description/${id}`,
    method: 'DELETE'
  })
}

export function analyzeMatch(jobDescriptionId, jobContent, resumeFile) {
  const formData = new FormData()
  if (jobDescriptionId) {
    formData.append('jobDescriptionId', jobDescriptionId)
  }
  if (jobContent) {
    formData.append('jobContent', jobContent)
  }
  formData.append('resumeFile', resumeFile)

  return uploadRequest({
    url: '/job-match/analyze',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function listMatchRecords() {
  return request({
    url: '/job-match/records',
    method: 'GET'
  })
}

export function getMatchRecord(id) {
  return request({
    url: `/job-match/records/${id}`,
    method: 'GET'
  })
}