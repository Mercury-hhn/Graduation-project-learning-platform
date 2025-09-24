package com.example.learning.auth.otp;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 实现的 OTP 存储，利用 key 前缀区分渠道与频率限制。
 */
@Component
@Profile("!test")
public class RedisOtpStore implements OtpStore {

    private final StringRedisTemplate redisTemplate;

    public RedisOtpStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean rateLimit(String receiver, int seconds) {
        String key = "otp:rate:" + receiver;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(seconds));
            return false;
        }
        return true;
    }

    @Override
    public void save(String channel, String receiver, String code, Duration ttl) {
        String key = buildKey(channel, receiver);
        redisTemplate.opsForValue().set(key, code, ttl);
    }

    @Override
    public Optional<String> get(String channel, String receiver) {
        String key = buildKey(channel, receiver);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public boolean consume(String channel, String receiver, String code) {
        String key = buildKey(channel, receiver);
        String cached = redisTemplate.opsForValue().get(key);
        if (code.equals(cached)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String buildKey(String channel, String receiver) {
        return "otp:" + channel + ":" + receiver;
    }
}