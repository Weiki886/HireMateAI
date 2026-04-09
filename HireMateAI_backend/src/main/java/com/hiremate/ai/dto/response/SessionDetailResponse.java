package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 会话详情响应（包含消息列表）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDetailResponse {

    private Long sessionId;

    private String jobPosition;

    private String interviewType;

    private String resumeText;

    private String status;

    private String createdAt;

    private String updatedAt;

    private List<MessageResponse> messages;
}
