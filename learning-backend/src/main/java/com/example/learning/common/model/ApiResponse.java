package com.example.learning.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体封装：所有接口返回 {code,msg,data} 结构，便于前端解析。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务状态码，0 表示成功，其余表示各种业务错误。
     */
    private int code;

    /**
     * 文字说明，配合 code 定位问题。
     */
    private String msg;

    /**
     * 真正的数据部分，类型使用泛型以复用。
     */
    private T data;

    /**
     * 快捷构建成功响应，默认 msg=ok。
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    /**
     * 快捷构建失败响应。
     */
    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}