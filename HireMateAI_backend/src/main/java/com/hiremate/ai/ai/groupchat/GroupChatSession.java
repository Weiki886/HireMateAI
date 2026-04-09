package com.hiremate.ai.ai.groupchat;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
public class GroupChatSession {

    private final Long userId;
    private final String targetPosition;
    private final String originalResumeText;
    private final String jdText;
    private final LocalDateTime createdAt;
    private final List<ChatMessage> history;
    private int hrRound;
    private int techRound;
    private int careerRound;

    public GroupChatSession(Long userId, String targetPosition,
                            String originalResumeText, String jdText) {
        this.userId = userId;
        this.targetPosition = targetPosition;
        this.originalResumeText = originalResumeText;
        this.jdText = jdText;
        this.createdAt = LocalDateTime.now();
        this.history = new ArrayList<>();
        this.hrRound = 0;
        this.techRound = 0;
        this.careerRound = 0;
    }

    public void addMessage(ChatMessage message) {
        history.add(message);
        incrementRound(message.role());
    }

    private void incrementRound(String role) {
        switch (role) {
            case "HR总监" -> hrRound++;
            case "技术专家" -> techRound++;
            case "职业规划师" -> careerRound++;
        }
    }

    public List<ChatMessage> getHistory() {
        return new ArrayList<>(history);
    }

    public record ChatMessage(
            String role,
            String content,
            LocalDateTime timestamp
    ) {}
}
