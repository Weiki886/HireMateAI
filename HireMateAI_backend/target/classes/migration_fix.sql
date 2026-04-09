-- ============================================
-- HireMate AI 增量修复脚本（已有数据库执行）
-- ============================================

USE hiremate;

-- 1. 升级 interview_session.resume_text 为 MEDIUMTEXT（解决 "Data too long for resume_text"）
ALTER TABLE `interview_session`
    MODIFY COLUMN `resume_text` MEDIUMTEXT COMMENT '简历文本(可选，MEDIUMTEXT 支持约 16MB)';

-- 2. 升级 resume.content_json 为 MEDIUMTEXT（简历 JSON 内容可能较大）
ALTER TABLE `resume`
    MODIFY COLUMN `content_json` MEDIUMTEXT COMMENT '简历内容(JSON格式，MEDIUMTEXT 支持约 16MB)';

-- 3. 新建缺失的 resume_analysis_record 表（岗位匹配分析记录）
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
