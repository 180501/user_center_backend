package com.ljj.user_center.exception;

import com.ljj.user_center.utils.ErrorCode;
import lombok.Getter;

/**
 * @author ljj
 * @date 2021/1/15 10:50
 * 业务异常类将任何属于用户中心的代码报错进行统一规范为一个异常来处理
 */
public class BusinessException extends RuntimeException {//继承RuntimeException,无显示的捕获
    @Getter
    private int code;

    @Getter
    private String description;

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }
    public BusinessException(String message, int code, String description)   {
        super(message);
        this.code = code;
        this.description = description;
    }


}

