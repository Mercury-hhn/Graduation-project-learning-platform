package com.example.learning.modules.learn.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learning.common.exception.BizException;
import com.example.learning.modules.learn.entity.Enroll;
import com.example.learning.modules.learn.entity.Favorite;
import com.example.learning.modules.learn.entity.Progress;
import com.example.learning.modules.learn.mapper.EnrollMapper;
import com.example.learning.modules.learn.mapper.FavoriteMapper;
import com.example.learning.modules.learn.mapper.ProgressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 学习模块服务：封装选课、进度、收藏等业务逻辑。
 */
@Service
public class LearnService {

    private final EnrollMapper enrollMapper;
    private final ProgressMapper progressMapper;
    private final FavoriteMapper favoriteMapper;

    public LearnService(EnrollMapper enrollMapper, ProgressMapper progressMapper, FavoriteMapper favoriteMapper) {
        this.enrollMapper = enrollMapper;
        this.progressMapper = progressMapper;
        this.favoriteMapper = favoriteMapper;
    }

    /**
     * 选课，保证幂等。
     */
    @Transactional
    public void enroll(Long userId, Long courseId) {
        Enroll exist = enrollMapper.selectOne(new LambdaQueryWrapper<Enroll>()
            .eq(Enroll::getUserId, userId)
            .eq(Enroll::getCourseId, courseId));
        if (exist != null) {
            return;
        }
        Enroll enroll = new Enroll();
        enroll.setUserId(userId);
        enroll.setCourseId(courseId);
        enroll.setCreatedAt(LocalDateTime.now());
        enrollMapper.insert(enroll);
    }

    /**
     * 更新学习进度。
     */
    @Transactional
    public void updateProgress(Long userId, Long courseId, Long sectionId, int progress) {
        if (progress < 0 || progress > 100) {
            throw new BizException(400, "进度必须在 0~100 之间");
        }
        Progress record = progressMapper.selectOne(new LambdaQueryWrapper<Progress>()
            .eq(Progress::getUserId, userId)
            .eq(Progress::getCourseId, courseId)
            .eq(Progress::getSectionId, sectionId));
        if (record == null) {
            record = new Progress();
            record.setUserId(userId);
            record.setCourseId(courseId);
            record.setSectionId(sectionId);
            record.setProgress(progress);
            record.setLastLearnAt(LocalDateTime.now());
            progressMapper.insert(record);
        } else {
            record.setProgress(progress);
            record.setLastLearnAt(LocalDateTime.now());
            progressMapper.updateById(record);
        }
    }

    /**
     * 收藏课程。
     */
    @Transactional
    public void favorite(Long userId, Long courseId) {
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getCourseId, courseId));
        if (exist != null) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setCourseId(courseId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);
    }

    /**
     * 取消收藏。
     */
    @Transactional
    public void unfavorite(Long userId, Long courseId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
            .eq(Favorite::getUserId, userId)
            .eq(Favorite::getCourseId, courseId));
    }
}