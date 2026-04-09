<!--
  QuestionBank.vue — 面试题库页面 (现代化重构版)
  设计理念：清新、现代、高效、沉浸式
-->
<template>
  <div class="qb-root">

    <!-- ═══════════════════════════════════════
         顶部 Hero 区域
    ═══════════════════════════════════════ -->
    <header class="qb-hero">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="hero-title-icon">
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              </svg>
            </span>
            面试题库
          </h1>
          <p class="hero-desc">海量面试题目 + AI 智能生成，助你高效备战面试</p>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-num">{{ total > 0 ? total : '--' }}</span>
            <span class="stat-label">题目总数</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ favoriteCount }}</span>
            <span class="stat-label">我的收藏</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ favoriteCategories.length }}</span>
            <span class="stat-label">收藏分类</span>
          </div>
        </div>
      </div>

      <!-- 装饰背景 -->
      <div class="hero-bg">
        <div class="hero-blob hero-blob-1"></div>
        <div class="hero-blob hero-blob-2"></div>
      </div>
    </header>

    <!-- ═══════════════════════════════════════
         搜索与筛选栏
    ═══════════════════════════════════════ -->
    <div class="qb-filters">
      <div class="filter-search">
        <div class="search-wrap">
          <svg class="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="searchKeyword"
            class="search-input"
            placeholder="搜索题目内容、标签..."
            @keyup.enter="handleSearch"
          />
          <button v-if="searchKeyword" class="search-clear" @click="clearSearch">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="filter-chips">
        <!-- 难度筛选 -->
        <div class="chip-group">
          <span class="chip-label">难度</span>
          <div class="chip-list">
            <button
              class="chip"
              :class="{ 'chip--active': filterDifficulty === '' }"
              @click="setDifficulty('')"
            >不限</button>
            <button
              class="chip chip--easy"
              :class="{ 'chip--active': filterDifficulty === 'EASY' }"
              @click="setDifficulty('EASY')"
            >
              <span class="chip-dot"></span>简单
            </button>
            <button
              class="chip chip--medium"
              :class="{ 'chip--active': filterDifficulty === 'MEDIUM' }"
              @click="setDifficulty('MEDIUM')"
            >
              <span class="chip-dot"></span>中等
            </button>
            <button
              class="chip chip--hard"
              :class="{ 'chip--active': filterDifficulty === 'HARD' }"
              @click="setDifficulty('HARD')"
            >
              <span class="chip-dot"></span>困难
            </button>
          </div>
        </div>

        <div class="filter-sep"></div>

        <!-- 面试类型筛选 -->
        <div class="chip-group">
          <span class="chip-label">类型</span>
          <div class="chip-list">
            <button
              class="chip"
              :class="{ 'chip--active': filterInterviewType === '' }"
              @click="setInterviewType('')"
            >不限</button>
            <button
              v-for="t in interviewTypeOptions"
              :key="t.value"
              class="chip"
              :class="{ 'chip--active': filterInterviewType === t.value }"
              :style="filterInterviewType === t.value ? { '--chip-color': t.color, background: t.bg, borderColor: t.color } : {}"
              @click="setInterviewType(t.value)"
            >{{ t.label }}</button>
          </div>
        </div>
      </div>

      <div class="filter-actions">
        <div class="mode-toggle">
          <button
            class="mode-btn"
            :class="{ 'mode-btn--active': listMode === 'all' }"
            @click="switchToAllMode"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
            全部题目
          </button>
          <button
            class="mode-btn"
            :class="{ 'mode-btn--active': listMode === 'favorites' }"
            @click="switchToFavoritesMode"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" :fill="listMode === 'favorites' ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            我的收藏
            <span v-if="favoriteCount > 0" class="mode-badge">{{ favoriteCount }}</span>
          </button>
        </div>

        <el-button type="primary" class="btn-generate" @click="showGenerateDialog = true">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 2L2 7L12 12L22 7L12 2Z"/><path d="M2 17L12 22L22 17"/><path d="M2 12L12 17L22 12"/>
          </svg>
          AI 生成
        </el-button>
      </div>
    </div>

    <!-- ═══════════════════════════════════════
         主内容区
    ═══════════════════════════════════════ -->
    <div class="qb-body">

      <!-- 收藏分类侧边栏 (仅收藏模式) -->
      <aside class="qb-sidebar" :class="{ 'qb-sidebar--visible': listMode === 'favorites' }">
        <div class="sidebar-card">
          <div class="sidebar-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
            收藏分类
          </div>

          <div class="sidebar-list">
            <!-- 全部收藏 -->
            <button
              class="sidebar-item"
              :class="{ 'sidebar-item--active': selectedFavoriteType === null }"
              @click="selectFavoriteType(null)"
            >
              <div class="si-left">
                <div class="si-icon si-icon--all">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                  </svg>
                </div>
                <span class="si-name">全部收藏</span>
              </div>
              <span class="si-count">{{ favoriteCount }}</span>
            </button>

            <!-- 分类列表 -->
            <button
              v-for="cat in favoriteCategories"
              :key="cat.interviewType"
              class="sidebar-item"
              :class="{ 'sidebar-item--active': selectedFavoriteType === cat.interviewType }"
              @click="selectFavoriteType(cat.interviewType)"
            >
              <div class="si-left">
                <div class="si-icon" :style="{ background: getTypeBg(cat.interviewType) }">
                  <svg width="14" height="14" viewBox="0 0 24 24" :fill="getTypeColor(cat.interviewType)" :stroke="getTypeColor(cat.interviewType)" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                </div>
                <span class="si-name">{{ cat.interviewTypeLabel }}</span>
              </div>
              <span class="si-count">{{ cat.count }}</span>
            </button>

            <!-- 空状态 -->
            <div v-if="favoriteCategories.length === 0" class="sidebar-empty">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
              </svg>
              <p>暂无收藏分类</p>
              <p class="empty-hint">收藏题目后将自动归类</p>
            </div>
          </div>
        </div>
      </aside>

      <!-- 题目列表主区域 -->
      <main class="qb-main">
        <!-- 当前筛选状态 -->
        <div v-if="hasActiveFilters" class="active-filters">
          <span class="af-label">当前筛选：</span>
          <div class="af-tags">
            <span v-if="filterDifficulty" class="af-tag">
              {{ getDifficultyLabel(filterDifficulty) }}
              <button @click="setDifficulty('')">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </span>
            <span v-if="filterInterviewType" class="af-tag">
              {{ filterInterviewType }}
              <button @click="setInterviewType('')">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </span>
            <span v-if="searchKeyword" class="af-tag">
              "{{ searchKeyword }}"
              <button @click="clearSearch">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </span>
            <button class="af-clear" @click="clearAllFilters">清除全部</button>
          </div>
        </div>

        <!-- 加载骨架屏 -->
        <div v-if="loading" class="qb-grid">
          <div v-for="n in 8" :key="n" class="qb-skeleton">
            <div class="sk-header">
              <div class="sk-tag"></div>
              <div class="sk-fav"></div>
            </div>
            <div class="sk-title"></div>
            <div class="sk-title sk-title--short"></div>
            <div class="sk-footer">
              <div class="sk-meta"></div>
              <div class="sk-views"></div>
            </div>
          </div>
        </div>

        <!-- 题目卡片列表 -->
        <div v-else-if="questions.length > 0" class="qb-grid">
          <article
            v-for="q in questions"
            :key="q.id"
            class="qb-card"
            @click="openQuestionDetail(q)"
          >
            <div class="qc-top">
              <div class="qc-tags">
                <span class="qc-tag qc-tag--type" :style="{ color: getTypeColor(q.interviewType), background: getTypeBg(q.interviewType), borderColor: getTypeColor(q.interviewType) + '40' }">
                  {{ q.interviewType || '综合' }}
                </span>
                <span class="qc-tag qc-tag--diff" :class="`diff-${(q.difficulty || 'MEDIUM').toLowerCase()}`">
                  {{ q.difficultyLabel || q.difficulty }}
                </span>
              </div>
              <button
                class="qc-fav"
                :class="{ 'qc-fav--active': q.isFavorited }"
                @click.stop="handleToggleFavorite(q)"
                :title="q.isFavorited ? '取消收藏' : '收藏'"
              >
                <svg width="18" height="18" viewBox="0 0 24 24" :fill="q.isFavorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </button>
            </div>

            <p class="qc-text">{{ q.questionText }}</p>

            <div class="qc-bottom">
              <div class="qc-meta">
                <span v-if="q.targetPosition" class="qc-meta-item">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
                  {{ q.targetPosition }}
                </span>
                <span v-if="q.source" class="qc-meta-item qc-source" :class="`qc-source--${q.source.toLowerCase()}`">
                  {{ q.source === 'AI' ? 'AI生成' : '录入' }}
                </span>
              </div>
              <div class="qc-views">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                {{ q.viewCount || 0 }}
              </div>
            </div>
          </article>
        </div>

        <!-- 空状态 -->
        <div v-else class="qb-empty">
          <div class="empty-illustration">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="#E5E7EB" stroke-width="1">
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
              <line x1="9" y1="7" x2="15" y2="7"/><line x1="9" y1="11" x2="15" y2="11"/>
            </svg>
          </div>
          <h3 class="empty-title">{{ getEmptyTitle() }}</h3>
          <p class="empty-desc">{{ getEmptyDesc() }}</p>
          <div class="empty-actions">
            <el-button v-if="hasActiveFilters" plain @click="clearAllFilters">清除筛选</el-button>
            <el-button v-if="listMode === 'all'" type="primary" @click="showGenerateDialog = true">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <path d="M12 2L2 7L12 12L22 7L12 2Z"/><path d="M2 17L12 22L22 17"/><path d="M2 12L12 17L22 12"/>
              </svg>
              AI 生成题目
            </el-button>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="!loading && questions.length > 0" class="qb-pagination">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next, jumper"
            background
            @current-change="loadQuestions"
          />
          <span class="page-info">共 {{ total }} 条</span>
        </div>
      </main>
    </div>

    <!-- ═══════════════════════════════════════
         AI 生成题目弹窗
    ═══════════════════════════════════════ -->
    <el-dialog v-model="showGenerateDialog" title="AI 生成面试题目" width="560px" class="gen-dialog" :close-on-click-modal="false">
      <div class="gen-form">
        <div class="gen-field">
          <label>目标岗位</label>
          <el-input v-model="generateForm.jobPosition" placeholder="如：Java 后端开发工程师、前端工程师" size="large" />
        </div>

        <div class="gen-row">
          <div class="gen-field">
            <label>面试类型</label>
            <el-select v-model="generateForm.interviewType" placeholder="选择类型" size="large" style="width:100%">
              <el-option label="综合面试" value="综合面试" />
              <el-option label="技术面试" value="技术面试" />
              <el-option label="HR面试" value="HR面试" />
              <el-option label="行为面试" value="行为面试" />
              <el-option label="终面" value="终面" />
            </el-select>
          </div>
          <div class="gen-field">
            <label>题目难度</label>
            <el-select v-model="generateForm.difficulty" placeholder="选择难度" size="large" style="width:100%">
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </div>
        </div>

        <div class="gen-row">
          <div class="gen-field">
            <label>生成数量</label>
            <el-input-number v-model="generateForm.count" :min="3" :max="20" size="large" :precision="0" />
          </div>
          <div class="gen-field">
            <label>保存分类</label>
            <el-select v-model="generateForm.categoryId" placeholder="不指定" clearable size="large" style="width:100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button size="large" @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" size="large" :loading="generating" class="btn-gen-submit" @click="handleGenerate">
          <svg v-if="!generating" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M12 2L2 7L12 12L22 7L12 2Z"/><path d="M2 17L12 22L22 17"/><path d="M2 12L12 17L22 12"/>
          </svg>
          {{ generating ? '生成中...' : '开始生成' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ═══════════════════════════════════════
         题目详情弹窗
    ═══════════════════════════════════════ -->
    <el-dialog v-model="showDetailDialog" :title="'题目详情'" width="640px" class="detail-dialog">
      <div v-if="selectedQuestion" class="detail-content">
        <div class="detail-tags">
          <el-tag v-if="selectedQuestion.interviewType" effect="plain">{{ selectedQuestion.interviewType }}</el-tag>
          <el-tag v-if="selectedQuestion.difficulty" effect="plain" :type="getDifficultyTagType(selectedQuestion.difficulty)">
            {{ selectedQuestion.difficultyLabel || selectedQuestion.difficulty }}
          </el-tag>
          <el-tag v-if="selectedQuestion.categoryName" effect="plain" type="info">{{ selectedQuestion.categoryName }}</el-tag>
          <el-tag v-if="selectedQuestion.targetPosition" effect="plain" type="info">{{ selectedQuestion.targetPosition }}</el-tag>
        </div>

        <div class="detail-section">
          <div class="detail-label">题目内容</div>
          <div class="detail-text">{{ selectedQuestion.questionText }}</div>
        </div>

        <div class="detail-section" v-if="selectedQuestion.answerPoints?.length">
          <div class="detail-label">答案要点</div>
          <ul class="answer-points">
            <li v-for="(point, i) in selectedQuestion.answerPoints" :key="i">{{ point }}</li>
          </ul>
        </div>

        <div class="detail-section" v-if="selectedQuestion.modelAnswer">
          <div class="detail-label">参考示范回答</div>
          <div class="model-answer">{{ selectedQuestion.modelAnswer }}</div>
        </div>

        <div class="detail-section" v-if="selectedQuestion.tags?.length">
          <div class="detail-label">标签</div>
          <div class="detail-tags-wrap">
            <el-tag v-for="tag in selectedQuestion.tags" :key="tag" size="small" effect="plain" type="info">{{ tag }}</el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button size="large" @click="showDetailDialog = false">关闭</el-button>
        <el-button size="large" :type="selectedQuestion?.isFavorited ? 'default' : 'primary'" @click="handleToggleFavorite(selectedQuestion, true)">
          <svg width="14" height="14" viewBox="0 0 24 24" :fill="selectedQuestion?.isFavorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          {{ selectedQuestion?.isFavorited ? '取消收藏' : '收藏题目' }}
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategories, getQuestions, generateQuestions, toggleFavorite, getFavorites, getFavoriteCategories, getFavoritesByType } from '@/api/questionBank'

// ─── 状态 ─────────────────────────────────────────
const searchKeyword = ref('')
const filterDifficulty = ref('')
const filterInterviewType = ref('')
const selectedCategoryId = ref(null)
const listMode = ref('all')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const favoriteCategories = ref([])
const favoriteCount = ref(0)
const selectedFavoriteType = ref(null)

const questions = ref([])
const categories = ref([])
const loading = ref(false)

const showGenerateDialog = ref(false)
const generating = ref(false)
const generateForm = reactive({
  jobPosition: '',
  interviewType: '',
  difficulty: 'MEDIUM',
  count: 5,
  categoryId: null
})

const showDetailDialog = ref(false)
const selectedQuestion = ref(null)

// ─── 常量 ─────────────────────────────────────────
const interviewTypeOptions = [
  { value: '技术面试', label: '技术面试', color: '#3B82F6', bg: 'rgba(102,126,234,0.1)' },
  { value: '行为面试', label: '行为面试', color: '#F59E0B', bg: 'rgba(245,158,11,0.1)' },
  { value: 'HR面试', label: 'HR面试', color: '#10B981', bg: 'rgba(16,185,129,0.1)' },
  { value: '综合面试', label: '综合面试', color: '#6366F1', bg: 'rgba(99,102,241,0.1)' },
  { value: '终面', label: '终面', color: '#EC4899', bg: 'rgba(236,72,153,0.1)' }
]

// ─── 计算属性 ───────────────────────────────────────
const hasActiveFilters = computed(() => {
  return filterDifficulty.value !== '' || filterInterviewType.value !== '' || searchKeyword.value !== ''
})

// ─── 数据加载 ───────────────────────────────────────
const loadCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const loadFavoriteCategories = async () => {
  try {
    const res = await getFavoriteCategories()
    favoriteCategories.value = res.data || []
    favoriteCount.value = favoriteCategories.value.reduce((sum, cat) => sum + cat.count, 0)
  } catch (e) {
    console.error('加载收藏分类失败', e)
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    let res
    if (listMode.value === 'favorites') {
      res = await getFavoritesByType({
        interviewType: selectedFavoriteType.value || undefined,
        difficulty: filterDifficulty.value || undefined,
        page: page.value,
        size: pageSize.value
      })
      questions.value = res.data?.records || []
      total.value = res.data?.total || 0
    } else {
      res = await getQuestions({
        categoryId: selectedCategoryId.value,
        keyword: searchKeyword.value || undefined,
        difficulty: filterDifficulty.value || undefined,
        interviewType: filterInterviewType.value || undefined,
        page: page.value,
        size: pageSize.value
      })
      questions.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (e) {
    console.error('加载题目失败', e)
    questions.value = []
  } finally {
    loading.value = false
  }
}

// ─── 交互方法 ───────────────────────────────────────
const switchToAllMode = () => {
  listMode.value = 'all'
  selectedFavoriteType.value = null
  page.value = 1
  loadQuestions()
}

const switchToFavoritesMode = async () => {
  listMode.value = 'favorites'
  selectedFavoriteType.value = null
  page.value = 1
  await loadFavoriteCategories()
  loadQuestions()
}

const selectFavoriteType = (interviewType) => {
  selectedFavoriteType.value = interviewType
  page.value = 1
  loadQuestions()
}

const setDifficulty = (val) => {
  filterDifficulty.value = val
  page.value = 1
  loadQuestions()
}

const setInterviewType = (val) => {
  filterInterviewType.value = val
  page.value = 1
  loadQuestions()
}

const handleSearch = () => {
  page.value = 1
  loadQuestions()
}

const clearSearch = () => {
  searchKeyword.value = ''
  page.value = 1
  loadQuestions()
}

const clearAllFilters = () => {
  searchKeyword.value = ''
  filterDifficulty.value = ''
  filterInterviewType.value = ''
  page.value = 1
  loadQuestions()
}

const selectCategory = (id) => {
  selectedCategoryId.value = selectedCategoryId.value === id ? null : id
  page.value = 1
  loadQuestions()
}

const handleGenerate = async () => {
  if (!generateForm.jobPosition && !generateForm.interviewType) {
    ElMessage.warning('请至少填写目标岗位或面试类型')
    return
  }
  generating.value = true
  try {
    await generateQuestions(generateForm)
    ElMessage.success('题目生成成功！')
    showGenerateDialog.value = false
    page.value = 1
    loadQuestions()
  } catch (e) {
    console.error('生成题目失败', e)
  } finally {
    generating.value = false
  }
}

const openQuestionDetail = (q) => {
  selectedQuestion.value = q
  showDetailDialog.value = true
}

const handleToggleFavorite = async (q, closeDetail = false) => {
  try {
    const res = await toggleFavorite(q.id)
    const isFav = res.data?.isFavorited ?? res.data
    q.isFavorited = isFav
    ElMessage.success(isFav ? '已收藏' : '已取消收藏')
    if (listMode.value === 'favorites') {
      await loadFavoriteCategories()
      if (selectedFavoriteType.value && !favoriteCategories.value.find(c => c.interviewType === selectedFavoriteType.value)) {
        selectedFavoriteType.value = null
      }
      loadQuestions()
    } else {
      await loadFavoriteCategories()
    }
    if (closeDetail) {
      showDetailDialog.value = false
    }
  } catch (e) {
    console.error('收藏操作失败', e)
  }
}

// ─── 辅助方法 ───────────────────────────────────────
const getTypeBg = (type) => {
  const map = {
    '技术面试': 'rgba(102, 126, 234, 0.1)',
    'HR面试': 'rgba(16, 185, 129, 0.1)',
    '综合面试': 'rgba(99, 102, 241, 0.1)',
    '行为面试': 'rgba(245, 158, 11, 0.1)',
    '终面': 'rgba(236, 72, 153, 0.1)'
  }
  return map[type] || 'rgba(102, 126, 234, 0.1)'
}

const getTypeColor = (type) => {
  const map = {
    '技术面试': '#3B82F6',
    'HR面试': '#10B981',
    '综合面试': '#6366F1',
    '行为面试': '#F59E0B',
    '终面': '#EC4899'
  }
  return map[type] || '#3B82F6'
}

const getDifficultyLabel = (diff) => {
  const map = { 'EASY': '简单', 'MEDIUM': '中等', 'HARD': '困难' }
  return map[diff] || diff
}

const getDifficultyTagType = (diff) => {
  const map = { EASY: 'success', MEDIUM: 'warning', HARD: 'danger' }
  return map[diff] || 'info'
}

const getEmptyTitle = () => {
  if (listMode.value === 'favorites') return '暂无收藏题目'
  if (hasActiveFilters.value) return '没有符合条件的题目'
  return '暂无相关题目'
}

const getEmptyDesc = () => {
  if (listMode.value === 'favorites') return '快去收藏感兴趣的题目吧'
  if (hasActiveFilters.value) return '尝试调整筛选条件，或让 AI 为你生成题目'
  return '尝试调整筛选条件，或让 AI 为你生成题目'
}

// ─── 生命周期 ───────────────────────────────────────
onMounted(async () => {
  await loadCategories()
  await loadFavoriteCategories()
  loadQuestions()
})
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

// ═══════════════════════════════════════
// 根容器
// ═══════════════════════════════════════
.qb-root {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 $spacing-6 $spacing-10;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: $spacing-5;
}

// ═══════════════════════════════════════
// Hero 区域
// ═══════════════════════════════════════
.qb-hero {
  position: relative;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: 32px 36px;
  border: 1px solid $color-border-light;
  overflow: hidden;
  box-shadow: $shadow-sm;

  .hero-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: $spacing-6;
  }

  .hero-text {
    .hero-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: $font-size-2xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 6px;

      .hero-title-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 48px;
        height: 48px;
        border-radius: $radius-xl;
        background: rgba(102, 126, 234, 0.1);
        color: $color-primary;
        flex-shrink: 0;
      }
    }

    .hero-desc {
      font-size: $font-size-base;
      color: $color-text-secondary;
      margin: 0;
    }
  }

  .hero-stats {
    display: flex;
    align-items: center;
    gap: $spacing-6;
    background: $color-bg-page;
    border-radius: $radius-xl;
    padding: 16px 28px;
    border: 1px solid $color-border-light;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;

      .stat-num {
        font-size: $font-size-2xl;
        font-weight: $font-weight-bold;
        color: $color-primary;
        line-height: 1;
      }

      .stat-label {
        font-size: $font-size-xs;
        color: $color-text-placeholder;
      }
    }

    .stat-divider {
      width: 1px;
      height: 32px;
      background: $color-border;
    }
  }

  .hero-bg {
    position: absolute;
    inset: 0;
    pointer-events: none;

    .hero-blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(60px);
      opacity: 0.15;
    }

    .hero-blob-1 {
      width: 200px;
      height: 200px;
      background: #3B82F6;
      top: -80px;
      right: 200px;
    }

    .hero-blob-2 {
      width: 150px;
      height: 150px;
      background: #60A5FA;
      bottom: -60px;
      right: 80px;
    }
  }
}

// ═══════════════════════════════════════
// 筛选栏
// ═══════════════════════════════════════
.qb-filters {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: 20px 24px;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-search {
  .search-wrap {
    position: relative;
    display: flex;
    align-items: center;

    .search-icon {
      position: absolute;
      left: 16px;
      color: $color-text-placeholder;
      pointer-events: none;
    }

    .search-input {
      width: 100%;
      height: 44px;
      padding: 0 44px;
      border: 1.5px solid $color-border;
      border-radius: $radius-xl;
      font-size: $font-size-base;
      color: $color-text-primary;
      background: $color-bg-page;
      transition: all $duration-normal $ease-default;
      outline: none;

      &::placeholder { color: $color-text-placeholder; }

      &:focus {
        border-color: $color-primary;
        background: $color-bg-card;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
      }
    }

    .search-clear {
      position: absolute;
      right: 14px;
      background: none;
      border: none;
      cursor: pointer;
      color: $color-text-placeholder;
      display: flex;
      align-items: center;
      padding: 4px;
      border-radius: $radius-md;
      transition: all $duration-fast;

      &:hover { color: $color-text-primary; background: $color-bg-hover; }
    }
  }
}

.filter-chips {
  display: flex;
  align-items: center;
  gap: $spacing-4;
  flex-wrap: wrap;

  .chip-group {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .chip-label {
    font-size: $font-size-sm;
    font-weight: $font-weight-medium;
    color: $color-text-secondary;
    white-space: nowrap;
  }

  .chip-list {
    display: flex;
    gap: 6px;
  }

  .filter-sep {
    width: 1px;
    height: 20px;
    background: $color-border;
    flex-shrink: 0;
  }
}

.chip {
  padding: 5px 14px;
  border-radius: $radius-full;
  border: 1.5px solid $color-border;
  background: $color-bg-page;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all $duration-fast $ease-default;

  .chip-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: currentColor;
  }

  &:hover {
    border-color: $color-primary;
    color: $color-primary;
    background: rgba(102, 126, 234, 0.05);
  }

  &--active {
    border-color: var(--chip-color, $color-primary);
    color: var(--chip-color, $color-primary);
    background: var(--chip-bg, rgba(102, 126, 234, 0.08));
    font-weight: $font-weight-semibold;
  }

  &--easy.chip--active { --chip-color: #10B981; --chip-bg: rgba(16,185,129,0.1); }
  &--medium.chip--active { --chip-color: #F59E0B; --chip-bg: rgba(245,158,11,0.1); }
  &--hard.chip--active { --chip-color: #EF4444; --chip-bg: rgba(239,68,68,0.1); }
}

.filter-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-4;
  padding-top: 4px;
  border-top: 1px solid $color-border-light;
}

.mode-toggle {
  display: flex;
  background: $color-bg-page;
  border-radius: $radius-xl;
  padding: 4px;
  gap: 2px;
}

.mode-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: none;
  border-radius: $radius-lg;
  background: transparent;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  cursor: pointer;
  transition: all $duration-normal $ease-default;
  position: relative;

  &:hover { color: $color-text-primary; background: $color-bg-hover; }

  &--active {
    background: $color-bg-card;
    color: $color-primary;
    font-weight: $font-weight-semibold;
    box-shadow: $shadow-sm;
  }
}

.mode-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: $color-primary;
  color: #fff;
  font-size: 10px;
  font-weight: $font-weight-bold;
  border-radius: $radius-full;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-generate {
  display: flex;
  align-items: center;
  gap: 6px;
  background: $color-primary;
  border: none;
  border-radius: $radius-xl;
  padding: 10px 20px;
  font-weight: $font-weight-semibold;
  transition: all $duration-normal $ease-spring;

  &:hover {
    transform: translateY(-1px);
    background: $color-primary-dark;
    box-shadow: $shadow-primary;
  }
}

// ═══════════════════════════════════════
// 主内容区
// ═══════════════════════════════════════
.qb-body {
  display: flex;
  gap: 0;
  flex: 1;
  align-items: flex-start;
}

// 侧边栏：非收藏模式不占宽度，避免主区左侧大块留白
.qb-sidebar {
  width: 0;
  min-width: 0;
  flex-shrink: 0;
  margin-right: 0;
  opacity: 0;
  overflow: hidden;
  transform: translateX(-8px);
  pointer-events: none;
  transition:
    width $duration-normal $ease-default,
    min-width $duration-normal $ease-default,
    margin-right $duration-normal $ease-default,
    opacity $duration-normal $ease-default,
    transform $duration-normal $ease-default;

  &--visible {
    width: 220px;
    min-width: 220px;
    margin-right: $spacing-5;
    opacity: 1;
    overflow: visible;
    transform: translateX(0);
    pointer-events: all;
  }
}

.sidebar-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
  overflow: hidden;
  position: sticky;
  top: 16px;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 18px;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
  border-bottom: 1px solid $color-border-light;
  svg { color: $color-primary; }
}

.sidebar-list {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 480px;
  overflow-y: auto;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: $color-border; border-radius: 2px; }
}

.sidebar-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: $radius-lg;
  border: none;
  background: transparent;
  cursor: pointer;
  width: 100%;
  transition: all $duration-fast $ease-default;

  .si-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .si-icon {
    width: 28px;
    height: 28px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $color-bg-hover;
    color: $color-text-secondary;
    flex-shrink: 0;

    &--all {
      background: rgba(102, 126, 234, 0.1);
      color: $color-primary;
    }
  }

  .si-name {
    font-size: $font-size-sm;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
  }

  .si-count {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    background: $color-bg-hover;
    padding: 2px 8px;
    border-radius: $radius-full;
  }

  &:hover {
    background: $color-bg-hover;
    .si-name { color: $color-text-primary; }
  }

  &--active {
    background: rgba(102, 126, 234, 0.08);
    .si-name { color: $color-primary; font-weight: $font-weight-semibold; }
    .si-icon { color: $color-primary; }
  }
}

.sidebar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 16px;
  text-align: center;
  gap: 8px;

  p { margin: 0; font-size: $font-size-sm; color: $color-text-secondary; }
  .empty-hint { font-size: $font-size-xs; color: $color-text-placeholder; }
}

// 主区域
.qb-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

// 活跃筛选标签
.active-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;

  .af-label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  .af-tags {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .af-tag {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    padding: 4px 10px 4px 12px;
    background: rgba(102, 126, 234, 0.08);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: $radius-full;
    font-size: $font-size-xs;
    color: $color-primary;
    font-weight: $font-weight-medium;

    button {
      display: flex;
      align-items: center;
      justify-content: center;
      background: none;
      border: none;
      cursor: pointer;
      color: $color-primary;
      opacity: 0.6;
      padding: 0;
      transition: opacity $duration-fast;
      &:hover { opacity: 1; }
    }
  }

  .af-clear {
    background: none;
    border: none;
    cursor: pointer;
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    padding: 4px 8px;
    border-radius: $radius-md;
    transition: all $duration-fast;
    &:hover { color: $color-text-secondary; background: $color-bg-hover; }
  }
}

// 骨架屏
.qb-skeleton {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: 18px;
  border: 1px solid $color-border-light;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .sk-header {
    display: flex;
    justify-content: space-between;
    .sk-tag { width: 70px; height: 22px; border-radius: 6px; background: $color-bg-hover; }
    .sk-fav { width: 28px; height: 28px; border-radius: 8px; background: $color-bg-hover; }
  }

  .sk-title {
    height: 16px;
    border-radius: 6px;
    background: $color-bg-hover;
    animation: skeleton-pulse 1.5s ease-in-out infinite;
    background: linear-gradient(90deg, $color-bg-hover 25%, $color-bg-active 50%, $color-bg-hover 75%);
    background-size: 400px 100%;
    &--short { width: 70%; }
  }

  .sk-footer {
    display: flex;
    justify-content: space-between;
    padding-top: 12px;
    border-top: 1px solid $color-border-light;
    .sk-meta { width: 80px; height: 14px; border-radius: 4px; background: $color-bg-hover; }
    .sk-views { width: 40px; height: 14px; border-radius: 4px; background: $color-bg-hover; }
  }
}

@keyframes skeleton-pulse {
  0%   { background-position: -200px 0; }
  100% { background-position: 200px 0; }
}

// 题目卡片网格：宽屏固定 4 列，与筛选区左对齐
.qb-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: $spacing-4;
}

@media (max-width: 1200px) {
  .qb-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .qb-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .qb-grid {
    grid-template-columns: 1fr;
  }
}

.qb-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: 18px;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: all $duration-normal $ease-default;

  &:hover {
    border-color: rgba(102, 126, 234, 0.3);
    box-shadow: $shadow-md;
    transform: translateY(-3px);
  }

  .qc-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .qc-tags {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
  }

  .qc-tag {
    font-size: $font-size-xs;
    padding: 3px 10px;
    border-radius: $radius-full;
    font-weight: $font-weight-semibold;
    border: 1px solid;

    &--type {
      // dynamic styles via inline
    }

    &--diff {
      &.diff-easy { color: #10B981; border-color: rgba(16,185,129,0.3); background: rgba(16,185,129,0.06); }
      &.diff-medium { color: #F59E0B; border-color: rgba(245,158,11,0.3); background: rgba(245,158,11,0.06); }
      &.diff-hard { color: #EF4444; border-color: rgba(239,68,68,0.3); background: rgba(239,68,68,0.06); }
    }
  }

  .qc-fav {
    background: none;
    border: none;
    cursor: pointer;
    color: $color-text-placeholder;
    display: flex;
    align-items: center;
    padding: 4px;
    border-radius: $radius-md;
    transition: all $duration-fast;
    flex-shrink: 0;

    &:hover { color: #EC4899; transform: scale(1.15); }
    &--active { color: #EC4899; }
  }

  .qc-text {
    font-size: $font-size-base;
    line-height: 1.7;
    color: $color-text-primary;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    flex: 1;
  }

  .qc-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 10px;
    border-top: 1px solid $color-border-light;
  }

  .qc-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .qc-meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }

  .qc-source {
    padding: 1px 6px;
    border-radius: $radius-sm;
    &--ai { background: rgba(102,126,234,0.1); color: $color-primary; }
    &--manual { background: $color-bg-hover; color: $color-text-secondary; }
  }

  .qc-views {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

// 空状态
.qb-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 40px;
  text-align: center;
  gap: 12px;

  .empty-illustration {
    margin-bottom: 8px;
    opacity: 0.6;
  }

  .empty-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0;
  }

  .empty-desc {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin: 0;
    max-width: 320px;
    line-height: $line-height-loose;
  }

  .empty-actions {
    display: flex;
    gap: 10px;
    margin-top: 8px;
  }
}

// 分页
.qb-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding-top: $spacing-4;

  .page-info {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

// ═══════════════════════════════════════
// AI 生成弹窗
// ═══════════════════════════════════════
.gen-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.gen-field {
  display: flex;
  flex-direction: column;
  gap: 8px;

  label {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
  }
}

.gen-row {
  display: flex;
  gap: $spacing-5;
  .gen-field { flex: 1; }
}

.btn-gen-submit {
  display: flex;
  align-items: center;
  gap: 6px;
  background: $color-primary;
  border: none;
  &:hover { background: $color-primary-dark; box-shadow: $shadow-primary; }
}

// ═══════════════════════════════════════
// 详情弹窗
// ═══════════════════════════════════════
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-secondary;
}

.detail-text {
  font-size: $font-size-base;
  line-height: 1.8;
  color: $color-text-primary;
  background: $color-bg-page;
  border-radius: $radius-lg;
  padding: 14px 18px;
}

.answer-points {
  margin: 0;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  li { font-size: $font-size-sm; line-height: 1.7; color: $color-text-primary; }
}

.model-answer {
  font-size: $font-size-sm;
  line-height: 1.8;
  color: $color-text-primary;
  background: rgba(59, 130, 246, 0.04);
  border-left: 3px solid #3B82F6;
  border-radius: $radius-lg;
  padding: 12px 16px;
}

.detail-tags-wrap {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
