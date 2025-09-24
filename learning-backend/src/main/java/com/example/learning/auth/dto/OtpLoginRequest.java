package com.example.learning.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OTP 登录请求体：包含渠道、接收者与验证码。
 */
@Data
public class OtpLoginRequest {

    /**
     * 渠道。
     */
    @NotBlank(message = "渠道不能为空")
    private String channel;

    /**
     * 接收者。
     */
    @NotBlank(message = "接收者不能为空")
    private String receiver;

    /**
     * 验证码。
     */
    @NotBlank(message = "验证码不能为空")
    private String code;
}