<template>
  <div class="history-detail-page">
    <!-- 顶部操作按钮 -->
    <div class="detail-actions">
      <el-button @click="router.push({ path: '/interview', query: { tab: 'records' } })">返回列表</el-button>
      <el-button type="primary" @click="showReviewDialog = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
        </svg>
        生成复盘报告
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="detail-skeleton">
      <div class="skeleton" style="width: 100%; height: 120px; border-radius: 20px; margin-bottom: 20px"></div>
      <div class="skeleton" style="width: 100%; height: 400px; border-radius: 20px"></div>
    </div>

    <!-- 会话详情 -->
    <div v-else-if="sessionDetail" class="detail-content">
      <!-- 会话信息卡片 -->
      <div class="session-info-card">
        <div class="info-left">
          <div class="session-icon" :style="{ background: getTypeColor(sessionDetail.interviewType) }">
            {{ sessionDetail.interviewType?.charAt(0) || 'A' }}
          </div>
          <div class="info-text">
            <h2>{{ sessionDetail.jobPosition || '未指定岗位' }}</h2>
            <div class="info-tags">
              <span class="type-tag" :style="{ background: getTypeColor(sessionDetail.interviewType) }">
                {{ sessionDetail.interviewType }}
              </span>
              <span class="status-badge" :class="sessionDetail.status === 'ACTIVE' ? 'active' : 'closed'">
                {{ sessionDetail.status === 'ACTIVE' ? '进行中' : '已结束' }}
              </span>
            </div>
          </div>
        </div>
        <div class="info-right">
          <div class="time-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            <span>{{ formatDateTime(sessionDetail.createdAt) }}</span>
          </div>
        </div>
      </div>

      <!-- 消息历史 -->
      <div class="messages-section">
        <div class="messages-header">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          <h3>对话记录</h3>
          <span class="msg-count">{{ sessionDetail.messages?.length || 0 }} 条消息</span>
        </div>

        <div class="messages-list">
          <div
            v-for="(msg, index) in sessionDetail.messages"
            :key="index"
            :class="['msg-item', msg.role === 'user' ? 'msg-user' : 'msg-ai']"
          >
            <!-- 用户消息 -->
            <div v-if="msg.role === 'user'" class="msg-bubble msg-bubble--user">
              {{ msg.content }}
            </div>
            <!-- AI 消息 -->
            <div v-else class="msg-ai-wrap">
              <div class="ai-avatar">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#3B82F6" opacity="0.8"/>
                  <path d="M2 17L12 22L22 17" stroke="#60A5FA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M2 12L12 17L22 12" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <div class="msg-bubble msg-bubble--ai" v-html="formatContent(msg.content)"></div>
            </div>
            <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 复盘报告弹窗 -->
    <InterviewReviewReport
      v-model="showReviewDialog"
      :session-id="route.params.id"
      @generated="onReviewGenerated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSessionDetail } from '@/api/history'
import InterviewReviewReport from '@/components/InterviewReviewReport.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const sessionDetail = ref(null)
const showReviewDialog = ref(false)

const getTypeColor = (type) => {
  const map = {
    '技术面试': 'rgba(102, 126, 234, 0.12)',
    'HR面试': 'rgba(16, 185, 129, 0.12)',
    '综合面试': 'rgba(99, 102, 241, 0.12)',
    '行为面试': 'rgba(245, 158, 11, 0.12)',
    '终面': 'rgba(236, 72, 153, 0.12)',
  }
  return map[type] || 'rgba(102, 126, 234, 0.12)'
}

const formatDateTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}

const formatContent = (content) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
}

const onReviewGenerated = (report) => {
  ElMessage.success('复盘报告已生成！')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getSessionDetail(route.params.id)
    sessionDetail.value = res.data
  } catch (error) {
    console.error('Failed to load session detail:', error)
    ElMessage.error('加载会话详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.history-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}

.detail-topbar {
  margin-bottom: $spacing-4;

  .back-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: $color-text-secondary;
    font-size: $font-size-sm;
    padding: 6px 12px;
    border-radius: $radius-lg;
    &:hover {
      background: $color-bg-hover;
      color: $color-text-primary;
    }
  }
}

.detail-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-4;

  .el-button {
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
}

.detail-skeleton {
  .skeleton {
    background: linear-gradient(90deg, $color-bg-hover 25%, $color-bg-active 50%, $color-bg-hover 75%);
    background-size: 800px 100%;
    animation: skeleton-pulse 1.5s ease-in-out infinite;
    border-radius: $radius-2xl;
  }
}

@keyframes skeleton-pulse {
  0%   { background-position: -400px 0; }
  100% { background-position: 400px 0; }
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

// 会话信息卡
.session-info-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-5;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-sm;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .info-left {
    display: flex;
    align-items: center;
    gap: $spacing-4;
  }

  .session-icon {
    width: 52px;
    height: 52px;
    border-radius: $radius-xl;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-primary;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    flex-shrink: 0;
  }

  .info-text {
    h2 {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 8px;
    }
  }

  .info-tags {
    display: flex;
    gap: 8px;

    .type-tag {
      font-size: $font-size-xs;
      font-weight: $font-weight-medium;
      padding: 2px 10px;
      border-radius: $radius-full;
      color: $color-primary;
    }

    .status-badge {
      font-size: $font-size-xs;
      font-weight: $font-weight-medium;
      padding: 2px 10px;
      border-radius: $radius-full;

      &.active { background: #DCFCE7; color: $color-success; }
      &.closed { background: $color-bg-hover; color: $color-text-secondary; }
    }
  }

  .info-right {
    .time-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: $font-size-sm;
      color: $color-text-placeholder;
    }
  }
}

// 消息列表
.messages-section {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-sm;
  overflow: hidden;
}

.messages-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 24px;
  border-bottom: 1px solid $color-border-light;
  color: $color-primary;

  h3 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0;
  }

  .msg-count {
    margin-left: auto;
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    background: $color-bg-hover;
    padding: 2px 10px;
    border-radius: $radius-full;
  }
}

.messages-list {
  padding: 24px;
  max-height: 600px;
  overflow-y: auto;
  background: #F8FAFC;
}

.msg-item {
  margin-bottom: 24px;
  animation: msg-in 0.25s ease;

  &.msg-user {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;

    .msg-bubble--user {
      background: $color-primary;
      color: #fff;
      border-radius: 18px 18px 4px 18px;
      padding: 12px 16px;
      max-width: 72%;
      font-size: $font-size-base;
      line-height: 1.7;
    }
  }

  &.msg-ai {
    display: flex;
    flex-direction: column;
    gap: 6px;

    .msg-ai-wrap {
      display: flex;
      align-items: flex-start;
      gap: 10px;
    }

    .ai-avatar {
      width: 32px;
      height: 32px;
      border-radius: $radius-lg;
      background: rgba(102, 126, 234, 0.1);
      border: 1px solid rgba(102, 126, 234, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .msg-bubble--ai {
      background: $color-bg-card;
      color: $color-text-primary;
      border-radius: 18px 18px 18px 4px;
      padding: 12px 16px;
      max-width: 80%;
      font-size: $font-size-base;
      line-height: 1.7;
      border: 1px solid $color-border-light;
      box-shadow: $shadow-xs;

      :deep(.inline-code) {
        background: rgba(102, 126, 234, 0.08);
        color: $color-primary;
        padding: 1px 6px;
        border-radius: 4px;
        font-family: monospace;
        font-size: 0.9em;
      }
    }
  }

  .msg-time {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    padding-left: 42px;
  }
}

@keyframes msg-in {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}

.empty-state {
  text-align: center;
  padding: 80px;
  color: $color-text-secondary;
}
</style>
