import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      const status = error.response.status
      // 403：当前项目下多为 JWT 无效/签名不匹配导致匿名访问受保护接口，与未登录表现一致
      if (status === 401 || status === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.error(
          status === 401 ? '登录已过期，请重新登录' : '登录状态无效，请重新登录'
        )
        window.location.href = '/login'
      } else {
        ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.code === 'ECONNABORTED') {
      if (!error.config?.suppressTimeoutToast) {
        ElMessage.error('请求超时，请稍后重试（AI 分析可能需数分钟）')
      }
    } else {
      ElMessage.error('网络连接失败，请确认后端已启动（默认 http://localhost:8080）且已重启 Vite 开发服务')
    }
    return Promise.reject(error)
  }
)

export default request
