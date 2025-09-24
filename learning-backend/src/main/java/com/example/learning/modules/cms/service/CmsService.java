package com.example.learning.modules.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learning.modules.cms.entity.Comment;
import com.example.learning.modules.cms.entity.Notice;
import com.example.learning.modules.cms.mapper.CommentMapper;
import com.example.learning.modules.cms.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CMS 服务：处理留言和公告相关逻辑。
 */
@Service
public class CmsService {

    private final CommentMapper commentMapper;
    private final NoticeMapper noticeMapper;

    public CmsService(CommentMapper commentMapper, NoticeMapper noticeMapper) {
        this.commentMapper = commentMapper;
        this.noticeMapper = noticeMapper;
    }

    /**
     * 获取课程留言列表。
     */
    public List<Comment> listComments(Long courseId) {
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>().eq(Comment::getCourseId, courseId).orderByDesc(Comment::getCreatedAt));
    }

    /**
     * 新增留言。
     */
    public Comment addComment(Long courseId, Long userId, String content) {
        Comment comment = new Comment();
        comment.setCourseId(courseId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return comment;
    }

    /**
     * 公告列表。
     */
    public List<Notice> listNotices() {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getCreatedAt));
    }

    /**
     * 发布公告。
     */
    public Notice addNotice(String title, String content, Long userId) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCreatedAt(LocalDateTime.now());
        notice.setCreatedBy(userId);
        noticeMapper.insert(notice);
        return notice;
    }
}