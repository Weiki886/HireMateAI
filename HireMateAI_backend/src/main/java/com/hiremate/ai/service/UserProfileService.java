package com.hiremate.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.dto.request.ChangePasswordRequest;
import com.hiremate.ai.dto.request.UpdateProfileRequest;
import com.hiremate.ai.dto.response.UserProfileResponse;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String newEmail = request.getEmail().trim();
        String currentEmail = user.getEmail() == null ? "" : user.getEmail();
        if (!newEmail.equalsIgnoreCase(currentEmail)) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, newEmail)
                            .ne(User::getId, userId));
            if (emailCount > 0) {
                throw new BusinessException(ResultCode.EMAIL_EXISTS);
            }
        }
        user.setEmail(newEmail);
        userMapper.updateById(user);
        return toResponse(userMapper.selectById(userId));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_INCORRECT);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    private static UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
