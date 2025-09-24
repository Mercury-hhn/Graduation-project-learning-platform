package com.example.learning.auth.otp;

/**
 * OTP 发送接口：可扩展为邮箱、短信等渠道。
 */
public interface OtpSender {

    /**
     * 发送验证码。
     */
    void send(String channel, String receiver, String code);
}