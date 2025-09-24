package com.example.learning.modules.stats;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learning.modules.course.entity.Course;
import com.example.learning.modules.course.mapper.CourseMapper;
import com.example.learning.modules.learn.entity.Enroll;
import com.example.learning.modules.learn.entity.Progress;
import com.example.learning.modules.learn.mapper.EnrollMapper;
import com.example.learning.modules.learn.mapper.ProgressMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务：计算个人学习与课程概览数据。
 */
@Service
public class StatsService {

    private final EnrollMapper enrollMapper;
    private final ProgressMapper progressMapper;
    private final CourseMapper courseMapper;

    public StatsService(EnrollMapper enrollMapper, ProgressMapper progressMapper, CourseMapper courseMapper) {
        this.enrollMapper = enrollMapper;
        this.progressMapper = progressMapper;
        this.courseMapper = courseMapper;
    }

    /**
     * 个人统计。
     */
    public Map<String, Object> myStats(Long userId) {
        List<Enroll> enrolls = enrollMapper.selectList(new LambdaQueryWrapper<Enroll>().eq(Enroll::getUserId, userId));
        List<Progress> progresses = progressMapper.selectList(new LambdaQueryWrapper<Progress>().eq(Progress::getUserId, userId));
        long finished = progresses.stream().filter(p -> p.getProgress() != null && p.getProgress() == 100).count();
        Map<String, Object> map = new HashMap<>();
        map.put("enrolled", enrolls.size());
        map.put("finished", finished);
        map.put("hours", progresses.size() * 0.5);
        return map;
    }

    /**
     * 课程统计。
     */
    public Map<String, Object> courseStats(Long courseId) {
        long enrollCount = enrollMapper.selectCount(new LambdaQueryWrapper<Enroll>().eq(Enroll::getCourseId, courseId));
        long finishCount = progressMapper.selectCount(new LambdaQueryWrapper<Progress>().eq(Progress::getCourseId, courseId).eq(Progress::getProgress, 100));
        Map<String, Object> map = new HashMap<>();
        map.put("enrollCount", enrollCount);
        map.put("finishRate", enrollCount == 0 ? 0 : (double) finishCount / enrollCount);
        return map;
    }
}