package com.hiremate.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeOptimizationResponse {

    private Long recordId;
    private String originalText;
    private String targetPosition;
    private List<DiscussionRound> discussionRounds;
    private OptimizationResult finalResult;
    private LocalDateTime generatedAt;
    private List<String> warnings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiscussionRound {
        private Integer roundNumber;
        private String aiRole;
        private String content;
        private List<String> keyPoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OptimizationResult {
        private Integer overallScore;
        private ModuleSuggestions moduleSuggestions;
        private String overallComment;
        private List<String> nextSteps;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ModuleSuggestions {
        private List<String> personalInfo;
        private List<String> workExperience;
        private List<String> projectExperience;
        private List<String> skills;
        private List<String> selfEvaluation;
        private List<String> formatting;
    }
}
