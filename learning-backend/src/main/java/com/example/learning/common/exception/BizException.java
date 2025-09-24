package com.example.learning.common.exception;

/**
 * 业务异常：用于抛出带有业务状态码与友好提示的错误。
 */
public class BizException extends RuntimeException {

    /**
     * 业务状态码。
     */
    private final int code;

    /**
     * 构造业务异常，包含状态码和提示信息。
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务状态码。
     */
    public int getCode() {
        return code;
    }
}