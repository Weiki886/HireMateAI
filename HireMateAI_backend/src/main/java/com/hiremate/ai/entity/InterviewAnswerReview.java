package com.hiremate.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试回答详细评价表
 */
@TableName("interview_answer_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewAnswerReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("report_id")
    private Long reportId;

    @TableField("message_id")
    private Long messageId;

    @TableField("question_summary")
    private String questionSummary;

    @TableField("answer_quality_score")
    private BigDecimal answerQualityScore;

    @TableField("strengths")
    private String strengths;

    @TableField("weaknesses")
    private String weaknesses;

    @TableField("improvement_tips")
    private String improvementTips;

    @TableField("model_answer")
    private String modelAnswer;

    @TableField("dimension_type")
    private String dimensionType;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
