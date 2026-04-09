package com.hiremate.ai.controller;

import com.hiremate.ai.common.Result;
import com.hiremate.ai.dto.request.LoginRequest;
import com.hiremate.ai.dto.request.RegisterRequest;
import com.hiremate.ai.dto.response.LoginResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户注册、登录、登出")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return Result.success("注册成功", user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(Authentication authentication, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return Result.success("退出成功", null);
    }
}
