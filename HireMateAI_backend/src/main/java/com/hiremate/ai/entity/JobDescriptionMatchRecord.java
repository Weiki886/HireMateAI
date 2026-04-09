package com.hiremate.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("job_description_match_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDescriptionMatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("job_description_id")
    private Long jobDescriptionId;

    @TableField("resume_id")
    private Long resumeId;

    @TableField("resume_file_name")
    private String resumeFileName;

    @TableField("match_score")
    private BigDecimal matchScore;

    @TableField("match_result")
    private String matchResult;

    @TableField("analysis_details")
    private String analysisDetails;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}