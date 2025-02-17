package com.ljj.user_center.exception;

import com.ljj.user_center.utils.BaseResponse;
import com.ljj.user_center.utils.ErrorCode;
import com.ljj.user_center.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author ljj
 * @Date 2019/11/25 10:50
 * @Description 捕获代码中的所有异常，集中处理，让前端收到更详细的业务的报错
 *隐藏部分在前端返回的信息
 */

@RestControllerAdvice //这个注解可以捕获所有的controller层的异常
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 当你在 GlobalExceptionHandler 类中使用 @ExceptionHandler(Exception.class) 注解一个方法时，
     * 这个方法将会被用来处理所有在 @RestController 注解的控制器中抛出的 Exception 异常及其子类异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse BusinessExceptionHandler(BusinessException e) {//Throwable是所有异常的父类
        log.error("BusinessException:"+e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(),e.getDescription());//屏蔽服务器内所有异常
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse RuntimeExceptionHandler(Throwable e) {
        log.error("运行时异常", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(),"");//屏蔽服务器内所有异常
    }
}
