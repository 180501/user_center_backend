package com.ljj.user_center.utils;

import com.ljj.user_center.model.domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author ljj
 * @Date 2019/11/25 10:52
 * @Description 返回结果工具类
 * @Version 1.0.0
 */
public class ResultUtils {
    /**
     * 成功返回结果
     * 失败没有data
     * @param data
     * @return
     * @param <T>
     */
    public static<T> BaseResponse<T> success(T data) {
        return new BaseResponse(0, "OK", data,"成功");
    }
    public static<T> BaseResponse<T> success(T data,String description) {
        return new BaseResponse(0, "OK", data,description);
    }

    public static<T> BaseResponse<T> success(String description) {
        return new BaseResponse(0, "OK",null,description);
    }
    public static<T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse(errorCode);
    }
    public static<T> BaseResponse<T> error(ErrorCode errorCode,String message,String description) {
        return new BaseResponse(errorCode.getCode(),message,description);
    }
    public static<T> BaseResponse<T> error(ErrorCode errorCode,String description) {
        return new BaseResponse(errorCode.getCode(),errorCode.getMessage(),description);
    }
    public static<T> BaseResponse<T> error(int code,String message,String description) {
        return new BaseResponse(code,message,description);
    }
}
