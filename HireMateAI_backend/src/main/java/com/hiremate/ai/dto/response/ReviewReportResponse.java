package com.hiremate.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试复盘报告响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewReportResponse {

    private Long id;

    private Long sessionId;

    private String jobPosition;

    private String interviewType;

    private BigDecimal totalScore;

    private BigDecimal professionalScore;

    private BigDecimal expressionScore;

    private BigDecimal logicScore;

    private BigDecimal confidenceScore;

    private String overallSummary;

    private List<String> improvementSuggestions;

    private String status;

    private String createdAt;

    private List<ReviewAnswerResponse> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewAnswerResponse {

        private Long id;

        private Long messageId;

        private String questionSummary;

        private String userAnswer;

        private BigDecimal answerQualityScore;

        private List<String> strengths;

        private List<String> weaknesses;

        private String improvementTips;

        private String modelAnswer;

        private String dimensionType;
    }
}
