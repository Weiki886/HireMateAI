package com.hiremate.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobDescriptionRequest {

    @NotBlank(message = "岗位描述内容不能为空")
    private String content;

    private String title;
}