package com.hiremate.ai.service;

import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.config.DashScopeProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    private final DashScopeProperties dashScopeProperties;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个专业、经验丰富的AI面试官，正在帮助候选人准备面试。

            【面试信息】
            - 目标岗位：%s
            - 面试类型：%s
            - 候选人简历：%s

            【面试要求】
            1. 先进行简短的自我介绍和环境说明（如"你好，欢迎来到AI模拟面试，我是今天的面试官..."）
            2. 根据简历（如有）和岗位信息，提出针对性的问题
            3. 问题应该由浅入深，符合真实面试场景
            4. 对用户的回答给予简短点评（1-2句话），然后继续追问或提出下一个问题
            5. 注意控制问题数量，每轮对话不超过2-3个问题
            6. 当面试接近尾声时（通常5-8轮后），给出建设性的总结和建议
            7. 保持专业、友好、鼓励的面试风格
            8. 如果用户简历为空，使用该岗位最常见的面试题库进行提问

            现在开始面试：
            """;

    public String buildSystemPrompt(String jobPosition, String interviewType, String resumeText) {
        String resume = (resumeText == null || resumeText.isBlank()) ? "（未提供简历）" : resumeText;
        return String.format(SYSTEM_PROMPT_TEMPLATE, jobPosition, interviewType, resume);
    }

    public interface StreamingCallback {
        void onChunk(String content);
        void onComplete(String fullContent);
        void onError(Throwable t);
    }

    public void chatStream(List<ChatMessage> history, String userMessage, String jobPosition,
                          String interviewType, String resumeText, StreamingCallback callback) {
        String systemPrompt = buildSystemPrompt(jobPosition, interviewType, resumeText);

        try {
            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of("role", "system", "content", systemPrompt));

            for (ChatMessage msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }

            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", dashScopeProperties.getModel());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2000);
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", true);

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

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("Stream API call failed: {}", e.getMessage());
                    callback.onError(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        log.error("Stream API error: {} - {}", response.code(), errorBody);
                        callback.onError(new IOException("API call failed: " + errorBody));
                        return;
                    }

                    StringBuilder fullContent = new StringBuilder();
                    MediaType contentType = response.body().contentType();
                    assert contentType != null;

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data:") && !line.contains("[DONE]")) {
                                String jsonStr = line.substring(5).trim();
                                if (!jsonStr.isEmpty()) {
                                    JsonNode node = objectMapper.readTree(jsonStr);
                                    if (node.has("choices") && node.get("choices").isArray()) {
                                        JsonNode delta = node.get("choices").get(0).get("delta");
                                        if (delta != null && delta.has("content") && !delta.get("content").isNull()) {
                                            JsonNode contentNode = delta.get("content");
                                            if (!contentNode.isTextual()) {
                                                continue;
                                            }
                                            String content = contentNode.asText();
                                            if (content.isEmpty()) {
                                                continue;
                                            }
                                            fullContent.append(content);
                                            callback.onChunk(content);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    callback.onComplete(fullContent.toString());
                }
            });

        } catch (IOException e) {
            log.error("AI service stream error: {}", e.getMessage(), e);
            callback.onError(e);
        }
    }

    public String chat(List<ChatMessage> history, String userMessage, String jobPosition,
                      String interviewType, String resumeText) {
        StringBuilder fullResponse = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);

        chatStream(history, userMessage, jobPosition, interviewType, resumeText,
            new StreamingCallback() {
                @Override
                public void onChunk(String content) {
                    fullResponse.append(content);
                }

                @Override
                public void onComplete(String fullContent) {
                    fullResponse.setLength(0);
                    fullResponse.append(fullContent);
                    latch.countDown();
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                }
            });

        try {
            if (!latch.await(120, TimeUnit.SECONDS)) {
                log.warn("Chat request timeout after 120 seconds");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return fullResponse.toString();
    }

    /**
     * 流式调用 AI 服务，实时推送 SSE 事件
     */
    public Flux<String> chatStreamFlux(List<ChatMessage> history, String userMessage,
                                       String jobPosition, String interviewType, String resumeText) {
        return Flux.create(emitter -> {
            chatStream(history, userMessage, jobPosition, interviewType, resumeText,
                new StreamingCallback() {
                    @Override
                    public void onChunk(String content) {
                        if (!emitter.isCancelled()) {
                            emitter.next(content);
                        }
                    }

                    @Override
                    public void onComplete(String fullContent) {
                        if (!emitter.isCancelled()) {
                            emitter.next("[DONE]" + fullContent);
                            emitter.complete();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (!emitter.isCancelled()) {
                            emitter.error(t);
                        }
                    }
                });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
