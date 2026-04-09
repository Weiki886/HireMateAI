<!--
  ChatInput.vue — 聊天输入框
  支持：Enter 发送、Shift+Enter 换行、停止生成、防抖
-->
<template>
  <div class="chat-input">
    <!-- 停止生成按钮 -->
    <transition name="fade">
      <button v-if="streaming" class="stop-btn" @click="handleStop">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
          <rect x="6" y="6" width="12" height="12" rx="2"/>
        </svg>
        停止生成
      </button>
    </transition>

    <div class="input-wrap" :class="{ 'input-wrap--disabled': disabled }">
      <!-- 用户输入 -->
      <textarea
        ref="textareaRef"
        v-model="inputText"
        class="input-area"
        :placeholder="placeholder"
        :disabled="disabled"
        :rows="rows"
        @keydown.enter.exact.prevent="handleEnter"
        @keydown.enter.shift.exact="handleShiftEnter"
      />
      <!-- 发送按钮 -->
      <button
        class="send-btn"
        :class="{ 'send-btn--active': canSend }"
        :disabled="!canSend"
        @click="handleSend"
        aria-label="发送消息"
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <line x1="12" y1="19" x2="12" y2="5"/>
          <polyline points="5 12 12 5 19 12"/>
        </svg>
      </button>
    </div>

    <div class="input-hint">
      <span><kbd>Enter</kbd> 发送 · <kbd>Shift + Enter</kbd> 换行</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  streaming: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入你的回答，AI 面试官将为你点评...'
  },
  rows: {
    type: Number,
    default: 3
  }
})

const emit = defineEmits(['update:modelValue', 'send', 'stop'])

const textareaRef = ref()
const inputText = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const canSend = computed(() => {
  return inputText.value.trim().length > 0 && !props.streaming && !props.disabled
})

// 自动 focus
const focus = () => {
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

defineExpose({ focus })

const handleEnter = () => {
  if (canSend.value) {
    emit('send', inputText.value.trim())
    inputText.value = ''
    focus()
  }
}

const handleShiftEnter = () => {
  // Shift+Enter: 让 textarea 自然换行，无需操作
  // 浏览器默认行为
}

const handleSend = () => {
  if (canSend.value) {
    emit('send', inputText.value.trim())
    inputText.value = ''
    focus()
  }
}

const handleStop = () => {
  emit('stop')
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.chat-input {
  background: $color-bg-card;
  border-top: 1px solid $color-border-light;
  padding: 16px 24px;
  position: relative;
}

.stop-btn {
  position: absolute;
  top: -40px;
  right: 24px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: $color-danger;
  color: #fff;
  border: none;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  cursor: pointer;
  box-shadow: $shadow-md;
  transition: all $duration-fast;

  &:hover {
    background: #DC2626;
    transform: scale(1.02);
  }
}

.input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: $color-bg-page;
  border: 1.5px solid $color-border;
  border-radius: $radius-xl;
  padding: 8px 8px 8px 16px;
  transition: border-color $duration-fast, box-shadow $duration-fast;

  &:focus-within {
    border-color: $color-primary;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.12);
  }

  &--disabled {
    opacity: 0.6;
    pointer-events: none;
  }
}

.input-area {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-family: $font-family-base;
  font-size: $font-size-base;
  color: $color-text-primary;
  line-height: 1.6;
  resize: none;
  min-height: 24px;
  max-height: 120px;
  overflow-y: auto;

  &::placeholder {
    color: $color-text-placeholder;
  }

  &:disabled {
    cursor: not-allowed;
  }
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: $radius-lg;
  border: none;
  background: $color-border;
  color: $color-text-placeholder;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all $duration-fast;

  &--active {
    background: $color-primary;
    color: #fff;
    box-shadow: $shadow-primary;

    &:hover {
      transform: scale(1.05);
      background: $color-primary-dark;
      box-shadow: $shadow-primary-lg;
    }

    &:active {
      transform: scale(0.96);
    }
  }

  &:disabled {
    cursor: not-allowed;
  }
}

.input-hint {
  margin-top: 6px;
  text-align: center;
  font-size: $font-size-xs;
  color: $color-text-placeholder;

  kbd {
    display: inline-block;
    padding: 1px 5px;
    background: $color-bg-hover;
    border: 1px solid $color-border;
    border-radius: 3px;
    font-size: 10px;
    font-family: inherit;
    color: $color-text-secondary;
  }
}

// 过渡动画
.fade-enter-active, .fade-leave-active {
  transition: opacity 200ms, transform 200ms;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
