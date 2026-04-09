package com.hiremate.ai.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeRequest {

    private String title;
    private String targetPosition;
    private String targetIndustry;
    private String contentJson;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {
        private String title;
        private String targetPosition;
        private String targetIndustry;
        private String contentJson;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String title;
        private String targetPosition;
        private String targetIndustry;
        private String contentJson;
    }
}
