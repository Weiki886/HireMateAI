package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.request.ResumeRequest;
import com.hiremate.ai.dto.response.ResumeAnalysisRecordResponse;
import com.hiremate.ai.dto.response.ResumeResponse;
import com.hiremate.ai.security.CurrentUserAccessor;
import com.hiremate.ai.service.NotificationService;
import com.hiremate.ai.service.ResumeAnalysisRecordService;
import com.hiremate.ai.service.ResumeOptimizationService;
import com.hiremate.ai.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeOptimizationService resumeOptimizationService;
    private final ResumeAnalysisRecordService recordService;
    private final NotificationService notificationService;
    private final CurrentUserAccessor currentUserAccessor;

    @PostMapping(value = "/optimize-suggestions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result> optimizeSuggestions(
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam("targetPosition") String targetPosition,
            @RequestParam(value = "jdText", required = false) String jdText,
            @RequestParam(value = "discussionRounds", required = false) Integer discussionRounds) {
        Long userId = currentUserAccessor.getCurrentUserId();
        return ResponseEntity.ok(Result.success("分析完成",
                resumeOptimizationService.optimizeSuggestions(userId, pdfFile, resumeText, targetPosition, jdText, discussionRounds)));
    }

    @PostMapping(value = "/optimize-async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result> optimizeAsync(
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam("targetPosition") String targetPosition,
            @RequestParam(value = "jdText", required = false) String jdText,
            @RequestParam(value = "discussionRounds", required = false) Integer discussionRounds) {
        Long userId = currentUserAccessor.getCurrentUserId();
        ResumeOptimizationService.OptimizationTaskInfo taskInfo = resumeOptimizationService.submitOptimizationTask(
                userId, pdfFile, resumeText, targetPosition, jdText, discussionRounds);
        return ResponseEntity.ok(Result.success("任务已提交，正在后台优化中", taskInfo));
    }

    @GetMapping(value = "/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeNotifications() {
        Long userId = currentUserAccessor.getCurrentUserId();
        return notificationService.subscribe(userId);
    }

    @PostMapping
    public ResponseEntity<Result> create(@RequestBody ResumeRequest.Create request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        ResumeResponse response = resumeService.createResume(userId, request);
        return ResponseEntity.ok(Result.success("简历创建成功", response));
    }

    /** 必须写在 /{id} 之前，否则路径 /list 会被当成 id */
    @GetMapping("/list")
    public ResponseEntity<Result> list() {
        Long userId = currentUserAccessor.getCurrentUserId();
        List<ResumeResponse> response = resumeService.listResumes(userId);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result> get(@PathVariable Long id) {
        Long userId = currentUserAccessor.getCurrentUserId();
        ResumeResponse response = resumeService.getResume(userId, id);
        return ResponseEntity.ok(Result.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result> update(
            @PathVariable Long id,
            @RequestBody ResumeRequest.Update request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        ResumeResponse response = resumeService.updateResume(userId, id, request);
        return ResponseEntity.ok(Result.success("简历更新成功", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result> delete(@PathVariable Long id) {
        Long userId = currentUserAccessor.getCurrentUserId();
        resumeService.deleteResume(userId, id);
        return ResponseEntity.ok(Result.success("简历删除成功", null));
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<Result> versions(@PathVariable Long id) {
        Long userId = currentUserAccessor.getCurrentUserId();
        List<ResumeResponse> response = resumeService.getVersions(userId, id);
        return ResponseEntity.ok(Result.success(response));
    }

    // ========== 简历分析记录 ==========

    @GetMapping("/analysis-records")
    public ResponseEntity<Result> listAnalysisRecords() {
        Long userId = currentUserAccessor.getCurrentUserId();
        List<ResumeAnalysisRecordResponse> records = recordService.listByUser(userId);
        return ResponseEntity.ok(Result.success(records));
    }

    @GetMapping("/analysis-records/{recordId}")
    public ResponseEntity<Result> getAnalysisRecord(@PathVariable Long recordId) {
        Long userId = currentUserAccessor.getCurrentUserId();
        ResumeAnalysisRecordResponse record = recordService.getById(userId, recordId);
        return ResponseEntity.ok(Result.success(record));
    }

    @DeleteMapping("/analysis-records/{recordId}")
    public ResponseEntity<Result> deleteAnalysisRecord(@PathVariable Long recordId) {
        Long userId = currentUserAccessor.getCurrentUserId();
        recordService.deleteRecord(userId, recordId);
        return ResponseEntity.ok(Result.success("分析记录已删除", null));
    }
}
