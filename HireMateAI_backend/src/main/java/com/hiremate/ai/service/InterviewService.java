package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.request.CreateSessionRequest;
import com.hiremate.ai.dto.request.SendMessageRequest;
import com.hiremate.ai.dto.response.MessageResponse;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final AIChatService aiChatService;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public SessionResponse createSession(Long userId, CreateSessionRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        InterviewSession session = InterviewSession.builder()
                .userId(userId)
                .jobPosition(request.getJobPosition())
                .interviewType(request.getInterviewType())
                .resumeText(request.getResumeText())
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sessionMapper.insert(session);
        log.info("Created interview session: {} for user: {}", session.getId(), userId);

        return SessionResponse.builder()
                .sessionId(session.getId())
                .jobPosition(session.getJobPosition())
                .interviewType(session.getInterviewType())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    public SessionResponse getSession(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        return SessionResponse.builder()
                .sessionId(session.getId())
                .jobPosition(session.getJobPosition())
                .interviewType(session.getInterviewType())
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }

    @Transactional
    public String sendMessageAndGetResponse(Long userId, Long sessionId, SendMessageRequest request) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }
        if ("closed".equals(session.getStatus())) {
            throw new BusinessException(ResultCode.SESSION_CLOSED);
        }

        // Save user message
        InterviewMessage userMsg = InterviewMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        messageMapper.insert(userMsg);

        // Get history messages (excluding current message)
        List<InterviewMessage> historyMsgs = messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getCreatedAt)
                        .last("LIMIT 40")
        );

        // Convert to AI service format
        List<AIChatService.ChatMessage> history = new ArrayList<>();
        for (InterviewMessage msg : historyMsgs) {
            // Skip the message we just inserted (it's the last one)
            if (msg.getId().equals(userMsg.getId())) {
                continue;
            }
            String role = "user".equals(msg.getRole()) ? "user" : "assistant";
            history.add(new AIChatService.ChatMessage(role, msg.getContent()));
        }

        // Call AI service
        String aiResponse = aiChatService.chat(history, request.getContent(),
                session.getJobPosition(), session.getInterviewType(), session.getResumeText());

        // Save AI response
        InterviewMessage aiMsg = InterviewMessage.builder()
                .sessionId(sessionId)
                .role("assistant")
                .content(aiResponse)
                .createdAt(LocalDateTime.now())
                .build();
        messageMapper.insert(aiMsg);

        // Update session
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        return aiResponse;
    }

    /**
     * 流式发送消息并实时推送 SSE 事件
     */
    @Transactional
    public void sendMessageSse(Long userId, Long sessionId, SendMessageRequest request,
                               SseEmitter emitter) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            try {
                emitter.send(SseEmitter.event().name("error").data("{\"error\":\"会话不存在\"}"));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return;
        }
        if (!session.getUserId().equals(userId)) {
            try {
                emitter.send(SseEmitter.event().name("error").data("{\"error\":\"无权限访问此会话\"}"));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return;
        }
        if ("closed".equals(session.getStatus())) {
            try {
                emitter.send(SseEmitter.event().name("error").data("{\"error\":\"会话已关闭\"}"));
            } catch (IOException e) {
                log.error("Failed to send error event", e);
            }
            emitter.complete();
            return;
        }

        InterviewMessage userMsg = InterviewMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        messageMapper.insert(userMsg);

        List<InterviewMessage> historyMsgs = messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getCreatedAt)
                        .last("LIMIT 40")
        );

        List<AIChatService.ChatMessage> history = new ArrayList<>();
        for (InterviewMessage msg : historyMsgs) {
            if (msg.getId().equals(userMsg.getId())) {
                continue;
            }
            String role = "user".equals(msg.getRole()) ? "user" : "assistant";
            history.add(new AIChatService.ChatMessage(role, msg.getContent()));
        }

        StringBuilder fullResponse = new StringBuilder();

        aiChatService.chatStream(history, request.getContent(),
                session.getJobPosition(), session.getInterviewType(), session.getResumeText(),
                new AIChatService.StreamingCallback() {
                    @Override
                    public void onChunk(String content) {
                        if (content == null || content.isEmpty()) {
                            return;
                        }
                        fullResponse.append(content);
                        try {
                            Map<String, String> payload = new HashMap<>(1);
                            payload.put("content", content);
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(payload, MediaType.APPLICATION_JSON));
                        } catch (IOException e) {
                            log.warn("Failed to send SSE chunk, closing emitter");
                            emitter.complete();
                        }
                    }

                    @Override
                    public void onComplete(String fullContent) {
                        InterviewMessage aiMsg = InterviewMessage.builder()
                                .sessionId(sessionId)
                                .role("assistant")
                                .content(fullContent)
                                .createdAt(LocalDateTime.now())
                                .build();
                        messageMapper.insert(aiMsg);

                        session.setUpdatedAt(LocalDateTime.now());
                        sessionMapper.updateById(session);

                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data(Map.of(), MediaType.APPLICATION_JSON));
                        } catch (IOException e) {
                            log.warn("Failed to send done event");
                        }
                        emitter.complete();
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("AI stream error for session {}: {}", sessionId, t.getMessage());
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("{\"error\":\"AI服务错误: " + escapeJson(t.getMessage()) + "\"}"));
                        } catch (IOException e) {
                            log.warn("Failed to send error event");
                        }
                        emitter.complete();
                    }
                });
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 流式发送消息并实时返回 AI 回复（Flux方式，仅内部使用）
     */
    @Transactional
    public Flux<String> sendMessageStream(Long userId, Long sessionId, SendMessageRequest request) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }
        if ("closed".equals(session.getStatus())) {
            throw new BusinessException(ResultCode.SESSION_CLOSED);
        }

        // Save user message
        InterviewMessage userMsg = InterviewMessage.builder()
                .sessionId(sessionId)
                .role("user")
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        messageMapper.insert(userMsg);

        // Get history messages (excluding current message)
        List<InterviewMessage> historyMsgs = messageMapper.selectList(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .orderByAsc(InterviewMessage::getCreatedAt)
                        .last("LIMIT 40")
        );

        List<AIChatService.ChatMessage> history = new ArrayList<>();
        for (InterviewMessage msg : historyMsgs) {
            if (msg.getId().equals(userMsg.getId())) {
                continue;
            }
            String role = "user".equals(msg.getRole()) ? "user" : "assistant";
            history.add(new AIChatService.ChatMessage(role, msg.getContent()));
        }

        // Use Flux to stream AI response
        AtomicReference<String> fullResponse = new AtomicReference<>("");

        Flux<String> stream = aiChatService.chatStreamFlux(history, request.getContent(),
                session.getJobPosition(), session.getInterviewType(), session.getResumeText());

        return stream.map(chunk -> {
            if (chunk.startsWith("[DONE]")) {
                String content = chunk.substring(5);
                fullResponse.set(content);
                return content;
            }
            fullResponse.set(fullResponse.get() + chunk);
            return chunk;
        }).doOnComplete(() -> {
            // Save complete AI response after stream finishes
            String finalResponse = fullResponse.get();
            InterviewMessage aiMsg = InterviewMessage.builder()
                    .sessionId(sessionId)
                    .role("assistant")
                    .content(finalResponse)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageMapper.insert(aiMsg);

            // Update session
            session.setUpdatedAt(LocalDateTime.now());
            sessionMapper.updateById(session);
        });
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

        List<MessageResponse> msgResponses = new ArrayList<>();
        for (InterviewMessage msg : messages) {
            msgResponses.add(MessageResponse.builder()
                    .id(msg.getId())
                    .sessionId(msg.getSessionId())
                    .role(msg.getRole())
                    .content(msg.getContent())
                    .createdAt(msg.getCreatedAt())
                    .build());
        }

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
    public void closeSession(Long userId, Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.SESSION_ACCESS_DENIED);
        }

        session.setStatus("closed");
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
        log.info("Closed interview session: {}", sessionId);
    }
}
