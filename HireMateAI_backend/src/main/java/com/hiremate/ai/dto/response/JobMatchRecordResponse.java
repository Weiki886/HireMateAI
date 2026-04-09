package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchRecordResponse {

    private Long id;
    private Long userId;
    private Long jobDescriptionId;
    private Long resumeId;
    private String resumeFileName;
    private BigDecimal matchScore;
    private String matchResult;
    private String analysisDetails;
    private LocalDateTime createdAt;
}