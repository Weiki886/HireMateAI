package com.hiremate.ai.controller;

import com.hiremate.ai.ai.groupchat.GroupChatService;
import com.hiremate.ai.ai.groupchat.GroupChatSession;
import com.hiremate.ai.common.Result;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.security.CurrentUserAccessor;
import com.hiremate.ai.service.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-chat")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final CurrentUserAccessor currentUserAccessor;
    private final PdfTextExtractor pdfTextExtractor;

    /**
     * 创建群聊会话：multipart/form-data，可选上传简历 PDF（服务端提取正文后注入专家上下文）。
     */
    @PostMapping(value = "/session", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result> createSession(
            @RequestParam(value = "targetPosition", required = false) String targetPosition,
            @RequestParam(value = "jdText", required = false) String jdText,
            @RequestPart(value = "resumePdf", required = false) MultipartFile resumePdf) {
        Long userId = currentUserAccessor.getCurrentUserId();
        String resumeText = null;
        if (resumePdf != null && !resumePdf.isEmpty()) {
            try {
                PdfTextExtractor.ExtractResult er = pdfTextExtractor.extractText(resumePdf);
                if (er.isImagePdf || er.text == null || er.text.isBlank()) {
                    return ResponseEntity.ok(Result.error(ResultCode.BAD_REQUEST,
                            "无法从该 PDF 提取文字，请使用可复制文字的 PDF（非纯扫描件）"));
                }
                resumeText = pdfTextExtractor.cleanText(er.text);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(Result.error(ResultCode.BAD_REQUEST, e.getMessage()));
            } catch (RuntimeException e) {
                return ResponseEntity.ok(Result.error(ResultCode.BAD_REQUEST, "PDF 解析失败，请稍后重试"));
            }
        }

        GroupChatSession session = groupChatService.getOrCreateSession(userId, targetPosition, resumeText, jdText);
        return ResponseEntity.ok(Result.success("群聊会话已创建", Map.of(
                "sessionId", session.getUserId(),
                "targetPosition", session.getTargetPosition()
        )));
    }

    @PostMapping("/message")
    public ResponseEntity<Result> sendMessage(@RequestBody Map<String, String> request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        String userMessage = request.get("message");

        List<GroupChatService.RoleMessage> responses = groupChatService.sendMessage(userId, userMessage);
        return ResponseEntity.ok(Result.success(responses));
    }

    @PostMapping(value = "/message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(@RequestBody Map<String, String> request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        String userMessage = request.get("message");
        return groupChatService.streamMessage(userId, userMessage);
    }

    @GetMapping("/history")
    public ResponseEntity<Result> getHistory() {
        Long userId = currentUserAccessor.getCurrentUserId();
        List<GroupChatSession.ChatMessage> history = groupChatService.getHistory(userId);
        return ResponseEntity.ok(Result.success(history));
    }

    @DeleteMapping("/session")
    public ResponseEntity<Result> destroySession() {
        Long userId = currentUserAccessor.getCurrentUserId();
        groupChatService.destroySession(userId);
        return ResponseEntity.ok(Result.success("群聊会话已销毁", null));
    }
}
