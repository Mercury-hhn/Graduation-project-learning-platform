package com.example.learning.modules.learn.controller;

import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.learn.service.LearnService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 学习模块接口：处理选课、进度与收藏操作。
 */
@RestController
@RequestMapping("/api")
public class LearnController {

    private final LearnService learnService;

    public LearnController(LearnService learnService) {
        this.learnService = learnService;
    }

    private Long currentUserId(Authentication authentication) {
        UserDetails details = (UserDetails) authentication.getPrincipal();
        return Long.valueOf(details.getUsername());
    }

    /**
     * 选课。
     */
    @PostMapping("/enroll/{courseId}")
    public ApiResponse<Void> enroll(@PathVariable Long courseId, Authentication authentication) {
        learnService.enroll(currentUserId(authentication), courseId);
        return ApiResponse.ok(null);
    }

    /**
     * 更新进度。
     */
    @PutMapping("/progress")
    public ApiResponse<Void> updateProgress(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long courseId = Long.valueOf(body.get("courseId").toString());
        Long sectionId = Long.valueOf(body.get("sectionId").toString());
        Integer progress = Integer.valueOf(body.get("progress").toString());
        learnService.updateProgress(currentUserId(authentication), courseId, sectionId, progress);
        return ApiResponse.ok(null);
    }

    /**
     * 收藏课程。
     */
    @PostMapping("/fav/{courseId}")
    public ApiResponse<Void> favorite(@PathVariable Long courseId, Authentication authentication) {
        learnService.favorite(currentUserId(authentication), courseId);
        return ApiResponse.ok(null);
    }

    /**
     * 取消收藏。
     */
    @DeleteMapping("/fav/{courseId}")
    public ApiResponse<Void> unfavorite(@PathVariable Long courseId, Authentication authentication) {
        learnService.unfavorite(currentUserId(authentication), courseId);
        return ApiResponse.ok(null);
    }
}