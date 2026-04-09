package com.hiremate.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@TableName("resume_analysis_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeAnalysisRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    /** 目标岗位 */
    @TableField("target_position")
    private String targetPosition;

    /** 原始简历文本（摘要） */
    @TableField("original_text")
    private String originalText;

    /** 原始简历文件路径（PDF） */
    @TableField("original_pdf_url")
    private String originalPdfUrl;

    /** 职位描述文本 */
    @TableField("jd_text")
    private String jdText;

    /** AI 返回的完整 JSON 结果 */
    @TableField("analysis_result_json")
    private String analysisResultJson;

    /** 简历总分 0-100 */
    @TableField("overall_score")
    private Integer overallScore;

    /** 讨论轮数 */
    @TableField("discussion_rounds")
    private Integer discussionRounds;

    /** 状态：ANALYZING / COMPLETED / FAILED */
    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
