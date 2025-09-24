package com.example.learning.auth.controller;

import com.example.learning.auth.OtpService;
import com.example.learning.auth.dto.LoginResponse;
import com.example.learning.auth.dto.OtpLoginRequest;
import com.example.learning.auth.dto.OtpSendRequest;
import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.user.entity.User;
import com.example.learning.modules.user.service.UserService;
import com.example.learning.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口：负责 OTP 发送与登录，以及获取当前用户信息。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(OtpService otpService, UserService userService, JwtUtil jwtUtil) {
        this.otpService = otpService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 发送验证码。
     */
    @PostMapping("/otp/send")
    public ApiResponse<Void> send(@Valid @RequestBody OtpSendRequest req) {
        otpService.send(req.getChannel(), req.getReceiver());
        return ApiResponse.ok(null);
    }

    /**
     * OTP 登录。
     */
    @PostMapping("/otp/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody OtpLoginRequest req) {
        otpService.verify(req.getChannel(), req.getReceiver(), req.getCode());
        User user = userService.getOrCreate(req.getChannel(), req.getReceiver());
        String token = jwtUtil.generateToken(user.getId().toString(), Map.of("role", user.getRole()));
        return ApiResponse.ok(new LoginResponse(token, user.getRole()));
    }

    /**
     * 获取当前用户信息。
     */
    @GetMapping("/profile")
    public ApiResponse<User> profile(Authentication authentication) {
        UserDetails details = (UserDetails) authentication.getPrincipal();
        Long userId = Long.valueOf(details.getUsername());
        User user = userService.findById(userId).orElse(null);
        return ApiResponse.ok(user);
    }
}