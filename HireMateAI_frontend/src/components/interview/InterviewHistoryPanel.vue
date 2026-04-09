<template>
  <div class="interview-history-panel">
    <div class="panel-header">
      <h2 class="panel-title">面试记录</h2>
      <p class="panel-desc">查看和管理您的面试历史记录</p>
    </div>

    <div v-if="loading" class="loading-grid">
      <div v-for="i in 3" :key="i" class="skeleton-card">
        <div class="skeleton" style="width: 60%; height: 18px; margin-bottom: 12px"></div>
        <div class="skeleton" style="width: 40%; height: 14px; margin-bottom: 8px"></div>
        <div class="skeleton" style="width: 30%; height: 14px"></div>
      </div>
    </div>

    <div v-else-if="sessions.length > 0" class="history-list">
      <div
        v-for="session in sessions"
        :key="session.id"
        class="session-card hover-lift"
        @click="viewDetail(session.sessionId)"
      >
        <div class="session-card__left">
          <div class="session-icon" :style="{ background: getTypeColor(session.interviewType) }">
            {{ session.interviewType?.charAt(0) || 'A' }}
          </div>
          <div class="session-info">
            <h3>{{ session.jobPosition || '未指定岗位' }}</h3>
            <div class="session-tags">
              <span class="type-tag" :style="{ background: getTypeColor(session.interviewType) }">
                {{ session.interviewType }}
              </span>
              <span class="status-badge" :class="session.status === 'ACTIVE' ? 'active' : 'closed'">
                {{ session.status === 'ACTIVE' ? '进行中' : '已结束' }}
              </span>
            </div>
          </div>
        </div>
        <div class="session-card__right">
          <div class="session-time">
            <div class="time-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              <span>{{ formatTime(session.createdAt) }}</span>
            </div>
            <div class="time-item" v-if="session.updatedAt !== session.createdAt">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 13 10 10 4"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
              <span>{{ formatTime(session.updatedAt) }}</span>
            </div>
          </div>
          <div class="session-actions" @click.stop>
            <el-button size="small" type="primary" plain @click="viewDetail(session.sessionId)">
              查看详情
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(session.sessionId)">
              删除
            </el-button>
          </div>
        </div>
      </div>

      <div class="pagination-wrapper" v-if="total > size">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          @current-change="loadSessions"
        />
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
        </svg>
      </div>
      <h3>还没有面试记录</h3>
      <p>开始你的第一次 AI 模拟面试吧</p>
      <el-button type="primary" @click="$emit('start-interview')">开始面试</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessionList, deleteSession } from '@/api/history'

defineEmits(['start-interview'])

const router = useRouter()

const loading = ref(false)
const sessions = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const getTypeColor = (type) => {
  const map = {
    '技术面试': 'rgba(59, 130, 246, 0.12)',
    'HR面试': 'rgba(16, 185, 129, 0.12)',
    '综合面试': 'rgba(99, 102, 241, 0.12)',
    '行为面试': 'rgba(245, 158, 11, 0.12)',
    '终面': 'rgba(236, 72, 153, 0.12)',
  }
  return map[type] || 'rgba(59, 130, 246, 0.12)'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const loadSessions = async () => {
  loading.value = true
  try {
    const res = await getSessionList(page.value, size.value)
    sessions.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load sessions:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/history/${id}`)
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条面试记录吗？此操作不可恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSession(id)
    ElMessage.success('删除成功')
    loadSessions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete:', error)
    }
  }
}

onMounted(() => {
  loadSessions()
})

defineExpose({ loadSessions })
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.interview-history-panel {
  max-width: 1000px;
  margin: 0 auto;
}

.panel-header {
  margin-bottom: $spacing-6;

  .panel-title {
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 6px;
  }

  .panel-desc {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 0;
  }
}

.loading-grid {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.skeleton-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-5;
  border: 1px solid $color-border-light;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.session-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-5;
  border: 1px solid $color-border-light;
  cursor: pointer;
  transition: all $duration-normal $ease-default;
  display: flex;
  justify-content: space-between;
  align-items: center;

  &:hover {
    border-color: $color-primary;
    box-shadow: $shadow-primary;
    transform: translateY(-2px);
  }

  &__left {
    display: flex;
    align-items: center;
    gap: $spacing-4;
  }

  &__right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: $spacing-3;
  }
}

.session-icon {
  width: 48px;
  height: 48px;
  border-radius: $radius-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-primary;
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  flex-shrink: 0;
}

.session-info {
  h3 {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 8px;
  }
}

.session-tags {
  display: flex;
  gap: 8px;
  align-items: center;

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

    &.active {
      background: #DCFCE7;
      color: $color-success;
    }

    &.closed {
      background: $color-bg-hover;
      color: $color-text-secondary;
    }
  }
}

.session-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;

  .time-item {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

.session-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: $spacing-4;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;

  .empty-icon {
    width: 96px;
    height: 96px;
    border-radius: $radius-full;
    background: rgba(59, 130, 246, 0.06);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
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
    margin: 0 0 20px;
  }
}
</style>
