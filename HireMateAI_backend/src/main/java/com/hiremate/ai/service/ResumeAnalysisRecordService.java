package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.response.ResumeAnalysisRecordResponse;
import com.hiremate.ai.entity.ResumeAnalysisRecord;
import com.hiremate.ai.mapper.ResumeAnalysisRecordMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisRecordService {

    private final ResumeAnalysisRecordMapper recordMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResumeAnalysisRecord saveRecord(Long userId, String targetPosition,
                                          String originalText, String originalPdfUrl,
                                          String jdText, Integer discussionRounds) {
        ResumeAnalysisRecord record = ResumeAnalysisRecord.builder()
                .userId(userId)
                .targetPosition(targetPosition)
                .originalText(originalText != null && originalText.length() > 2000
                        ? originalText.substring(0, 2000) : originalText)
                .originalPdfUrl(originalPdfUrl)
                .jdText(jdText)
                .discussionRounds(discussionRounds)
                .status("ANALYZING")
                .build();
        recordMapper.insert(record);
        return record;
    }

    @Transactional
    public void updateRecord(Long recordId, String analysisResultJson,
                             Integer overallScore, String status) {
        ResumeAnalysisRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分析记录不存在");
        }
        record.setAnalysisResultJson(analysisResultJson);
        record.setOverallScore(overallScore);
        record.setStatus(status);
        recordMapper.updateById(record);
    }

    public List<ResumeAnalysisRecordResponse> listByUser(Long userId) {
        List<ResumeAnalysisRecord> records = recordMapper.selectList(
                new LambdaQueryWrapper<ResumeAnalysisRecord>()
                        .eq(ResumeAnalysisRecord::getUserId, userId)
                        .orderByDesc(ResumeAnalysisRecord::getCreatedAt)
        );
        return records.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ResumeAnalysisRecordResponse getById(Long userId, Long recordId) {
        ResumeAnalysisRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分析记录不存在");
        }
        return toDetailResponse(record);
    }

    @Transactional
    public void deleteRecord(Long userId, Long recordId) {
        ResumeAnalysisRecord record = recordMapper.selectById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分析记录不存在");
        }
        recordMapper.deleteById(recordId);
    }

    private ResumeAnalysisRecordResponse toResponse(ResumeAnalysisRecord record) {
        return ResumeAnalysisRecordResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .targetPosition(record.getTargetPosition())
                .originalText(record.getOriginalText())
                .originalPdfUrl(record.getOriginalPdfUrl())
                .jdText(record.getJdText())
                .overallScore(record.getOverallScore())
                .discussionRounds(record.getDiscussionRounds())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private ResumeAnalysisRecordResponse toDetailResponse(ResumeAnalysisRecord record) {
        ResumeAnalysisRecordResponse resp = toResponse(record);
        if (record.getAnalysisResultJson() == null) {
            return resp;
        }
        try {
            JsonNode root = objectMapper.readTree(record.getAnalysisResultJson());

            JsonNode finalResult = root.path("finalResult");
            if (finalResult.isObject()) {
                resp.setOverallComment(finalResult.path("overallComment").asText(null));
                resp.setNextSteps(parseStringList(finalResult.path("nextSteps")));

                JsonNode moduleSuggestions = finalResult.path("moduleSuggestions");
                if (moduleSuggestions.isObject()) {
                    resp.setModuleSuggestions(ResumeAnalysisRecordResponse.ModuleSuggestionsResponse.builder()
                            .personalInfo(parseStringList(moduleSuggestions.path("personalInfo")))
                            .workExperience(parseStringList(moduleSuggestions.path("workExperience")))
                            .projectExperience(parseStringList(moduleSuggestions.path("projectExperience")))
                            .skills(parseStringList(moduleSuggestions.path("skills")))
                            .selfEvaluation(parseStringList(moduleSuggestions.path("selfEvaluation")))
                            .formatting(parseStringList(moduleSuggestions.path("formatting")))
                            .build());
                }
            }

            List<ResumeAnalysisRecordResponse.DiscussionRoundResponse> rounds = new ArrayList<>();
            JsonNode roundsNode = root.path("discussionRounds");
            if (roundsNode.isArray()) {
                for (JsonNode round : roundsNode) {
                    rounds.add(ResumeAnalysisRecordResponse.DiscussionRoundResponse.builder()
                            .roundNumber(round.path("roundNumber").asInt(0))
                            .aiRole(round.path("aiRole").asText(null))
                            .content(round.path("content").asText(null))
                            .keyPoints(parseStringList(round.path("keyPoints")))
                            .build());
                }
            }
            resp.setDiscussionRoundsParsed(rounds);
        } catch (Exception e) {
            log.warn("解析分析结果 JSON 失败: {}", e.getMessage());
        }
        return resp;
    }

    private List<String> parseStringList(JsonNode node) {
        if (!node.isArray()) return List.of();
        List<String> list = new ArrayList<>();
        node.forEach(n -> list.add(n.asText()));
        return list;
    }
}
