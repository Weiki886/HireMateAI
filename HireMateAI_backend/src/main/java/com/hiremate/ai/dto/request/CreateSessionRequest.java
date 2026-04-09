package com.hiremate.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建面试会话请求
 */
@Data
public class CreateSessionRequest {

    private String jobPosition;

    @NotNull(message = "面试类型不能为空")
    private String interviewType;

    private String resumeText;
}
