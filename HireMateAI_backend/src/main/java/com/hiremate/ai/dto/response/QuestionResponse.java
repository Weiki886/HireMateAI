package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试题目响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private String questionText;

    private List<String> answerPoints;

    private String modelAnswer;

    private String difficulty;

    private String difficultyLabel;

    private String interviewType;

    private String targetPosition;

    private String targetIndustry;

    private List<String> tags;

    private Integer viewCount;

    private String source;

    private Boolean isFavorited;

    private String createdAt;
}
