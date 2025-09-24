package com.example.learning.modules.cms.controller;

import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.cms.entity.Comment;
import com.example.learning.modules.cms.entity.Notice;
import com.example.learning.modules.cms.service.CmsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CMS 接口：留言与公告管理。
 */
@RestController
@RequestMapping("/api")
public class CmsController {

    private final CmsService cmsService;

    public CmsController(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    private Long currentUserId(Authentication authentication) {
        UserDetails details = (UserDetails) authentication.getPrincipal();
        return Long.valueOf(details.getUsername());
    }

    /**
     * 获取课程留言。
     */
    @GetMapping("/courses/{id}/comments")
    public ApiResponse<List<Comment>> comments(@PathVariable Long id) {
        return ApiResponse.ok(cmsService.listComments(id));
    }

    /**
     * 添加留言。
     */
    @PostMapping("/courses/{id}/comments")
    public ApiResponse<Comment> addComment(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           Authentication authentication) {
        String content = body.get("content");
        return ApiResponse.ok(cmsService.addComment(id, currentUserId(authentication), content));
    }

    /**
     * 公告列表。
     */
    @GetMapping("/notices")
    public ApiResponse<List<Notice>> notices() {
        return ApiResponse.ok(cmsService.listNotices());
    }

    /**
     * 发布公告。
     */
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    @PostMapping("/notices")
    public ApiResponse<Notice> addNotice(@RequestBody Map<String, String> body,
                                         Authentication authentication) {
        return ApiResponse.ok(cmsService.addNotice(body.get("title"), body.get("content"), currentUserId(authentication)));
    }
}