<!--
  InterviewReviewReport.vue — AI 面试复盘报告弹窗
  展示：总分圆环图 + 各维度评分条 + 总体总结 + 改进建议 + 逐条回答评价卡片
-->
<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="860px"
    :close-on-click-modal="false"
    class="review-report-dialog"
    @closed="onClosed"
  >
    <!-- 加载状态 -->
    <div v-if="loading" class="report-loading">
      <div class="loading-spinner"></div>
      <p>AI 正在分析您的面试表现，请稍候...</p>
    </div>

    <!-- 生成失败 -->
    <div v-else-if="reportData && reportData.status === 'FAILED'" class="report-error">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="1.5">
        <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
      </svg>
      <h3>生成失败</h3>
      <p>{{ reportData.overallSummary }}</p>
    </div>

    <!-- 报告内容 -->
    <div v-else-if="reportData && reportData.status === 'COMPLETED'" class="report-content">

      <!-- 顶部：总分圆环 + 四维评分条 -->
      <div class="report-overview">
        <!-- 综合评分圆环 -->
        <div class="score-panel">
          <ScoreRing :score="reportData.totalScore" :size="160" :stroke-width="14" />
          <div class="score-meta">
            <div class="score-job">{{ reportData.jobPosition || '未指定岗位' }}</div>
            <div class="score-type">{{ reportData.interviewType }}</div>
            <div class="score-time">{{ reportData.createdAt }}</div>
          </div>
        </div>

        <!-- 四维评分 -->
        <div class="dimension-scores">
          <div class="dim-title">各维度评分</div>
          <div class="dim-item" v-for="dim in dimensionList" :key="dim.key">
            <div class="dim-header">
              <span class="dim-label">{{ dim.label }}</span>
              <span class="dim-value">{{ Math.round(dim.score) }}<span class="dim-unit">分</span></span>
            </div>
            <el-progress
              :percentage="Math.round(dim.score)"
              :color="dim.color"
              :show-text="false"
              :stroke-width="8"
            />
          </div>
        </div>
      </div>

      <!-- 总体总结 -->
      <div class="summary-section" v-if="reportData.overallSummary">
        <div class="section-header">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          <h4>总体复盘总结</h4>
        </div>
        <div class="summary-text">{{ reportData.overallSummary }}</div>
      </div>

      <!-- 改进建议 -->
      <div class="suggestions-section" v-if="reportData.improvementSuggestions?.length">
        <div class="section-header">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <h4>改进建议</h4>
        </div>
        <ul class="suggestions-list">
          <li v-for="(s, i) in reportData.improvementSuggestions" :key="i" class="suggestion-item">
            <span class="suggestion-num">{{ i + 1 }}</span>
            <span>{{ s }}</span>
          </li>
        </ul>
      </div>

      <!-- 逐条回答评价（仅 detail 模式） -->
      <div class="answers-section" v-if="showDetail && reportData.answers?.length">
        <div class="section-header">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
          </svg>
          <h4>回答详细评价</h4>
          <span class="answer-count">{{ reportData.answers.length }} 条</span>
        </div>

        <div class="answer-cards">
          <div class="answer-card" v-for="(ans, i) in reportData.answers" :key="ans.id">
            <div class="answer-header">
              <span class="answer-num">#{{ i + 1 }}</span>
              <el-tag size="small" type="info">{{ ans.dimensionType || '综合' }}</el-tag>
              <span class="answer-score">
                <span class="score-val">{{ Math.round(ans.answerQualityScore || 0) }}</span>
                <span class="score-denom">/100</span>
              </span>
            </div>

            <div class="answer-question" v-if="ans.questionSummary">
              <span class="q-label">问题：</span>{{ ans.questionSummary }}
            </div>

            <div class="answer-user" v-if="ans.userAnswer">
              <span class="q-label">你的回答：</span>{{ truncate(ans.userAnswer, 150) }}
            </div>

            <!-- 优点 -->
            <div class="answer-strengths" v-if="ans.strengths?.length">
              <div class="review-tag strength-tag">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                优点
              </div>
              <div class="review-tags">
                <span class="review-chip chip-green" v-for="s in ans.strengths" :key="s">{{ s }}</span>
              </div>
            </div>

            <!-- 缺点 -->
            <div class="answer-weaknesses" v-if="ans.weaknesses?.length">
              <div class="review-tag weakness-tag">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
                不足
              </div>
              <div class="review-tags">
                <span class="review-chip chip-warn" v-for="w in ans.weaknesses" :key="w">{{ w }}</span>
              </div>
            </div>

            <!-- 改进建议 -->
            <div class="answer-tip" v-if="ans.improvementTips">
              <div class="review-tag tip-tag">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2.5">
                  <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                </svg>
                改进建议
              </div>
              <div class="tip-text">{{ ans.improvementTips }}</div>
            </div>

            <!-- 参考示范回答 -->
            <div class="answer-model" v-if="ans.modelAnswer">
              <div class="review-tag model-tag">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2.5">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                </svg>
                参考示范回答
              </div>
              <div class="model-text">{{ ans.modelAnswer }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 无报告且未加载 -->
    <div v-else-if="!loading" class="report-empty">
      <p>暂无复盘报告</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="visible = false">关闭</el-button>
        <el-button v-if="!reportData" type="primary" :loading="loading" @click="handleGenerate">
          生成复盘报告
        </el-button>
        <el-button v-if="reportData && reportData.status === 'COMPLETED' && showDetail"
          type="primary" plain @click="handleRegenerate">
          重新生成
        </el-button>
        <el-button v-if="reportData && reportData.status === 'COMPLETED' && !showDetail"
          type="primary" @click="handleShowDetail">
          查看详细评价
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import ScoreRing from '@/components/ui/ScoreRing.vue'
import { generateReviewReport, getReviewReport, getReviewReportDetail } from '@/api/interviewReview'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  sessionId: { type: [Number, String], default: null },
  autoGenerate: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'generated'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const reportData = ref(null)
const showDetail = ref(false)

const dialogTitle = computed(() => {
  if (loading.value) return '正在生成复盘报告...'
  if (reportData.value?.status === 'FAILED') return '复盘报告生成失败'
  if (reportData.value) return 'AI 面试复盘报告'
  return '面试复盘报告'
})

const dimensionList = computed(() => {
  if (!reportData.value) return []
  return [
    { key: 'professional', label: '专业能力', score: reportData.value.professionalScore || 0, color: '#3B82F6' },
    { key: 'expression',   label: '表达能力',   score: reportData.value.expressionScore   || 0, color: '#10B981' },
    { key: 'logic',        label: '逻辑思维',   score: reportData.value.logicScore        || 0, color: '#F59E0B' },
    { key: 'confidence',   label: '自信心',     score: reportData.value.confidenceScore   || 0, color: '#3B82F6' }
  ]
})

const loadReport = async () => {
  if (!props.sessionId) return
  loading.value = true
  try {
    const res = await getReviewReport(props.sessionId)
    reportData.value = res.data
    showDetail.value = false
  } catch {
    reportData.value = null
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  if (!props.sessionId) return
  loading.value = true
  reportData.value = null
  showDetail.value = false
  try {
    const res = await generateReviewReport(props.sessionId)
    reportData.value = res.data
    emit('generated', res.data)
    ElMessage.success('复盘报告生成成功！')
  } catch (error) {
    console.error('生成复盘报告失败:', error)
    reportData.value = { status: 'FAILED', overallSummary: '生成失败，请稍后重试。' }
  } finally {
    loading.value = false
  }
}

const handleShowDetail = async () => {
  if (!props.sessionId) return
  loading.value = true
  try {
    const res = await getReviewReportDetail(props.sessionId)
    reportData.value = res.data
    showDetail.value = true
  } catch (error) {
    console.error('获取详细报告失败:', error)
    ElMessage.error('获取详细评价失败')
  } finally {
    loading.value = false
  }
}

const handleRegenerate = async () => {
  await handleGenerate()
  if (reportData.value?.status === 'COMPLETED') {
    await handleShowDetail()
  }
}

const onClosed = () => {
  reportData.value = null
  showDetail.value = false
}

const truncate = (text, maxLen) => {
  if (!text || text.length <= maxLen) return text
  return text.substring(0, maxLen) + '...'
}

watch(() => props.modelValue, (val) => {
  if (val && props.sessionId) {
    loadReport()
    if (props.autoGenerate && !reportData.value) {
      setTimeout(() => handleGenerate(), 300)
    }
  }
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.review-report-dialog {
  :deep(.el-dialog__header) {
    border-bottom: 1px solid $color-border-light;
    padding: 16px 24px;
    margin-right: 0;
  }
  :deep(.el-dialog__body) {
    padding: 0;
  }
  :deep(.el-dialog__footer) {
    border-top: 1px solid $color-border-light;
    padding: 16px 24px;
  }
}

// Loading
.report-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  gap: 16px;
  .loading-spinner {
    width: 40px; height: 40px;
    border: 3px solid $color-border-light;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  p { color: $color-text-secondary; font-size: $font-size-sm; }
}
@keyframes spin { to { transform: rotate(360deg); } }

.report-error {
  display: flex; flex-direction: column; align-items: center;
  padding: 40px 24px; gap: 12px;
  h3 { margin: 0; font-size: $font-size-lg; color: $color-text-primary; }
  p { color: $color-text-secondary; text-align: center; margin: 0; }
}

.report-empty {
  padding: 40px; text-align: center; color: $color-text-secondary;
}

// 内容区
.report-content {
  max-height: 70vh;
  overflow-y: auto;
  padding: 24px;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: $color-border-light; border-radius: 3px; }
}

// 概览区
.report-overview {
  display: flex;
  gap: 32px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.score-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.score-meta {
  text-align: center;
  .score-job { font-size: $font-size-base; font-weight: $font-weight-semibold; color: $color-text-primary; }
  .score-type { font-size: $font-size-xs; color: $color-text-secondary; margin-top: 2px; }
  .score-time { font-size: $font-size-xs; color: $color-text-placeholder; margin-top: 2px; }
}

.dimension-scores {
  flex: 1;
  .dim-title {
    font-size: $font-size-sm; font-weight: $font-weight-semibold;
    color: $color-text-primary; margin-bottom: 14px;
  }
  .dim-item { margin-bottom: 12px; }
  .dim-header {
    display: flex; justify-content: space-between; align-items: center;
    margin-bottom: 6px;
  }
  .dim-label { font-size: $font-size-sm; color: $color-text-secondary; }
  .dim-value { font-size: $font-size-sm; font-weight: $font-weight-bold; color: $color-text-primary; }
  .dim-unit { font-size: $font-size-xs; color: $color-text-secondary; font-weight: normal; margin-left: 1px; }
}

// Section通用
.section-header {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 14px;
  color: $color-primary;
  h4 { margin: 0; font-size: $font-size-base; font-weight: $font-weight-semibold; color: $color-text-primary; }
  .answer-count {
    margin-left: auto; font-size: $font-size-xs; color: $color-text-placeholder;
    background: $color-bg-hover; padding: 2px 8px; border-radius: $radius-full;
  }
}

// 总体总结
.summary-section {
  background: rgba(59, 130, 246, 0.04);
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: $radius-xl;
  padding: 18px 20px;
  margin-bottom: 20px;
  .summary-text { font-size: $font-size-base; line-height: 1.8; color: $color-text-primary; }
}

// 改进建议
.suggestions-section { margin-bottom: 20px; }
.suggestions-list {
  list-style: none; padding: 0; margin: 0;
  display: flex; flex-direction: column; gap: 10px;
}
.suggestion-item {
  display: flex; align-items: flex-start; gap: 10px;
  font-size: $font-size-sm; line-height: 1.6; color: $color-text-primary;
}
.suggestion-num {
  width: 20px; height: 20px; border-radius: $radius-full;
  background: rgba(102, 126, 234, 0.1); color: $color-primary;
  font-size: $font-size-xs; font-weight: $font-weight-bold;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0; margin-top: 1px;
}

// 回答评价卡片
.answers-section { margin-top: 8px; }
.answer-cards { display: flex; flex-direction: column; gap: 16px; }

.answer-card {
  background: $color-bg-card;
  border: 1px solid $color-border-light;
  border-radius: $radius-xl;
  padding: 16px 20px;
  transition: box-shadow $duration-fast;
  &:hover { box-shadow: $shadow-md; }
}

.answer-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
  .answer-num { font-size: $font-size-sm; font-weight: $font-weight-bold; color: $color-primary; }
  .answer-score {
    margin-left: auto;
    .score-val { font-size: $font-size-lg; font-weight: $font-weight-bold; color: $color-text-primary; }
    .score-denom { font-size: $font-size-xs; color: $color-text-secondary; }
  }
}

.answer-question, .answer-user {
  font-size: $font-size-sm; line-height: 1.7; margin-bottom: 8px;
  .q-label { font-weight: $font-weight-semibold; color: $color-text-secondary; }
}
.answer-user { color: $color-text-primary; }

// Review tags
.review-tag {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: $font-size-xs; font-weight: $font-weight-semibold;
  margin-bottom: 8px; margin-top: 8px;
}
.strength-tag { color: #10B981; }
.weakness-tag { color: #F59E0B; }
.tip-tag { color: $color-primary; }
.model-tag { color: #3B82F6; }

.review-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.review-chip {
  font-size: $font-size-xs; padding: 3px 10px; border-radius: $radius-full;
  &.chip-green { background: rgba(16, 185, 129, 0.1); color: #10B981; }
  &.chip-warn  { background: rgba(245, 158, 11, 0.1); color: #F59E0B; }
}

.tip-text, .model-text {
  font-size: $font-size-sm; line-height: 1.7; color: $color-text-primary;
  background: rgba(102, 126, 234, 0.04);
  border-radius: $radius-lg; padding: 10px 14px;
  border-left: 3px solid $color-primary;
}
.model-text { border-left-color: #3B82F6; }

.dialog-footer {
  display: flex; gap: 10px; justify-content: flex-end;
}
</style>
