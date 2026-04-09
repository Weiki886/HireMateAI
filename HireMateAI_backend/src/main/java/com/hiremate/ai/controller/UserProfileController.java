package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.request.ChangePasswordRequest;
import com.hiremate.ai.dto.request.UpdateProfileRequest;
import com.hiremate.ai.dto.response.UserProfileResponse;
import com.hiremate.ai.security.CurrentUserAccessor;
import com.hiremate.ai.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户中心", description = "个人资料与密码")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final CurrentUserAccessor currentUserAccessor;

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户资料")
    public Result<UserProfileResponse> getProfile() {
        Long userId = currentUserAccessor.getCurrentUserId();
        return Result.success(userProfileService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新邮箱")
    public Result<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        return Result.success("资料已更新", userProfileService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    @Operation(summary = "修改登录密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = currentUserAccessor.getCurrentUserId();
        userProfileService.changePassword(userId, request);
        return Result.success("密码已更新", null);
    }
}
