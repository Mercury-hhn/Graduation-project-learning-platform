package com.example.learning.auth.otp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock 发送器：将验证码打印到日志，在演示环境使用。
 */
@Slf4j
@Component
public class MockOtpSender implements OtpSender {

    @Override
    public void send(String channel, String receiver, String code) {
        log.info("[MockOtpSender] 渠道:{} 接收者:{} 验证码:{}", channel, receiver, code);
    }
}