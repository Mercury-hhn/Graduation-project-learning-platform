package com.example.learning.auth.otp;

import java.time.Duration;
import java.util.Optional;

/**
 * OTP 存储抽象：统一验证码存取与限流逻辑，默认使用 Redis 实现。
 */
public interface OtpStore {

    /**
     * 判断是否触发发送频率限制，true 表示超限。
     */
    boolean rateLimit(String receiver, int seconds);

    /**
     * 保存验证码并设置过期时间。
     */
    void save(String channel, String receiver, String code, Duration ttl);

    /**
     * 获取验证码。
     */
    Optional<String> get(String channel, String receiver);

    /**
     * 校验并消费验证码，成功则删除。
     */
    boolean consume(String channel, String receiver, String code);
}