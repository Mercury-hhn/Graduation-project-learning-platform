package com.example.learning.modules.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learning.common.exception.BizException;
import com.example.learning.modules.work.entity.Assignment;
import com.example.learning.modules.work.entity.Submission;
import com.example.learning.modules.work.mapper.AssignmentMapper;
import com.example.learning.modules.work.mapper.SubmissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业服务：封装作业发布、提交、评分等逻辑。
 */
@Service
public class AssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;

    public AssignmentService(AssignmentMapper assignmentMapper, SubmissionMapper submissionMapper) {
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
    }

    /**
     * 发布作业。
     */
    public Assignment create(Assignment assignment) {
        assignmentMapper.insert(assignment);
        return assignment;
    }

    /**
     * 学生提交作业。
     */
    @Transactional
    public Submission submit(Long assignmentId, Long userId, String content, String attachment) {
        Submission submission = submissionMapper.selectOne(new LambdaQueryWrapper<Submission>()
            .eq(Submission::getAssignmentId, assignmentId)
            .eq(Submission::getUserId, userId));
        if (submission == null) {
            submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setUserId(userId);
            submission.setCreatedAt(LocalDateTime.now());
            submission.setContent(content);
            submission.setAttachment(attachment);
            submissionMapper.insert(submission);
        } else {
            submission.setContent(content);
            submission.setAttachment(attachment);
            submissionMapper.updateById(submission);
        }
        return submission;
    }

    /**
     * 教师评分。
     */
    @Transactional
    public void grade(Long submissionId, Integer score, String comment) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(404, "提交记录不存在");
        }
        submission.setScore(score);
        submission.setAiComment(comment);
        submission.setGradedAt(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }

    /**
     * 查询学生提交列表。
     */
    public List<Submission> listByAssignment(Long assignmentId) {
        return submissionMapper.selectList(new LambdaQueryWrapper<Submission>().eq(Submission::getAssignmentId, assignmentId));
    }
}