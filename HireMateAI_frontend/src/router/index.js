import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'interview',
        name: 'Interview',
        component: () => import('@/views/Interview.vue')
      },
      {
        path: 'interview/:id',
        name: 'InterviewSession',
        component: () => import('@/views/InterviewSession.vue')
      },
      {
        path: 'history',
        redirect: { path: '/interview', query: { tab: 'records' } }
      },
      {
        path: 'history/:id',
        name: 'HistoryDetail',
        component: () => import('@/views/HistoryDetail.vue')
      },
      {
        path: 'resume',
        name: 'Resume',
        component: () => import('@/views/Resume.vue')
      },
      {
        path: 'group-chat',
        name: 'GroupChat',
        component: () => import('@/views/GroupChat.vue')
      },
      {
        path: 'job-match',
        name: 'JobMatch',
        component: () => import('@/views/JobMatch.vue')
      },
      {
        path: 'question-bank',
        name: 'QuestionBank',
        component: () => import('@/views/QuestionBank.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

  if (requiresAuth && !userStore.token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
