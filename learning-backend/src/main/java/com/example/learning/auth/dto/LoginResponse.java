package com.example.learning.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应：返回 token 与角色。
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT 字符串。
     */
    private String token;

    /**
     * 当前用户角色。
     */
    private String role;
}