package com.hiremate.ai.ai.groupchat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.ai.config.DashScopeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final DashScopeProperties dashScopeProperties;
    private final ObjectMapper objectMapper;
    private final GroupChatPromptService promptService;

    private static final List<String> ROLES = List.of("HR总监", "技术专家", "职业规划师");

    private final Map<Long, GroupChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, List<String>>> roleHistories = new ConcurrentHashMap<>();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ExecutorService sharedExecutor = Executors.newFixedThreadPool(3);

    public GroupChatSession getOrCreateSession(Long userId, String targetPosition,
                                               String originalResumeText, String jdText) {
        sessions.put(userId, new GroupChatSession(userId, targetPosition, originalResumeText, jdText));
        roleHistories.put(userId, new ConcurrentHashMap<>());
        for (String role : ROLES) {
            roleHistories.get(userId).put(role, new ArrayList<>());
        }
        return sessions.get(userId);
    }

    public GroupChatSession getSession(Long userId) {
        GroupChatSession s = sessions.get(userId);
        if (s == null) {
            throw new IllegalStateException("请先「进入群聊」建立会话");
        }
        return s;
    }

    public List<RoleMessage> sendMessage(Long userId, String userMessage) {
        GroupChatSession session = getSession(userId);

        session.addMessage(new GroupChatSession.ChatMessage("USER", userMessage, LocalDateTime.now()));
        addUserMessageToAllMemories(userId, userMessage);

        ExecutorService executor = sharedExecutor;
        CompletableFuture<RoleMessage> f1 = CompletableFuture.supplyAsync(
                () -> callRole(userId, session, "HR总监", userMessage), executor);
        CompletableFuture<RoleMessage> f2 = CompletableFuture.supplyAsync(
                () -> callRole(userId, session, "技术专家", userMessage), executor);
        CompletableFuture<RoleMessage> f3 = CompletableFuture.supplyAsync(
                () -> callRole(userId, session, "职业规划师", userMessage), executor);

        CompletableFuture.allOf(f1, f2, f3).join();

        List<RoleMessage> results = new ArrayList<>();
        results.add(f1.join());
        results.add(f2.join());
        results.add(f3.join());

        for (RoleMessage rm : results) {
            if (!rm.isError()) {
                session.addMessage(new GroupChatSession.ChatMessage(
                        rm.role(), rm.content(), LocalDateTime.now()));
            }
        }

        return results;
    }

    private static final int MAX_ROLE_HISTORY_SIZE = 20;

    private void addUserMessageToAllMemories(Long userId, String message) {
        Map<String, List<String>> histories = roleHistories.get(userId);
        if (histories == null) return;
        for (List<String> history : histories.values()) {
            history.add("用户：" + message);
            if (history.size() > MAX_ROLE_HISTORY_SIZE) {
                history.remove(0);
            }
        }
    }

    private void addRoleMessageToMemory(Long userId, String role, String message) {
        Map<String, List<String>> histories = roleHistories.get(userId);
        if (histories == null) return;
        List<String> history = histories.get(role);
        if (history != null) {
            history.add(role + "：" + message);
            if (history.size() > MAX_ROLE_HISTORY_SIZE) {
                history.remove(0);
            }
        }
    }

    private RoleMessage callRole(Long userId, GroupChatSession session, String role, String userMessage) {
        String response;
        try {
            response = callWithRetry(() -> {
                try {
                    String systemPrompt = buildSystemPrompt(session, role);
                    String userPrompt = promptService.buildUserPrompt(userMessage, "");
                    return callAiSync(systemPrompt, userPrompt);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, role);
        } catch (Exception e) {
            log.error("群聊角色 {} 调用失败: {}", role, e.getMessage());
            response = getFallbackContent(role);
        }

        if (response == null || response.isBlank()) {
            response = getFallbackContent(role);
        }

        addRoleMessageToMemory(userId, role, response);
        return new RoleMessage(role, response, getRoleRound(session, role), false);
    }

    private String callAiSync(String systemPrompt, String userPrompt) throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", dashScopeProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.5);

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

    private String buildSystemPrompt(GroupChatSession session, String role) {
        String chatHistory = promptService.extractRoleHistory(session, role);

        return switch (role) {
            case "HR总监" -> promptService.getHrSystemPrompt(
                    session.getTargetPosition(), session.getOriginalResumeText(),
                    session.getJdText(), chatHistory);
            case "技术专家" -> promptService.getTechSystemPrompt(
                    session.getTargetPosition(), session.getOriginalResumeText(),
                    session.getJdText(), chatHistory);
            case "职业规划师" -> promptService.getCareerSystemPrompt(
                    session.getTargetPosition(), session.getOriginalResumeText(),
                    session.getJdText(), chatHistory);
            default -> throw new IllegalArgumentException("未知角色: " + role);
        };
    }

    private int getRoleRound(GroupChatSession session, String role) {
        return switch (role) {
            case "HR总监" -> session.getHrRound();
            case "技术专家" -> session.getTechRound();
            case "职业规划师" -> session.getCareerRound();
            default -> 0;
        };
    }

    public void destroySession(Long userId) {
        sessions.remove(userId);
        roleHistories.remove(userId);
        log.info("群聊会话已销毁: userId={}", userId);
    }

    public List<GroupChatSession.ChatMessage> getHistory(Long userId) {
        GroupChatSession s = sessions.get(userId);
        if (s == null) {
            return List.of();
        }
        return s.getHistory();
    }

    private String callWithRetry(Supplier<String> action, String role) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                if (isNetworkError(e) && attempt < MAX_RETRY_ATTEMPTS) {
                    log.warn("群聊角色[{}] - 第 {} 次尝试失败 (网络错误): {}，{}ms 后重试",
                            role, attempt, e.getMessage(), RETRY_DELAY_MS);
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return getFallbackContent(role);
                    }
                } else {
                    log.error("群聊角色[{}] - 调用失败: {}", role, e.getMessage());
                    return getFallbackContent(role);
                }
            }
        }
        return getFallbackContent(role);
    }

    private boolean isNetworkError(Exception e) {
        if (e == null) return false;
        String msg = e.getMessage() != null ? e.getMessage() : "";
        return msg.contains("Connection reset")
                || msg.contains("Connection refused")
                || msg.contains("ConnectException")
                || msg.contains("SocketException")
                || msg.contains("SocketTimeoutException")
                || e instanceof SocketException
                || e instanceof java.net.ConnectException;
    }

    private String getFallbackContent(String role) {
        return switch (role) {
            case "HR总监" -> "抱歉，暂时无法获取 HR 视角的建议，请稍后重试。";
            case "技术专家" -> "抱歉，暂时无法获取技术视角的分析，请稍后重试。";
            case "职业规划师" -> "抱歉，暂时无法获取职业规划建议，请稍后重试。";
            default -> "系统繁忙，请稍后重试。";
        };
    }

    public static final long STREAM_TIMEOUT_SECONDS = 300L;

    public SseEmitter streamMessage(Long userId, String userMessage) {
        GroupChatSession session = getSession(userId);
        session.addMessage(new GroupChatSession.ChatMessage("USER", userMessage, LocalDateTime.now()));
        addUserMessageToAllMemories(userId, userMessage);

        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_SECONDS * 1000);

        String[] roleOrder = {"HR总监", "技术专家", "职业规划师"};
        Map<String, StringBuilder> roleBuffers = new ConcurrentHashMap<>();
        for (String role : roleOrder) {
            roleBuffers.put(role, new StringBuilder());
        }
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        String[] finalResponses = {"", "", ""};

        java.util.function.BiConsumer<String, String> sendChunk = (String role, String chunk) -> {
            try {
                String json = objectMapper.writeValueAsString(Map.of(
                        "type", "chunk",
                        "role", role,
                        "content", chunk
                ));
                emitter.send(SseEmitter.event().name("chunk").data(json));
            } catch (IOException e) {
                log.warn("SSE send chunk failed: {}", e.getMessage());
            }
        };

        Runnable checkComplete = () -> {
            int total = completedCount.get() + failedCount.get();
            if (total == 3) {
                try {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        results.add(Map.of(
                                "role", roleOrder[i],
                                "content", finalResponses[i],
                                "roundNum", getRoleRound(session, roleOrder[i]),
                                "isError", failedCount.get() > 0 && completedCount.get() <= i
                        ));
                    }
                    String json = objectMapper.writeValueAsString(Map.of(
                            "type", "done",
                            "results", results
                    ));
                    emitter.send(SseEmitter.event().name("done").data(json));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("SSE completion error: {}", e.getMessage());
                    emitter.completeWithError(e);
                }
            }
        };

        ExecutorService executor = sharedExecutor;
        for (int i = 0; i < 3; i++) {
            String role = roleOrder[i];
            int idx = i;
            CompletableFuture.runAsync(() -> {
                try {
                    String systemPrompt = buildSystemPrompt(session, role);
                    String response = callWithRetry(() -> {
                        try {
                            return callAiStreamSync(systemPrompt, userMessage,
                                    (String chunk) -> {
                                        roleBuffers.get(role).append(chunk);
                                        sendChunk.accept(role, roleBuffers.get(role).toString());
                                    });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }, role);
                    if (response == null || response.isBlank()) {
                        response = getFallbackContent(role);
                    }
                    finalResponses[idx] = response;
                    addRoleMessageToMemory(userId, role, response);
                    session.addMessage(new GroupChatSession.ChatMessage(role, response, LocalDateTime.now()));
                    completedCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("群聊角色 {} 调用失败: {}", role, e.getMessage());
                    finalResponses[idx] = getFallbackContent(role);
                    failedCount.incrementAndGet();
                    completedCount.incrementAndGet();
                }
                checkComplete.run();
            }, executor);
        }

        return emitter;
    }

    private String callAiStreamSync(String systemPrompt, String userMessage,
                                     java.util.function.Consumer<String> onChunk) throws IOException {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", dashScopeProperties.getModel());
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.5);
        requestBody.put("stream", true);

        RequestBody body = RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + dashScopeProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .build();

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder[] fullContent = new StringBuilder[]{new StringBuilder()};
        IOException[] thrown = new IOException[]{null};

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                thrown[0] = e;
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    thrown[0] = new IOException("API call failed: " + response.code());
                    latch.countDown();
                    return;
                }
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(response.body().byteStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:") && !line.contains("[DONE]")) {
                            String jsonStr = line.substring(5).trim();
                            if (!jsonStr.isEmpty()) {
                                JsonNode node = objectMapper.readTree(jsonStr);
                                if (node.has("choices") && node.get("choices").isArray()) {
                                    JsonNode delta = node.get("choices").get(0).get("delta");
                                    if (delta != null && delta.has("content") && !delta.get("content").isNull()
                                            && delta.get("content").isTextual()) {
                                        String content = delta.get("content").asText();
                                        if (!content.isEmpty()) {
                                            fullContent[0].append(content);
                                            onChunk.accept(content);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                latch.countDown();
            }
        });

        boolean ok;
        try {
            ok = latch.await(300, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Stream interrupted", e);
        }
        if (!ok || thrown[0] != null) {
            throw new IOException("Stream timed out or failed", thrown[0]);
        }
        return fullContent[0].toString();
    }

    public record RoleMessage(
            String role,
            String content,
            int roundNum,
            boolean isError
    ) {}
}
