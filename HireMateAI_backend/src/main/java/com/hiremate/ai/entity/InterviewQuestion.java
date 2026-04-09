package com.hiremate.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 面试题目表
 */
@TableName("interview_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("category_id")
    private Long categoryId;

    @TableField("question_text")
    private String questionText;

    @TableField("answer_points")
    private String answerPoints;

    @TableField("model_answer")
    private String modelAnswer;

    @TableField("difficulty")
    private String difficulty;

    @TableField("interview_type")
    private String interviewType;

    @TableField("target_position")
    private String targetPosition;

    @TableField("target_industry")
    private String targetIndustry;

    @TableField("tags")
    private String tags;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("created_by")
    private Long createdBy;

    @TableField("user_id")
    private Long userId;

    @TableField("is_public")
    private Integer isPublic;

    @TableField("source")
    private String source;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
