package com.hiremate.ai.dto.request;

import lombok.Data;

/**
 * 生成面试题目请求
 */
@Data
public class GenerateQuestionsRequest {

    private String jobPosition;

    private String interviewType;

    private String difficulty;

    private Integer count = 5;

    private Long categoryId;
}
