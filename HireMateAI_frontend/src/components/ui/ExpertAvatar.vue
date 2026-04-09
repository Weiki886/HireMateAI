<!--
  ExpertAvatar.vue — AI 专家头像
  三种角色：HR / 技术 / 职业规划
-->
<template>
  <div class="expert-avatar" :class="`expert-avatar--${role}`" :style="avatarStyle">
    <div class="expert-avatar__icon" v-html="iconSvg" />
    <div class="expert-avatar__label">{{ label }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const EXPERT_CONFIG = {
  hr: {
    color: '#F59E0B',
    bg: 'rgba(245, 158, 11, 0.1)',
    label: 'HR 总监',
    icon: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>`
  },
  tech: {
    color: '#6366F1',
    bg: 'rgba(99, 102, 241, 0.1)',
    label: '技术专家',
    icon: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <polyline points="16 18 22 12 16 6"/>
      <polyline points="8 6 2 12 8 18"/>
    </svg>`
  },
  career: {
    color: '#10B981',
    bg: 'rgba(16, 185, 129, 0.1)',
    label: '职业规划师',
    icon: `<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
    </svg>`
  }
}

const props = defineProps({
  role: {
    type: String,
    default: 'hr',
    validator: (v) => ['hr', 'tech', 'career'].includes(v)
  },
  size: {
    type: String,
    default: 'md' // sm / md / lg
  }
})

const config = computed(() => EXPERT_CONFIG[props.role] || EXPERT_CONFIG.hr)
const label = computed(() => config.value.label)
const iconSvg = computed(() => config.value.icon)
const avatarStyle = computed(() => ({
  '--expert-color': config.value.color,
  '--expert-bg': config.value.bg,
  '--avatar-size': props.size === 'sm' ? '36px' : props.size === 'lg' ? '56px' : '44px'
}))
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.expert-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;

  &__icon {
    width: var(--avatar-size);
    height: var(--avatar-size);
    border-radius: $radius-xl;
    background: var(--expert-bg);
    border: 1.5px solid color-mix(in srgb, var(--expert-color) 30%, transparent);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--expert-color);
    transition: all $duration-normal;
  }

  &__label {
    font-size: $font-size-xs;
    font-weight: $font-weight-semibold;
    color: var(--expert-color);
    white-space: nowrap;
  }
}
</style>