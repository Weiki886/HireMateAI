package com.hiremate.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试复盘报告主表
 */
@TableName("interview_review_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewReviewReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("total_score")
    private BigDecimal totalScore;

    @TableField("professional_score")
    private BigDecimal professionalScore;

    @TableField("expression_score")
    private BigDecimal expressionScore;

    @TableField("logic_score")
    private BigDecimal logicScore;

    @TableField("confidence_score")
    private BigDecimal confidenceScore;

    @TableField("overall_summary")
    private String overallSummary;

    @TableField("improvement_suggestions")
    private String improvementSuggestions;

    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
