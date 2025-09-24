package com.example.learning.modules.stats.controller;

import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.stats.StatsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统计接口：提供个人与课程数据。
 */
@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    private Long currentUserId(Authentication authentication) {
        UserDetails details = (UserDetails) authentication.getPrincipal();
        return Long.valueOf(details.getUsername());
    }

    @GetMapping("/stats/my")
    public ApiResponse<Map<String, Object>> my(Authentication authentication) {
        return ApiResponse.ok(statsService.myStats(currentUserId(authentication)));
    }

    @GetMapping("/stats/course/{id}")
    public ApiResponse<Map<String, Object>> course(@PathVariable Long id) {
        return ApiResponse.ok(statsService.courseStats(id));
    }
}