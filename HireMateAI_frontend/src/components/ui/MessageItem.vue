<!--
  MessageItem.vue — 单条消息气泡组件
  支持：Markdown 渲染 / 代码高亮 / 停止生成光标 / 操作按钮
-->
<template>
  <div :class="['message-item', `message-item--${message.role}`]">
    <!-- AI 头像 -->
    <div v-if="message.role === 'ai'" class="message-avatar avatar-ai">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
        <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#3B82F6" opacity="0.8"/>
        <path d="M2 17L12 22L22 17" stroke="#475569" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M2 12L12 17L22 12" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>

    <!-- 用户头像 -->
    <div v-else class="message-avatar avatar-user">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
        <circle cx="12" cy="7" r="4"/>
      </svg>
    </div>

    <!-- 消息内容 -->
    <div class="message-body">
      <!-- AI: 角色名 + 内容 -->
      <div v-if="message.role === 'ai'" class="message-role">HireMate AI</div>

      <div class="message-bubble-wrap">
        <div
          v-if="message.role === 'ai'"
          class="message-content markdown-content"
          v-html="renderedContent"
        ></div>
        <div v-else class="message-content">
          {{ message.content }}
        </div>

        <!-- 打字机光标 -->
        <span v-if="message.role === 'ai' && message.streaming" class="typing-cursor" aria-hidden="true" />

        <!-- 消息操作按钮 (AI 消息Hover时显示) -->
        <div v-if="message.role === 'ai' && !message.streaming && message.content" class="message-actions">
          <button class="action-btn" @click="copyContent" :title="copied ? '已复制' : '复制'">
            <svg v-if="!copied" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
              <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
            {{ copied ? '已复制' : '复制' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useMarkdown } from '@/composables/useMarkdown'

const props = defineProps({
  message: {
    type: Object,
    required: true
  }
})

const { renderMarkdown } = useMarkdown()
const copied = ref(false)

const renderedContent = computed(() => {
  if (!props.message.content) return ''
  return renderMarkdown(props.message.content)
})

const copyContent = async () => {
  try {
    await navigator.clipboard.writeText(props.message.content)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // ignore
  }
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: msg-in 0.25s $ease-out;

  &--user {
    flex-direction: row-reverse;

    .message-bubble-wrap {
      background: $color-primary;
      color: #fff;
      border-radius: 18px 18px 4px 18px;
    }

    .message-avatar {
      background: $color-primary;
    }
  }

  &--ai {
    .message-bubble-wrap {
      background: $color-bg-card;
      color: $color-text-primary;
      border-radius: 18px 18px 18px 4px;
      box-shadow: $shadow-sm;
    }

    .message-avatar {
      background: rgba(59, 130, 246, 0.1);
      border: 1px solid rgba(59, 130, 246, 0.15);
    }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: $radius-full;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  align-self: flex-end;
}

.avatar-user {
  align-self: flex-start;
}

.message-body {
  flex: 1;
  max-width: 80%;
  min-width: 0;

  .message-role {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    margin-bottom: 4px;
    font-weight: $font-weight-medium;
  }
}

.message-bubble-wrap {
  padding: 12px 16px;
  line-height: 1.7;
  word-break: break-word;
  position: relative;
  transition: box-shadow $duration-fast;

  &:hover .message-actions {
    opacity: 1;
  }
}

// Markdown 内容样式
.markdown-content {
  :deep(.md-h1), :deep(.md-h2), :deep(.md-h3) {
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 8px 0 4px;

    &:first-child { margin-top: 0; }
  }

  :deep(.md-h1) { font-size: 1.2em; }
  :deep(.md-h2) { font-size: 1.1em; }
  :deep(.md-h3) { font-size: 1em; }

  :deep(.md-p) {
    margin: 4px 0;
    &:first-child { margin-top: 0; }
  }

  :deep(strong) {
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  :deep(.inline-code) {
    background: rgba(59, 130, 246, 0.08);
    color: $color-primary;
    padding: 1px 6px;
    border-radius: 4px;
    font-family: $font-family-code;
    font-size: 0.9em;
  }

  :deep(.code-block) {
    background: #F1F5F9;
    border: 1px solid $color-border;
    border-radius: $radius-lg;
    padding: 12px 16px;
    margin: 8px 0;
    overflow-x: auto;

    code {
      color: #1E293B;
      font-family: $font-family-code;
      font-size: 13px;
      line-height: 1.6;
    }
  }

  :deep(li) {
    margin: 2px 0;
    padding-left: 4px;
  }

  :deep(a) {
    color: $color-primary;
    text-decoration: none;

    &:hover { text-decoration: underline; }
  }
}

// 操作按钮
.message-actions {
  opacity: 0;
  position: absolute;
  bottom: -8px;
  right: 8px;
  display: flex;
  gap: 4px;
  background: $color-bg-card;
  border: 1px solid $color-border;
  border-radius: $radius-lg;
  padding: 2px;
  box-shadow: $shadow-sm;
  transition: opacity $duration-fast;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: none;
  background: transparent;
  color: $color-text-secondary;
  font-size: $font-size-xs;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all $duration-fast;
  white-space: nowrap;

  &:hover {
    background: $color-bg-hover;
    color: $color-primary;
  }
}

// 打字机光标
.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  vertical-align: text-bottom;
  background: $color-primary;
  animation: blink 0.9s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0; }
}

// 消息入场动画
@keyframes msg-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
