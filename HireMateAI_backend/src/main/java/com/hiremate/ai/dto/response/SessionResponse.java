package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    private Long sessionId;

    private String jobPosition;

    private String interviewType;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
