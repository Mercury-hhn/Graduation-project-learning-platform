package com.example.learning.modules.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.learning.common.exception.BizException;
import com.example.learning.modules.course.entity.Course;
import com.example.learning.modules.course.entity.Resource;
import com.example.learning.modules.course.entity.Section;
import com.example.learning.modules.course.mapper.CourseMapper;
import com.example.learning.modules.course.mapper.ResourceMapper;
import com.example.learning.modules.course.mapper.SectionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务：封装课程/章节/资源的增删查改逻辑。
 */
@Service
public class CourseService {

    private final CourseMapper courseMapper;
    private final SectionMapper sectionMapper;
    private final ResourceMapper resourceMapper;

    public CourseService(CourseMapper courseMapper, SectionMapper sectionMapper, ResourceMapper resourceMapper) {
        this.courseMapper = courseMapper;
        this.sectionMapper = sectionMapper;
        this.resourceMapper = resourceMapper;
    }

    /**
     * 分页查询课程，可按关键词或标签过滤。
     */
    public Page<Course> queryCourses(String kw, String tag, String sort, long page, long size) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (kw != null && !kw.isBlank()) {
            wrapper.like(Course::getTitle, kw);
        }
        if (tag != null && !tag.isBlank()) {
            wrapper.like(Course::getTags, tag);
        }
        if ("hot".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(Course::getEnrollCount, Course::getViewCount);
        } else {
            wrapper.orderByDesc(Course::getCreatedAt);
        }
        return courseMapper.selectPage(Page.of(page, size), wrapper);
    }

    /**
     * 查询课程详情，包含章节与资源。
     */
    public CourseDetail detail(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        List<Section> sections = sectionMapper.selectList(new LambdaQueryWrapper<Section>().eq(Section::getCourseId, id).orderByAsc(Section::getSort));
        List<Long> sectionIds = sections.stream().map(Section::getId).collect(Collectors.toList());
        List<Resource> resources = resourceMapper.selectList(new LambdaQueryWrapper<Resource>().in(sectionIds.size() > 0, Resource::getSectionId, sectionIds));
        Map<Long, List<Resource>> grouped = resources.stream().collect(Collectors.groupingBy(resource -> resource.getSectionId() == null ? 0L : resource.getSectionId()));
        return new CourseDetail(course, sections, grouped);
    }

    /**
     * 新增课程。
     */
    public Course createCourse(Course course) {
        courseMapper.insert(course);
        return course;
    }

    /**
     * 更新课程。
     */
    public void updateCourse(Course course) {
        if (courseMapper.updateById(course) == 0) {
            throw new BizException(404, "课程不存在");
        }
    }

    /**
     * 删除课程。
     */
    @Transactional
    public void deleteCourse(Long id) {
        courseMapper.deleteById(id);
        sectionMapper.delete(new LambdaQueryWrapper<Section>().eq(Section::getCourseId, id));
        resourceMapper.delete(new LambdaQueryWrapper<Resource>().eq(Resource::getCourseId, id));
    }

    /**
     * 新增章节。
     */
    public Section createSection(Section section) {
        sectionMapper.insert(section);
        return section;
    }

    /**
     * 上传资源记录。
     */
    public Resource createResource(Resource resource) {
        resourceMapper.insert(resource);
        return resource;
    }

    /**
     * 详情模型，包含课程、章节、资源聚合。
     */
    public record CourseDetail(Course course, List<Section> sections, Map<Long, List<Resource>> sectionResources) {
    }
}