package com.hiremate.ai.dto.response;

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
public class ResumeAnalysisRecordResponse {

    private Long id;
    private Long userId;
    private String targetPosition;
    private String originalText;
    private String originalPdfUrl;
    private String jdText;
    private String analysisResultJson;
    private Integer overallScore;
    private Integer discussionRounds;
    private String status;
    private LocalDateTime createdAt;

    // 解析后的结构化数据（仅在详情接口返回）
    private List<DiscussionRoundResponse> discussionRoundsParsed;
    private ModuleSuggestionsResponse moduleSuggestions;
    private String overallComment;
    private List<String> nextSteps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscussionRoundResponse {
        private Integer roundNumber;
        private String aiRole;
        private String content;
        private List<String> keyPoints;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleSuggestionsResponse {
        private List<String> personalInfo;
        private List<String> workExperience;
        private List<String> projectExperience;
        private List<String> skills;
        private List<String> selfEvaluation;
        private List<String> formatting;
    }
}
