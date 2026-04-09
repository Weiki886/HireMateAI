<template>
  <div class="resume-page">
    <!-- ══════════════════════════════════════
         页面标题区
    ══════════════════════════════════════ -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">简历管理</h1>
        <p class="page-desc">管理你的简历，AI 智能优化提升竞争力</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" class="btn-ai-primary" @click="handleOptimize">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
          AI 优化
        </el-button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         Tab 导航
    ══════════════════════════════════════ -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span v-html="tab.icon"></span>
        {{ tab.label }}
        <span v-if="tab.count" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- ══════════════════════════════════════
         简历列表
    ══════════════════════════════════════ -->
    <div v-if="activeTab === 'list'" class="resume-list">
      <div v-if="resumes.length > 0" class="resume-grid">
        <div
          v-for="resume in resumes"
          :key="resume.id"
          class="resume-card hover-lift"
          @click="handleEdit(resume)"
        >
          <div class="resume-card__header">
            <div class="resume-card__icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
            </div>
            <span class="badge" :class="getStatusBadge(resume.status)">{{ getStatusLabel(resume.status) }}</span>
          </div>
          <h3 class="resume-card__title">{{ resume.title || '未命名简历' }}</h3>
          <div class="resume-card__meta">
            <span class="meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
              {{ resume.targetPosition || '未指定岗位' }}
            </span>
            <span class="meta-item">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
              {{ resume.targetIndustry || '未指定行业' }}
            </span>
          </div>
          <div class="resume-card__footer">
            <span class="version-tag">v{{ resume.version }}</span>
            <span class="update-time">{{ formatDate(resume.updatedAt) }}</span>
          </div>
          <div class="resume-card__actions" @click.stop>
            <el-button size="small" @click.stop="handleView(resume)">查看</el-button>
            <el-button size="small" type="primary" @click.stop="handleEdit(resume)">编辑</el-button>
            <el-button size="small" type="danger" @click.stop="handleDelete(resume)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
        </div>
        <h3>还没有简历</h3>
        <p>使用「AI 优化」上传简历获得 AI 建议</p>
        <div class="empty-actions">
          <el-button type="primary" @click="handleOptimize">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
            AI 优化
          </el-button>
        </div>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         分析记录列表
    ══════════════════════════════════════ -->
    <div v-if="activeTab === 'analysis'" class="analysis-list">
      <div v-if="analysisRecords.length > 0" class="record-grid">
        <div
          v-for="record in analysisRecords"
          :key="record.id"
          class="record-card hover-lift"
        >
          <div class="record-card__top">
            <div class="record-score">
              <ScoreRing :score="record.overallScore || 0" :size="80" :stroke-width="8" />
            </div>
            <div class="record-info">
              <h4>{{ record.targetPosition || '未指定岗位' }}</h4>
              <span class="badge" :class="getAnalysisBadge(record.status)">{{ getAnalysisLabel(record.status) }}</span>
            </div>
          </div>
          <div class="record-card__meta">
            <span>{{ formatDateTime(record.createdAt) }}</span>
            <span>{{ record.discussionRounds || 0 }} 轮讨论</span>
          </div>
          <div class="record-card__actions">
            <el-button size="small" type="primary" @click="viewAnalysisRecord(record)">查看详情</el-button>
            <el-button size="small" type="danger" @click="deleteAnalysisRecord(record)">删除</el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.2">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
          </svg>
        </div>
        <h3>暂无分析记录</h3>
        <p>使用 AI 优化功能后，分析记录将显示在这里</p>
        <el-button type="primary" @click="handleOptimize">AI 优化</el-button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         简历表单对话框
    ══════════════════════════════════════ -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑简历' : '新建简历'" width="640px"
      :close-on-click-modal="false" class="hm-dialog">
      <el-form ref="formRef" :model="form" label-width="90px" class="hm-form">
        <el-form-item label="简历标题" required>
          <el-input v-model="form.title" placeholder="如：Java开发工程师简历" />
        </el-form-item>
        <el-form-item label="目标岗位">
          <el-input v-model="form.targetPosition" placeholder="如：高级Java开发工程师" />
        </el-form-item>
        <el-form-item label="目标行业">
          <el-input v-model="form.targetIndustry" placeholder="如：互联网/金融科技" />
        </el-form-item>
        <el-form-item label="简历内容">
          <el-input v-model="form.contentJson" type="textarea" :rows="8" placeholder="请粘贴简历内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ══════════════════════════════════════
         简历详情对话框
    ══════════════════════════════════════ -->
    <el-dialog v-model="detailVisible" title="简历详情" width="720px" class="hm-dialog">
      <div v-if="currentResume" class="resume-detail">
        <div class="detail-header">
          <div class="detail-title">
            <h2>{{ currentResume.title }}</h2>
            <span class="badge" :class="getStatusBadge(currentResume.status)">{{ getStatusLabel(currentResume.status) }}</span>
          </div>
          <div class="detail-meta">
            <div class="meta-row">
              <span class="meta-label">目标岗位</span>
              <span class="meta-value">{{ currentResume.targetPosition || '-' }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">目标行业</span>
              <span class="meta-value">{{ currentResume.targetIndustry || '-' }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">版本</span>
              <span class="meta-value">v{{ currentResume.version }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">更新时间</span>
              <span class="meta-value">{{ formatDateTime(currentResume.updatedAt) }}</span>
            </div>
          </div>
        </div>
        <div v-if="currentResume.contentJson" class="detail-content">
          <h4>简历内容</h4>
          <div class="content-text">{{ currentResume.contentJson }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- ══════════════════════════════════════
         AI 优化对话框
    ══════════════════════════════════════ -->
    <el-dialog v-model="optimizeVisible" title="AI 简历优化" width="900px"
      :close-on-click-modal="false" class="hm-dialog hm-dialog--lg" destroy-on-close>
      <div class="optimize-container">
        <!-- 步骤条 -->
        <div class="step-bar">
          <div v-for="(s, idx) in steps" :key="idx" class="step-item" :class="getStepClass(idx)">
            <div class="step-dot">
              <svg v-if="currentStep > idx" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
              <span v-else>{{ idx + 1 }}</span>
            </div>
            <span class="step-label">{{ s }}</span>
          </div>
        </div>

        <!-- Step 1: 选择简历来源 -->
        <div v-if="currentStep === 0" class="step-content">
          <div class="source-grid">
            <div class="source-card" :class="{ active: resumeSource === 'file' }" @click="resumeSource = 'file'">
              <div class="source-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.8"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </div>
              <h3>上传 PDF</h3>
              <p>从本地选择简历文件</p>
            </div>
            <div class="source-card" :class="{ active: resumeSource === 'text' }" @click="resumeSource = 'text'">
              <div class="source-icon">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="1.8"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </div>
              <h3>粘贴文本</h3>
              <p>直接粘贴简历内容</p>
            </div>
          </div>

          <div class="optimize-form">
            <el-form label-width="100px">
              <el-form-item v-if="resumeSource === 'file'" label="上传简历">
                <el-upload ref="uploadRef" :auto-upload="false" :limit="1" accept=".pdf"
                  :on-change="handleFileChange">
                  <template #trigger>
                    <el-button>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 16 12 12 8 16"/><line x1="12" y1="12" x2="12" y2="21"/><path d="M20.39 18.39A5 5 0 0 0 18 9h-1.26A8 8 0 1 0 3 16.3"/></svg>
                      选择 PDF 文件
                    </el-button>
                  </template>
                </el-upload>
                <div class="form-hint">支持 PDF 格式，大小不超过 10MB</div>
              </el-form-item>
              <el-form-item v-else label="简历文本">
                <el-input v-model="optimizeForm.resumeText" type="textarea" :rows="10" placeholder="请粘贴您的简历内容..." />
              </el-form-item>
              <el-form-item label="目标岗位" required>
                <el-input v-model="optimizeForm.targetPosition" placeholder="如：Java 高级开发工程师" />
              </el-form-item>
              <el-form-item label="讨论轮数">
                <el-slider v-model="optimizeForm.discussionRounds" :min="1" :max="8" :step="1"
                  show-stops :marks="{1:'1轮',3:'3轮',5:'5轮',8:'8轮'}" style="width:300px" />
                <div class="form-hint">轮数越多，分析越深入（建议 3-5 轮）</div>
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- Step 2: 分析中 -->
        <div v-if="currentStep === 1" class="step-content loading-step">
          <div class="loading-animation">
            <div class="loading-orbs">
              <div class="orb orb-1"></div>
              <div class="orb orb-2"></div>
              <div class="orb orb-3"></div>
            </div>
            <p>AI 正在多角度分析你的简历</p>
            <div class="loading-steps">
              <div class="ls-item" :class="{ done: loadStage >= 1 }">提取简历文本</div>
              <div class="ls-item" :class="{ done: loadStage >= 2 }">多角色讨论分析</div>
              <div class="ls-item" :class="{ done: loadStage >= 3 }">生成优化建议</div>
            </div>
          </div>
        </div>

        <!-- Step 3: 查看结果 -->
        <div v-if="currentStep === 2 && optimizeResult" class="step-content result-step">
          <!-- 综合评分 -->
          <div class="result-score-section">
            <ScoreRing :score="optimizeResult.finalResult?.overallScore || 0" :size="140" :stroke-width="12" />
            <div class="result-score-info">
              <h3>简历综合评分</h3>
              <p>{{ optimizeResult.finalResult?.overallComment || '暂无评语' }}</p>
              <div v-if="optimizeResult.warnings?.length" class="warnings">
                <div v-for="(warn, i) in optimizeResult.warnings" :key="i" class="warning-item">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                  {{ warn }}
                </div>
              </div>
            </div>
          </div>

          <!-- 模块建议 Tabs -->
          <el-tabs v-model="activeTab2" class="result-tabs">
            <el-tab-pane label="个人信息" name="personalInfo">
              <div class="suggestion-list">
                <div v-for="(item, idx) in optimizeResult.finalResult?.moduleSuggestions?.personalInfo" :key="idx" class="suggestion-card">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{{ item }}</span>
                </div>
                <el-empty v-if="!optimizeResult.finalResult?.moduleSuggestions?.personalInfo?.length" description="暂无建议" :image-size="60" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="工作经历" name="workExperience">
              <div class="suggestion-list">
                <div v-for="(item, idx) in optimizeResult.finalResult?.moduleSuggestions?.workExperience" :key="idx" class="suggestion-card">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{{ item }}</span>
                </div>
                <el-empty v-if="!optimizeResult.finalResult?.moduleSuggestions?.workExperience?.length" description="暂无建议" :image-size="60" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="项目经验" name="projectExperience">
              <div class="suggestion-list">
                <div v-for="(item, idx) in optimizeResult.finalResult?.moduleSuggestions?.projectExperience" :key="idx" class="suggestion-card">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{{ item }}</span>
                </div>
                <el-empty v-if="!optimizeResult.finalResult?.moduleSuggestions?.projectExperience?.length" description="暂无建议" :image-size="60" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="技能描述" name="skills">
              <div class="suggestion-list">
                <div v-for="(item, idx) in optimizeResult.finalResult?.moduleSuggestions?.skills" :key="idx" class="suggestion-card">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{{ item }}</span>
                </div>
                <el-empty v-if="!optimizeResult.finalResult?.moduleSuggestions?.skills?.length" description="暂无建议" :image-size="60" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="自我评价" name="selfEvaluation">
              <div class="suggestion-list">
                <div v-for="(item, idx) in optimizeResult.finalResult?.moduleSuggestions?.selfEvaluation" :key="idx" class="suggestion-card">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{{ item }}</span>
                </div>
                <el-empty v-if="!optimizeResult.finalResult?.moduleSuggestions?.selfEvaluation?.length" description="暂无建议" :image-size="60" />
              </div>
            </el-tab-pane>
            <el-tab-pane label="格式与排版" name="formatting">
              <div class="suggestion-list">
                <div v-for="(item, idx) in optimizeResult.finalResult?.moduleSuggestions?.formatting" :key="idx" class="suggestion-card">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  <span>{{ item }}</span>
                </div>
                <el-empty v-if="!optimizeResult.finalResult?.moduleSuggestions?.formatting?.length" description="暂无建议" :image-size="60" />
              </div>
            </el-tab-pane>
          </el-tabs>

          <!-- 下一步 -->
          <div v-if="optimizeResult.finalResult?.nextSteps?.length" class="next-steps">
            <h4>下一步行动</h4>
            <div class="next-steps-list">
              <div v-for="(step, idx) in optimizeResult.finalResult.nextSteps" :key="idx" class="next-step-item">
                <div class="step-number">{{ idx + 1 }}</div>
                <span>{{ step }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div v-if="currentStep === 0">
          <el-button @click="optimizeVisible = false">取消</el-button>
          <el-button type="primary" @click="startOptimize" :loading="optimizing">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
            开始优化
          </el-button>
        </div>
        <div v-if="currentStep === 1">
          <el-button disabled>分析中，请稍候...</el-button>
        </div>
        <div v-if="currentStep === 2">
          <el-button @click="resetOptimize">重新分析</el-button>
          <el-button type="primary" @click="optimizeVisible = false">完成</el-button>
        </div>
        <div v-if="optimizeSubmitted" class="async-tip">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
          任务已提交，关闭弹窗后可在「分析记录」查看结果，完成后会自动通知您
        </div>
      </template>
    </el-dialog>

    <!-- ══════════════════════════════════════
         分析记录详情对话框
    ══════════════════════════════════════ -->
    <el-dialog v-model="recordDetailVisible" title="分析记录详情" width="860px" destroy-on-close class="hm-dialog record-detail-dialog">
      <div v-if="currentRecord" v-loading="loadingRecordDetail" class="record-detail">

        <!-- 顶部综合概览 -->
        <div class="detail-hero">
          <div class="detail-hero__score">
            <ScoreRing :score="currentRecord.overallScore || 0" :size="120" :stroke-width="10" />
            <div class="score-label">综合评分</div>
          </div>
          <div class="detail-hero__info">
            <div class="hero-meta">
              <div class="meta-badge" :style="{ background: getScoreBg(currentRecord.overallScore) }">
                {{ getScoreLabel(currentRecord.overallScore) }}
              </div>
              <el-tag size="small" :type="getStatusTagType(currentRecord.status)">
                {{ getAnalysisLabel(currentRecord.status) }}
              </el-tag>
            </div>
            <h2 class="hero-title">{{ currentRecord.targetPosition || '未指定岗位' }}</h2>
            <div class="hero-sub">
              <span>{{ formatDateTime(currentRecord.createdAt) }}</span>
              <span class="dot">·</span>
              <span>{{ currentRecord.discussionRounds }} 轮专家讨论</span>
            </div>

            <!-- 简历摘要 -->
            <div v-if="currentRecord.originalText" class="hero-summary">
              <div class="summary-label">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                简历摘要
              </div>
              <p class="summary-text">{{ currentRecord.originalText }}</p>
            </div>
          </div>
        </div>

        <!-- 专家讨论内容 -->
        <div v-if="currentRecord.discussionRoundsParsed?.length" class="discussion-section">
          <div class="section-header">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              专家多轮讨论
            </h3>
            <span class="section-sub">点击展开查看各方观点</span>
          </div>

          <div class="discussion-rounds">
            <el-collapse>
              <el-collapse-item
                v-for="(round, idx) in currentRecord.discussionRoundsParsed"
                :key="idx"
                :name="idx"
              >
                <template #title>
                  <div class="round-header">
                    <div class="round-meta">
                      <span class="round-num">第 {{ round.roundNumber }} 轮</span>
                      <span class="round-role-badge" :class="getRoleClass(round.aiRole)">
                        {{ round.aiRole }}
                      </span>
                    </div>
                    <div class="round-points">
                      <span v-for="(pt, pi) in (round.keyPoints || []).slice(0, 2)" :key="pi" class="point-chip">
                        {{ pt }}
                      </span>
                      <span v-if="(round.keyPoints || []).length > 2" class="point-more">
                        +{{ round.keyPoints.length - 2 }}
                      </span>
                    </div>
                  </div>
                </template>

                <div class="round-content">
                  <div class="round-role-card" :class="getRoleClass(round.aiRole)">
                    <div class="role-card-header">
                      <span class="role-avatar">{{ getRoleInitial(round.aiRole) }}</span>
                      <span class="role-name">{{ round.aiRole }}</span>
                      <span class="role-round">第 {{ round.roundNumber }} 轮发言</span>
                    </div>
                    <div class="role-card-body">
                      <p>{{ round.content }}</p>
                    </div>
                    <div v-if="round.keyPoints?.length" class="role-card-points">
                      <div class="points-label">核心观点</div>
                      <ul class="points-list">
                        <li v-for="(pt, pi) in round.keyPoints" :key="pi">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                          {{ pt }}
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </div>

        <!-- 模块优化建议 -->
        <div v-if="currentRecord.moduleSuggestions" class="suggestions-section">
          <div class="section-header">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>
              模块优化建议
            </h3>
          </div>

          <div class="suggestions-grid">
            <div
              v-for="(items, module) in currentRecord.moduleSuggestions"
              :key="module"
              v-if="items?.length"
              class="suggestion-module"
            >
              <div class="module-header">
                <span class="module-icon" v-html="getModuleIcon(module)"></span>
                <span class="module-name">{{ getModuleLabel(module) }}</span>
                <span class="module-count">{{ items.length }} 条建议</span>
              </div>
              <div class="module-items">
                <div v-for="(item, idx) in items" :key="idx" class="suggestion-item">
                  <div class="item-num">{{ idx + 1 }}</div>
                  <div class="item-text">{{ item }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 综合评语 -->
        <div v-if="currentRecord.overallComment" class="comment-section">
          <div class="section-header">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              综合评语
            </h3>
          </div>
          <div class="comment-body">
            <p>{{ currentRecord.overallComment }}</p>
          </div>
        </div>

        <!-- 下一步行动 -->
        <div v-if="currentRecord.nextSteps?.length" class="nextsteps-section">
          <div class="section-header">
            <h3 class="section-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 8 12 12 14 14"/></svg>
              下一步行动
            </h3>
          </div>
          <div class="nextsteps-list">
            <div v-for="(step, idx) in currentRecord.nextSteps" :key="idx" class="nextstep-item">
              <div class="nextstep-num">{{ idx + 1 }}</div>
              <div class="nextstep-text">{{ step }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import {
  getResumeList, createResume, updateResume,
  deleteResume, getResume, optimizeResume,
  optimizeResumeAsync,
  getAnalysisRecords, getAnalysisRecord,
  deleteAnalysisRecord as delAnalysisRecord
} from '@/api/resume'
import { useNotification } from '@/composables/useNotification'
import ScoreRing from '@/components/ui/ScoreRing.vue'

const activeTab = ref('list')
const resumes = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const optimizeVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const optimizing = ref(false)
const loading = ref(false)
const uploadRef = ref()
const formRef = ref()

// 分析记录
const analysisRecords = ref([])
const loadingRecords = ref(false)
const recordDetailVisible = ref(false)
const loadingRecordDetail = ref(false)
const currentRecord = ref(null)
const currentResume = ref(null)
const currentStep = ref(0)
const activeTab2 = ref('personalInfo')
const loadStage = ref(0)
let loadTimer = null

const resumeSource = ref('file')
const optimizeForm = ref({
  resumeText: '',
  targetPosition: '',
  jdText: '',
  discussionRounds: 3,
  pdfFile: null
})
const optimizeResult = ref(null)
const optimizeSubmitted = ref(false)
const submittedTaskInfo = ref(null)
const submittedRecordId = ref(null)

const steps = ['上传简历', 'AI 分析', '查看结果']
const tabs = computed(() => [
  { key: 'list', label: '简历列表', count: resumes.value.length },
  { key: 'analysis', label: '分析记录', count: analysisRecords.value.length }
])

const form = ref({
  title: '',
  targetPosition: '',
  targetIndustry: '',
  contentJson: ''
})

const getStepClass = (idx) => {
  if (currentStep.value > idx) return 'done'
  if (currentStep.value === idx) return 'active'
  return ''
}

const loadResumes = async () => {
  try {
    const res = await getResumeList()
    resumes.value = res.data || []
  } catch (error) {
    console.error('加载简历列表失败:', error)
  }
}

const handleEdit = async (resume) => {
  isEdit.value = true
  try {
    const res = await getResume(resume.id)
    currentResume.value = res.data
    form.value = { ...currentResume.value }
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取简历详情失败')
  }
}

const handleView = async (resume) => {
  try {
    const res = await getResume(resume.id)
    currentResume.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('获取简历详情失败')
  }
}

const handleSubmit = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入简历标题')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateResume(currentResume.value.id, { ...form.value })
      ElMessage.success('简历更新成功')
    } else {
      await createResume({ ...form.value })
      ElMessage.success('简历创建成功')
    }
    dialogVisible.value = false
    loadResumes()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (resume) => {
  try {
    await ElMessageBox.confirm(`确定要删除简历"${resume.title}"吗？`, '删除确认', { type: 'warning' })
    await deleteResume(resume.id)
    ElMessage.success('删除成功')
    loadResumes()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleOptimize = () => {
  resetOptimize()
  optimizeVisible.value = true
}

const handleFileChange = (file) => {
  optimizeForm.value.pdfFile = file.raw
}

const startOptimize = async () => {
  if (!optimizeForm.value.targetPosition) {
    ElMessage.warning('请填写目标岗位')
    return
  }
  if (resumeSource.value === 'file' && !optimizeForm.value.pdfFile) {
    ElMessage.warning('请上传 PDF 文件')
    return
  }
  if (resumeSource.value === 'text' && !optimizeForm.value.resumeText) {
    ElMessage.warning('请填写简历文本')
    return
  }

  optimizing.value = true
  currentStep.value = 1

  try {
    const res = await optimizeResumeAsync({
      pdfFile: resumeSource.value === 'file' ? optimizeForm.value.pdfFile : null,
      resumeText: resumeSource.value === 'text' ? optimizeForm.value.resumeText : null,
      targetPosition: optimizeForm.value.targetPosition,
      jdText: optimizeForm.value.jdText || null,
      discussionRounds: optimizeForm.value.discussionRounds
    })

    submittedTaskInfo.value = res.data
    submittedRecordId.value = res.data?.recordId
    optimizeSubmitted.value = true
    currentStep.value = 0
    optimizeVisible.value = false

    ElNotification({
      title: '优化任务已提交',
      message: `正在后台优化「${optimizeForm.value.targetPosition}」岗位的简历，完成后会自动通知您~`,
      type: 'success',
      duration: 5000
    })

    loadAnalysisRecords()
  } catch (error) {
    ElMessage.error('提交优化任务失败，请重试')
    currentStep.value = 0
  } finally {
    optimizing.value = false
  }
}

const resetOptimize = () => {
  currentStep.value = 0
  resumeSource.value = 'file'
  optimizeForm.value = { resumeText: '', targetPosition: '', jdText: '', discussionRounds: 3, pdfFile: null }
  optimizeResult.value = null
  activeTab2.value = 'personalInfo'
  loadStage.value = 0
}

const getStatusBadge = (status) => {
  return { 'DRAFT': 'badge--default', 'COMPLETED': 'badge--success', 'OPTIMIZED': 'badge--info' }[status] || 'badge--default'
}

const getStatusLabel = (status) => {
  return { 'DRAFT': '草稿', 'COMPLETED': '已完成', 'OPTIMIZED': '已优化' }[status] || status
}

const getAnalysisBadge = (status) => {
  return { 'ANALYZING': 'badge--warning', 'COMPLETED': 'badge--success', 'FAILED': 'badge--danger' }[status] || 'badge--default'
}

const getAnalysisLabel = (status) => {
  return { 'ANALYZING': '分析中', 'COMPLETED': '已完成', 'FAILED': '失败' }[status] || status
}

const getModuleLabel = (key) => ({
  personalInfo: '个人信息', workExperience: '工作经历', projectExperience: '项目经验',
  skills: '技能描述', selfEvaluation: '自我评价', formatting: '格式与排版'
}[key] || key)

const getStatusTagType = (status) => ({
  'ANALYZING': 'warning', 'COMPLETED': 'success', 'FAILED': 'danger'
}[status] || 'info')

const getScoreLabel = (score) => {
  if (!score) return '暂无评分'
  if (score >= 85) return '优秀'
  if (score >= 70) return '良好'
  if (score >= 55) return '一般'
  if (score >= 40) return '较差'
  return '待提升'
}

const getScoreBg = (score) => {
  if (!score) return 'rgba(156,163,175,0.1)'
  if (score >= 85) return 'rgba(16,185,129,0.1)'
  if (score >= 70) return 'rgba(59,130,246,0.1)'
  if (score >= 55) return 'rgba(245,158,11,0.1)'
  return 'rgba(239,68,68,0.1)'
}

const getRoleClass = (role) => ({
  'HR总监': 'role-hr',
  '技术专家': 'role-tech',
  '职业规划师': 'role-career'
}[role] || 'role-default')

const getRoleInitial = (role) => ({
  'HR总监': 'HR',
  '技术专家': 'TE',
  '职业规划师': 'CP'
}[role] || '??')

const getModuleIcon = (module) => ({
  personalInfo: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  workExperience: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>`,
  projectExperience: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 2 7 12 12 22 7 12 2"/><polyline points="2 17 12 22 22 17"/><polyline points="2 12 12 17 22 12"/></svg>`,
  skills: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>`,
  selfEvaluation: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>`,
  formatting: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="21" y1="10" x2="3" y2="10"/><line x1="21" y1="6" x2="3" y2="6"/><line x1="21" y1="14" x2="3" y2="14"/><line x1="21" y1="18" x2="3" y2="18"/></svg>`
}[module] || '')

const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : '-'
const formatDateTime = (d) => d ? new Date(d).toLocaleString('zh-CN') : '-'

const loadAnalysisRecords = async () => {
  loadingRecords.value = true
  try {
    const res = await getAnalysisRecords()
    analysisRecords.value = res.data || []
  } catch (error) {
    console.error('加载分析记录失败:', error)
  } finally {
    loadingRecords.value = false
  }
}

const viewAnalysisRecord = async (record) => {
  loadingRecordDetail.value = true
  recordDetailVisible.value = true
  currentRecord.value = null
  try {
    const res = await getAnalysisRecord(record.id)
    currentRecord.value = res.data
  } catch (error) {
    ElMessage.error('加载详情失败')
    recordDetailVisible.value = false
  } finally {
    loadingRecordDetail.value = false
  }
}

const deleteAnalysisRecord = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除该分析记录吗？', '删除确认', { type: 'warning' })
    await delAnalysisRecord(record.id)
    ElMessage.success('删除成功')
    loadAnalysisRecords()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadResumes()
  loadAnalysisRecords()
  setupAsyncOptimizationNotification()
})

onUnmounted(() => {
  if (pendingNotificationOff) pendingNotificationOff()
})

let pendingNotificationOff = null
function setupAsyncOptimizationNotification() {
  const { connect, on, off } = useNotification()

  const handler = (data) => {
    if (data.type !== 'RESUME_OPTIMIZED') return
    if (data.recordId !== submittedRecordId.value) return

    const { status, result } = data
    if (status === 'COMPLETED' && result) {
      optimizeResult.value = result
      currentStep.value = 2
      optimizeSubmitted.value = false
      submittedRecordId.value = null
      off(handler)
      pendingNotificationOff = null
    }
  }

  on(handler)
  pendingNotificationOff = off
  connect()
}
</script>

<style scoped lang="scss">
// variables 已通过 vite.config.js 全局注入，无需重复导入

.resume-page {
  max-width: 1200px;
  margin: 0 auto;
}

// ── 页面标题区 ───────────────────────────────
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.btn-ai-primary {
  display: inline-flex !important;
  align-items: center;
  gap: 8px;
}

// ── Tab 导航 ───────────────────────────────
.tab-nav {
  display: flex;
  gap: 4px;
  background: $color-bg-card;
  padding: 6px;
  border-radius: $radius-xl;
  margin-bottom: $spacing-6;
  border: 1px solid $color-border-light;
  box-shadow: $shadow-xs;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border: none;
  background: transparent;
  border-radius: $radius-lg;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text-secondary;
  cursor: pointer;
  transition: all $duration-normal;

  &:hover {
    background: $color-bg-hover;
    color: $color-text-primary;
  }

  &--active {
    background: $color-primary;
    color: #fff;
    font-weight: $font-weight-semibold;
  }

  .tab-count {
    background: rgba(255, 255, 255, 0.25);
    color: inherit;
    padding: 1px 7px;
    border-radius: $radius-full;
    font-size: $font-size-xs;
  }
}

// ── 简历卡片网格 ────────────────────────────
.resume-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-5;

  @media (max-width: 900px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.resume-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-5;
  border: 1px solid $color-border-light;
  cursor: pointer;
  transition: all $duration-normal $ease-default;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  &__icon {
    width: 44px;
    height: 44px;
    border-radius: $radius-xl;
    background: rgba(102, 126, 234, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  &__meta {
    display: flex;
    flex-direction: column;
    gap: 4px;
    margin-bottom: 12px;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid $color-border-light;
    margin-bottom: 12px;
  }

  &__actions {
    display: flex;
    gap: 6px;
    opacity: 0;
    transition: opacity $duration-fast;
  }

  &:hover &__actions { opacity: 1; }
}

.resume-card__meta .meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.version-tag {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $color-primary;
  background: rgba(102, 126, 234, 0.08);
  padding: 2px 8px;
  border-radius: $radius-full;
}

.update-time {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
}

// ── 分析记录 ───────────────────────────────
.record-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $spacing-5;

  @media (max-width: 900px) { grid-template-columns: repeat(2, 1fr); }
  @media (max-width: 600px) { grid-template-columns: 1fr; }
}

.record-card {
  background: $color-bg-card;
  border-radius: $radius-2xl;
  padding: $spacing-5;
  border: 1px solid $color-border-light;
  transition: all $duration-normal;

  &__top {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;
  }

  &__meta {
    display: flex;
    justify-content: space-between;
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    margin-bottom: 12px;
  }

  &__actions {
    display: flex;
    gap: 6px;
    padding-top: 12px;
    border-top: 1px solid $color-border-light;
  }
}

.record-info {
  h4 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 6px;
  }
}

// ── 空状态 ───────────────────────────────
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
    background: rgba(102, 126, 234, 0.06);
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
    max-width: 360px;
  }

  .empty-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    justify-content: center;
    align-items: center;
  }
}

// ── 简历详情 ───────────────────────────────
.resume-detail {
  .detail-header {
    margin-bottom: 24px;
    .detail-title {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      h2 { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $color-text-primary; margin: 0; }
    }
    .detail-meta {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      background: $color-bg-page;
      border-radius: $radius-lg;
      padding: 16px;
      .meta-row {
        display: flex;
        gap: 8px;
        font-size: $font-size-sm;
        .meta-label { color: $color-text-placeholder; }
        .meta-value { color: $color-text-primary; font-weight: $font-weight-medium; }
      }
    }
  }
  .detail-content {
    h4 { font-size: $font-size-base; font-weight: $font-weight-semibold; color: $color-text-primary; margin-bottom: 12px; }
    .content-text {
      background: $color-bg-page;
      border-radius: $radius-lg;
      padding: 16px;
      font-size: $font-size-sm;
      color: $color-text-regular;
      white-space: pre-wrap;
      line-height: $line-height-loose;
      max-height: 300px;
      overflow-y: auto;
    }
  }
}

// ── AI 优化对话框 ───────────────────────────
.optimize-container {
  min-height: 480px;
}

// 步骤条
.step-bar {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
  gap: 0;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  position: relative;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    left: calc(50% + 20px);
    right: calc(-50% + 20px);
    top: 16px;
    height: 2px;
    background: $color-border;
  }

  &.done::after { background: $color-primary; }

  .step-dot {
    width: 32px;
    height: 32px;
    border-radius: $radius-full;
    background: $color-bg-hover;
    border: 2px solid $color-border;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    flex-shrink: 0;
    position: relative;
    z-index: 1;
    transition: all $duration-normal;
  }

  .step-label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    font-weight: $font-weight-medium;
  }

  &.active {
    .step-dot {
      background: $color-primary;
      border-color: $color-primary;
      color: #fff;
    }
    .step-label { color: $color-primary; font-weight: $font-weight-semibold; }
  }

  &.done {
    .step-dot {
      background: $color-primary;
      border-color: $color-primary;
      color: #fff;
    }
    .step-label { color: $color-primary; }
  }
}

// Step 内容
.step-content {
  padding: 8px 0;
  animation: fade-in 0.3s ease;
}

// Step 1: 来源选择
.source-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-4;
  margin-bottom: 24px;
}

.source-card {
  padding: 28px;
  border-radius: $radius-2xl;
  border: 2px solid $color-border;
  text-align: center;
  cursor: pointer;
  transition: all $duration-normal;
  background: $color-bg-card;

  &.active {
    border-color: $color-primary;
    background: rgba(102, 126, 234, 0.04);
    box-shadow: $shadow-primary;
  }

  &:not(.active):hover {
    border-color: $color-primary;
    background: $color-bg-hover;
  }

  .source-icon {
    width: 60px;
    height: 60px;
    border-radius: $radius-xl;
    background: rgba(102, 126, 234, 0.08);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
  }

  h3 {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0 0 6px;
  }

  p {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin: 0;
  }
}

.optimize-form {
  background: $color-bg-page;
  border-radius: $radius-xl;
  padding: 20px;
}

.form-hint {
  font-size: $font-size-xs;
  color: $color-text-placeholder;
  margin-top: 6px;
}

// Step 2: 加载中
.loading-step {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}

.loading-animation {
  text-align: center;

  .loading-orbs {
    position: relative;
    width: 100px;
    height: 100px;
    margin: 0 auto 24px;
  }

  .orb {
    position: absolute;
    border-radius: $radius-full;
    animation: orbit 2s linear infinite;

    &.orb-1 {
      width: 20px; height: 20px;
      background: $color-primary;
      top: 0; left: 50%; transform: translateX(-50%);
    }
    &.orb-2 {
      width: 16px; height: 16px;
      background: $color-secondary;
      bottom: 10px; left: 0;
      animation-delay: -0.6s;
    }
    &.orb-3 {
      width: 16px; height: 16px;
      background: #10B981;
      bottom: 10px; right: 0;
      animation-delay: -1.2s;
    }
  }

  p {
    font-size: $font-size-lg;
    color: $color-text-secondary;
    margin: 0 0 20px;
  }

  .loading-steps {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-width: 200px;
    margin: 0 auto;
  }

  .ls-item {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    padding: 6px 12px;
    border-radius: $radius-lg;
    background: $color-bg-page;
    transition: all $duration-normal;

    &.done {
      color: $color-success;
      background: #DCFCE7;
    }
  }
}

@keyframes orbit {
  from { transform: translateX(-50%) rotate(0deg); }
  to   { transform: translateX(-50%) rotate(360deg); }
}

// Step 3: 结果
.result-step {
  .result-score-section {
    display: flex;
    align-items: center;
    gap: 32px;
    padding: 24px;
    background: $color-bg-page;
    border-radius: $radius-2xl;
    margin-bottom: 24px;

    h3 {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
      margin: 0 0 8px;
    }

    p {
      color: $color-text-secondary;
      line-height: $line-height-loose;
      margin: 0;
    }
  }

  .warnings {
    margin-top: 12px;
    .warning-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: $font-size-sm;
      color: $color-warning;
      margin-top: 4px;
    }
  }
}

.result-tabs {
  :deep(.el-tabs__item) {
    font-weight: $font-weight-medium;
    &.is-active { color: $color-primary; }
  }
  :deep(.el-tabs__active-bar) { background: $color-primary; }
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  background: $color-bg-page;
  border-radius: $radius-lg;
  border: 1px solid $color-border-light;
  font-size: $font-size-sm;
  color: $color-text-regular;
  line-height: $line-height-base;
  transition: all $duration-fast;

  &:hover {
    border-color: rgba(16, 185, 129, 0.3);
    background: #F0FDF4;
  }

  svg { flex-shrink: 0; margin-top: 2px; }
}

.next-steps {
  margin-top: 24px;
  padding: 20px;
  background: #F0FDF4;
  border-radius: $radius-xl;
  border: 1px solid rgba(16, 185, 129, 0.2);

  h4 {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-success;
    margin: 0 0 12px;
  }
}

.next-steps-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.next-step-item {
  display: flex;
  align-items: center;
  gap: 12px;

  .step-number {
    width: 24px;
    height: 24px;
    border-radius: $radius-full;
    background: $color-success;
    color: #fff;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  span {
    font-size: $font-size-sm;
    color: $color-text-regular;
  }
}

// ── 分析记录详情 ───────────────────────────
.record-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

// Hero 综合概览
.detail-hero {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: $color-bg-page;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;

  &__score {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;

    .score-label {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $color-text-secondary;
    }
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-badge {
  padding: 3px 12px;
  border-radius: $radius-full;
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.hero-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin: 0;
}

.hero-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: $font-size-sm;
  color: $color-text-secondary;

  .dot { color: $color-text-placeholder; }
}

.hero-summary {
  margin-top: 4px;
  padding: 12px 14px;
  background: $color-bg-card;
  border-radius: $radius-lg;
  border: 1px solid $color-border-light;

  .summary-label {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: $font-size-xs;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    margin-bottom: 6px;
    svg { color: $color-primary; }
  }

  .summary-text {
    font-size: $font-size-sm;
    color: $color-text-regular;
    line-height: $line-height-loose;
    margin: 0;
  }
}

// 通用区块头部
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin: 0;
    svg { color: $color-primary; }
  }

  .section-sub {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

// 专家讨论区
.discussion-section {
  padding: 20px 24px;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
}

.discussion-rounds {
  :deep(.el-collapse) {
    border: none;
    .el-collapse-item__header {
      border-radius: $radius-xl;
      border: 1.5px solid $color-border;
      padding: 12px 16px;
      margin-bottom: 8px;
      background: $color-bg-page;
      font-size: inherit;
      font-weight: inherit;
      color: inherit;
      transition: all $duration-normal;
      &:hover { border-color: $color-primary; }
    }
    .el-collapse-item__wrap {
      border: none;
      background: transparent;
    }
    .el-collapse-item__content {
      padding: 0 0 8px;
    }
    .el-collapse-item.is-active .el-collapse-item__header {
      border-color: $color-primary;
      background: rgba(102, 126, 234, 0.04);
      border-bottom-left-radius: 0;
      border-bottom-right-radius: 0;
    }
  }
}

.round-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;

  .round-meta {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .round-num {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
    font-weight: $font-weight-medium;
  }

  .round-points {
    display: flex;
    align-items: center;
    gap: 6px;
    flex-wrap: wrap;
  }

  .point-chip {
    font-size: $font-size-xs;
    padding: 2px 8px;
    border-radius: $radius-full;
    background: $color-bg-active;
    color: $color-primary;
    font-weight: $font-weight-medium;
  }

  .point-more {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}

.round-role-badge {
  padding: 2px 10px;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;

  &.role-hr { background: rgba(245,158,11,0.12); color: #B45309; }
  &.role-tech { background: rgba(102,126,234,0.12); color: #3B82F6; }
  &.role-career { background: rgba(16,185,129,0.12); color: #15803D; }
  &.role-default { background: $color-bg-hover; color: $color-text-secondary; }
}

.round-content {
  padding: 16px;
  background: $color-bg-page;
  border-radius: $radius-xl;
  border: 1.5px solid $color-border;
  margin-top: 4px;
}

.round-role-card {
  border-radius: $radius-xl;
  overflow: hidden;

  &.role-hr { .role-card-header { background: rgba(245,158,11,0.08); border-left: 3px solid #F59E0B; } .role-avatar { background: #F59E0B; } }
  &.role-tech { .role-card-header { background: rgba(102,126,234,0.08); border-left: 3px solid #3B82F6; } .role-avatar { background: #3B82F6; } }
  &.role-career { .role-card-header { background: rgba(16,185,129,0.08); border-left: 3px solid #10B981; } .role-avatar { background: #10B981; } }
  &.role-default { .role-card-header { background: $color-bg-hover; border-left: 3px solid $color-text-placeholder; } }

  .role-card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    border-bottom: 1px solid $color-border-light;

    .role-avatar {
      width: 32px;
      height: 32px;
      min-width: 32px;
      border-radius: $radius-lg;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      font-weight: $font-weight-bold;
      color: #fff;
    }

    .role-name {
      font-size: $font-size-sm;
      font-weight: $font-weight-bold;
      color: $color-text-primary;
    }

    .role-round {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
      margin-left: auto;
    }
  }

  .role-card-body {
    padding: 16px;

    p {
      font-size: $font-size-sm;
      color: $color-text-regular;
      line-height: 1.9;
      margin: 0;
      white-space: pre-wrap;
    }
  }

  .role-card-points {
    padding: 0 16px 16px;

    .points-label {
      font-size: $font-size-xs;
      font-weight: $font-weight-semibold;
      color: $color-text-secondary;
      margin-bottom: 8px;
    }

    .points-list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 6px;

      li {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        font-size: $font-size-sm;
        color: $color-text-regular;
        line-height: $line-height-base;
        svg { flex-shrink: 0; margin-top: 2px; color: $color-success; }
      }
    }
  }
}

// 模块建议区
.suggestions-section {
  padding: 20px 24px;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  @media (max-width: 700px) { grid-template-columns: 1fr; }
}

.suggestion-module {
  background: $color-bg-page;
  border-radius: $radius-xl;
  border: 1px solid $color-border-light;
  overflow: hidden;
  transition: border-color $duration-fast;

  &:hover { border-color: rgba(102,126,234,0.3); }

  .module-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: rgba(102,126,234,0.04);
    border-bottom: 1px solid $color-border-light;

    .module-icon {
      color: $color-primary;
      display: flex;
      align-items: center;
    }

    .module-name {
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      flex: 1;
    }

    .module-count {
      font-size: $font-size-xs;
      color: $color-text-placeholder;
      background: $color-bg-hover;
      padding: 2px 8px;
      border-radius: $radius-full;
    }
  }

  .module-items {
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-height: 280px;
    overflow-y: auto;
  }
}

.suggestion-item {
  display: flex;
  gap: 10px;
  padding: 8px 10px;
  border-radius: $radius-lg;
  background: $color-bg-card;
  transition: all $duration-fast;

  &:hover {
    background: #F0FDF4;
    .item-num { background: $color-success; }
  }

  .item-num {
    width: 20px;
    height: 20px;
    min-width: 20px;
    border-radius: $radius-full;
    background: rgba(102,126,234,0.1);
    color: $color-primary;
    font-size: 10px;
    font-weight: $font-weight-bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-top: 1px;
    transition: background $duration-fast;
  }

  .item-text {
    font-size: $font-size-sm;
    color: $color-text-regular;
    line-height: $line-height-base;
  }
}

// 综合评语
.comment-section {
  padding: 20px 24px;
  background: $color-bg-card;
  border-radius: $radius-2xl;
  border: 1px solid $color-border-light;
}

.comment-body {
  padding: 16px 20px;
  background: rgba(59, 130, 246, 0.04);
  border-left: 3px solid #3B82F6;
  border-radius: $radius-xl;

  p {
    font-size: $font-size-base;
    color: $color-text-primary;
    line-height: 1.9;
    margin: 0;
    white-space: pre-wrap;
  }
}

// 下一步行动
.nextsteps-section {
  padding: 20px 24px;
  background: #F0FDF4;
  border-radius: $radius-2xl;
  border: 1px solid rgba(16,185,129,0.2);
}

.nextsteps-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.nextstep-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 12px 16px;
  background: $color-bg-card;
  border-radius: $radius-xl;
  border: 1px solid $color-border-light;

  .nextstep-num {
    width: 26px;
    height: 26px;
    min-width: 26px;
    border-radius: $radius-full;
    background: $color-success;
    color: #fff;
    font-size: $font-size-xs;
    font-weight: $font-weight-bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .nextstep-text {
    font-size: $font-size-sm;
    color: $color-text-regular;
    line-height: $line-height-base;
    padding-top: 3px;
  }
}

// Dialog 覆盖样式
:deep(.record-detail-dialog) {
  .el-dialog__body {
    padding: 20px 24px 28px;
    max-height: 80vh;
    overflow-y: auto;
    &::-webkit-scrollbar { width: 6px; }
    &::-webkit-scrollbar-thumb { background: $color-border; border-radius: 3px; }
  }
}

:deep(.hm-dialog) {
  .el-dialog__header {
    padding: 20px 24px 16px;
    border-bottom: 1px solid $color-border-light;
    margin-right: 0;
  }
  .el-dialog__body {
    padding: 24px;
  }
  .el-dialog__footer {
    padding: 16px 24px;
    border-top: 1px solid $color-border-light;
  }
}

@keyframes fade-in {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
