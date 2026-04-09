package com.hiremate.ai.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobMatchRequest {

    private Long jobDescriptionId;

    private String resumeText;

    private String resumeFileName;

    private String jobContent;
}