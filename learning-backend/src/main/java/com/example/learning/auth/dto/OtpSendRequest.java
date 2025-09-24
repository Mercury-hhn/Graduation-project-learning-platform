package com.example.learning.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送 OTP 请求体：包含渠道与接收者。
 */
@Data
public class OtpSendRequest {

    /**
     * 渠道，支持 email/sms。
     */
    @NotBlank(message = "渠道不能为空")
    private String channel;

    /**
     * 接收者邮箱或手机号。
     */
    @NotBlank(message = "接收者不能为空")
    private String receiver;
}