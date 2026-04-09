/**
 * useTheme — 主题色 & Element Plus CSS 变量注入
 * 统一管理全局颜色和过渡效果
 */
import { ref } from 'vue'

export const THEME_COLORS = {
  primary: '#667EEA',
  secondary: '#764BA2',
  success: '#10B981',
  warning: '#F59E0B',
  danger: '#EF4444',
  info: '#6366F1',
}

export const GRADIENT_PRIMARY = 'linear-gradient(135deg, #667EEA 0%, #764BA2 100%)'

export function useTheme() {
  // 注入 CSS 变量到 :root
  const applyTheme = () => {
    const root = document.documentElement
    root.style.setProperty('--theme-primary', THEME_COLORS.primary)
    root.style.setProperty('--theme-secondary', THEME_COLORS.secondary)
    root.style.setProperty('--theme-gradient', GRADIENT_PRIMARY)
    root.style.setProperty('--theme-success', THEME_COLORS.success)
    root.style.setProperty('--theme-warning', THEME_COLORS.warning)
    root.style.setProperty('--theme-danger', THEME_COLORS.danger)
  }

  return { applyTheme, THEME_COLORS, GRADIENT_PRIMARY }
}