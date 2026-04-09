package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.response.MessageResponse;
import com.hiremate.ai.dto.response.PageResponse;
import com.hiremate.ai.dto.response.SessionDetailResponse;
import com.hiremate.ai.dto.response.SessionResponse;
import com.hiremate.ai.entity.InterviewMessage;
import com.hiremate.ai.entity.InterviewSession;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.InterviewMessageMapper;
import com.hiremate.ai.mapper.InterviewSessionMapper;
import com.hiremate.ai.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewMessageMapper messageMapper;
    private final UserMapper userMapper;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PageResponse<SessionResponse> getSessionList(Long userId, int page, int size) {
        Page<InterviewSession> pageResult = sessionMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .orderByDesc(InterviewSession::getCreatedAt)
        );

        List<InterviewSession> sessions = pageResult.getRecords();
        List<SessionResponse> responses = new ArrayList<>();

        for (InterviewSession session : sessions) {
            int messageCount = messageMapper.selectCount(
                    new LambdaQueryWrapper<InterviewMessage>()
                            .eq(InterviewMessage::getSessionId, session.getId())
            ).intValue();
            responses.add(SessionResponse.builder()
                    .sessionId(session.getId())
                    .jobPosition(session.getJobPosition())
                    .interviewType(session.getInterviewType())
                    .status(session.getStatus())
                    .createdAt(session.getCreatedAt())
                    .updatedAt(session.getUpdatedAt())
                    .build());
        }

        return PageResponse.<SessionResponse>builder()
                .records(responses)
                .total(pageResult.getTotal())
                .page((long) page)
                .size((long) size)
                .build();
    }

    public SessionDetailResponse getSessionDetail(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        List<InterviewMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getCreatedAt)
        );

        List<MessageResponse> msgResponses = messages.stream()
                .map(msg -> MessageResponse.builder()
                        .id(msg.getId())
                        .sessionId(msg.getSessionId())
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SessionDetailResponse.builder()
                .sessionId(session.getId())
                .jobPosition(session.getJobPosition())
                .interviewType(session.getInterviewType())
                .resumeText(session.getResumeText())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt().format(DATE_FORMATTER))
                .updatedAt(session.getUpdatedAt().format(DATE_FORMATTER))
                .messages(msgResponses)
                .build();
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        // Delete messages first
        messageMapper.delete(new LambdaQueryWrapper<InterviewMessage>()
                .eq(InterviewMessage::getSessionId, sessionId));

        // Delete session
        sessionMapper.deleteById(sessionId);
        log.info("Deleted interview session: {} and its messages", sessionId);
    }
}
