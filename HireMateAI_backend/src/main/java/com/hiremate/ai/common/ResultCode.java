package com.hiremate.ai.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    PARAM_ERROR(4001, "业务参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    USER_NOT_FOUND(4041, "用户不存在"),
    SESSION_NOT_FOUND(4042, "会话不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // 业务错误 5xx
    USERNAME_EXISTS(5001, "用户名已存在"),
    EMAIL_EXISTS(5002, "邮箱已被注册"),
    USERNAME_OR_PASSWORD_ERROR(5003, "用户名或密码错误"),
    TOKEN_INVALID(5004, "Token无效或已过期"),
    TOKEN_BLACKLISTED(5005, "Token已被加入黑名单"),
    SESSION_ACCESS_DENIED(5006, "无权访问此会话"),
    SESSION_CLOSED(5007, "会话已结束"),
    AI_SERVICE_ERROR(5008, "AI服务调用失败"),
    SYSTEM_ERROR(5009, "系统错误"),
    PASSWORD_INCORRECT(5010, "原密码错误"),

    // 服务器错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;
}
