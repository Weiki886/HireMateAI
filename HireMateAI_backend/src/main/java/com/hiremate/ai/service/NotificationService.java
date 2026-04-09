package com.hiremate.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NotificationService {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L; // 30分钟

    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        userEmitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            log.debug("SSE 连接关闭: userId={}", userId);
            userEmitters.remove(userId);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE 连接超时: userId={}", userId);
            userEmitters.remove(userId);
        });
        emitter.onError(e -> {
            log.warn("SSE 连接异常: userId={}, error={}", userId, e.getMessage());
            userEmitters.remove(userId);
        });

        log.info("SSE 订阅成功: userId={}", userId);
        return emitter;
    }

    public void notify(Long userId, String eventName, Object data) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter == null) {
            log.debug("用户无活跃 SSE 连接，跳过通知: userId={}", userId);
            return;
        }
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
            emitter.send(SseEmitter.event().name(eventName).data(json));
            log.info("SSE 通知发送成功: userId={}, event={}", userId, eventName);
        } catch (IOException e) {
            log.warn("SSE 通知发送失败，将移除连接: userId={}, error={}", userId, e.getMessage());
            userEmitters.remove(userId);
        }
    }

    public void notifyResumeOptimized(Long userId, Long recordId, String targetPosition, String status, Object result) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("type", "RESUME_OPTIMIZED");
        payload.put("recordId", recordId);
        payload.put("targetPosition", targetPosition);
        payload.put("status", status);
        payload.put("result", result);
        payload.put("message", "COMPLETED".equals(status)
                ? "简历 AI 优化已完成，点击查看结果"
                : "简历 AI 优化失败，请重试");
        notify(userId, "notification", payload);
    }
}
