<!--
  ScoreRing.vue — 环形评分展示
  支持：动态颜色 / 渐变圆环 / 大分值 + 小单位
-->
<template>
  <div class="score-ring" :style="ringStyle">
    <div class="score-ring__inner" :style="innerStyle" />
    <div class="score-ring__content">
      <span class="score-ring__value" :style="{ color: scoreColor }">{{ displayScore }}</span>
      <span class="score-ring__unit">分</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: {
    type: Number,
    default: 0
  },
  size: {
    type: Number,
    default: 140
  },
  strokeWidth: {
    type: Number,
    default: 10
  }
})

const displayScore = computed(() => Math.round(props.score || 0))

const scoreColor = computed(() => {
  if (displayScore.value >= 80) return '#10B981'
  if (displayScore.value >= 60) return '#F59E0B'
  return '#EF4444'
})

// conic-gradient 圆环
const ringStyle = computed(() => {
  const deg = ((displayScore.value / 100) * 360).toFixed(1)
  const innerPx = props.size - props.strokeWidth * 2
  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    background: `conic-gradient(${scoreColor.value} 0deg ${deg}deg, #E5E7EB 0deg)`,
    '--ring-size': `${props.size}px`,
    '--ring-stroke': `${props.strokeWidth}px`,
    '--ring-inner': `${innerPx}px`
  }
})

const innerStyle = computed(() => {
  const innerSize = props.size - props.strokeWidth * 2
  return {
    width: `${innerSize}px`,
    height: `${innerSize}px`
  }
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.score-ring {
  border-radius: 50%;
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.score-ring__inner {
  position: absolute;
  border-radius: 50%;
  background: #fff;
}

.score-ring__content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-width: calc(var(--ring-inner) * 0.78);
}

.score-ring__value {
  font-size: calc(var(--ring-inner) * 0.36);
  font-weight: 700;
  line-height: 1;
}

.score-ring__unit {
  font-size: calc(var(--ring-inner) * 0.22);
  color: $color-text-secondary;
  margin-top: 1px;
}
</style>