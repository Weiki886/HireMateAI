package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.request.JobDescriptionRequest;
import com.hiremate.ai.dto.request.JobMatchRequest;
import com.hiremate.ai.dto.response.JobDescriptionResponse;
import com.hiremate.ai.dto.response.JobMatchRecordResponse;
import com.hiremate.ai.security.CurrentUserAccessor;
import com.hiremate.ai.service.JobDescriptionService;
import com.hiremate.ai.service.PdfTextExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/job-match")
@RequiredArgsConstructor
@Tag(name = "岗位匹配", description = "岗位描述管理和简历匹配分析")
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;
    private final CurrentUserAccessor currentUserAccessor;
    private final PdfTextExtractor pdfTextExtractor;

    @PostMapping("/job-description")
    @Operation(summary = "保存岗位描述")
    public Result<JobDescriptionResponse> saveJobDescription(@Valid @RequestBody JobDescriptionRequest request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        JobDescriptionResponse response = jobDescriptionService.saveJobDescription(userId, request);
        return Result.success("岗位描述保存成功", response);
    }

    @GetMapping("/job-description/{id}")
    @Operation(summary = "获取岗位描述")
    public Result<JobDescriptionResponse> getJobDescription(@PathVariable Long id) {
        Long userId = currentUserAccessor.getCurrentUserId();
        JobDescriptionResponse response = jobDescriptionService.getJobDescription(userId, id);
        return Result.success(response);
    }

    @GetMapping("/job-description")
    @Operation(summary = "获取岗位描述列表")
    public Result<List<JobDescriptionResponse>> listJobDescriptions() {
        Long userId = currentUserAccessor.getCurrentUserId();
        List<JobDescriptionResponse> list = jobDescriptionService.listJobDescriptions(userId);
        return Result.success(list);
    }

    @DeleteMapping("/job-description/{id}")
    @Operation(summary = "删除岗位描述")
    public Result<Void> deleteJobDescription(@PathVariable Long id) {
        Long userId = currentUserAccessor.getCurrentUserId();
        jobDescriptionService.deleteJobDescription(userId, id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/analyze")
    @Operation(summary = "分析简历与岗位匹配度")
    public Result<JobMatchRecordResponse> analyzeMatch(
            @RequestParam(value = "jobDescriptionId", required = false) Long jobDescriptionId,
            @RequestParam(value = "jobContent", required = false) String jobContent,
            @RequestParam("resumeFile") MultipartFile resumeFile) {

        Long userId = currentUserAccessor.getCurrentUserId();

        String resumeText;
        String resumeFileName = resumeFile.getOriginalFilename();

        try {
            String contentType = resumeFile.getContentType();
            if (contentType != null && contentType.equals("application/pdf")) {
                PdfTextExtractor.ExtractResult extractResult = pdfTextExtractor.extractText(resumeFile);
                if (extractResult.isImagePdf) {
                    return Result.fail(400, "该PDF文件为图片扫描件，无法提取文字内容，请上传文本型PDF");
                }
                if (extractResult.garbled) {
                    resumeText = extractResult.text;
                } else {
                    resumeText = pdfTextExtractor.cleanText(extractResult.text);
                }
            } else {
                resumeText = new String(resumeFile.getBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            return Result.fail(400, "简历读取失败: " + e.getMessage());
        }

        JobMatchRequest request = new JobMatchRequest();
        request.setJobDescriptionId(jobDescriptionId);
        request.setJobContent(jobContent);
        request.setResumeText(resumeText);
        request.setResumeFileName(resumeFileName);

        JobMatchRecordResponse response = jobDescriptionService.analyzeMatch(userId, request);
        return Result.success("分析完成", response);
    }

    @GetMapping("/records")
    @Operation(summary = "获取匹配记录列表")
    public Result<List<JobMatchRecordResponse>> listMatchRecords() {
        Long userId = currentUserAccessor.getCurrentUserId();
        List<JobMatchRecordResponse> list = jobDescriptionService.listMatchRecords(userId);
        return Result.success(list);
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "获取匹配记录详情")
    public Result<JobMatchRecordResponse> getMatchRecord(@PathVariable Long id) {
        Long userId = currentUserAccessor.getCurrentUserId();
        JobMatchRecordResponse response = jobDescriptionService.getMatchRecord(userId, id);
        return Result.success(response);
    }
}