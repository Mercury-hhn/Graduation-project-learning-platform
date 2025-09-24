package com.example.learning.modules.recommend;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learning.modules.course.entity.Course;
import com.example.learning.modules.course.mapper.CourseMapper;
import com.example.learning.modules.learn.entity.Favorite;
import com.example.learning.modules.learn.mapper.FavoriteMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 推荐服务：根据热门、标签和个人偏好生成推荐列表。
 */
@Service
public class RecommendService {

    private final CourseMapper courseMapper;
    private final FavoriteMapper favoriteMapper;

    public RecommendService(CourseMapper courseMapper, FavoriteMapper favoriteMapper) {
        this.courseMapper = courseMapper;
        this.favoriteMapper = favoriteMapper;
    }

    /**
     * 首页推荐结果，使用缓存。
     */
    @Cacheable(value = "recommend", key = "'home:' + #userId")
    public Map<String, List<Course>> home(Long userId) {
        Map<String, List<Course>> result = new HashMap<>();
        result.put("hot", hotCourses());
        Set<String> tags = userTags(userId);
        result.put("byTags", tags.isEmpty() ? List.of() : byTags(tags));
        result.put("personal", personal(userId));
        return result;
    }

    private List<Course> hotCourses() {
        return courseMapper.selectPage(Page.of(1, 8), new LambdaQueryWrapper<Course>().orderByDesc(Course::getEnrollCount, Course::getViewCount)).getRecords();
    }

    private Set<String> userTags(Long userId) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
        return favorites.stream()
            .map(f -> courseMapper.selectById(f.getCourseId()))
            .filter(course -> course != null && course.getTags() != null)
            .flatMap(course -> List.of(course.getTags().split(",")).stream())
            .map(String::trim)
            .collect(Collectors.toSet());
    }

    private List<Course> byTags(Set<String> tags) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        for (String tag : tags) {
            wrapper.like(Course::getTags, tag).or();
        }
        return courseMapper.selectList(wrapper.last("limit 8"));
    }

    private List<Course> personal(Long userId) {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>().orderByDesc(Course::getCreatedAt).last("limit 8"));
    }
}