<template>
  <div class="dashboard">

    <!-- ══════════════════════════════════════
         Hero 区域
    ══════════════════════════════════════ -->
    <section class="hero-section">
      <div class="hero-bg-grid"></div>
      <div class="hero-bg-glow"></div>
      <div class="hero-inner">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          AI 驱动的新一代面试平台
        </div>
        <h1 class="hero-title">
          让每一次面试<br>
          <span class="hero-highlight">都有备而来</span>
        </h1>
        <p class="hero-desc">
          HireMate AI 集成多模型 AI 能力，为求职者提供仿真面试、简历优化、
          岗位匹配等全链路服务，大幅提升面试通过率。
        </p>
        <div class="hero-cta">
          <el-button type="primary" size="large" class="cta-btn-primary" @click="router.push('/interview')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
            免费开始面试
          </el-button>
          <el-button size="large" class="cta-btn-outline" @click="scrollToFeatures">
            了解更多
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>
          </el-button>
        </div>
        <div class="hero-meta">
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            无需信用卡
          </div>
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            3 分钟快速上手
          </div>
          <div class="meta-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            全功能免费体验
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════
         数据条
    ══════════════════════════════════════ -->
    <section class="stats-bar">
      <div class="stats-bar-inner">
        <div class="stats-bar-item" v-for="s in statsBar" :key="s.label">
          <span class="stats-number">{{ s.value }}</span>
          <span class="stats-label">{{ s.label }}</span>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════
         功能特性
    ══════════════════════════════════════ -->
    <section class="features-section" id="features">
      <div class="section-label">核心功能</div>
      <h2 class="section-title">全方位提升你的求职竞争力</h2>
      <p class="section-desc">从简历到面试，AI 全程保驾护航</p>

      <div class="features-grid">
        <div class="feature-card" v-for="f in features" :key="f.title"
          :style="{ '--accent': f.color }">
          <div class="feature-icon-wrap">
            <div class="feature-icon">
              <span v-html="f.icon"></span>
            </div>
            <div class="feature-icon-glow"></div>
          </div>
          <h3>{{ f.title }}</h3>
          <p>{{ f.desc }}</p>
          <div class="feature-tags">
            <span v-for="tag in f.tags" :key="tag" class="feature-tag">{{ tag }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════
         工作流程
    ══════════════════════════════════════ -->
    <section class="workflow-section">
      <div class="section-label">使用流程</div>
      <h2 class="section-title">三步开启你的 AI 面试之旅</h2>
      <div class="workflow-steps">
        <div class="workflow-step" v-for="(step, i) in workflowSteps" :key="step.title">
          <div class="step-number">{{ String(i + 1).padStart(2, '0') }}</div>
          <div class="step-icon-wrap">
            <div class="step-icon">
              <span v-html="step.icon"></span>
            </div>
          </div>
          <h3>{{ step.title }}</h3>
          <p>{{ step.desc }}</p>
          <div v-if="i < workflowSteps.length - 1" class="step-arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
          </div>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════
         CTA 底部横幅
    ══════════════════════════════════════ -->
    <section class="cta-section">
      <div class="cta-inner">
        <div class="cta-text">
          <h2>准备好提升你的面试表现了吗？</h2>
          <p>加入 thousands of 用户，免费使用全部 AI 面试功能</p>
        </div>
        <div class="cta-actions">
          <el-button type="primary" size="large" class="cta-btn" @click="router.push('/interview')">
            立即开始
          </el-button>
          <el-button size="large" class="cta-btn-white" @click="router.push('/question-bank')">
            浏览题库
          </el-button>
        </div>
      </div>
    </section>

    <!-- ══════════════════════════════════════
         页脚
    ══════════════════════════════════════ -->
    <footer class="dashboard-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" fill="#3B82F6"/>
              <path d="M2 17L12 22L22 17" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="#60A5FA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span>HireMate AI</span>
          </div>
          <p class="footer-tagline">AI 驱动的智能面试平台</p>
        </div>
        <div class="footer-links">
          <div class="footer-col" v-for="col in footerLinks" :key="col.title">
            <h4>{{ col.title }}</h4>
            <ul>
              <li v-for="link in col.links" :key="link.label">
                <a @click="router.push(link.path)">{{ link.label }}</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© {{ currentYear }} HireMate AI. All rights reserved.</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getSessionList } from '@/api/history'

const router = useRouter()
const userStore = useUserStore()

const currentYear = computed(() => new Date().getFullYear())

const statsBar = [
  { value: '10,000+', label: '面试记录' },
  { value: '98.5%', label: '用户满意度' },
  { value: '50+', label: '覆盖岗位类型' },
  { value: '7×24h', label: 'AI 随时在线' }
]

const features = [
  {
    title: 'AI 模拟面试',
    desc: '基于真实面试场景的 AI 模拟，支持多种面试类型，实时生成个性化问题与点评。',
    tags: ['实时反馈', '多类型覆盖', '智能追问'],
    color: '#3B82F6',
    icon: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`
  },
  {
    title: '简历智能优化',
    desc: '上传简历 PDF，AI 从 HR、技术、职业规划三维度深度分析，给出可落地的优化建议。',
    tags: ['多角色视角', 'PDF 解析', '逐条建议'],
    color: '#10B981',
    icon: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>`
  },
  {
    title: 'AI 专家群聊',
    desc: '三位 AI 专家（HR 总监 / 技术专家 / 职业规划师）同时为你多维分析任何求职问题。',
    tags: ['三专家协作', '多维建议', '即时响应'],
    color: '#F59E0B',
    icon: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>`
  },
  {
    title: '岗位智能匹配',
    desc: '将你的简历与目标岗位 JD 智能分析，输出详细的匹配度报告与改进建议。',
    tags: ['一键匹配', '详细报告', '关键词分析'],
    color: '#6366F1',
    icon: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`
  },
  {
    title: '面试题库',
    desc: '覆盖技术、行为、HR 等多类型面试题，支持分类筛选和 AI 按需生成定制题目。',
    tags: ['海量题库', '分类筛选', 'AI 生成'],
    color: '#EC4899',
    icon: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/><line x1="9" y1="7" x2="15" y2="7"/><line x1="9" y1="11" x2="15" y2="11"/></svg>`
  },
  {
    title: '面试复盘报告',
    desc: '每次面试结束后自动生成详细复盘报告，涵盖表现评分、改进建议与知识要点。',
    tags: ['自动生成', '多维评分', '改进建议'],
    color: '#8B5CF6',
    icon: `<svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polyline points="23 4 23 10 13 10 10 4"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>`
  }
]

const workflowSteps = [
  {
    title: '上传简历',
    desc: '上传你的简历 PDF，AI 自动解析并理解你的经历与技能。',
    icon: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/></svg>`
  },
  {
    title: '选择面试场景',
    desc: '选择目标岗位和面试类型，AI 为你定制专属面试方案。',
    icon: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`
  },
  {
    title: '开始面试 & 复盘',
    desc: '与 AI 面试官实时互动，结束后获取详细复盘报告并持续改进。',
    icon: `<svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`
  }
]

const footerLinks = [
  {
    title: '产品',
    links: [
      { label: 'AI 模拟面试', path: '/interview' },
      { label: '简历优化', path: '/resume' },
      { label: '岗位匹配', path: '/job-match' },
      { label: '面试题库', path: '/question-bank' }
    ]
  },
  {
    title: '社区',
    links: [
      { label: 'AI 专家群聊', path: '/group-chat' },
      { label: '面试记录', path: '/interview?tab=records' }
    ]
  },
  {
    title: '账户',
    links: [
      { label: '个人中心', path: '/profile' },
      { label: '退出登录', path: '/login' }
    ]
  }
]

const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.dashboard {
  margin: -$spacing-6;
  background: $color-bg-page;
}

// ── Hero ──────────────────────────────────────
.hero-section {
  position: relative;
  background: #0F172A;
  min-height: 580px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 80px $spacing-6 100px;
}

.hero-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(59, 130, 246, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(59, 130, 246, 0.07) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 80% 80% at 50% 0%, black 40%, transparent 100%);
}

.hero-bg-glow {
  position: absolute;
  top: -120px;
  left: 50%;
  transform: translateX(-50%);
  width: 700px;
  height: 500px;
  background: radial-gradient(ellipse, rgba(59, 130, 246, 0.18) 0%, rgba(99, 102, 241, 0.08) 40%, transparent 70%);
  pointer-events: none;
}

.hero-inner {
  position: relative;
  z-index: 1;
  max-width: 760px;
  margin: 0 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 100px;
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.3);
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: #93C5FD;
  margin-bottom: 28px;
  letter-spacing: 0.02em;

  .badge-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #3B82F6;
    box-shadow: 0 0 6px #3B82F6;
    animation: pulse-dot 2s ease-in-out infinite;
  }
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.85); }
}

.hero-title {
  font-size: clamp(36px, 6vw, 60px);
  font-weight: $font-weight-bold;
  color: #FFFFFF;
  line-height: 1.1;
  margin: 0 0 20px;
  letter-spacing: -0.02em;

  .hero-highlight {
    background: linear-gradient(135deg, #60A5FA 0%, #818CF8 50%, #A78BFA 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-desc {
  font-size: clamp(15px, 2vw, 17px);
  color: rgba(255, 255, 255, 0.55);
  line-height: 1.8;
  margin: 0 0 36px;
  max-width: 560px;
}

.hero-cta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 28px;
}

.cta-btn-primary {
  display: inline-flex !important;
  align-items: center;
  gap: 8px;
  padding: 14px 28px !important;
  border-radius: $radius-lg !important;
  font-size: $font-size-base !important;
  font-weight: $font-weight-semibold !important;
  background: $color-primary !important;
  border-color: $color-primary !important;
  color: #fff !important;
  transition: all $duration-normal $ease-spring !important;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4) !important;

  &:hover {
    background: $color-primary-dark !important;
    border-color: $color-primary-dark !important;
    transform: translateY(-2px) !important;
    box-shadow: 0 12px 32px rgba(59, 130, 246, 0.5) !important;
  }
}

.cta-btn-outline {
  display: inline-flex !important;
  align-items: center;
  gap: 6px;
  padding: 14px 24px !important;
  border-radius: $radius-lg !important;
  font-size: $font-size-base !important;
  font-weight: $font-weight-medium !important;
  background: transparent !important;
  border: 1.5px solid rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.75) !important;
  transition: all $duration-normal !important;

  &:hover {
    border-color: rgba(255, 255, 255, 0.4) !important;
    color: #fff !important;
    background: rgba(255, 255, 255, 0.06) !important;
  }
}

.hero-meta {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.4);
  }
}

// ── Stats Bar ──────────────────────────────────
.stats-bar {
  background: $color-bg-card;
  border-bottom: 1px solid $color-border-light;
}

.stats-bar-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 $spacing-6;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  divide-x: 1px solid $color-border-light;

  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stats-bar-item {
  padding: 28px 24px;
  text-align: center;
  border-right: 1px solid $color-border-light;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  &:last-child {
    border-right: none;
  }

  .stats-number {
    font-size: $font-size-3xl;
    font-weight: $font-weight-bold;
    color: $color-primary;
    line-height: 1;
    letter-spacing: -0.02em;
  }

  .stats-label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }
}

// ── 通用 Section 头部 ──────────────────────────
.section-label {
  display: inline-block;
  font-size: $font-size-xs;
  font-weight: $font-weight-bold;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: $color-primary;
  background: $color-bg-active;
  padding: 4px 12px;
  border-radius: $radius-full;
  margin-bottom: 12px;
}

.section-title {
  font-size: clamp(24px, 4vw, 36px);
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin: 0 0 12px;
  line-height: $line-height-tight;
  letter-spacing: -0.01em;
}

.section-desc {
  font-size: $font-size-lg;
  color: $color-text-secondary;
  margin: 0;
}

// ── Features ───────────────────────────────────
.features-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px $spacing-6 100px;
  text-align: center;

  .section-title {
    margin-bottom: 8px;
  }

  .section-desc {
    margin-bottom: 56px;
  }
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-5;
  text-align: left;

  @media (max-width: 900px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.feature-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $spacing-6;
  border: 1px solid $color-border-light;
  transition: all $duration-normal $ease-default;
  position: relative;
  overflow: hidden;

  &:hover {
    border-color: color-mix(in srgb, var(--accent) 50%, transparent);
    box-shadow: 0 8px 32px color-mix(in srgb, var(--accent) 12%, transparent);
    transform: translateY(-4px);

    .feature-icon-glow {
      opacity: 1;
    }
  }

  h3 {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 10px;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: $line-height-loose;
    margin: 0 0 16px;
  }
}

.feature-icon-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 16px;
}

.feature-icon {
  width: 52px;
  height: 52px;
  border-radius: $radius-lg;
  background: color-mix(in srgb, var(--accent) 10%, transparent);
  border: 1.5px solid color-mix(in srgb, var(--accent) 25%, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  position: relative;
  z-index: 1;
}

.feature-icon-glow {
  position: absolute;
  inset: -4px;
  border-radius: $radius-xl;
  background: radial-gradient(circle, color-mix(in srgb, var(--accent) 15%, transparent), transparent 70%);
  opacity: 0;
  transition: opacity $duration-normal;
  pointer-events: none;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.feature-tag {
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  padding: 3px 10px;
  border-radius: $radius-full;
  background: color-mix(in srgb, var(--accent) 8%, transparent);
  color: var(--accent);
}

// ── Workflow ───────────────────────────────────
.workflow-section {
  background: $color-bg-page;
  border-top: 1px solid $color-border-light;
  border-bottom: 1px solid $color-border-light;
  padding: 80px $spacing-6;
  text-align: center;
}

.workflow-steps {
  max-width: 900px;
  margin: 48px auto 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 0;
  position: relative;
}

.workflow-step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  padding: 0 24px;

  .step-number {
    font-size: 11px;
    font-weight: $font-weight-bold;
    letter-spacing: 0.08em;
    color: $color-text-placeholder;
    margin-bottom: 16px;
  }

  .step-icon-wrap {
    margin-bottom: 16px;
  }

  .step-icon {
    width: 64px;
    height: 64px;
    border-radius: $radius-xl;
    background: $color-bg-card;
    border: 1.5px solid $color-border;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $color-primary;
    box-shadow: $shadow-sm;
  }

  h3 {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin: 0 0 8px;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: $line-height-base;
    margin: 0;
  }
}

.step-arrow {
  position: absolute;
  right: -10px;
  top: 64px;
  z-index: 2;
  background: $color-bg-page;
  padding: 0 8px;
}

// ── CTA Banner ─────────────────────────────────
.cta-section {
  background: linear-gradient(135deg, #1E3A5F 0%, #1E40AF 100%);
  padding: 80px $spacing-6;
}

.cta-inner {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;

  @media (max-width: 700px) {
    flex-direction: column;
    text-align: center;
  }
}

.cta-text {
  h2 {
    font-size: clamp(22px, 3.5vw, 32px);
    font-weight: $font-weight-bold;
    color: #FFFFFF;
    margin: 0 0 10px;
    line-height: $line-height-tight;
  }

  p {
    font-size: $font-size-base;
    color: rgba(255, 255, 255, 0.6);
    margin: 0;
  }
}

.cta-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.cta-btn {
  display: inline-flex !important;
  align-items: center;
  padding: 13px 28px !important;
  border-radius: $radius-lg !important;
  font-size: $font-size-base !important;
  font-weight: $font-weight-semibold !important;
  background: #fff !important;
  border: none !important;
  color: $color-primary !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15) !important;
  transition: all $duration-normal !important;

  &:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2) !important;
  }
}

.cta-btn-white {
  display: inline-flex !important;
  align-items: center;
  padding: 13px 24px !important;
  border-radius: $radius-lg !important;
  font-size: $font-size-base !important;
  font-weight: $font-weight-medium !important;
  background: transparent !important;
  border: 1.5px solid rgba(255, 255, 255, 0.4) !important;
  color: rgba(255, 255, 255, 0.85) !important;
  transition: all $duration-normal !important;

  &:hover {
    border-color: rgba(255, 255, 255, 0.7) !important;
    color: #fff !important;
  }
}

// ── Footer ─────────────────────────────────────
.dashboard-footer {
  background: #0F172A;
  padding: 60px $spacing-6 0;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 80px;
  padding-bottom: 48px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 40px;
  }
}

.footer-brand {
  flex-shrink: 0;
  max-width: 220px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;

  span {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: #FFFFFF;
  }
}

.footer-tagline {
  font-size: $font-size-sm;
  color: rgba(255, 255, 255, 0.35);
  margin: 0;
  line-height: $line-height-base;
}

.footer-links {
  flex: 1;
  display: flex;
  gap: 60px;
  flex-wrap: wrap;
}

.footer-col {
  h4 {
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    color: rgba(255, 255, 255, 0.4);
    letter-spacing: 0.08em;
    text-transform: uppercase;
    margin: 0 0 16px;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  li a {
    font-size: $font-size-sm;
    color: rgba(255, 255, 255, 0.5);
    cursor: pointer;
    text-decoration: none;
    transition: color $duration-fast;

    &:hover {
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

.footer-bottom {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;

  span {
    font-size: $font-size-xs;
    color: rgba(255, 255, 255, 0.25);
  }
}
</style>
