package com.hiremate.ai.service;

import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.config.DashScopeProperties;
import com.hiremate.ai.dto.request.JobDescriptionRequest;
import com.hiremate.ai.dto.request.JobMatchRequest;
import com.hiremate.ai.dto.response.JobDescriptionResponse;
import com.hiremate.ai.dto.response.JobMatchRecordResponse;
import com.hiremate.ai.entity.JobDescription;
import com.hiremate.ai.entity.JobDescriptionMatchRecord;
import com.hiremate.ai.mapper.JobDescriptionMapper;
import com.hiremate.ai.mapper.JobDescriptionMatchRecordMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionMapper jobDescriptionMapper;
    private final JobDescriptionMatchRecordMapper matchRecordMapper;
    private final DashScopeProperties dashScopeProperties;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String MATCH_SYSTEM_PROMPT = """
            你是一个专业的简历与岗位匹配分析师，负责评估候选人的简历与岗位描述之间的匹配程度。

            【分析要求】
            1. 深入分析简历内容与岗位描述的匹配度
            2. 从多个维度进行评估：技能匹配度、经验匹配度、教育背景、综合素质
            3. 给出0-100的综合匹配分数（保留一位小数）
            4. 详细说明匹配和不匹配的具体点
            5. 提供改进建议

            【输出格式】
            请严格按照以下JSON格式返回分析结果，不要包含任何其他内容：
            {
              "matchScore": 85.5,
              "matchResult": "匹配结果概述（50-100字）",
              "analysisDetails": "详细分析内容，包含：\n1. 技能匹配分析\n2. 经验匹配分析\n3. 教育背景分析\n4. 优势\n5. 不足与建议"
            }

            现在开始分析：
            """;

    @Transactional
    public JobDescriptionResponse saveJobDescription(Long userId, JobDescriptionRequest request) {
        JobDescription jobDescription = JobDescription.builder()
                .userId(userId)
                .title(request.getTitle() != null ? request.getTitle() : extractTitleFromContent(request.getContent()))
                .content(request.getContent())
                .build();
        jobDescriptionMapper.insert(jobDescription);
        return toResponse(jobDescription);
    }

    @Transactional(readOnly = true)
    public JobDescriptionResponse getJobDescription(Long userId, Long jobId) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(jobId);
        if (jobDescription == null || !jobDescription.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "岗位描述不存在");
        }
        return toResponse(jobDescription);
    }

    @Transactional(readOnly = true)
    public List<JobDescriptionResponse> listJobDescriptions(Long userId) {
        return jobDescriptionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobDescription>()
                                .eq(JobDescription::getUserId, userId)
                                .orderByDesc(JobDescription::getCreatedAt)
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteJobDescription(Long userId, Long jobId) {
        JobDescription jobDescription = jobDescriptionMapper.selectById(jobId);
        if (jobDescription == null || !jobDescription.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "岗位描述不存在");
        }
        jobDescriptionMapper.deleteById(jobId);
    }

    @Transactional
    public JobMatchRecordResponse analyzeMatch(Long userId, JobMatchRequest request) {
        JobDescription jobDescription = null;
        if (request.getJobDescriptionId() != null) {
            jobDescription = jobDescriptionMapper.selectById(request.getJobDescriptionId());
            if (jobDescription == null || !jobDescription.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.NOT_FOUND, "岗位描述不存在");
            }
        }

        String resumeText = request.getResumeText();
        if (resumeText == null || resumeText.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "简历内容不能为空");
        }

        String jobContent = (jobDescription != null) ? jobDescription.getContent() : request.getJobContent();
        if (jobContent == null || jobContent.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "岗位描述内容不能为空");
        }

        String analysisResult = callAiAnalyze(resumeText, jobContent);

        BigDecimal matchScore = extractScoreFromResult(analysisResult);
        String matchResult = extractMatchResultFromResult(analysisResult);
        String analysisDetails = extractAnalysisDetailsFromResult(analysisResult);

        JobDescriptionMatchRecord record = JobDescriptionMatchRecord.builder()
                .userId(userId)
                .jobDescriptionId(request.getJobDescriptionId())
                .resumeFileName(request.getResumeFileName())
                .matchScore(matchScore)
                .matchResult(matchResult)
                .analysisDetails(analysisDetails)
                .build();
        matchRecordMapper.insert(record);

        return toMatchResponse(record);
    }

    @Transactional(readOnly = true)
    public List<JobMatchRecordResponse> listMatchRecords(Long userId) {
        return matchRecordMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<JobDescriptionMatchRecord>()
                                .eq(JobDescriptionMatchRecord::getUserId, userId)
                                .orderByDesc(JobDescriptionMatchRecord::getCreatedAt)
                ).stream()
                .map(this::toMatchResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobMatchRecordResponse getMatchRecord(Long userId, Long recordId) {
        JobDescriptionMatchRecord record = matchRecordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "匹配记录不存在");
        }
        return toMatchResponse(record);
    }

    private String callAiAnalyze(String resumeText, String jobDescription) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", MATCH_SYSTEM_PROMPT));
            messages.add(Map.of("role", "user", "content",
                    "【简历内容】\n" + resumeText + "\n\n【岗位描述】\n" + jobDescription));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", dashScopeProperties.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 4000);
            requestBody.put("temperature", 0.3);

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    JSON
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + dashScopeProperties.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI分析失败: " + response.message());
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                JsonNode root = objectMapper.readTree(responseBody);
                if (root.has("choices") && root.get("choices").isArray()) {
                    JsonNode delta = root.get("choices").get(0).get("message");
                    if (delta != null && delta.has("content")) {
                        return delta.get("content").asText();
                    }
                }
                throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI响应解析失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI analysis error: {}", e.getMessage(), e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "AI分析异常: " + e.getMessage());
        }
    }

    private BigDecimal extractScoreFromResult(String result) {
        try {
            int jsonStart = result.indexOf("{");
            int jsonEnd = result.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonStr = result.substring(jsonStart, jsonEnd + 1);
                JsonNode node = objectMapper.readTree(jsonStr);
                if (node.has("matchScore")) {
                    return new BigDecimal(node.get("matchScore").asDouble());
                }
            }
            return new BigDecimal("0");
        } catch (Exception e) {
            log.warn("Failed to extract score from result, using default: {}", e.getMessage());
            return new BigDecimal("0");
        }
    }

    private String extractMatchResultFromResult(String result) {
        try {
            int jsonStart = result.indexOf("{");
            int jsonEnd = result.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonStr = result.substring(jsonStart, jsonEnd + 1);
                JsonNode node = objectMapper.readTree(jsonStr);
                if (node.has("matchResult")) {
                    return node.get("matchResult").asText();
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractAnalysisDetailsFromResult(String result) {
        try {
            int jsonStart = result.indexOf("{");
            int jsonEnd = result.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1) {
                String jsonStr = result.substring(jsonStart, jsonEnd + 1);
                JsonNode node = objectMapper.readTree(jsonStr);
                if (node.has("analysisDetails")) {
                    return node.get("analysisDetails").asText();
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractTitleFromContent(String content) {
        if (content == null || content.isBlank()) {
            return "未命名岗位";
        }
        String[] lines = content.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("岗位") && !line.startsWith("职位") && line.length() <= 50) {
                return line.length() > 30 ? line.substring(0, 30) + "..." : line;
            }
        }
        return content.length() > 30 ? content.substring(0, 30) + "..." : content;
    }

    private JobDescriptionResponse toResponse(JobDescription jobDescription) {
        return JobDescriptionResponse.builder()
                .id(jobDescription.getId())
                .userId(jobDescription.getUserId())
                .title(jobDescription.getTitle())
                .content(jobDescription.getContent())
                .createdAt(jobDescription.getCreatedAt())
                .updatedAt(jobDescription.getUpdatedAt())
                .build();
    }

    private JobMatchRecordResponse toMatchResponse(JobDescriptionMatchRecord record) {
        return JobMatchRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .jobDescriptionId(record.getJobDescriptionId())
                .resumeId(record.getResumeId())
                .resumeFileName(record.getResumeFileName())
                .matchScore(record.getMatchScore())
                .matchResult(record.getMatchResult())
                .analysisDetails(record.getAnalysisDetails())
                .createdAt(record.getCreatedAt())
                .build();
    }
}