<template>
  <div class="job-match-page">
    <!-- ══════════════════════════════════════
         页面标题
    ══════════════════════════════════════ -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">岗位匹配分析</h1>
        <p class="page-desc">上传简历，系统将自动分析简历与岗位描述的匹配度</p>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         分析配置区
    ══════════════════════════════════════ -->
    <div class="analyze-grid">
      <!-- 左侧：JD 输入 -->
      <div class="panel jd-panel">
        <div class="panel-header">
          <div class="panel-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          </div>
          <span>岗位描述 (JD)</span>
        </div>
        <div class="panel-body">
          <el-input
            v-model="jobContent"
            type="textarea"
            :rows="10"
            placeholder="请输入或粘贴岗位描述内容，包括岗位职责、任职要求等..."
            resize="none"
            class="jd-textarea"
          />
        </div>
      </div>

      <!-- 右侧：简历上传 -->
      <div class="panel resume-panel">
        <div class="panel-header">
          <div class="panel-icon panel-icon--green">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 16 12 12 8 16"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/></svg>
          </div>
          <span>上传简历</span>
        </div>
        <div class="panel-body">
          <el-upload
            ref="uploadRef"
            class="hm-upload"
            drag
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".txt,.doc,.docx,.pdf"
          >
            <div class="upload-content">
              <div class="upload-icon-wrap">
                <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.5">
                  <polyline points="16 16 12 12 8 16"/>
                  <line x1="12" y1="12" x2="12" y2="21"/>
                  <path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/>
                </svg>
              </div>
              <p>拖拽文件或<span class="upload-link">点击上传</span></p>
              <span class="upload-hint">支持 .txt .doc .docx .pdf</span>
            </div>
          </el-upload>

          <div v-if="uploadedFile" class="file-info-card">
            <div class="file-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/></svg>
            </div>
            <div class="file-info">
              <span class="file-name">{{ uploadedFile.name }}</span>
              <span class="file-size">{{ formatFileSize(uploadedFile.size) }}</span>
            </div>
            <button class="file-remove" @click="handleFileRemove">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>

          <el-button
            type="primary"
            class="analyze-btn"
            :loading="analyzing"
            :disabled="!canAnalyze"
            @click="handleAnalyze"
          >
            <svg v-if="!analyzing" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         分析结果
    ══════════════════════════════════════ -->
    <div v-if="result" class="result-section">
      <div class="result-header">
        <h2>匹配结果</h2>
        <span class="badge badge--success">
          {{ getScoreLabel(result.matchScore) }}
        </span>
      </div>

      <!-- 评分 + 雷达图 -->
      <div class="result-overview">
        <!-- 综合评分 -->
        <div class="score-card">
          <ScoreRing :score="result.matchScore || 0" :size="160" :stroke-width="14" />
          <div class="score-detail">
            <h3>综合匹配度</h3>
            <p>{{ getScoreDescription(result.matchScore) }}</p>
          </div>
        </div>

        <!-- 雷达图 -->
        <div class="radar-card">
          <h3>多维度能力分析</h3>
          <div class="radar-container" ref="radarRef">
            <canvas ref="radarCanvas" width="300" height="260"></canvas>
          </div>
          <div class="radar-legend">
            <div class="legend-item" v-for="dim in dimensions" :key="dim.label">
              <span class="legend-dot" :style="{ background: dim.color }"></span>
              <span class="legend-label">{{ dim.label }}</span>
              <span class="legend-value" :style="{ color: dim.color }">{{ dim.value }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 简历 vs JD 对照表 -->
      <div class="comparison-section">
        <h3>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/><line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/></svg>
          简历 vs 岗位要求对照
        </h3>
        <div class="comparison-grid">
          <div
            v-for="(item, idx) in comparisonItems"
            :key="idx"
            class="comparison-row"
            :class="`comparison-row--${item.match}`"
          >
            <div class="compare-label">{{ item.label }}</div>
            <div class="compare-indicator">
              <svg v-if="item.match === 'high'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              <svg v-else-if="item.match === 'mid'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#F59E0B" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </div>
            <div class="compare-tags">
              <span v-for="tag in item.tags" :key="tag" class="compare-tag" :class="`compare-tag--${item.match}`">{{ tag }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细分析 -->
      <div class="detail-section">
        <div class="detail-card">
          <h3>匹配概述</h3>
          <p>{{ result.matchResult || '暂无详细概述' }}</p>
        </div>
        <div class="detail-card" v-if="result.analysisDetails">
          <h3>详细分析</h3>
          <div class="detail-content" v-html="formatAnalysisDetails(result.analysisDetails)"></div>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         历史记录
    ══════════════════════════════════════ -->
    <div class="history-section">
      <div class="history-header">
        <h2>历史记录</h2>
      </div>
      <div v-if="historyList.length > 0" class="history-list">
        <div
          v-for="item in historyList"
          :key="item.id"
          class="history-card hover-lift"
          @click="viewHistoryDetail(item)"
        >
          <div class="history-card__left">
            <div class="history-score-wrap">
              <ScoreRing :score="item.matchScore || 0" :size="60" :stroke-width="6" />
            </div>
            <div class="history-info">
              <h4>{{ item.resumeFileName || '未知文件' }}</h4>
              <span class="history-date">{{ formatDate(item.createdAt) }}</span>
            </div>
          </div>
          <div class="history-card__right">
            <span class="badge" :class="getScoreBadge(item.matchScore)">
              {{ getScoreLabel(item.matchScore) }}
            </span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <p>暂无历史记录</p>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         详情对话框
    ══════════════════════════════════════ -->
    <el-dialog v-model="detailVisible" title="匹配详情" width="1100px" class="hm-dialog">
      <div v-if="currentRecord" class="detail-dialog-body">
        <!-- 顶部概览 -->
        <div class="detail-hero">
          <div class="detail-hero__score">
            <ScoreRing :score="currentRecord.matchScore || 0" :size="140" :stroke-width="12" />
            <div class="score-label">综合匹配度</div>
          </div>
          <div class="detail-hero__info">
            <div class="hero-meta">
              <span class="badge" :class="getScoreBadge(currentRecord.matchScore)">{{ getScoreLabel(currentRecord.matchScore) }}</span>
              <span class="hero-date">{{ formatDateTime(currentRecord.createdAt) }}</span>
            </div>
            <h2 class="hero-title">{{ currentRecord.resumeFileName || '简历匹配分析' }}</h2>
            <div class="hero-overview">
              <div class="overview-label">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                匹配概述
              </div>
              <p>{{ currentRecord.matchResult || '暂无概述' }}</p>
            </div>
          </div>
        </div>

        <!-- 详细分析 -->
        <div class="detail-analysis" v-if="currentRecord.analysisDetails">
          <h3 class="analysis-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
            详细分析
          </h3>
          <div class="analysis-body" v-html="formatAnalysisDetails(currentRecord.analysisDetails)"></div>
        </div>

        <!-- 空白提示 -->
        <el-empty v-if="!currentRecord.analysisDetails" description="暂无详细分析内容" :image-size="80" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { listMatchRecords, getMatchRecord } from '@/api/jobMatch'
import { ElMessage } from 'element-plus'
import ScoreRing from '@/components/ui/ScoreRing.vue'

const jobContent = ref('')
const uploadedFile = ref(null)
const analyzing = ref(false)
const result = ref(null)
const historyList = ref([])
const detailVisible = ref(false)
const currentRecord = ref(null)
const uploadRef = ref()
const radarCanvas = ref()
const radarRef = ref()

// 模拟维度数据（实际应由后端返回）
const dimensions = ref([
  { label: '技能匹配', value: 85, color: '#3B82F6' },
  { label: '经验匹配', value: 72, color: '#10B981' },
  { label: '教育匹配', value: 90, color: '#F59E0B' },
  { label: '行业匹配', value: 65, color: '#6366F1' },
  { label: '薪资匹配', value: 78, color: '#EC4899' },
])

// 模拟对照数据
const comparisonItems = ref([
  { label: 'Vue / React 前端框架', match: 'high', tags: ['精通', '3年+'] },
  { label: 'TypeScript 类型系统', match: 'mid', tags: ['熟悉', '1年'] },
  { label: 'Node.js 后端开发', match: 'low', tags: ['不了解'] },
  { label: '分布式系统设计', match: 'low', tags: ['未提及'] },
  { label: '项目管理经验', match: 'mid', tags: ['有经验'] },
])

const canAnalyze = computed(() => {
  return (jobContent.value.trim() || result.value) && uploadedFile.value
})

const handleFileChange = (file) => {
  uploadedFile.value = file.raw
}

const handleFileRemove = () => {
  uploadedFile.value = null
}

const handleAnalyze = async () => {
  if (!uploadedFile.value) {
    ElMessage.warning('请先上传简历文件')
    return
  }

  analyzing.value = true
  try {
    const { analyzeMatch } = await import('@/api/jobMatch')
    const response = await analyzeMatch(
      result.value ? result.value.jobDescriptionId : null,
      jobContent.value.trim(),
      uploadedFile.value
    )
    result.value = response.data
    // 模拟维度数据
    dimensions.value = [
      { label: '技能匹配', value: Math.floor(Math.random() * 30) + 60, color: '#3B82F6' },
      { label: '经验匹配', value: Math.floor(Math.random() * 30) + 60, color: '#10B981' },
      { label: '教育匹配', value: Math.floor(Math.random() * 30) + 60, color: '#F59E0B' },
      { label: '行业匹配', value: Math.floor(Math.random() * 30) + 60, color: '#6366F1' },
      { label: '薪资匹配', value: Math.floor(Math.random() * 30) + 60, color: '#EC4899' },
    ]
    // 模拟对照数据
    comparisonItems.value = [
      { label: 'Vue / React 前端框架', match: Math.random() > 0.3 ? 'high' : 'mid', tags: ['精通', '3年+'] },
      { label: 'TypeScript 类型系统', match: Math.random() > 0.5 ? 'mid' : 'low', tags: ['熟悉'] },
      { label: 'Node.js 后端开发', match: 'low', tags: ['不了解'] },
      { label: '分布式系统设计', match: Math.random() > 0.6 ? 'mid' : 'low', tags: ['有了解'] },
      { label: '团队协作能力', match: 'high', tags: ['突出'] },
    ]
    await loadHistory()
    ElMessage.success('分析完成')
    // 渲染雷达图
    nextTick(() => drawRadarChart())
  } catch (error) {
    console.error('Analyze failed:', error)
    ElMessage.error('分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

// 绘制雷达图（Canvas）
const drawRadarChart = () => {
  const canvas = radarCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = canvas.width
  const h = canvas.height
  const cx = w / 2
  const cy = h / 2 - 10
  const radius = Math.min(w, h) / 2 - 40
  const n = dimensions.value.length
  const angleStep = (Math.PI * 2) / n

  ctx.clearRect(0, 0, w, h)

  // 背景圆环
  for (let i = 1; i <= 4; i++) {
    const r = (radius / 4) * i
    ctx.beginPath()
    for (let j = 0; j < n; j++) {
      const angle = j * angleStep - Math.PI / 2
      const x = cx + r * Math.cos(angle)
      const y = cy + r * Math.sin(angle)
      if (j === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    }
    ctx.closePath()
    ctx.strokeStyle = '#E5E7EB'
    ctx.lineWidth = 1
    ctx.stroke()
  }

  // 维度标签
  ctx.font = '12px Inter, sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  for (let i = 0; i < n; i++) {
    const angle = i * angleStep - Math.PI / 2
    const lx = cx + (radius + 24) * Math.cos(angle)
    const ly = cy + (radius + 24) * Math.sin(angle)
    ctx.fillStyle = '#6B7280'
    ctx.fillText(dimensions.value[i].label, lx, ly)
  }

  // 数据多边形
  ctx.beginPath()
  for (let i = 0; i < n; i++) {
    const val = (dimensions.value[i].value / 100) * radius
    const angle = i * angleStep - Math.PI / 2
    const x = cx + val * Math.cos(angle)
    const y = cy + val * Math.sin(angle)
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  }
  ctx.closePath()
  ctx.fillStyle = 'rgba(102, 126, 234, 0.15)'
  ctx.fill()
  ctx.strokeStyle = '#3B82F6'
  ctx.lineWidth = 2
  ctx.stroke()

  // 数据点
  for (let i = 0; i < n; i++) {
    const val = (dimensions.value[i].value / 100) * radius
    const angle = i * angleStep - Math.PI / 2
    const x = cx + val * Math.cos(angle)
    const y = cy + val * Math.sin(angle)
    ctx.beginPath()
    ctx.arc(x, y, 4, 0, Math.PI * 2)
    ctx.fillStyle = dimensions.value[i].color
    ctx.fill()
    ctx.strokeStyle = '#fff'
    ctx.lineWidth = 2
    ctx.stroke()
  }
}

const loadHistory = async () => {
  try {
    const response = await listMatchRecords()
    historyList.value = response.data || []
  } catch (error) {
    console.error('Load history failed:', error)
  }
}

const viewHistoryDetail = async (item) => {
  try {
    const response = await getMatchRecord(item.id)
    currentRecord.value = response.data
    detailVisible.value = true
  } catch (error) {
    console.error('Load detail failed:', error)
  }
}

const getScoreLabel = (score) => {
  if (score >= 80) return '优秀'
  if (score >= 60) return '良好'
  if (score >= 40) return '一般'
  return '较差'
}

const getScoreBadge = (score) => {
  if (score >= 80) return 'badge--success'
  if (score >= 60) return 'badge--warning'
  return 'badge--danger'
}

const getScoreDescription = (score) => {
  if (score >= 80) return '简历与岗位高度匹配，建议重点准备面试。'
  if (score >= 60) return '简历与岗位基本匹配，有一定提升空间。'
  if (score >= 40) return '简历与岗位匹配度一般，建议针对性优化。'
  return '简历与岗位匹配度较低，建议全面优化简历。'
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

const formatAnalysisDetails = (details) => {
  if (!details) return ''
  return details
    .replace(/\n/g, '<br>')
    .replace(/(\d+\.)/g, '<strong>$1</strong>')
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.job-match-page {
  max-width: 1200px;
  margin: 0 auto;
}

// ── 页面标题 ───────────────────────────────
.page-header {
  margin-bottom: $spacing-6;

  .header-left {
    .page-title {
      font-size: $font-size-3xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 6px;
    }
    .page-desc {
      font-size: $font-size-base;
      color: $color-text-secondary;
      margin: 0;
    }
  }
}

// ── 分析配置区 ──────────────────────────────
.analyze-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-5;
  margin-bottom: $spacing-6;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.panel {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  overflow: hidden;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-sm;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: #fff;
  background: $color-sidebar-bg;

  .panel-icon {
    width: 32px;
    height: 32px;
    border-radius: $radius-lg;
    background: rgba(102, 126, 234, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &--green {
      background: rgba(16, 185, 129, 0.2);
    }
  }
}

.panel-body {
  padding: 20px;
}

// JD 文本框
.jd-textarea {
  :deep(.el-textarea__inner) {
    font-family: $font-family-base;
    font-size: $font-size-sm;
    line-height: 1.8;
    border: none;
    background: $color-bg-page;
    padding: 12px 16px;
    border-radius: $radius-xl;
    &:focus { box-shadow: none; }
  }
}

// 上传
.hm-upload {
  width: 100%;
  :deep(.el-upload) { width: 100%; }
  :deep(.el-upload-dragger) {
    width: 100%;
    height: 160px;
    border-radius: $radius-xl;
    border: 1.5px dashed $color-border;
    background: $color-bg-page;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all $duration-normal;
    &:hover {
      border-color: $color-primary;
      background: $color-bg-active;
    }
  }
}

.upload-content {
  text-align: center;
  .upload-icon-wrap {
    width: 64px;
    height: 64px;
    border-radius: $radius-full;
    background: rgba(102, 126, 234, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
  }
  p {
    font-size: $font-size-base;
    color: $color-text-secondary;
    margin: 0 0 6px;
  }
  .upload-link {
    color: $color-primary;
    font-weight: $font-weight-medium;
  }
  .upload-hint {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

// 文件信息卡
.file-info-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $color-bg-page;
  border-radius: $radius-xl;
  margin-top: 12px;

  .file-icon {
    width: 40px;
    height: 40px;
    border-radius: $radius-lg;
    background: rgba(102, 126, 234, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .file-info {
    flex: 1;
    overflow: hidden;
    .file-name {
      display: block;
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .file-size {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }

  .file-remove {
    width: 28px;
    height: 28px;
    border-radius: $radius-md;
    border: none;
    background: transparent;
    color: $color-text-placeholder;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all $duration-fast;
    &:hover {
      background: #FEE2E2;
      color: $color-danger;
    }
  }
}

.analyze-btn {
  width: 100%;
  height: 48px;
  margin-top: 16px;
  border-radius: $radius-xl;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  background: $color-primary;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all $duration-normal $ease-spring;
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: $shadow-primary-lg;
  }
  &:disabled {
    background: #D1D5DB;
    cursor: not-allowed;
  }
}

// ── 结果区 ────────────────────────────────
.result-section {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-6;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-sm;
  margin-bottom: $spacing-6;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: $spacing-6;

  h2 {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0;
  }
}

.result-overview {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: $spacing-6;
  margin-bottom: $spacing-6;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.score-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: $color-bg-page;
  border-radius: $radius-2xl;

  .score-detail {
    h3 {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 8px;
    }
    p {
      font-size: $font-size-sm;
      color: $color-text-secondary;
      margin: 0;
      max-width: 220px;
      line-height: $line-height-loose;
    }
  }
}

// 雷达图卡片
.radar-card {
  background: $color-bg-page;
  border-radius: $radius-2xl;
  padding: 20px;
  display: flex;
  flex-direction: column;

  h3 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 12px;
  }
}

.radar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;
}

.radar-legend {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: $font-size-sm;

  .legend-dot {
    width: 8px;
    height: 8px;
    border-radius: $radius-full;
    flex-shrink: 0;
  }

  .legend-label {
    flex: 1;
    color: $color-text-secondary;
  }

  .legend-value {
    font-weight: $font-weight-semibold;
    min-width: 36px;
    text-align: right;
  }
}

// 对照表
.comparison-section {
  margin-bottom: $spacing-6;

  h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 16px;

    svg { color: $color-primary; }
  }
}

.comparison-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comparison-row {
  display: grid;
  grid-template-columns: 200px 32px 1fr;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  border-radius: $radius-xl;
  background: $color-bg-page;
  border: 1px solid $color-border-light;
  transition: all $duration-fast;

  &--high {
    border-left: 3px solid #10B981;
    .compare-indicator { color: #10B981; }
  }

  &--mid {
    border-left: 3px solid #F59E0B;
    .compare-indicator { color: #F59E0B; }
  }

  &--low {
    border-left: 3px solid #EF4444;
    .compare-indicator { color: #EF4444; }
  }
}

.compare-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.compare-indicator {
  display: flex;
  justify-content: center;
}

.compare-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.compare-tag {
  font-size: $font-size-xs;
  padding: 2px 10px;
  border-radius: $radius-full;
  font-weight: $font-weight-medium;

  &--high {
    background: #DCFCE7;
    color: #15803D;
  }
  &--mid {
    background: #FEF3C7;
    color: #B45309;
  }
  &--low {
    background: #FEE2E2;
    color: #DC2626;
  }
}

// 详细分析
.detail-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-5;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.detail-card {
  background: $color-bg-page;
  border-radius: $radius-xl;
  padding: 20px;

  h3 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 12px;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: $line-height-loose;
    margin: 0;
  }
}

.detail-content {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: $line-height-loose;
  max-height: 200px;
  overflow-y: auto;
}

// ── 历史记录 ──────────────────────────────
.history-section {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-6;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-sm;

  .history-header {
    margin-bottom: $spacing-4;
    h2 {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0;
    }
  }
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-3;
}

.history-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: $color-bg-page;
  border-radius: $radius-xl;
  border: 1px solid $color-border-light;
  cursor: pointer;
  transition: all $duration-normal;

  &:hover {
    border-color: $color-primary;
    box-shadow: $shadow-primary;
  }

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: 12px;
    color: $color-text-placeholder;
  }

  .history-score-wrap {
    flex-shrink: 0;
  }

  .history-info {
    h4 {
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin: 0 0 4px;
    }
    .history-date {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
    }
  }
}

// 详情对话框
:deep(.hm-dialog) {
  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid $color-border-light;
    margin-right: 0;
  }
  .el-dialog__body {
    padding: 0;
  }
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid $color-border-light;
  }
}

// Dialog body
.detail-dialog-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// Hero 概览
.detail-hero {
  display: flex;
  gap: 28px;
  padding: 24px;
  background: $color-bg-page;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  align-items: flex-start;

  &__score {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }

  &__info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}

.score-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hero-date {
  font-size: $font-size-sm;
  color: $color-text-placeholder;
}

.hero-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin: 0;
}

.hero-overview {
  background: $color-bg-card;
  border-radius: $radius-lg;
  border: 1px solid $color-border-light;
  padding: 12px 14px;

  .overview-label {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: $font-size-xs;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    margin-bottom: 6px;
    svg { color: $color-primary; }
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-regular;
    line-height: $line-height-loose;
    margin: 0;
    white-space: pre-wrap;
  }
}

// 详细分析
.detail-analysis {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  padding: 20px 24px;
}

.analysis-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  margin: 0 0 14px;
  svg { color: $color-primary; }
}

.analysis-body {
  font-size: $font-size-sm;
  color: $color-text-regular;
  line-height: $line-height-loose;
  max-height: 400px;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background: $color-border; border-radius: 3px; }

  :deep(p) { margin: 0 0 12px; }
  :deep(p:last-child) { margin-bottom: 0; }
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: $color-text-placeholder;
  font-size: $font-size-sm;
}
</style>