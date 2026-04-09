-- ============================================
-- HireMate AI 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS hiremate DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hiremate;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_username` (`username`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 面试会话表
CREATE TABLE IF NOT EXISTS `interview_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `job_position` VARCHAR(100) COMMENT '目标岗位',
    `interview_type` VARCHAR(50) NOT NULL COMMENT '面试类型：综合面试/技术面试/HR面试/行为面试/终面',
    `resume_text` MEDIUMTEXT COMMENT '简历文本(可选，MEDIUMTEXT 支持约 16MB)',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '会话状态：active/closed',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试会话表';

-- 面试消息表
CREATE TABLE IF NOT EXISTS `interview_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `interview_session`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试消息表';

-- 简历表
CREATE TABLE IF NOT EXISTS `resume` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) COMMENT '简历标题',
    `version` INT DEFAULT 1 COMMENT '版本号',
    `target_position` VARCHAR(100) COMMENT '目标岗位',
    `target_industry` VARCHAR(100) COMMENT '目标行业',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/COMPLETED/OPTIMIZED',
    `content_json` MEDIUMTEXT COMMENT '简历内容(JSON格式，MEDIUMTEXT 支持约 16MB)',
    `content_pdf_url` VARCHAR(500) COMMENT 'PDF文件URL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_resume_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

-- 简历分析记录表
CREATE TABLE IF NOT EXISTS `resume_analysis_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_position` VARCHAR(100) COMMENT '目标岗位',
    `original_text` MEDIUMTEXT COMMENT '原始简历文本摘要(MEDIUMTEXT 支持约 16MB)',
    `original_pdf_url` VARCHAR(500) COMMENT '原始简历文件路径',
    `jd_text` MEDIUMTEXT COMMENT '职位描述文本',
    `analysis_result_json` MEDIUMTEXT COMMENT 'AI返回的完整分析JSON(MEDIUMTEXT 支持约 16MB)',
    `overall_score` DECIMAL(5,1) COMMENT '简历总分 0-100',
    `discussion_rounds` INT DEFAULT 0 COMMENT '讨论轮数',
    `status` VARCHAR(20) DEFAULT 'ANALYZING' COMMENT '状态：ANALYZING/COMPLETED/FAILED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历分析记录表';

-- 岗位描述表
CREATE TABLE IF NOT EXISTS `job_description` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `title` VARCHAR(200) COMMENT '岗位标题',
    `content` TEXT NOT NULL COMMENT '岗位描述内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_job_desc_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位描述表';

-- 岗位匹配记录表
CREATE TABLE IF NOT EXISTS `job_description_match_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `job_description_id` BIGINT COMMENT '岗位描述ID',
    `resume_id` BIGINT COMMENT '简历ID(预留字段)',
    `resume_file_name` VARCHAR(255) COMMENT '简历文件名',
    `match_score` DECIMAL(5,1) COMMENT '匹配分数(0-100)',
    `match_result` TEXT COMMENT '匹配结果概述',
    `analysis_details` TEXT COMMENT '详细分析内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_job_desc_id` (`job_description_id`),
    CONSTRAINT `fk_match_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位匹配记录表';

-- ============================================
-- 新增功能：面试复盘报告 & 面试题库
-- ============================================

-- 面试复盘报告主表
CREATE TABLE IF NOT EXISTS `interview_review_report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` BIGINT NOT NULL COMMENT '关联的面试会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_score` DECIMAL(5,1) DEFAULT 0 COMMENT '综合总分 0-100',
    `professional_score` DECIMAL(5,1) DEFAULT 0 COMMENT '专业能力评分 0-100',
    `expression_score` DECIMAL(5,1) DEFAULT 0 COMMENT '表达能力评分 0-100',
    `logic_score` DECIMAL(5,1) DEFAULT 0 COMMENT '逻辑思维评分 0-100',
    `confidence_score` DECIMAL(5,1) DEFAULT 0 COMMENT '自信心评分 0-100',
    `overall_summary` TEXT COMMENT 'AI 生成的总体复盘总结',
    `improvement_suggestions` TEXT COMMENT 'AI 生成的改进建议（JSON数组格式）',
    `status` VARCHAR(20) DEFAULT 'GENERATING' COMMENT '状态：GENERATING/COMPLETED/FAILED',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_review_session` FOREIGN KEY (`session_id`) REFERENCES `interview_session`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试复盘报告主表';

-- 面试回答详细评价表
CREATE TABLE IF NOT EXISTS `interview_answer_review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `report_id` BIGINT NOT NULL COMMENT '关联的复盘报告ID',
    `message_id` BIGINT NOT NULL COMMENT '关联的面试消息ID（用户回答）',
    `question_summary` TEXT COMMENT 'AI 总结的题目/问题',
    `answer_quality_score` DECIMAL(5,1) DEFAULT 0 COMMENT '回答质量评分 0-100',
    `strengths` TEXT COMMENT '回答优点（JSON数组格式）',
    `weaknesses` TEXT COMMENT '回答缺点/不足（JSON数组格式）',
    `improvement_tips` TEXT COMMENT '改进建议',
    `model_answer` TEXT COMMENT '参考示范回答',
    `dimension_type` VARCHAR(50) COMMENT '考察维度：技术能力/行为面试/HR问题/综合能力',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`),
    KEY `idx_message_id` (`message_id`),
    CONSTRAINT `fk_answer_review_report` FOREIGN KEY (`report_id`) REFERENCES `interview_review_report`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answer_review_message` FOREIGN KEY (`message_id`) REFERENCES `interview_message`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试回答详细评价表';

-- 题库分类表
CREATE TABLE IF NOT EXISTS `question_bank_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级分类ID，0 表示顶级',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(500) COMMENT '分类描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `type` VARCHAR(20) DEFAULT 'POSITION' COMMENT '分类类型：POSITION-按岗位/BEHAVIOR-行为问题/TECH-技术问题/HR-HR问题',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库分类表';

-- 面试题目表
CREATE TABLE IF NOT EXISTS `interview_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT COMMENT '所属分类ID',
    `question_text` TEXT NOT NULL COMMENT '题目/问题内容',
    `answer_points` TEXT COMMENT '答案要点（JSON数组格式）',
    `model_answer` TEXT COMMENT '参考示范回答（长文本）',
    `difficulty` VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '难度：EASY/MEDIUM/HARD',
    `interview_type` VARCHAR(50) COMMENT '适用面试类型：综合面试/技术面试/HR面试/行为面试/终面',
    `target_position` VARCHAR(100) COMMENT '目标岗位',
    `target_industry` VARCHAR(100) COMMENT '目标行业',
    `tags` VARCHAR(500) COMMENT '标签，逗号分隔',
    `view_count` INT DEFAULT 0 COMMENT '查看次数',
    `created_by` BIGINT COMMENT '创建者用户ID（AI生成时为空）',
    `user_id` BIGINT COMMENT '所属用户ID（AI生成题目归用户所有）',
    `is_public` TINYINT DEFAULT 1 COMMENT '是否公开：0-私有/1-公开',
    `source` VARCHAR(50) DEFAULT 'MANUAL' COMMENT '来源：MANUAL-手动录入/AI-AI生成',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_difficulty` (`difficulty`),
    KEY `idx_interview_type` (`interview_type`),
    KEY `idx_target_position` (`target_position`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_public` (`is_public`),
    CONSTRAINT `fk_question_category` FOREIGN KEY (`category_id`) REFERENCES `question_bank_category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试题目表';

-- 用户收藏题目表
CREATE TABLE IF NOT EXISTS `user_question_favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `note` TEXT COMMENT '用户备注/笔记',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_question_id` (`question_id`),
    CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_favorite_question` FOREIGN KEY (`question_id`) REFERENCES `interview_question`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏题目表';

-- ============================================
-- 初始化题库默认分类 (可选)
-- ============================================

-- INSERT INTO `question_bank_category` (`name`, `type`, `sort_order`, `description`) VALUES
-- ('技术面试', 'TECH', 1, '编程语言、算法、系统设计等技术问题'),
-- ('行为面试', 'BEHAVIOR', 2, 'STAR法则、过往经历、团队合作等行为问题'),
-- ('HR面试', 'HR', 3, '薪资期望、职业规划、离职原因等HR问题'),
-- ('综合面试', 'POSITION', 4, '按岗位分类的综合面试题');

-- ============================================
-- 初始化测试数据 (可选)
-- ============================================

-- INSERT INTO `user` (`username`, `password`, `email`) VALUES
-- ('test', '$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'test@example.com');
