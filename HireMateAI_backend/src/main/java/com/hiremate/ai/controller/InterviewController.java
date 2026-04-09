package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.request.CreateSessionRequest;
import com.hiremate.ai.dto.request.SendMessageRequest;
import com.hiremate.ai.dto.response.SessionResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import com.hiremate.ai.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Tag(name = "面试接口", description = "AI模拟面试相关接口")
public class InterviewController {

    private final InterviewService interviewService;
    private final UserMapper userMapper;

    @PostMapping("/session")
    @Operation(summary = "创建面试会话")
    public Result<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        Long userId = getCurrentUserId();
        SessionResponse response = interviewService.createSession(userId, request);
        return Result.success("会话创建成功", response);
    }

    @GetMapping("/session/{id}")
    @Operation(summary = "获取会话信息")
    public Result<SessionResponse> getSession(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        SessionResponse response = interviewService.getSession(userId, id);
        return Result.success(response);
    }

    @PostMapping(value = "/session/{id}/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息并流式获取AI回复")
    public SseEmitter sendMessageStream(@PathVariable Long id,
                                        @Valid @RequestBody SendMessageRequest request) {
        Long userId = getCurrentUserId();
        SseEmitter emitter = new SseEmitter(120_000L);

        interviewService.sendMessageSse(userId, id, request, emitter);

        emitter.onCompletion(() -> log.info("SSE completed for session {}", id));
        emitter.onTimeout(() -> log.warn("SSE timeout for session {}", id));
        emitter.onError(e -> log.error("SSE error for session {}: {}", id, e.getMessage()));

        return emitter;
    }

    @DeleteMapping("/session/{id}")
    @Operation(summary = "关闭面试会话")
    public Result<Void> closeSession(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        interviewService.closeSession(userId, id);
        return Result.success("会话已关闭", null);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        return user.getId();
    }
}
