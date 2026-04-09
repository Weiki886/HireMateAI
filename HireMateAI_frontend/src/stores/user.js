import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister, logout as apiLogout } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || '{}')
  }),

  actions: {
    async login(formData) {
      const res = await apiLogin(formData)
      this.token = res.data.token
      this.user = {
        userId: res.data.userId,
        username: res.data.username,
        email: res.data.email || ''
      }
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return res
    },

    syncUserFromProfile(profile) {
      if (!profile) return
      this.user = {
        ...this.user,
        userId: profile.userId,
        username: profile.username,
        email: profile.email || ''
      }
      localStorage.setItem('user', JSON.stringify(this.user))
    },

    async register(formData) {
      const res = await apiRegister(formData)
      return res
    },

    async logout() {
      try {
        await apiLogout()
      } finally {
        this.token = ''
        this.user = {}
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    }
  }
})
