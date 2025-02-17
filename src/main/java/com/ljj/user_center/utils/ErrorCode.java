package com.ljj.user_center.utils;

import lombok.Data;

/**
 * @Author ljj
 * @Date 2019/11/25 10:22
 * 全局错误码
 */
public enum ErrorCode {
    SUCCESS(0, "成功", ""),
    PARAMS_ERROR(40000, "参数错误", ""),
    NULL_ERROR(40001, "参数为空", ""),
    NOT_LOGIN(40101, "用户未登录", ""),
    NO_AUTH(40100, "没有权限", ""),

    SYSTEM_ERROR(50000, "系统内部异常", "");
    private final int code;
    private final String message;
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}

