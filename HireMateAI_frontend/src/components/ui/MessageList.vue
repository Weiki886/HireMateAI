<!--
  MessageList.vue — 消息列表容器
  自动滚动、虚拟化提示、空状态
-->
<template>
  <div class="message-list" ref="containerRef">
    <!-- 空状态 -->
    <div v-if="messages.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.5">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <h3>开始对话</h3>
      <p>输入你的回答，AI 面试官将为你提供专业反馈</p>
    </div>

    <!-- 消息列表 -->
    <transition-group name="msg" tag="div" class="messages-wrap">
      <MessageItem
        v-for="(msg, index) in messages"
        :key="index"
        :message="msg"
      />
    </transition-group>

    <!-- 底部锚点（用于自动滚动） -->
    <div ref="bottomRef" class="bottom-anchor" />
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import MessageItem from './MessageItem.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  }
})

const containerRef = ref()
const bottomRef = ref()

// 自动滚动到底部
const scrollToBottom = (smooth = true) => {
  nextTick(() => {
    if (bottomRef.value) {
      bottomRef.value.scrollIntoView({ behavior: smooth ? 'smooth' : 'instant' })
    }
  })
}

// 监听新消息，自动滚动
watch(() => props.messages.length, () => {
  scrollToBottom()
})

// 监听 streaming 状态变化，也滚动
watch(() => props.messages.some(m => m.streaming), (streaming) => {
  if (streaming) scrollToBottom(false)
})

defineExpose({ scrollToBottom })
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.message-list {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

.messages-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bottom-anchor {
  height: 8px;
  flex-shrink: 0;
}

// 空状态
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 40px 20px;
  animation: fade-in 0.4s ease;

  .empty-icon {
    width: 80px;
    height: 80px;
    border-radius: $radius-full;
    background: rgba(102, 126, 234, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
  }

  h3 {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 8px;
  }

  p {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 0;
    max-width: 300px;
    line-height: $line-height-loose;
  }
}

// 消息列表过渡动画
.msg-enter-active {
  transition: all 0.25s $ease-out;
}
.msg-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
