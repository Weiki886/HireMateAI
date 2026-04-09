<template>
  <div class="hm-layout">
    <header class="hm-topnav">
      <div class="topnav-inner">
        <div class="topnav-left">
          <router-link to="/" class="topnav-brand">
            <div class="brand-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#3B82F6"/>
                <path d="M2 17L12 22L22 17" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M2 12L12 17L22 12" stroke="#60A5FA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <span class="brand-text">HireMate AI</span>
          </router-link>

          <nav class="topnav-nav" aria-label="主导航">
            <router-link
              v-for="item in navItems"
              :key="item.path"
              :to="item.path"
              class="topnav-link"
              :class="{ 'topnav-link--active': isActive(item.path) }"
            >
              {{ item.label }}
            </router-link>
          </nav>
        </div>

        <div class="topnav-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="topnav-user">
              <div class="user-avatar">
                {{ userStore.user?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </div>
              <span class="user-name">{{ userStore.user?.username || '用户' }}</span>
              <el-icon class="user-caret"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="hm-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { User, SwitchButton, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { path: '/', label: '首页' },
  { path: '/interview', label: 'AI 模拟面试' },
  { path: '/resume', label: '简历管理' },
  { path: '/group-chat', label: 'AI 专家群聊' },
  { path: '/job-match', label: '岗位匹配' },
  { path: '/question-bank', label: '面试题库' }
]

const isActive = (path) => {
  if (path === '/') return route.path === '/'
  if (path === '/interview') {
    return (
      route.path === '/interview' ||
      route.path.startsWith('/interview/') ||
      route.path.startsWith('/history')
    )
  }
  return route.path.startsWith(path)
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'hm-message-box'
    })
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

$topnav-height: 56px;

.hm-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: $color-bg-page;
}

.hm-topnav {
  flex-shrink: 0;
  height: $topnav-height;
  background: $color-bg-base;
  border-bottom: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
  position: sticky;
  top: 0;
  z-index: 100;
}

.topnav-inner {
  width: 100%;
  height: 100%;
  padding: 0 $spacing-8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-4;
}

.topnav-left {
  display: flex;
  align-items: center;
  gap: $spacing-5;
  min-width: 0;
  flex: 1;
}

.topnav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: $color-text-primary;
  flex-shrink: 0;

  .brand-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 34px;
    height: 34px;
    background: $color-bg-active;
    border-radius: $radius-md;
    border: 1px solid $color-border-light;
  }

  .brand-text {
    font-size: $font-size-md;
    font-weight: $font-weight-bold;
    letter-spacing: 0.02em;
    white-space: nowrap;
  }

  &:hover {
    color: $color-primary;
  }
}

.topnav-nav {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 2px;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

.topnav-link {
  flex-shrink: 0;
  padding: 8px 14px;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  text-decoration: none;
  border-radius: $radius-md;
  transition: color $duration-fast $ease-default, background $duration-fast $ease-default;
  position: relative;

  &:hover {
    color: $color-text-primary;
    background: $color-bg-hover;
  }

  &--active {
    color: $color-primary;
    font-weight: $font-weight-semibold;

    &::after {
      content: '';
      position: absolute;
      left: 50%;
      bottom: 2px;
      transform: translateX(-50%);
      width: 22px;
      height: 3px;
      border-radius: 2px;
      background: $color-primary;
    }
  }
}

.topnav-right {
  flex-shrink: 0;
  margin-left: $spacing-4;
}

.topnav-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 10px 4px 4px;
  border-radius: $radius-lg;
  transition: background $duration-fast;

  &:hover {
    background: $color-bg-hover;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    border-radius: $radius-full;
    background: $color-primary;
    color: #fff;
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-regular;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-caret {
    font-size: 12px;
    color: $color-text-placeholder;
  }
}

.hm-main {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-6;
  min-height: 0;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 200ms $ease-default, transform 200ms $ease-default;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

@media (max-width: 960px) {
  .topnav-inner {
    padding: 0 $spacing-4;
  }

  .topnav-left {
    gap: $spacing-3;
  }

  .topnav-link {
    padding: 8px 10px;
    font-size: $font-size-sm;
  }
}
</style>
