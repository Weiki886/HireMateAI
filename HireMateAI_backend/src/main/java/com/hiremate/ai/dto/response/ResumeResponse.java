package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private Long userId;
    private String title;
    private Integer version;
    private String targetPosition;
    private String targetIndustry;
    private String status;
    private String contentJson;
    private String contentPdfUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
