package com.example.learning.auth;

import com.example.learning.auth.otp.OtpSender;
import com.example.learning.auth.otp.OtpStore;
import com.example.learning.common.exception.BizException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * OTP 服务：负责验证码生成、存储、限流以及校验。
 */
@Service
public class OtpService {

    private final OtpStore otpStore;
    private final OtpSender otpSender;
    private final int expireMinutes;
    private final int rateLimitSeconds;

    public OtpService(OtpStore otpStore,
                      OtpSender otpSender,
                      @Value("${otp.expireMinutes}") int expireMinutes,
                      @Value("${otp.rateLimitSeconds}") int rateLimitSeconds) {
        this.otpStore = otpStore;
        this.otpSender = otpSender;
        this.expireMinutes = expireMinutes;
        this.rateLimitSeconds = rateLimitSeconds;
    }

    /**
     * 发送验证码，包含限流与存储。
     */
    public void send(String channel, String receiver) {
        if (otpStore.rateLimit(receiver, rateLimitSeconds)) {
            throw new BizException(429, "发送过于频繁，请稍后再试");
        }
        String code = RandomStringUtils.randomNumeric(6);
        otpStore.save(channel, receiver, code, Duration.ofMinutes(expireMinutes));
        otpSender.send(channel, receiver, code);
    }

    /**
     * 校验验证码并消费，失败抛出异常。
     */
    public void verify(String channel, String receiver, String code) {
        if (!otpStore.consume(channel, receiver, code)) {
            throw new BizException(400, "验证码错误或已过期");
        }
    }
}