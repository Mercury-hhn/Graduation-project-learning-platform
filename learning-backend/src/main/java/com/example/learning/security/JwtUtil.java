package com.example.learning.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：负责生成与解析 Token，封装密钥读取逻辑。
 */
@Component
public class JwtUtil {

    /**
     * 密钥原文，从配置文件读取。
     */
    private final Key key;

    /**
     * 过期分钟数。
     */
    private final long expireMinutes;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expireMinutes}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(secret)));
        this.expireMinutes = expireMinutes;
    }

    /**
     * 生成签名后的 JWT。
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(expireMinutes * 60)))
            .addClaims(claims)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 解析 JWT，返回 Claims。
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * 将密钥转换成 Base64，如果原本不是则自动编码。
     */
    private String toBase64(String secret) {
        if (secret.matches("^[A-Za-z0-9+/=]+$")) {
            return secret;
        }
        return java.util.Base64.getEncoder().encodeToString(secret.getBytes());
    }
}