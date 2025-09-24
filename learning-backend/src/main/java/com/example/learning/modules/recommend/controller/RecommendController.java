package com.example.learning.modules.recommend.controller;

import com.example.learning.ai.AiService;
import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.recommend.RecommendService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 推荐接口：提供首页推荐与 AI 重排功能。
 */
@RestController
@RequestMapping("/api")
public class RecommendController {

    private final RecommendService recommendService;
    private final AiService aiService;

    public RecommendController(RecommendService recommendService, AiService aiService) {
        this.recommendService = recommendService;
        this.aiService = aiService;
    }

    private Long currentUserId(Authentication authentication) {
        UserDetails details = (UserDetails) authentication.getPrincipal();
        return Long.valueOf(details.getUsername());
    }

    /**
     * 首页推荐。
     */
    @GetMapping("/recommend/home")
    public ApiResponse<Map<String, List<?>>> home(Authentication authentication) {
        Long userId = currentUserId(authentication);
        Map<String, List<?>> data = (Map) recommendService.home(userId);
        return ApiResponse.ok(data);
    }

    /**
     * AI 重排接口。
     */
    @PostMapping("/ai/recommend/rerank")
    public ApiResponse<List<Map<String, Object>>> rerank(@RequestBody Map<String, Object> body) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
        return ApiResponse.ok(aiService.rerank(candidates));
    }
}