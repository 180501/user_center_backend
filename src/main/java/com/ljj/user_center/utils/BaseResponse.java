package com.ljj.user_center.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {//Serializable接口用于序列化对象，以便在网络传输中传输对象。
    private int code;
    private String message;
    private T data;
    private String description;
    public BaseResponse(int code, String message, T data, String description) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.description = description;
    }

    public BaseResponse(int code, T data) {
        this(code, "", data, "");
    }
    public BaseResponse(ErrorCode errorCode) {//容纳全局错误码的构造器
        this(errorCode.getCode(), errorCode.getMessage(),null, errorCode.getDescription());
    }

    public BaseResponse(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.data = null;
    }
}
