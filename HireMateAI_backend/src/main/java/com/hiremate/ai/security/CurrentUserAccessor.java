package com.hiremate.ai.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hiremate.ai.common.BusinessException;
import com.hiremate.ai.common.ResultCode;
import com.hiremate.ai.entity.User;
import com.hiremate.ai.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserAccessor {

    private final UserMapper userMapper;

    /**
     * 与 {@link com.hiremate.ai.controller.InterviewController#getCurrentUserId()} 一致：
     * JWT 解析后 {@code UserDetails#getUsername()} 为登录名，不是数字型 userId。
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        String username = authentication.getName();
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}
