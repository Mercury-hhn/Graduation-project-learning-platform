package com.example.learning.modules.work.controller;

import com.example.learning.ai.AiService;
import com.example.learning.common.exception.BizException;
import com.example.learning.common.model.ApiResponse;
import com.example.learning.modules.work.entity.Assignment;
import com.example.learning.modules.work.entity.Submission;
import com.example.learning.modules.work.service.AssignmentService;
import com.example.learning.storage.FileStorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

/**
 * 作业相关接口：涵盖发布作业、提交作业、评分以及 AI 批改。
 */
@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final FileStorageService fileStorageService;
    private final AiService aiService;

    public AssignmentController(AssignmentService assignmentService,
                                FileStorageService fileStorageService,
                                AiService aiService) {
        this.assignmentService = assignmentService;
        this.fileStorageService = fileStorageService;
        this.aiService = aiService;
    }

    private Long currentUserId(Authentication authentication) {
        UserDetails details = (UserDetails) authentication.getPrincipal();
        return Long.valueOf(details.getUsername());
    }

    /**
     * 发布作业（教师权限）。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PostMapping("/assignments")
    public ApiResponse<Assignment> create(@RequestBody Assignment assignment) {
        return ApiResponse.ok(assignmentService.create(assignment));
    }

    /**
     * 学生提交作业，可附带附件文件。
     */
    @PostMapping("/submissions")
    public ApiResponse<Submission> submit(@RequestParam Long assignmentId,
                                          @RequestParam(required = false) String content,
                                          @RequestParam(required = false) MultipartFile attachment,
                                          Authentication authentication) {
        String attachmentUrl = null;
        if (attachment != null && !attachment.isEmpty()) {
            attachmentUrl = fileStorageService.store(attachment).url();
        }
        Submission submission = assignmentService.submit(assignmentId, currentUserId(authentication), content, attachmentUrl);
        return ApiResponse.ok(submission);
    }

    /**
     * 教师评分接口。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @PutMapping("/submissions/{id}/score")
    public ApiResponse<Void> grade(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer score = body.get("score");
        if (score == null) {
            throw new BizException(400, "分数不能为空");
        }
        assignmentService.grade(id, score, null);
        return ApiResponse.ok(null);
    }

    /**
     * AI 批改接口：调用 AI 服务给出得分与评语，并写回数据库。
     */
    @PostMapping("/ai/grade")
    public ApiResponse<Map<String, Object>> aiGrade(@Valid @RequestBody AiGradeRequest req) {
        Submission submission = assignmentService.submit(req.assignmentId, req.userId, req.content, null);
        AiService.GradeResult result = aiService.grade(req.content, req.rubric, req.maxScore);
        assignmentService.grade(submission.getId(), result.score(), result.comment());
        return ApiResponse.ok(Map.of("score", result.score(), "comment", result.comment()));
    }

    /**
     * 查询作业提交列表。
     */
    @PreAuthorize("hasAnyAuthority('TEACHER','ADMIN')")
    @GetMapping("/assignments/{id}/submissions")
    public ApiResponse<List<Submission>> list(@PathVariable Long id) {
        return ApiResponse.ok(assignmentService.listByAssignment(id));
    }

    /**
     * AI 批改请求体。
     */
    public record AiGradeRequest(@NotNull Long assignmentId,
                                 @NotNull Long userId,
                                 @NotBlank String content,
                                 String rubric,
                                 int maxScore) {
    }
}