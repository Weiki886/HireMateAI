package com.hiremate.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.config.DashScopeProperties;
import com.hiremate.ai.dto.response.ResumeOptimizationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.hiremate.ai.entity.ResumeAnalysisRecord;
import com.hiremate.ai.service.ResumeAnalysisRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeOptimizationService {

    private final PdfTextExtractor pdfTextExtractor;
    private final DashScopeProperties dashScopeProperties;
    private final ObjectMapper objectMapper;
    private final ResumeAnalysisRecordService recordService;

    private static final int MAX_RESUME_CHARS = 20_000;
    private static final int MAX_JD_CHARS = 12_000;
    private static final int MAX_RETRIES = 3;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    private final com.hiremate.ai.service.NotificationService notificationService;

    private static final String RESUME_OPTIMIZATION_PROMPT_TEMPLATE = """
            ## Role
            你是一位经验丰富的简历分析与优化专家，同时扮演三位角色进行多轮深度讨论：
            - **HR总监**：从招聘方视角，评估简历的岗位匹配度、职业成长性、亮点与风险
            - **技术专家**：从专业技能视角，评估技术栈、项目深度、解决方案能力
            - **职业规划师**：从职业发展视角，评估成长轨迹、竞争力、发展建议

            ## 多轮讨论规则
            - 讨论轮数：{{discussionRounds}} 轮
            - 每轮按顺序使用角色：HR总监 → 技术专家 → 职业规划师 → HR总监 → …
            - 角色名称必须完全一致，不得使用别名
            - 每位专家的发言需有深度、有针对性、有具体例子，不说空话套话

            ## Input
            - 当前日期：{{currentDate}}
            - 目标岗位：{{targetPosition}}
            - 职位描述（可能为空）：{{jdText}}
            - 简历全文：{{resumeText}}

            ## Task（按顺序完成）
            1. **逐轮讨论**：每位专家在对应轮次给出有深度的发言（中文，200-400字），需包含：
               - 具体评价（优点、不足、与目标岗位的匹配度）
               - 有数据支撑的观点（如"项目规模X人"、"性能提升Y%"）
               - 对其他专家可能提出的观点的预判
            2. **综合评分**：综合三方观点，给出客观的0-100分评分
            3. **模块优化建议**：对6个模块分别给出3-6条具体可执行的优化建议
            4. **总体评语**：200-300字的综合评价
            5. **下一步行动**：3-5条具体可执行的后续步骤

            ## Output JSON Schema（字段名必须完全一致，仅输出JSON，不要任何前缀或后缀）
            {
              "originalText": "简历核心信息摘要（姓名、联系方式、教育背景、工作年限、核心技术栈一句话概括，150字以内）",
              "targetPosition": "{{targetPosition}}",
              "discussionRounds": [
                {
                  "roundNumber": 1,
                  "aiRole": "HR总监",
                  "content": "该轮完整发言，200-400字，包含具体评价、数据支撑、观点预判",
                  "keyPoints": ["核心观点1", "核心观点2", "核心观点3", "核心观点4"]
                }
              ],
              "finalResult": {
                "overallScore": 0,
                "moduleSuggestions": {
                  "personalInfo": ["建议1（具体可执行）", "建议2（具体可执行）", "建议3（具体可执行）"],
                  "workExperience": ["建议1（具体可执行）", "建议2（具体可执行）", "建议3（具体可执行）"],
                  "projectExperience": ["建议1（具体可执行）", "建议2（具体可执行）", "建议3（具体可执行）"],
                  "skills": ["建议1（具体可执行）", "建议2（具体可执行）", "建议3（具体可执行）"],
                  "selfEvaluation": ["建议1（具体可执行）", "建议2（具体可执行）", "建议3（具体可执行）"],
                  "formatting": ["建议1（具体可执行）", "建议2（具体可执行）", "建议3（具体可执行）"]
                },
                "overallComment": "200-300字的综合评语，客观描述简历的优势、不足、潜力，以及在目标岗位市场的竞争力评估",
                "nextSteps": ["下一步行动1（具体可执行）", "下一步行动2（具体可执行）", "下一步行动3（具体可执行）"]
              }
            }

            ## 约束
            - discussionRounds 数组长度必须等于 {{discussionRounds}}，不得遗漏任何一轮
            - overallScore 为 0-100 的整数，必须客观评分
            - 各 moduleSuggestions 下的数组至少3条建议，不得少于3条
            - originalText 字段仅填入摘要，控制在150字以内，绝不要填入完整简历原文
            - content 字段每条发言不得少于200字，必须有深度
            - 仅输出 JSON 字符串，不要任何 Markdown 代码块包裹，不要前缀说明文字
            """;

    public ResumeOptimizationResponse optimizeSuggestions(
            Long userId,
            MultipartFile pdfFile,
            String resumeText,
            String targetPosition,
            String jdText,
            Integer discussionRounds) {

        if (targetPosition == null || targetPosition.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请填写目标岗位");
        }

        int rounds = discussionRounds == null ? 3 : Math.max(1, Math.min(discussionRounds, 8));

        String raw = null;
        List<String> warnings = new ArrayList<>();

        if (pdfFile != null && !pdfFile.isEmpty()) {
            String filename = pdfFile.getOriginalFilename();
            raw = extractWithFallback(filename, pdfFile, warnings);
        } else if (resumeText != null && !resumeText.isBlank()) {
            raw = resumeText.trim();
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请上传 PDF 或填写简历文本");
        }

        String cleaned = pdfTextExtractor.cleanText(raw);

        // 保存分析记录（分析中状态）
        ResumeAnalysisRecord record = recordService.saveRecord(
                userId, targetPosition.trim(), cleaned, null, jdText, rounds);
        Long recordId = record.getId();

        try {
            String response = callAiForOptimization(cleaned, targetPosition.trim(), jdText, rounds);
            ResumeOptimizationResponse result = parseAiResponse(response);
            result.setWarnings(warnings);
            result.setOriginalText(cleaned);
            result.setTargetPosition(targetPosition.trim());
            result.setGeneratedAt(LocalDate.now().atStartOfDay());
            result.setRecordId(recordId);

            // 更新记录为已完成
            try {
                String resultJson = objectMapper.writeValueAsString(result);
                recordService.updateRecord(recordId, resultJson,
                        result.getFinalResult() != null ? result.getFinalResult().getOverallScore() : null,
                        "COMPLETED");
            } catch (JsonProcessingException jsonEx) {
                log.warn("序列化分析结果失败: {}", jsonEx.getMessage());
                recordService.updateRecord(recordId, null,
                        result.getFinalResult() != null ? result.getFinalResult().getOverallScore() : null,
                        "COMPLETED");
            }
            return result;
        } catch (BusinessException be) {
            recordService.updateRecord(recordId, null, null, "FAILED");
            throw be;
        } catch (Exception e) {
            recordService.updateRecord(recordId, null, null, "FAILED");
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "简历优化分析失败: " + e.getMessage());
        }
    }

    public record OptimizationTaskInfo(
            Long recordId,
            Long userId,
            String targetPosition,
            String status
    ) {}

    /**
     * 异步提交简历优化任务，立即返回 taskInfo，后台执行 AI 分析，完成后通过 SSE 通知前端。
     */
    public OptimizationTaskInfo submitOptimizationTask(
            Long userId,
            MultipartFile pdfFile,
            String resumeText,
            String targetPosition,
            String jdText,
            Integer discussionRounds) {

        if (targetPosition == null || targetPosition.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请填写目标岗位");
        }

        int rounds = discussionRounds == null ? 3 : Math.max(1, Math.min(discussionRounds, 8));

        String cleanedText;
        if (pdfFile != null && !pdfFile.isEmpty()) {
            String filename = pdfFile.getOriginalFilename();
            try {
                PdfTextExtractor.ExtractResult pr = pdfTextExtractor.extractText(pdfFile);
                cleanedText = pdfTextExtractor.cleanText(pr.text);
            } catch (Exception e) {
                throw new BusinessException(ResultCode.BAD_REQUEST,
                        "PDF「" + filename + "」文字提取失败，请将简历内容复制粘贴到文本框中。");
            }
        } else if (resumeText != null && !resumeText.isBlank()) {
            cleanedText = resumeText.trim();
        } else {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请上传 PDF 或填写简历文本");
        }

        ResumeAnalysisRecord record = recordService.saveRecord(
                userId, targetPosition.trim(), cleanedText, null, jdText, rounds);

        final String finalCleanedText = cleanedText;
        asyncExecutor.submit(() -> {
            try {
                String response = callAiForOptimization(finalCleanedText, targetPosition.trim(), jdText, rounds);
                ResumeOptimizationResponse result = parseAiResponse(response);
                result.setTargetPosition(targetPosition.trim());
                result.setGeneratedAt(LocalDate.now().atStartOfDay());
                result.setRecordId(record.getId());

                String resultJson = objectMapper.writeValueAsString(result);
                recordService.updateRecord(record.getId(), resultJson,
                        result.getFinalResult() != null ? result.getFinalResult().getOverallScore() : null,
                        "COMPLETED");

                notificationService.notifyResumeOptimized(userId, record.getId(), targetPosition.trim(), "COMPLETED", result);
                log.info("简历优化任务完成: recordId={}, userId={}", record.getId(), userId);

            } catch (Exception e) {
                log.error("简历优化任务失败: recordId={}, userId={}, error={}", record.getId(), userId, e.getMessage());
                recordService.updateRecord(record.getId(), null, null, "FAILED");
                notificationService.notifyResumeOptimized(userId, record.getId(), targetPosition.trim(), "FAILED", null);
            }
        });

        return new OptimizationTaskInfo(record.getId(), userId, targetPosition.trim(), "ANALYZING");
    }

    private String extractWithFallback(String filename, MultipartFile file, List<String> warnings) {
        PdfTextExtractor.ExtractResult pr = pdfTextExtractor.extractText(file);
        if (pr.text != null && !pr.text.isBlank()) {
            log.info("PDFBox 提取成功，文件: {}, 字符数: {}", filename, pr.text.length());
            if (pr.isImagePdf) {
                warnings.add("PDF「" + filename + "」可能为扫描件，文字提取可能不完整。");
            } else if (pr.garbled) {
                warnings.add("PDF「" + filename + "」存在部分乱码，已尝试识别。");
            }
            return pr.text;
        }

        String warn = "PDF「" + filename + "」文字提取失败，请将简历内容复制粘贴到文本框中。";
        log.error(warn);
        throw new IllegalStateException(warn);
    }

    private String callAiForOptimization(String resumeText, String targetPosition, String jdText, int rounds) {
        String jd = (jdText == null || jdText.isBlank()) ? "（未提供职位描述）" : jdText;
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy年M月d日"));

        String prompt = RESUME_OPTIMIZATION_PROMPT_TEMPLATE
                .replace("{{resumeText}}", truncate(resumeText, MAX_RESUME_CHARS))
                .replace("{{targetPosition}}", targetPosition)
                .replace("{{jdText}}", truncate(jd, MAX_JD_CHARS))
                .replace("{{discussionRounds}}", String.valueOf(rounds))
                .replace("{{currentDate}}", currentDate);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String response = sendChatRequest(prompt);
                String jsonStr = extractJsonFromResponse(response);
                validateJson(jsonStr);
                return jsonStr;
            } catch (Exception e) {
                log.warn("Attempt {} failed: {}", attempt + 1, e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 返回了格式异常的响应，请重试一次。");
                }
            }
        }
        throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "简历优化分析失败");
    }

    private String sendChatRequest(String prompt) throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", dashScopeProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 12000);
        requestBody.put("temperature", 0.3);

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + dashScopeProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API call failed: " + response.code());
            }
            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }
            throw new IOException("Invalid response format");
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "\n\n[内容已截断，仅分析前" + max + "字]";
    }

    private String extractJsonFromResponse(String response) {
        if (response == null || response.isBlank()) {
            return "{}";
        }
        String text = response.trim();
        text = text.replaceAll("(?i)^\\s*```\\w*\\s*\\n?", "");
        text = text.replaceAll("(?i)\\n?```\\s*$", "");
        text = text.trim();

        int jsonStart = text.indexOf('{');
        if (jsonStart == -1) {
            jsonStart = text.indexOf('[');
        }
        if (jsonStart == -1) {
            return "{}";
        }

        String jsonCandidate = text.substring(jsonStart);
        jsonCandidate = jsonCandidate.replaceFirst("(?i)^([\"']?以下是?[\"']?\\s*[,，：:]?\\s*)+", "");
        return jsonCandidate;
    }

    private void validateJson(String json) throws com.fasterxml.jackson.core.JsonProcessingException {
        objectMapper.readTree(json);
    }

    private ResumeOptimizationResponse parseAiResponse(String response) {
        try {
            String jsonStr = extractJsonFromResponse(response);
            String sanitized = sanitizeForJson(jsonStr);
            return objectMapper.readValue(sanitized, ResumeOptimizationResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 响应解析失败");
        }
    }

    private String sanitizeForJson(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        cleaned = cleaned.replaceAll(",(\\s*[\\]\\}])", "$1");
        cleaned = cleaned.replaceAll("\\\\([^\"\\\\/bfnrtu]|u(?!\\{)|$)", "\\\\$1");
        return cleaned;
    }
}
