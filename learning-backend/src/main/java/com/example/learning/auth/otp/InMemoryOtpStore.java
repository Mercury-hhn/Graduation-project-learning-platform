package com.example.learning.auth.otp;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试环境使用的内存 OTP 存储，避免依赖真实 Redis。
 */
@Component
@Profile("test")
public class InMemoryOtpStore implements OtpStore {

    private static class Entry {
        String code;
        Instant expireAt;
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final Map<String, Instant> rate = new ConcurrentHashMap<>();

    @Override
    public boolean rateLimit(String receiver, int seconds) {
        Instant now = Instant.now();
        Instant last = rate.get(receiver);
        if (last != null && now.isBefore(last.plusSeconds(seconds))) {
            return true;
        }
        rate.put(receiver, now);
        return false;
    }

    @Override
    public void save(String channel, String receiver, String code, Duration ttl) {
        Entry entry = new Entry();
        entry.code = code;
        entry.expireAt = Instant.now().plus(ttl);
        store.put(key(channel, receiver), entry);
    }

    @Override
    public Optional<String> get(String channel, String receiver) {
        Entry entry = store.get(key(channel, receiver));
        if (entry == null || Instant.now().isAfter(entry.expireAt)) {
            store.remove(key(channel, receiver));
            return Optional.empty();
        }
        return Optional.of(entry.code);
    }

    @Override
    public boolean consume(String channel, String receiver, String code) {
        String key = key(channel, receiver);
        Entry entry = store.get(key);
        if (entry != null && Instant.now().isBefore(entry.expireAt) && code.equals(entry.code)) {
            store.remove(key);
            return true;
        }
        return false;
    }

    private String key(String channel, String receiver) {
        return channel + ":" + receiver;
    }
}