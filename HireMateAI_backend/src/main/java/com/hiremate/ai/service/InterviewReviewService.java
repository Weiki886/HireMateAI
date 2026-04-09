package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.response.ReviewReportResponse;
import com.hiremate.ai.entity.*;
import com.hiremate.ai.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewReviewService {

    private final InterviewReviewReportMapper reportMapper;
    private final InterviewAnswerReviewMapper answerReviewMapper;
    private final InterviewSessionMapper sessionMapper;
    private final InterviewMessageMapper messageMapper;
    private final AIChatService aiChatService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public ReviewReportResponse generateReviewReport(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        // Check if report already exists
        InterviewReviewReport existing = reportMapper.selectOne(
                new LambdaQueryWrapper<InterviewReviewReport>()
                        .eq(InterviewReviewReport::getSessionId, sessionId));
        if (existing != null && "COMPLETED".equals(existing.getStatus())) {
            return buildReportResponse(existing, session);
        }

        // Create or update report record
        InterviewReviewReport report;
        if (existing != null) {
            report = existing;
        } else {
            report = InterviewReviewReport.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .status("GENERATING")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            reportMapper.insert(report);
        }

        try {
            // Get all messages in the session
            List<InterviewMessage> messages = messageMapper.selectList(
                    new LambdaQueryWrapper<InterviewMessage>()
                            .eq(InterviewMessage::getSessionId, sessionId)
                            .orderByAsc(InterviewMessage::getCreatedAt));

            if (messages.isEmpty()) {
                report.setStatus("FAILED");
                report.setOverallSummary("该面试会话没有任何消息，无法生成复盘报告。");
                reportMapper.updateById(report);
                return buildReportResponse(report, session);
            }

            // Build review prompt for AI
            String prompt = buildReviewPrompt(session, messages);
            List<AIChatService.ChatMessage> history = Collections.emptyList();
            String aiResponse = aiChatService.chat(history, prompt,
                    session.getJobPosition(), session.getInterviewType(), null);

            // Parse AI response and extract structured data
            Map<String, Object> reviewData = parseReviewResponse(aiResponse);

            // Update report with scores and summary
            if (reviewData.containsKey("totalScore")) {
                report.setTotalScore(new BigDecimal(reviewData.get("totalScore").toString()));
            }
            if (reviewData.containsKey("professionalScore")) {
                report.setProfessionalScore(new BigDecimal(reviewData.get("professionalScore").toString()));
            }
            if (reviewData.containsKey("expressionScore")) {
                report.setExpressionScore(new BigDecimal(reviewData.get("expressionScore").toString()));
            }
            if (reviewData.containsKey("logicScore")) {
                report.setLogicScore(new BigDecimal(reviewData.get("logicScore").toString()));
            }
            if (reviewData.containsKey("confidenceScore")) {
                report.setConfidenceScore(new BigDecimal(reviewData.get("confidenceScore").toString()));
            }
            if (reviewData.containsKey("overallSummary")) {
                report.setOverallSummary(reviewData.get("overallSummary").toString());
            }
            if (reviewData.containsKey("improvementSuggestions")) {
                Object suggestions = reviewData.get("improvementSuggestions");
                report.setImprovementSuggestions(objectMapper.writeValueAsString(suggestions));
            }

            report.setStatus("COMPLETED");
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(report);

            // Parse and save individual answer reviews
            if (reviewData.containsKey("answerReviews")) {
                Object answerReviewsObj = reviewData.get("answerReviews");
                List<Map<String, Object>> answerReviews = getList(answerReviewsObj);
                List<InterviewMessage> userMessages = messages.stream()
                        .filter(m -> "user".equals(m.getRole()))
                        .collect(Collectors.toList());

                for (int i = 0; i < Math.min(answerReviews.size(), userMessages.size()); i++) {
                    Map<String, Object> ar = answerReviews.get(i);
                    InterviewMessage userMsg = userMessages.get(i);

                    InterviewAnswerReview answerReview = InterviewAnswerReview.builder()
                            .reportId(report.getId())
                            .messageId(userMsg.getId())
                            .questionSummary(getString(ar, "questionSummary"))
                            .answerQualityScore(getBigDecimal(ar, "answerQualityScore"))
                            .strengths(objectMapper.writeValueAsString(getList(ar, "strengths")))
                            .weaknesses(objectMapper.writeValueAsString(getList(ar, "weaknesses")))
                            .improvementTips(getString(ar, "improvementTips"))
                            .modelAnswer(getString(ar, "modelAnswer"))
                            .dimensionType(getString(ar, "dimensionType"))
                            .createdAt(LocalDateTime.now())
                            .build();
                    answerReviewMapper.insert(answerReview);
                }
            }

            return buildReportResponse(report, session);

        } catch (Exception e) {
            log.error("Failed to generate review report for session {}: {}", sessionId, e.getMessage(), e);
            report.setStatus("FAILED");
            report.setOverallSummary("生成复盘报告时发生错误：" + e.getMessage());
            reportMapper.updateById(report);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "生成复盘报告失败：" + e.getMessage());
        }
    }

    public ReviewReportResponse getReport(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        InterviewReviewReport report = reportMapper.selectOne(
                new LambdaQueryWrapper<InterviewReviewReport>()
                        .eq(InterviewReviewReport::getSessionId, sessionId));

        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "复盘报告不存在");
        }

        return buildReportResponse(report, session);
    }

    public ReviewReportResponse getReportDetail(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        InterviewReviewReport report = reportMapper.selectOne(
                new LambdaQueryWrapper<InterviewReviewReport>()
                        .eq(InterviewReviewReport::getSessionId, sessionId));

        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "复盘报告不存在");
        }

        List<InterviewAnswerReview> answerReviews = answerReviewMapper.selectList(
                new LambdaQueryWrapper<InterviewAnswerReview>()
                        .eq(InterviewAnswerReview::getReportId, report.getId())
                        .orderByAsc(InterviewAnswerReview::getCreatedAt));

        // Get user answers
        List<InterviewMessage> userMessages = messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .eq(InterviewMessage::getRole, "user")
                        .orderByAsc(InterviewMessage::getCreatedAt));

        Map<Long, String> messageContentMap = userMessages.stream()
                .collect(Collectors.toMap(InterviewMessage::getId, InterviewMessage::getContent));

        List<ReviewReportResponse.ReviewAnswerResponse> answerResponseList = answerReviews.stream()
                .map(ar -> {
                    List<String> strengths = parseJsonArray(ar.getStrengths());
                    List<String> weaknesses = parseJsonArray(ar.getWeaknesses());
                    return ReviewReportResponse.ReviewAnswerResponse.builder()
                            .id(ar.getId())
                            .messageId(ar.getMessageId())
                            .questionSummary(ar.getQuestionSummary())
                            .userAnswer(messageContentMap.getOrDefault(ar.getMessageId(), ""))
                            .answerQualityScore(ar.getAnswerQualityScore())
                            .strengths(strengths)
                            .weaknesses(weaknesses)
                            .improvementTips(ar.getImprovementTips())
                            .modelAnswer(ar.getModelAnswer())
                            .dimensionType(ar.getDimensionType())
                            .build();
                })
                .collect(Collectors.toList());

        return buildReportResponse(report, session, answerResponseList);
    }

    private String buildReviewPrompt(InterviewSession session, List<InterviewMessage> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的面试复盘分析师。请根据以下面试对话内容，生成一份详细的复盘报告。\n\n");
        sb.append("【面试信息】\n");
        sb.append("目标岗位：").append(session.getJobPosition() != null ? session.getJobPosition() : "未指定").append("\n");
        sb.append("面试类型：").append(session.getInterviewType() != null ? session.getInterviewType() : "综合面试").append("\n\n");
        sb.append("【面试对话记录】\n");

        for (InterviewMessage msg : messages) {
            String role = "user".equals(msg.getRole()) ? "候选人" : "面试官";
            sb.append(role).append("：").append(msg.getContent()).append("\n\n");
        }

        sb.append("\n请根据以上对话内容，生成一份JSON格式的复盘报告，包含以下字段：\n");
        sb.append("{\n");
        sb.append("  \"totalScore\": 综合总分(0-100的整数),\n");
        sb.append("  \"professionalScore\": 专业能力评分(0-100),\n");
        sb.append("  \"expressionScore\": 表达能力评分(0-100),\n");
        sb.append("  \"logicScore\": 逻辑思维评分(0-100),\n");
        sb.append("  \"confidenceScore\": 自信心评分(0-100),\n");
        sb.append("  \"overallSummary\": 总体复盘总结(一段话),\n");
        sb.append("  \"improvementSuggestions\": [改进建议数组，每个建议为一段话],\n");
        sb.append("  \"answerReviews\": [\n");
        sb.append("    {\n");
        sb.append("      \"questionSummary\": \"该回答对应的问题摘要\",\n");
        sb.append("      \"answerQualityScore\": 回答质量评分(0-100),\n");
        sb.append("      \"strengths\": [优点数组],\n");
        sb.append("      \"weaknesses\": [缺点/不足数组],\n");
        sb.append("      \"improvementTips\": 改进建议,\n");
        sb.append("      \"modelAnswer\": 参考示范回答,\n");
        sb.append("      \"dimensionType\": \"考察维度\",\n");
        sb.append("    }\n");
        sb.append("  ]\n");
        sb.append("}\n\n");
        sb.append("注意：answerReviews数组应对应候选人的每个回答。请直接返回JSON，不要添加任何前缀说明文字。");

        return sb.toString();
    }

    private Map<String, Object> parseReviewResponse(String aiResponse) {
        try {
            String json = extractJson(aiResponse);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse AI review response as JSON, trying fallback: {}", e.getMessage());
            return parseFallbackReview(aiResponse);
        }
    }

    private String extractJson(String text) {
        if (text == null || text.isBlank()) {
            return "{}";
        }
        text = text.trim();
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        }
        return "{}";
    }

    private Map<String, Object> parseFallbackReview(String text) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", "70");
        result.put("professionalScore", "70");
        result.put("expressionScore", "70");
        result.put("logicScore", "70");
        result.put("confidenceScore", "70");
        result.put("overallSummary", text.length() > 50 ? text.substring(0, 200) : text);
        result.put("improvementSuggestions", Collections.singletonList("建议持续练习面试技巧"));
        result.put("answerReviews", Collections.emptyList());
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getList(Object obj) {
        if (obj instanceof List) {
            return (List<T>) obj;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<String> getList(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof List) {
            return (List<String>) val;
        }
        return Collections.emptyList();
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return BigDecimal.ZERO;
        try {
            if (val instanceof Number) {
                return new BigDecimal(val.toString());
            }
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private ReviewReportResponse buildReportResponse(InterviewReviewReport report, InterviewSession session) {
        return buildReportResponse(report, session, Collections.emptyList());
    }

    private ReviewReportResponse buildReportResponse(InterviewReviewReport report, InterviewSession session,
            List<ReviewReportResponse.ReviewAnswerResponse> answers) {
        List<String> suggestions = parseJsonArray(report.getImprovementSuggestions());
        return ReviewReportResponse.builder()
                .id(report.getId())
                .sessionId(report.getSessionId())
                .jobPosition(session.getJobPosition())
                .interviewType(session.getInterviewType())
                .totalScore(report.getTotalScore())
                .professionalScore(report.getProfessionalScore())
                .expressionScore(report.getExpressionScore())
                .logicScore(report.getLogicScore())
                .confidenceScore(report.getConfidenceScore())
                .overallSummary(report.getOverallSummary())
                .improvementSuggestions(suggestions)
                .status(report.getStatus())
                .createdAt(report.getCreatedAt() != null ? report.getCreatedAt().format(DATE_FORMATTER) : null)
                .answers(answers)
                .build();
    }
}
