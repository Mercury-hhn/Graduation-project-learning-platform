package com.example.learning.common.exception;

import com.example.learning.common.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一将异常转换成 {code,msg} 响应，避免栈追踪泄露。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，按异常自带的 code 返回。
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常，提示用户具体错误信息。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ":" + err.getDefaultMessage())
            .orElse("参数不合法");
        return ApiResponse.error(400, msg);
    }

    /**
     * 处理其它校验异常。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    /**
     * 兜底异常处理，返回 500。
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        return ApiResponse.error(500, "服务器忙，请稍后再试");
    }
}