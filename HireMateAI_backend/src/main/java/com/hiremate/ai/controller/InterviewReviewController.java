package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.response.ReviewReportResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import com.hiremate.ai.service.InterviewReviewService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/interview/review")
@RequiredArgsConstructor
@Tag(name = "面试复盘报告接口", description = "AI面试复盘报告生成与查询")
public class InterviewReviewController {

    private final InterviewReviewService reviewService;
    private final UserMapper userMapper;

    @PostMapping("/{sessionId}")
    @Operation(summary = "生成面试复盘报告")
    public Result<ReviewReportResponse> generateReport(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        ReviewReportResponse report = reviewService.generateReviewReport(userId, sessionId);
        return Result.success("复盘报告生成成功", report);
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "获取面试复盘报告")
    public Result<ReviewReportResponse> getReport(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        ReviewReportResponse report = reviewService.getReport(userId, sessionId);
        return Result.success(report);
    }

    @GetMapping("/{sessionId}/detail")
    @Operation(summary = "获取面试复盘报告（含所有回答评价）")
    public Result<ReviewReportResponse> getReportDetail(@PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        ReviewReportResponse report = reviewService.getReportDetail(userId, sessionId);
        return Result.success(report);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        return user.getId();
    }
}
