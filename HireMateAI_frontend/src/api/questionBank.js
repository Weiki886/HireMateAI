import request from '@/utils/request'

export function getCategories() {
  return request({
    url: '/question-bank/categories',
    method: 'GET'
  })
}

export function getQuestions(params) {
  return request({
    url: '/question-bank/questions',
    method: 'GET',
    params
  })
}

export function getQuestionDetail(id) {
  return request({
    url: `/question-bank/questions/${id}`,
    method: 'GET'
  })
}

export function generateQuestions(data) {
  return request({
    url: '/question-bank/questions',
    method: 'POST',
    data,
    timeout: 120000
  })
}

export function toggleFavorite(questionId, note) {
  return request({
    url: '/question-bank/questions/favorite',
    method: 'POST',
    params: { questionId, note }
  })
}

export function getFavorites(params) {
  return request({
    url: '/question-bank/favorites',
    method: 'GET',
    params
  })
}

/**
 * 获取收藏分类统计（按面试类型分组）
 */
export function getFavoriteCategories() {
  return request({
    url: '/question-bank/favorites/categories',
    method: 'GET'
  })
}

/**
 * 按面试类型获取收藏题目（支持难度筛选）
 */
export function getFavoritesByType(params) {
  return request({
    url: '/question-bank/favorites/by-type',
    method: 'GET',
    params
  })
}
