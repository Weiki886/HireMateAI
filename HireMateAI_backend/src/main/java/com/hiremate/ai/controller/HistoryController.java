package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.response.PageResponse;
import com.hiremate.ai.dto.response.SessionDetailResponse;
import com.hiremate.ai.dto.response.SessionResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import com.hiremate.ai.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "历史记录接口", description = "面试历史记录查询与管理")
public class HistoryController {

    private final HistoryService historyService;
    private final UserMapper userMapper;

    @GetMapping("/sessions")
    @Operation(summary = "分页查询面试会话列表")
    public Result<PageResponse<SessionResponse>> getSessionList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        PageResponse<SessionResponse> response = historyService.getSessionList(userId, page, size);
        return Result.success(response);
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取会话详情")
    public Result<SessionDetailResponse> getSessionDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        SessionDetailResponse response = historyService.getSessionDetail(userId, id);
        return Result.success(response);
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除面试记录")
    public Result<Void> deleteSession(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        historyService.deleteSession(userId, id);
        return Result.success("删除成功", null);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return user.getId();
    }
}
