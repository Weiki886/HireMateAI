package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.request.LoginRequest;
import com.hiremate.ai.dto.request.RegisterRequest;
import com.hiremate.ai.dto.response.LoginResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import com.hiremate.ai.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public User register(RegisterRequest request) {
        // Check username uniqueness
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (usernameCount > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // Check email uniqueness
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            throw new BusinessException(ResultCode.EMAIL_EXISTS);
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);
        log.info("User registered: {}", user.getUsername());
        return user;
    }

    public LoginResponse login(LoginRequest request) {
        // Find user
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // Generate token
        String token = jwtTokenProvider.generateToken(user);

        log.info("User logged in: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .expiresIn(jwtTokenProvider.getExpirationSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public void logout(String token) {
        jwtTokenProvider.addToBlacklist(token);
        log.info("User logged out, token added to blacklist");
    }
}
